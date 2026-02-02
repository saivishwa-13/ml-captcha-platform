import React, { useState, useEffect, useRef } from 'react';
import { captchaService } from '../../services/captcha';
import { useBehaviorTracking } from '../../hooks/useBehaviorTracking';
import { CaptchaChallenge, BehaviorMetrics } from '../../types/captcha';

type SequenceStatus = 'loading' | 'displaying' | 'input' | 'verifying';
interface SequenceMemoryProps {
  apiKey: string;
  difficultyLevel?: number;
  onSuccess: (token: string) => void;
  onError: (error: string) => void;
}

const SequenceMemory: React.FC<SequenceMemoryProps> = ({
  apiKey,
  difficultyLevel = 1,
  onSuccess,
  onError,
}) => {
  const [challenge, setChallenge] = useState<CaptchaChallenge | null>(null);
  const [sequence, setSequence] = useState<any[]>([]);
  const [userInput, setUserInput] = useState<any[]>([]);
  const [showSequence, setShowSequence] = useState(false);
  const [displayDuration, setDisplayDuration] = useState(5000);
  const [sequenceType, setSequenceType] = useState<string>('numeric');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [status, setStatus] = useState<SequenceStatus>('loading');

  
  const { startTracking, stopTracking, getMetrics } = useBehaviorTracking();
  const displayTimerRef = useRef<number | null>(null);
  const challengeStartTimeRef = useRef<number>(0);

  // Load challenge on mount
  useEffect(() => {
    loadChallenge();
  }, []);

  // Cleanup timers on unmount
  useEffect(() => {
    return () => {
      if (displayTimerRef.current) {
        clearTimeout(displayTimerRef.current);
      }
    };
  }, []);

  const loadChallenge = async () => {
    try {
      setLoading(true);
      setError(null);
      setStatus('loading');
      
      const newChallenge = await captchaService.generateChallenge(apiKey, 'sequence_memory', difficultyLevel);
      setChallenge(newChallenge);
      
      const seq = newChallenge.challengeData.sequence || [];
      const duration = newChallenge.challengeData.displayDuration || 5000;
      const type = newChallenge.challengeData.sequenceType || 'numeric';
      
      setSequence(seq);
      setDisplayDuration(duration);
      setSequenceType(type);
      setUserInput([]);
      challengeStartTimeRef.current = Date.now();
      
      // Start behavior tracking
      startTracking();
      
      // Show sequence
      setStatus('displaying');
      setShowSequence(true);
      
      // Hide sequence after display duration
      displayTimerRef.current = window.setTimeout(() => {
        setShowSequence(false);
        setStatus('input');
      }, duration);
      
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || err.message || 'Failed to load challenge';
      setError(errorMsg);
      setStatus('loading');
      onError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  const handleInput = (value: any) => {
    if (userInput.length < sequence.length) {
      setUserInput([...userInput, value]);
    }
  };

  const handleRemoveLast = () => {
    if (userInput.length > 0) {
      setUserInput(userInput.slice(0, -1));
    }
  };

  const handleSubmit = async () => {
    if (!challenge || userInput.length !== sequence.length) {
      return;
    }

    try {
      setStatus('verifying');
      setLoading(true);
      
      // Get behavior metrics
      const behaviorMetrics: BehaviorMetrics = getMetrics();
      
      // Calculate completion time
      const completionTime = Date.now() - challengeStartTimeRef.current;
      behaviorMetrics.completionTime = completionTime;
      
      // Stop tracking
      stopTracking();
      
      // Verify challenge
      const result = await captchaService.verifyChallenge(
        apiKey,
        challenge.sessionToken,
        { sequence: userInput },
        behaviorMetrics
      );

      if (result.verified && result.isHuman) {
        onSuccess(result.token);
      } else {
        const errorMsg = 'Verification failed. Please try again.';
        setError(errorMsg);
        onError(errorMsg);
        // Reload challenge for retry
        setTimeout(() => {
          loadChallenge();
        }, 2000);
      }
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || err.message || 'Verification failed';
      setError(errorMsg);
      onError(errorMsg);
      setStatus('input');
      // Reload challenge for retry
      setTimeout(() => {
        loadChallenge();
      }, 2000);
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setUserInput([]);
    setShowSequence(true);
    setStatus('displaying');
    
    if (displayTimerRef.current) {
      clearTimeout(displayTimerRef.current);
    }
    
    displayTimerRef.current = setTimeout(() => {
      setShowSequence(false);
      setStatus('input');
    }, displayDuration);
  };

  // Render numeric input buttons
  const renderNumericInput = () => (
    <div className="input-options numeric">
      {[0, 1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
        <button
          key={num}
          onClick={() => handleInput(num)}
          className="number-button"
          disabled={userInput.length >= sequence.length || loading}
        >
          {num}
        </button>
      ))}
    </div>
  );

  // Render symbolic input buttons
  const renderSymbolicInput = () => {
    const symbols = ['!', '@', '#', '$', '%', '&', '*', '+', '=', '?'];
    return (
      <div className="input-options symbolic">
        {symbols.map((symbol) => (
          <button
            key={symbol}
            onClick={() => handleInput(symbol)}
            className="symbol-button"
            disabled={userInput.length >= sequence.length || loading}
          >
            {symbol}
          </button>
        ))}
      </div>
    );
  };

  // Render alphanumeric input buttons
  const renderAlphanumericInput = () => {
    const options = [
      ...Array.from({ length: 10 }, (_, i) => i.toString()),
      ...Array.from({ length: 26 }, (_, i) => String.fromCharCode(65 + i)),
    ];
    
    return (
      <div className="input-options alphanumeric">
        {options.map((option) => (
          <button
            key={option}
            onClick={() => handleInput(option)}
            className="alphanumeric-button"
            disabled={userInput.length >= sequence.length || loading}
          >
            {option}
          </button>
        ))}
      </div>
    );
  };

  if (status === 'loading') {
    return (
      <div className="sequence-memory-challenge loading">
        <div className="loading-spinner">Loading challenge...</div>
      </div>
    );
  }

  return (
    <div className="sequence-memory-challenge">
      <h3>Sequence Memory Challenge</h3>
      
      {error && <div className="error-message">{error}</div>}
      
      {status === 'displaying' && showSequence && (
        <div className="sequence-display">
          <p className="instruction">Remember this sequence:</p>
          <div className="sequence-container">
            {sequence.map((item, index) => (
              <span key={index} className="sequence-item">
                {item}
              </span>
            ))}
          </div>
          <div className="countdown">
            Sequence will hide in {Math.ceil((displayDuration - (Date.now() - challengeStartTimeRef.current)) / 1000)}s
          </div>
        </div>
      )}
      
      {status === 'input' && (
        <div className="input-section">
          <p className="instruction">Enter the sequence you saw:</p>
          
          <div className="user-input-display">
            <div className="input-sequence">
              {sequence.map((_, index) => (
                <span
                  key={index}
                  className={`input-slot ${userInput[index] !== undefined ? 'filled' : 'empty'}`}
                >
                  {userInput[index] !== undefined ? userInput[index] : '?'}
                </span>
              ))}
            </div>
            <div className="input-progress">
              {userInput.length} / {sequence.length}
            </div>
          </div>
          
          <div className="input-controls">
            {sequenceType === 'numeric' && renderNumericInput()}
            {sequenceType === 'symbolic' && renderSymbolicInput()}
            {sequenceType === 'alphanumeric' && renderAlphanumericInput()}
          </div>
          
          <div className="actions">
            <button
              onClick={handleRemoveLast}
              disabled={userInput.length === 0 || loading}
              className="action-button secondary"
            >
              Remove Last
            </button>
            <button
              onClick={handleReset}
              disabled={loading}
              className="action-button secondary"
            >
              Reset
            </button>
            <button
              onClick={handleSubmit}
              disabled={userInput.length !== sequence.length || loading}
              className="action-button primary"
            >
              {loading ? 'Verifying...' : 'Submit'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default SequenceMemory;
