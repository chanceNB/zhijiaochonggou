package com.zhijiao.foundation.demo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

@Service
public class BaselineSeedService {
    public static final String BASELINE_VERSION = "baseline-ds-v1";
    public static final String SOURCE_VERSION = BASELINE_VERSION;
    public static final String DATA_ORIGIN = "BASELINE_SIMULATED";
    public static final String COURSE_ID = "course-data-structures";
    public static final String XIAOMING_ID = "stu-xiaoming";
    public static final String DEMO_CASE_ID = "DEMO-GRAPH-001";
    public static final String CLASS_ONE_ID = "class-cs-2024-01";
    public static final String CLASS_TWO_ID = "class-cs-2024-02";
    public static final String BFS_DFS_ID = "kp-graph-bfs-dfs";
    public static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 8, 29);

    private final BaselineRepository repository;

    public BaselineSeedService(BaselineRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BaselineSeedResult seed() {
        Instant createdAt = REFERENCE_DATE.atStartOfDay(ZoneOffset.UTC).toInstant();
        repository.insertBaselineMetadata(BASELINE_VERSION, Date.valueOf(REFERENCE_DATE), DATA_ORIGIN,
                SOURCE_VERSION, createdAt);
        repository.insertCourse(COURSE_ID, "数据结构", BASELINE_VERSION, DATA_ORIGIN, SOURCE_VERSION, createdAt);
        repository.insertClassroom(CLASS_ONE_ID, COURSE_ID, "2024级计算机1班", BASELINE_VERSION,
                DATA_ORIGIN, SOURCE_VERSION, createdAt);
        repository.insertClassroom(CLASS_TWO_ID, COURSE_ID, "2024级计算机2班", BASELINE_VERSION,
                DATA_ORIGIN, SOURCE_VERSION, createdAt);

        List<KnowledgePointSeed> knowledgePoints = knowledgePoints();
        for (KnowledgePointSeed knowledgePoint : knowledgePoints) {
            repository.insertKnowledgePoint(knowledgePoint.id(), COURSE_ID, knowledgePoint.parentId(),
                    knowledgePoint.name(), knowledgePoint.sortOrder(), BASELINE_VERSION, DATA_ORIGIN,
                    SOURCE_VERSION, createdAt);
        }

        int learningEventCount = 0;
        int practiceAttemptCount = 0;
        for (int studentIndex = 0; studentIndex < 80; studentIndex++) {
            String studentId = studentIndex == 0 ? XIAOMING_ID
                    : String.format(Locale.ROOT, "stu-ds-%03d", studentIndex + 1);
            String classId = studentIndex < 40 ? CLASS_ONE_ID : CLASS_TWO_ID;
            String displayName = studentIndex == 0 ? "小明"
                    : String.format(Locale.ROOT, "学生%02d", studentIndex + 1);
            repository.insertStudent(studentId, classId, COURSE_ID, displayName, BASELINE_VERSION,
                    DATA_ORIGIN, SOURCE_VERSION, createdAt);

            int learningEventsForStudent = studentIndex == 0 ? 12 : 6 + (studentIndex % 5);
            for (int eventIndex = 0; eventIndex < learningEventsForStudent; eventIndex++) {
                int knowledgePointIndex = studentIndex == 0 && eventIndex < 4
                        ? indexOf(knowledgePoints, BFS_DFS_ID)
                        : (studentIndex + eventIndex * 2) % knowledgePoints.size();
                KnowledgePointSeed knowledgePoint = knowledgePoints.get(knowledgePointIndex);
                boolean correct = studentIndex == 0
                        ? eventIndex >= 4
                        : ((studentIndex + eventIndex + knowledgePointIndex) % 4 != 0);
                long historyDayOffset = learningEventsForStudent == 1 ? 0
                        : Math.round((double) eventIndex * 29 / (learningEventsForStudent - 1));
                long boundedHistoryDayOffset = Math.min(29, historyDayOffset + studentIndex % 3);
                Instant eventTime = REFERENCE_DATE.minusDays(boundedHistoryDayOffset)
                        .atTime(9 + eventIndex % 4, 0).toInstant(ZoneOffset.UTC);
                repository.insertLearningEvent(
                        String.format(Locale.ROOT, "baseline-%s-learning-%02d", studentId, eventIndex + 1),
                        eventTime, studentId, COURSE_ID, classId, knowledgePoint.id(), "HISTORICAL_LEARNING",
                        correct, null, null, null, null,
                        DATA_ORIGIN, SOURCE_VERSION, BASELINE_VERSION, null, null, null, eventTime);
                learningEventCount++;
            }

            int practiceAttemptsForStudent = studentIndex == 0 ? 8 : 3 + (studentIndex % 4);
            for (int attemptIndex = 0; attemptIndex < practiceAttemptsForStudent; attemptIndex++) {
                int knowledgePointIndex = studentIndex == 0 && attemptIndex < 4
                        ? indexOf(knowledgePoints, BFS_DFS_ID)
                        : (studentIndex * 3 + attemptIndex) % knowledgePoints.size();
                KnowledgePointSeed knowledgePoint = knowledgePoints.get(knowledgePointIndex);
                boolean correct = studentIndex == 0
                        ? attemptIndex >= 5
                        : ((studentIndex + attemptIndex + knowledgePointIndex) % 3 != 0);
                long attemptDayOffset = practiceAttemptsForStudent == 1 ? 0
                        : Math.round((double) attemptIndex * 29 / (practiceAttemptsForStudent - 1));
                long boundedAttemptDayOffset = Math.min(29, attemptDayOffset + studentIndex % 4);
                Instant attemptTime = REFERENCE_DATE.minusDays(boundedAttemptDayOffset)
                        .atTime(14 + attemptIndex % 3, 0).toInstant(ZoneOffset.UTC);
                String questionId = String.format(Locale.ROOT, "baseline-q-%s-%02d", knowledgePoint.id(), attemptIndex + 1);
                String difficulty = difficultyFor(attemptIndex);
                if (knowledgePoint.id().equals(BFS_DFS_ID)) {
                    difficulty = "HARD";
                }
                int durationSeconds = studentIndex == 0 && knowledgePoint.id().equals(BFS_DFS_ID)
                        ? 180 + attemptIndex * 10
                        : 30 + ((studentIndex * 7 + attemptIndex * 11) % 90);
                repository.insertQuestionItem(questionId, COURSE_ID, knowledgePoint.id(),
                        "BASELINE_PRACTICE", difficulty, itemDifficultyFor(difficulty), BASELINE_VERSION,
                        DATA_ORIGIN, SOURCE_VERSION, createdAt);
                repository.insertPracticeAttempt(
                        String.format(Locale.ROOT, "baseline-%s-attempt-%02d", studentId, attemptIndex + 1),
                        attemptTime, studentId, COURSE_ID, classId, knowledgePoint.id(),
                        questionId, "BASELINE_PRACTICE", difficulty, correct, durationSeconds,
                        durationSeconds * 1000, attemptIndex + 1, null,
                        DATA_ORIGIN, SOURCE_VERSION, BASELINE_VERSION, null, null, null, attemptTime);
                practiceAttemptCount++;
            }
        }

        return new BaselineSeedResult(BASELINE_VERSION, 1, 2, 80, knowledgePoints.size(),
                learningEventCount, practiceAttemptCount);
    }

    private List<KnowledgePointSeed> knowledgePoints() {
        return List.of(
                new KnowledgePointSeed("kp-linear-list", null, "线性表", 1),
                new KnowledgePointSeed("kp-stack", null, "栈", 2),
                new KnowledgePointSeed("kp-queue", null, "队列", 3),
                new KnowledgePointSeed("kp-tree", null, "树", 4),
                new KnowledgePointSeed("kp-binary-tree", "kp-tree", "二叉树", 5),
                new KnowledgePointSeed("kp-graph-basics", null, "图基础", 6),
                new KnowledgePointSeed(BFS_DFS_ID, "kp-graph-basics", "图遍历 BFS / DFS", 7),
                new KnowledgePointSeed("kp-sorting", null, "排序", 8),
                new KnowledgePointSeed("kp-searching", null, "查找", 9),
                new KnowledgePointSeed("kp-hash", null, "散列表", 10)
        );
    }

    private String difficultyFor(int attemptIndex) {
        return switch (attemptIndex % 3) {
            case 0 -> "EASY";
            case 1 -> "MEDIUM";
            default -> "HARD";
        };
    }

    private BigDecimal itemDifficultyFor(String difficulty) {
        return switch (difficulty) {
            case "EASY" -> BigDecimal.valueOf(0.25);
            case "MEDIUM" -> BigDecimal.valueOf(0.50);
            default -> BigDecimal.valueOf(0.75);
        };
    }

    private int indexOf(List<KnowledgePointSeed> knowledgePoints, String id) {
        for (int index = 0; index < knowledgePoints.size(); index++) {
            if (knowledgePoints.get(index).id().equals(id)) {
                return index;
            }
        }
        throw new IllegalStateException("Knowledge point missing from baseline: " + id);
    }

    private record KnowledgePointSeed(String id, String parentId, String name, int sortOrder) {
    }
}
