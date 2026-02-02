package com.mlcaptcha.service.challenge;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for creating and managing challenge strategies.
 */
@Component
@RequiredArgsConstructor
public class ChallengeFactory {
    
    private final List<ChallengeStrategy> strategies;
    private final Random random = new Random();
    private Map<String, ChallengeStrategy> strategyMap;
    
    private Map<String, ChallengeStrategy> getStrategyMap() {
        if (strategyMap == null) {
            strategyMap = strategies.stream()
                    .collect(Collectors.toMap(
                            ChallengeStrategy::getChallengeType,
                            Function.identity()
                    ));
        }
        return strategyMap;
    }
    
    /**
     * Get a challenge strategy by type
     */
    public ChallengeStrategy getStrategy(String challengeType) {
        return getStrategyMap().get(challengeType);
    }
    
    /**
     * Get a random challenge strategy
     */
    public ChallengeStrategy getRandomStrategy() {
        List<ChallengeStrategy> availableStrategies = strategies;
        if (availableStrategies.isEmpty()) {
            throw new IllegalStateException("No challenge strategies available");
        }
        return availableStrategies.get(random.nextInt(availableStrategies.size()));
    }
    
    /**
     * Check if a challenge type is supported
     */
    public boolean isSupported(String challengeType) {
        return getStrategyMap().containsKey(challengeType);
    }
}
