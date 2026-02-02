import React, { useState } from 'react';

interface PatternLogicProps {
  challengeData: Record<string, any>;
  onSubmit: (response: Record<string, any>) => void;
}

const PatternLogic: React.FC<PatternLogicProps> = ({ challengeData, onSubmit }) => {
  const [selectedAnswer, setSelectedAnswer] = useState<any>(null);
  const pattern = challengeData.pattern || [];
  const patternType = challengeData.patternType || 'numeric_sequence';

  const handleSubmit = () => {
    if (selectedAnswer !== null) {
      onSubmit({ answer: selectedAnswer });
    }
  };

  const generateOptions = () => {
    // Generate possible answers based on pattern type
    if (patternType === 'numeric_sequence') {
      const last = pattern[pattern.length - 1];
      const secondLast = pattern[pattern.length - 2];
      const step = (last as number) - (secondLast as number);
      const correct = (last as number) + step;
      
      return [
        correct,
        correct + 1,
        correct - 1,
        correct + step,
      ].sort(() => Math.random() - 0.5);
    }
    return [];
  };

  return (
    <div className="pattern-logic-challenge">
      <h3>Pattern Logic Challenge</h3>
      <p>What comes next in this pattern?</p>
      
      <div className="pattern-display">
        {pattern.map((item: any, index: number) => (
          <span key={index} className="pattern-item">
            {item}
          </span>
        ))}
        <span className="pattern-item question">?</span>
      </div>

      <div className="options">
        {generateOptions().map((option, index) => (
          <button
            key={index}
            onClick={() => setSelectedAnswer(option)}
            className={selectedAnswer === option ? 'selected' : ''}
          >
            {option}
          </button>
        ))}
      </div>

      <button onClick={handleSubmit} disabled={selectedAnswer === null}>
        Submit
      </button>
    </div>
  );
};

export default PatternLogic;
