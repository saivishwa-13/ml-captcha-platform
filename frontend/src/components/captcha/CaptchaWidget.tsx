import React from 'react';
import SequenceMemory from './SequenceMemory';

interface CaptchaWidgetProps {
  apiKey: string;
  onSuccess: (token: string) => void;
  onError: (error: string) => void;
  challengeType?: string;
  difficultyLevel?: number;
}

/**
 * Main CAPTCHA Widget Component
 * Currently supports Sequence Memory CAPTCHA only
 */
const CaptchaWidget: React.FC<CaptchaWidgetProps> = ({
  apiKey,
  onSuccess,
  onError,
  challengeType,
  difficultyLevel = 1,
}) => {
  // Force sequence_memory for now
  const effectiveChallengeType = 'sequence_memory';

  if (effectiveChallengeType === 'sequence_memory') {
    return (
      <SequenceMemory
        apiKey={apiKey}
        difficultyLevel={difficultyLevel}
        onSuccess={onSuccess}
        onError={onError}
      />
    );
  }

  return (
    <div className="captcha-widget">
      <div className="error">Unsupported challenge type: {challengeType}</div>
    </div>
  );
};

export default CaptchaWidget;
