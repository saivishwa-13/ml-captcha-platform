package com.mlcaptcha.service.challenge;

import java.util.Map;

/**
 * Strategy interface for different CAPTCHA challenge types.
 * Each implementation handles generation and verification of a specific challenge type.
 */
public interface ChallengeStrategy {
    
    /**
     * Get the challenge type identifier
     */
    String getChallengeType();
    
    /**
     * Generate a new challenge
     * @param difficultyLevel The difficulty level (1-5)
     * @return Map containing challenge data to be sent to client
     */
    Map<String, Object> generateChallenge(int difficultyLevel);
    
    /**
     * Verify the user's response to a challenge
     * @param expectedResponse The expected response data
     * @param userResponse The user's response
     * @return true if verification passes, false otherwise
     */
    boolean verifyChallenge(Map<String, Object> expectedResponse, Map<String, Object> userResponse);
    
    /**
     * Calculate expected response for a challenge
     * @param challengeData The challenge data
     * @return Expected response data
     */
    Map<String, Object> calculateExpectedResponse(Map<String, Object> challengeData);
}
