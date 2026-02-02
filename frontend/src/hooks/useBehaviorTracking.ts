import { useState, useEffect, useRef, useCallback } from 'react';
import { BehaviorMetrics, MouseMovement, KeystrokeTiming } from '../types/captcha';

export const useBehaviorTracking = () => {
  const [isTracking, setIsTracking] = useState(false);
  const mouseMovementsRef = useRef<MouseMovement[]>([]);
  const keystrokeTimingsRef = useRef<KeystrokeTiming[]>([]);
  const startTimeRef = useRef<number>(0);
  const mouseHandlerRef = useRef<((e: MouseEvent) => void) | null>(null);
  const keyHandlerRef = useRef<((e: KeyboardEvent) => void) | null>(null);

  const startTracking = useCallback(() => {
    setIsTracking(true);
    mouseMovementsRef.current = [];
    keystrokeTimingsRef.current = [];
    startTimeRef.current = Date.now();

    // Mouse movement handler
    mouseHandlerRef.current = (e: MouseEvent) => {
      mouseMovementsRef.current.push({
        x: e.clientX,
        y: e.clientY,
        timestamp: Date.now() - startTimeRef.current,
      });
    };

    // Keystroke handler
    keyHandlerRef.current = (e: KeyboardEvent) => {
      keystrokeTimingsRef.current.push({
        key: e.key,
        timestamp: Date.now() - startTimeRef.current,
      });
    };

    window.addEventListener('mousemove', mouseHandlerRef.current);
    window.addEventListener('keydown', keyHandlerRef.current);
  }, []);

  const stopTracking = useCallback(() => {
    setIsTracking(false);
    
    if (mouseHandlerRef.current) {
      window.removeEventListener('mousemove', mouseHandlerRef.current);
      mouseHandlerRef.current = null;
    }
    
    if (keyHandlerRef.current) {
      window.removeEventListener('keydown', keyHandlerRef.current);
      keyHandlerRef.current = null;
    }
  }, []);

  const getMetrics = useCallback((): BehaviorMetrics => {
    const completionTime = Date.now() - startTimeRef.current;
    return {
      mouseMovements: [...mouseMovementsRef.current],
      keystrokeTimings: [...keystrokeTimingsRef.current],
      completionTime,
    };
  }, []);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      stopTracking();
    };
  }, [stopTracking]);

  return {
    startTracking,
    stopTracking,
    getMetrics,
    isTracking,
  };
};
