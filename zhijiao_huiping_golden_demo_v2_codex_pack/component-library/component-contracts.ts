export type AsyncUiState =
  | 'INITIAL' | 'LOADING' | 'READY' | 'EMPTY' | 'SUBMITTING'
  | 'SUCCESS' | 'STALE' | 'DEGRADED' | 'FORBIDDEN' | 'ERROR';

export interface RagCitationVm {
  documentId: string;
  chunkId: string;
  title: string;
  excerpt: string;
  score?: number;
}

export interface TeacherAssignmentVm {
  assignmentId: string;
  interventionId: string;
  title: string;
  teacherName: string;
  knowledgePointName: string;
  objective: string;
  estimatedMinutes: number;
  dueAt?: string;
  practiceSetId?: string;
}

export interface StrategyCandidateVm {
  strategyCode: string;
  title: string;
  rationale: string;
  actionDescription: string;
  predictedLift?: number;
  predictionLow?: number;
  predictionHigh?: number;
}

export interface SmartBiAssetVm {
  assetKey: 'governance' | 'course-diagnosis' | 'student-risk' | 'intervention-outcome';
  displayName: string;
  status: 'PLATFORM_PENDING' | 'VERIFIED' | 'BLOCKED';
  launchMode: 'UNVERIFIED' | 'NEW_TAB' | 'IFRAME';
  resourceUrl?: string;
  dataFreshness?: string;
}
