package com.zhijiao.foundation.student.learning;

import com.zhijiao.foundation.analytics.AnalyticsProjectionService;
import com.zhijiao.foundation.student.learning.algorithm.BktModel;
import com.zhijiao.foundation.student.learning.algorithm.BktParameters;
import com.zhijiao.foundation.student.learning.algorithm.ConfidenceEvidence;
import com.zhijiao.foundation.student.learning.algorithm.ConfidenceModel;
import com.zhijiao.foundation.student.learning.algorithm.ConfidenceParameters;
import com.zhijiao.foundation.student.learning.algorithm.ForgettingEvidence;
import com.zhijiao.foundation.student.learning.algorithm.ForgettingParameters;
import com.zhijiao.foundation.student.learning.algorithm.ForgettingRiskModel;
import com.zhijiao.foundation.student.learning.algorithm.RaschEstimate;
import com.zhijiao.foundation.student.learning.algorithm.RaschModel;
import com.zhijiao.foundation.student.learning.algorithm.RaschObservation;
import com.zhijiao.foundation.student.learning.algorithm.RaschParameters;
import com.zhijiao.foundation.student.learning.config.LearningModelProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LearningStateEngine {
    public static final String WEAK_RANKING_MODEL_VERSION = "WEAK_RANKING_V1";

    private final LearningStateRepository repository;
    private final BktParameters bktParameters;
    private final BktModel bktModel;
    private final RaschModel raschModel;
    private final String raschModelVersion;
    private final ForgettingParameters forgettingParameters;
    private final ForgettingRiskModel forgettingRiskModel;
    private final ConfidenceParameters confidenceParameters;
    private final ConfidenceModel confidenceModel;
    private final AnalyticsProjectionService analyticsProjectionService;

    @org.springframework.beans.factory.annotation.Autowired
    public LearningStateEngine(LearningStateRepository repository, LearningModelProperties properties,
                               AnalyticsProjectionService analyticsProjectionService) {
        this.repository = repository;
        this.analyticsProjectionService = analyticsProjectionService;
        LearningModelProperties.Bkt bkt = properties.getBkt();
        this.bktParameters = new BktParameters(bkt.getInitialMastery(), bkt.getTransition(), bkt.getSlip(),
                bkt.getGuess(), bkt.getModelVersion());
        this.bktModel = new BktModel(bktParameters);

        LearningModelProperties.Rasch rasch = properties.getRasch();
        this.raschModelVersion = rasch.getModelVersion();
        this.raschModel = new RaschModel(new RaschParameters(rasch.getPriorMean(), rasch.getPriorVariance(),
                rasch.getDifficultyEpsilon(), rasch.getMaxIterations(), rasch.getTolerance(), rasch.getModelVersion()));

        LearningModelProperties.Forgetting forgetting = properties.getForgetting();
        this.forgettingParameters = new ForgettingParameters(forgetting.getWindowDays(), forgetting.getDensityTarget(),
                forgetting.getRecencyWeight(), forgetting.getGapWeight(), forgetting.getSparsityWeight(),
                forgetting.getMasteryWeight(), forgetting.getModelVersion());
        this.forgettingRiskModel = new ForgettingRiskModel(forgettingParameters);

        LearningModelProperties.Confidence confidence = properties.getConfidence();
        this.confidenceParameters = new ConfidenceParameters(confidence.getEvidenceScale(), confidence.getRecencyWindowDays(),
                confidence.getEvidenceWeight(), confidence.getUncertaintyWeight(), confidence.getConsistencyWeight(),
                confidence.getRecencyWeight(), confidence.getModelVersion());
        this.confidenceModel = new ConfidenceModel(confidenceParameters);
    }

    public LearningStateEngine(LearningStateRepository repository, LearningModelProperties properties) {
        this(repository, properties, null);
    }

    @Transactional
    public LearningStateComputationResult recompute(String baselineVersion) {
        LearningStateRepository.BaselineContext baseline = repository.findBaseline(baselineVersion)
                .orElseThrow(() -> new IllegalArgumentException("Unknown baseline version: " + baselineVersion));
        if (!"BASELINE_SIMULATED".equals(baseline.dataOrigin())) {
            throw new IllegalArgumentException("Learning state recompute requires a simulated baseline");
        }

        List<PracticeObservation> observations = repository.findPracticeObservations(baselineVersion);
        Instant computedAt = baseline.referenceDate().atTime(23, 59, 59)
                .atOffset(ZoneOffset.UTC).toInstant();
        Map<String, String> knowledgePointNames = repository.findKnowledgePoints(
                        observations.stream().findFirst().orElseThrow().courseId()).stream()
                .collect(Collectors.toMap(LearningStateRepository.KnowledgePointRef::knowledgePointId,
                        LearningStateRepository.KnowledgePointRef::name));
        Map<StudentCourseKey, List<PracticeObservation>> byStudentCourse = observations.stream()
                .collect(Collectors.groupingBy(o -> new StudentCourseKey(o.studentId(), o.courseId()), HashMap::new,
                        Collectors.toCollection(ArrayList::new)));
        byStudentCourse.values().forEach(this::sortObservations);

        List<StudentAbilityState> abilities = new ArrayList<>();
        Map<StudentCourseKey, RaschEstimate> estimates = new HashMap<>();
        for (Map.Entry<StudentCourseKey, List<PracticeObservation>> entry : byStudentCourse.entrySet()) {
            RaschEstimate estimate = raschModel.estimate(entry.getValue().stream()
                    .map(o -> new RaschObservation(o.correct(), o.itemDifficulty())).toList());
            estimates.put(entry.getKey(), estimate);
            PracticeObservation first = entry.getValue().get(0);
            abilities.add(new StudentAbilityState(first.studentId(), first.courseId(), estimate.theta(),
                    estimate.standardError(), raschModelVersion(), computedAt, baseline.sourceVersion()));
        }

        Map<KnowledgeStateKey, List<PracticeObservation>> byKnowledge = observations.stream()
                .collect(Collectors.groupingBy(o -> new KnowledgeStateKey(o.studentId(), o.courseId(), o.classId(),
                        o.knowledgePointId()), HashMap::new, Collectors.toCollection(ArrayList::new)));
        byKnowledge.values().forEach(this::sortObservations);

        List<StudentKnowledgeState> states = new ArrayList<>();
        List<WeakKnowledgePointCandidate> candidates = new ArrayList<>();
        Map<StudentCourseKey, List<WeakKnowledgePointCandidate>> candidatesByStudent = new HashMap<>();
        for (Map.Entry<KnowledgeStateKey, List<PracticeObservation>> entry : byKnowledge.entrySet()) {
            KnowledgeStateKey key = entry.getKey();
            List<PracticeObservation> group = entry.getValue();
            double mastery = bktModel.posterior(group.stream().map(PracticeObservation::correct).toList());
            PracticeObservation last = group.get(group.size() - 1);
            Duration gap = group.size() < 2 ? null
                    : Duration.between(group.get(group.size() - 2).attemptTime(), last.attemptTime());
            StudentCourseKey studentCourseKey = new StudentCourseKey(key.studentId(), key.courseId());
            RaschEstimate ability = estimates.get(studentCourseKey);
            double forgettingRisk = forgettingRiskModel.calculate(new ForgettingEvidence(
                    computedAt, last.attemptTime(), gap, group.size(), mastery));
            double consistency = observationConsistency(group);
            double confidence = confidenceModel.calculate(new ConfidenceEvidence(
                    group.size(), ability.standardError(), consistency, last.attemptTime(), computedAt));
            StudentKnowledgeState state = new StudentKnowledgeState(key.studentId(), key.courseId(), key.classId(),
                    key.knowledgePointId(), knowledgePointNames.getOrDefault(key.knowledgePointId(), key.knowledgePointId()), mastery,
                    confidence, forgettingRisk, group.size(), last.attemptTime(), bktParameters.modelVersion(),
                    raschModelVersion(), forgettingParameters.modelVersion(), confidenceParameters.modelVersion(),
                    computedAt, baseline.sourceVersion());
            states.add(state);

            List<String> reasons = reasonCodes(group, mastery, forgettingRisk, confidence);
            boolean repeatedRecentErrors = hasRepeatedRecentErrors(group);
            WeakKnowledgePointCandidate candidate = new WeakKnowledgePointCandidate(
                    key.studentId(), key.courseId(), key.knowledgePointId(), state.knowledgePointName(),
                    weaknessScore(mastery, forgettingRisk, repeatedRecentErrors), confidence, group.size(), 0, reasons,
                    WEAK_RANKING_MODEL_VERSION);
            candidatesByStudent.computeIfAbsent(studentCourseKey, ignored -> new ArrayList<>()).add(candidate);
        }

        for (List<WeakKnowledgePointCandidate> studentCandidates : candidatesByStudent.values()) {
            studentCandidates.sort(Comparator.comparingDouble(WeakKnowledgePointCandidate::weaknessScore).reversed()
                    .thenComparing(WeakKnowledgePointCandidate::knowledgePointId));
            for (int index = 0; index < studentCandidates.size(); index++) {
                WeakKnowledgePointCandidate candidate = studentCandidates.get(index);
                candidates.add(new WeakKnowledgePointCandidate(candidate.studentId(), candidate.courseId(),
                        candidate.knowledgePointId(), candidate.knowledgePointName(), candidate.weaknessScore(),
                        candidate.confidence(), candidate.evidenceCount(), index + 1, candidate.reasonCodes(),
                        candidate.modelVersion()));
            }
        }

        repository.replaceDerived(baselineVersion, baseline.sourceVersion(), abilities, states, candidates);
        if (analyticsProjectionService != null) analyticsProjectionService.refresh();
        return new LearningStateComputationResult(baselineVersion, abilities.size(), states.size(), candidates.size());
    }

    /** Recomputes only one student's course scope from baseline plus the active LIVE_DEMO run. */
    @Transactional
    public LearningStateComputationResult recomputeForStudentCourse(String studentId, String courseId, Instant asOf,
                                                                     String demoRunId, String demoCaseId,
                                                                     String correlationId) {
        LearningStateRepository.BaselineContext baseline = repository.findBaseline("baseline-ds-v1")
                .orElseThrow(() -> new IllegalArgumentException("Baseline is not available"));
        List<PracticeObservation> observations = repository.findPracticeObservationsForScope(
                baseline.baselineVersion(), studentId, courseId, demoRunId);
        if (observations.isEmpty()) throw new IllegalArgumentException("No practice evidence is available");
        Map<String, String> knowledgePointNames = repository.findKnowledgePoints(courseId).stream()
                .collect(Collectors.toMap(LearningStateRepository.KnowledgePointRef::knowledgePointId,
                        LearningStateRepository.KnowledgePointRef::name));
        observations.sort(Comparator.comparing(PracticeObservation::attemptTime).thenComparing(PracticeObservation::attemptId));
        RaschEstimate estimate = raschModel.estimate(observations.stream()
                .map(o -> new RaschObservation(o.correct(), o.itemDifficulty())).toList());
        StudentAbilityState ability = new StudentAbilityState(studentId, courseId, estimate.theta(), estimate.standardError(),
                raschModelVersion, estimateTime(asOf), baseline.sourceVersion());
        Map<String, List<PracticeObservation>> byKnowledge = observations.stream()
                .collect(Collectors.groupingBy(PracticeObservation::knowledgePointId, HashMap::new,
                        Collectors.toCollection(ArrayList::new)));
        List<StudentKnowledgeState> states = new ArrayList<>();
        for (Map.Entry<String, List<PracticeObservation>> entry : byKnowledge.entrySet()) {
            List<PracticeObservation> group = entry.getValue();
            sortObservations(group);
            PracticeObservation last = group.get(group.size() - 1);
            Duration gap = group.size() < 2 ? null : Duration.between(group.get(group.size() - 2).attemptTime(), last.attemptTime());
            double mastery = bktModel.posterior(group.stream().map(PracticeObservation::correct).toList());
            double forgettingRisk = forgettingRiskModel.calculate(new ForgettingEvidence(
                    asOf, last.attemptTime(), gap, group.size(), mastery));
            double confidence = confidenceModel.calculate(new ConfidenceEvidence(
                    group.size(), estimate.standardError(), observationConsistency(group), last.attemptTime(), asOf));
            states.add(new StudentKnowledgeState(studentId, courseId, last.classId(), entry.getKey(),
                    knowledgePointNames.getOrDefault(entry.getKey(), entry.getKey()), mastery, confidence, forgettingRisk,
                    group.size(), last.attemptTime(), bktParameters.modelVersion(), raschModelVersion,
                    forgettingParameters.modelVersion(), confidenceParameters.modelVersion(), asOf, baseline.sourceVersion()));
        }
        states.sort(Comparator.comparing(StudentKnowledgeState::knowledgePointId));
        List<WeakKnowledgePointCandidate> candidates = states.stream()
                .map(state -> {
                    List<PracticeObservation> group = byKnowledge.get(state.knowledgePointId());
                    boolean repeated = hasRepeatedRecentErrors(group);
                    return new WeakKnowledgePointCandidate(studentId, courseId, state.knowledgePointId(), state.knowledgePointName(),
                            weaknessScore(state.masteryProbability(), state.forgettingRisk(), repeated), state.confidence(),
                            state.evidenceCount(), 0, reasonCodes(group, state.masteryProbability(), state.forgettingRisk(), state.confidence()),
                            WEAK_RANKING_MODEL_VERSION);
                }).sorted(Comparator.comparingDouble(WeakKnowledgePointCandidate::weaknessScore).reversed()
                        .thenComparing(WeakKnowledgePointCandidate::knowledgePointId)).toList();
        List<WeakKnowledgePointCandidate> ranked = new ArrayList<>();
        for (int index = 0; index < candidates.size(); index++) {
            WeakKnowledgePointCandidate candidate = candidates.get(index);
            ranked.add(new WeakKnowledgePointCandidate(candidate.studentId(), candidate.courseId(), candidate.knowledgePointId(),
                    candidate.knowledgePointName(), candidate.weaknessScore(), candidate.confidence(), candidate.evidenceCount(),
                    index + 1, candidate.reasonCodes(), candidate.modelVersion()));
        }
        repository.replaceScopedDerived(baseline.baselineVersion(), baseline.sourceVersion(), studentId, courseId,
                demoRunId, demoCaseId, correlationId, ability, states, ranked);
        if (analyticsProjectionService != null) analyticsProjectionService.refresh();
        return new LearningStateComputationResult(baseline.baselineVersion(), 1, states.size(), ranked.size());
    }

    private Instant estimateTime(Instant asOf) {
        return asOf == null ? Instant.now() : asOf;
    }

    @Transactional(readOnly = true)
    public LearningStateView read(String studentId, String courseId, String knowledgePointId) {
        List<WeakKnowledgePointCandidate> candidates = repository.findCandidates(studentId, courseId);
        String selectedKnowledgePointId = knowledgePointId;
        if (selectedKnowledgePointId == null || selectedKnowledgePointId.isBlank()) {
            selectedKnowledgePointId = candidates.stream().findFirst()
                    .map(WeakKnowledgePointCandidate::knowledgePointId)
                    .orElse(null);
        }
        StudentKnowledgeState state = selectedKnowledgePointId == null
                ? null : repository.findState(studentId, courseId, selectedKnowledgePointId).orElse(null);
        StudentAbilityState ability = repository.findAbility(studentId, courseId).orElse(null);
        if (state == null || ability == null) {
            throw new LearningStateNotFoundException(studentId, courseId, selectedKnowledgePointId);
        }
        return new LearningStateView(state, ability, candidates);
    }

    @Transactional(readOnly = true)
    public GrowthReadModel readGrowth(String studentId, String courseId) {
        List<StudentKnowledgeState> states = repository.findStates(studentId, courseId);
        if (states.isEmpty()) {
            throw new LearningStateNotFoundException(studentId, courseId, null);
        }
        double averageMastery = states.stream().mapToDouble(StudentKnowledgeState::masteryProbability).average()
                .orElse(0.0);
        Instant computedAt = states.stream().map(StudentKnowledgeState::computedAt).max(Comparator.naturalOrder())
                .orElseThrow();
        return new GrowthReadModel(studentId, courseId, averageMastery,
                List.of(new GrowthReadModel.TrendPoint(computedAt.atZone(ZoneOffset.UTC).toLocalDate(), averageMastery)),
                0, 0, null);
    }

    private void sortObservations(List<PracticeObservation> observations) {
        observations.sort(Comparator.comparing(PracticeObservation::attemptTime)
                .thenComparing(PracticeObservation::attemptId));
    }

    private List<String> reasonCodes(List<PracticeObservation> observations, double mastery,
                                     double forgettingRisk, double confidence) {
        List<String> reasons = new ArrayList<>();
        if (mastery < 0.5) {
            reasons.add("LOW_MASTERY");
        }
        if (forgettingRisk >= 0.5) {
            reasons.add("HIGH_FORGETTING_RISK");
        }
        if (hasRepeatedRecentErrors(observations)) {
            reasons.add("REPEATED_RECENT_ERRORS");
        }
        if (confidence < 0.5) {
            reasons.add("LOW_EVIDENCE_CONFIDENCE");
        }
        if (reasons.isEmpty()) {
            reasons.add("LOW_EVIDENCE_CONFIDENCE");
        }
        return List.copyOf(reasons);
    }

    private double observationConsistency(List<PracticeObservation> observations) {
        long correct = observations.stream().filter(PracticeObservation::correct).count();
        double rate = (double) correct / observations.size();
        return Math.abs(2.0 * rate - 1.0);
    }

    private boolean hasRepeatedRecentErrors(List<PracticeObservation> observations) {
        return observations.size() >= 2
                && !observations.get(observations.size() - 1).correct()
                && !observations.get(observations.size() - 2).correct();
    }

    private double weaknessScore(double mastery, double forgettingRisk, boolean repeatedRecentErrors) {
        double repeatedErrorSignal = repeatedRecentErrors ? 1.0 : 0.0;
        return clamp(0.65 * (1.0 - mastery) + 0.20 * forgettingRisk + 0.15 * repeatedErrorSignal);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private String raschModelVersion() {
        return raschModelVersion;
    }

    private record StudentCourseKey(String studentId, String courseId) {
    }

    private record KnowledgeStateKey(String studentId, String courseId, String classId, String knowledgePointId) {
    }
}
