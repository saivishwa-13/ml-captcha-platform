"""
ML Inference Service
Handles all ML model inference for behavior analysis and human/bot classification.
"""
import logging
from typing import Dict, List, Optional
import numpy as np

logger = logging.getLogger(__name__)


class MLInferenceService:
    """
    Main ML inference service that coordinates all models.
    Currently implements fallback logic until models are trained.
    """
    
    def __init__(self):
        # TODO: Load trained models here
        # self.cnn_model = load_model('models/cnn/model.h5')
        # self.lstm_model = load_model('models/lstm/model.h5')
        # self.random_forest = joblib.load('models/random_forest/model.pkl')
        # self.kmeans = joblib.load('models/kmeans/model.pkl')
        logger.info("ML Inference Service initialized (using fallback logic)")
    
    def analyze(
        self,
        session_id: str,
        challenge_type: str,
        user_response: Dict,
        mouse_movements: Optional[List[Dict]] = None,
        keystroke_timings: Optional[List[Dict]] = None,
        completion_time: Optional[int] = None
    ) -> Dict:
        """
        Main analysis method that aggregates results from all models.
        
        Returns:
            Dict with 'is_human', 'confidence', and 'model_predictions'
        """
        # Extract features
        features = self._extract_features(
            mouse_movements=mouse_movements,
            keystroke_timings=keystroke_timings,
            completion_time=completion_time,
            challenge_type=challenge_type
        )
        
        # Run individual model predictions
        predictions = {}
        
        # K-Means clustering for behavior
        if mouse_movements or keystroke_timings:
            cluster_result = self._kmeans_clustering(features)
            predictions["kmeans"] = cluster_result
        
        # Random Forest final classification
        rf_result = self._random_forest_classify(features)
        predictions["random_forest"] = rf_result
        
        # Aggregate results
        is_human = rf_result["is_human"]
        confidence = rf_result["confidence"]
        
        return {
            "is_human": is_human,
            "confidence": confidence,
            "model_predictions": predictions
        }
    
    def _extract_features(
        self,
        mouse_movements: Optional[List[Dict]],
        keystroke_timings: Optional[List[Dict]],
        completion_time: Optional[int],
        challenge_type: str
    ) -> Dict:
        """Extract features from raw behavior data."""
        features = {}
        
        # Mouse movement features
        if mouse_movements:
            features["mouse_movement_count"] = len(mouse_movements)
            features["mouse_total_distance"] = self._calculate_mouse_distance(mouse_movements)
            features["mouse_avg_velocity"] = self._calculate_avg_velocity(mouse_movements)
        else:
            features["mouse_movement_count"] = 0
            features["mouse_total_distance"] = 0
            features["mouse_avg_velocity"] = 0
        
        # Keystroke features
        if keystroke_timings:
            features["keystroke_count"] = len(keystroke_timings)
            features["keystroke_avg_interval"] = self._calculate_avg_keystroke_interval(keystroke_timings)
        else:
            features["keystroke_count"] = 0
            features["keystroke_avg_interval"] = 0
        
        # Timing features
        features["completion_time"] = completion_time if completion_time else 0
        
        return features
    
    def _calculate_mouse_distance(self, movements: List[Dict]) -> float:
        """Calculate total mouse movement distance."""
        if not movements or len(movements) < 2:
            return 0.0
        
        total_distance = 0.0
        for i in range(1, len(movements)):
            prev = movements[i-1]
            curr = movements[i]
            dx = curr.get("x", 0) - prev.get("x", 0)
            dy = curr.get("y", 0) - prev.get("y", 0)
            total_distance += np.sqrt(dx*dx + dy*dy)
        
        return total_distance
    
    def _calculate_avg_velocity(self, movements: List[Dict]) -> float:
        """Calculate average mouse velocity."""
        if not movements or len(movements) < 2:
            return 0.0
        
        velocities = []
        for i in range(1, len(movements)):
            prev = movements[i-1]
            curr = movements[i]
            dt = curr.get("timestamp", 0) - prev.get("timestamp", 0)
            if dt > 0:
                dx = curr.get("x", 0) - prev.get("x", 0)
                dy = curr.get("y", 0) - prev.get("y", 0)
                distance = np.sqrt(dx*dx + dy*dy)
                velocity = distance / dt
                velocities.append(velocity)
        
        return np.mean(velocities) if velocities else 0.0
    
    def _calculate_avg_keystroke_interval(self, timings: List[Dict]) -> float:
        """Calculate average time between keystrokes."""
        if not timings or len(timings) < 2:
            return 0.0
        
        intervals = []
        for i in range(1, len(timings)):
            prev_time = timings[i-1].get("timestamp", 0)
            curr_time = timings[i].get("timestamp", 0)
            intervals.append(curr_time - prev_time)
        
        return np.mean(intervals) if intervals else 0.0
    
    def _kmeans_clustering(self, features: Dict) -> Dict:
        """
        K-Means clustering for behavior patterns.
        TODO: Replace with actual trained model.
        """
        # Fallback logic: simple heuristics
        movement_count = features.get("mouse_movement_count", 0)
        avg_velocity = features.get("mouse_avg_velocity", 0)
        
        # Human-like: moderate movement, natural velocity
        is_human_like = (movement_count > 3) and (0.1 < avg_velocity < 10.0)
        
        return {
            "cluster": "human" if is_human_like else "bot",
            "is_human": is_human_like,
            "confidence": 0.75
        }
    
    def _random_forest_classify(self, features: Dict) -> Dict:
        """
        Random Forest classifier for final human/bot decision.
        TODO: Replace with actual trained model.
        """
        # Fallback logic: rule-based classification
        completion_time = features.get("completion_time", 0)
        movement_count = features.get("mouse_movement_count", 0)
        keystroke_count = features.get("keystroke_count", 0)
        
        # Heuristics
        score = 0.0
        
        # Completion time check (too fast = bot)
        if 1000 < completion_time < 30000:
            score += 0.3
        elif completion_time < 500:
            score -= 0.5
        
        # Mouse movement check
        if movement_count > 5:
            score += 0.3
        elif movement_count == 0:
            score -= 0.4
        
        # Keystroke check
        if keystroke_count > 0:
            score += 0.2
        
        # Normalize to 0-1
        confidence = max(0.0, min(1.0, 0.5 + score))
        is_human = confidence > 0.6
        
        return {
            "is_human": is_human,
            "confidence": confidence
        }
