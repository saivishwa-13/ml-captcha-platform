export interface CaptchaChallenge {
  sessionToken: string;
  challengeType: string;
  challengeData: Record<string, any>;
  expiresAt: string;
}

export interface CaptchaResponse {
  verified: boolean;
  confidence: number;
  isHuman: boolean;
  token: string;
}

export type ChallengeType = 
  | 'sequence_memory'
  | 'pattern_logic'
  | 'arithmetic'
  | 'physics_game'
  | 'behavior_analysis';

export interface BehaviorMetrics {
  mouseMovements: MouseMovement[];
  keystrokeTimings: KeystrokeTiming[];
  completionTime: number;
}

export interface MouseMovement {
  x: number;
  y: number;
  timestamp: number;
}

export interface KeystrokeTiming {
  key: string;
  timestamp: number;
}
