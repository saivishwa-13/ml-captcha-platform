import React, { useState, useEffect } from 'react';

interface ArithmeticProps {
  challengeData: Record<string, any>;
  onSubmit: (response: Record<string, any>) => void;
}

const Arithmetic: React.FC<ArithmeticProps> = ({ challengeData, onSubmit }) => {
  const [answer, setAnswer] = useState<string>('');
  const [timeRemaining, setTimeRemaining] = useState<number>(0);
  const [startTime] = useState<number>(Date.now());

  const operand1 = challengeData.operand1 || 0;
  const operand2 = challengeData.operand2 || 0;
  const operation = challengeData.operation || '+';
  const timeLimit = challengeData.timeLimit || 15000;

  useEffect(() => {
    setTimeRemaining(timeLimit);
    const interval = setInterval(() => {
      setTimeRemaining((prev) => {
        if (prev <= 100) {
          clearInterval(interval);
          return 0;
        }
        return prev - 100;
      });
    }, 100);

    return () => clearInterval(interval);
  }, [timeLimit]);

  const getOperationSymbol = () => {
    switch (operation) {
      case '+': return '+';
      case '-': return '−';
      case '*': return '×';
      case '/': return '÷';
      default: return '+';
    }
  };

  const handleSubmit = () => {
    const completionTime = Date.now() - startTime;
    onSubmit({
      answer: parseInt(answer),
      completionTime,
    });
  };

  return (
    <div className="arithmetic-challenge">
      <h3>Arithmetic Challenge</h3>
      <div className="timer">
        Time remaining: {(timeRemaining / 1000).toFixed(1)}s
      </div>
      
      <div className="question">
        <span className="operand">{operand1}</span>
        <span className="operation">{getOperationSymbol()}</span>
        <span className="operand">{operand2}</span>
        <span className="equals">=</span>
        <input
          type="number"
          value={answer}
          onChange={(e) => setAnswer(e.target.value)}
          className="answer-input"
          autoFocus
        />
      </div>

      <button onClick={handleSubmit} disabled={!answer || timeRemaining === 0}>
        Submit
      </button>
    </div>
  );
};

export default Arithmetic;
