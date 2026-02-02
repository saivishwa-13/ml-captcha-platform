package com.mlcaptcha.service.challenge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Sequence Memory CAPTCHA Strategy
 * Generates sequences (numeric, visual, symbolic) that users must recall and reproduce.
 */
@Component
@Slf4j
public class SequenceMemoryStrategy implements ChallengeStrategy {
    
    private static final String CHALLENGE_TYPE = "sequence_memory";
    private static final Random random = new Random();
    
    @Override
    public String getChallengeType() {
        return CHALLENGE_TYPE;
    }
    
    @Override
    public Map<String, Object> generateChallenge(int difficultyLevel) {
        int sequenceLength = 3 + difficultyLevel; // 4-8 items based on difficulty
        String sequenceType = getSequenceType(difficultyLevel);
        
        List<Object> sequence = generateSequence(sequenceLength, sequenceType);
        int displayDuration = calculateDisplayDuration(sequenceLength, difficultyLevel);
        
        Map<String, Object> challenge = new HashMap<>();
        challenge.put("sequence", sequence);
        challenge.put("sequenceType", sequenceType);
        challenge.put("displayDuration", displayDuration);
        challenge.put("length", sequenceLength);
        
        return challenge;
    }
    
    @Override
    public boolean verifyChallenge(Map<String, Object> expectedResponse, Map<String, Object> userResponse) {
        if (expectedResponse == null || userResponse == null) {
            return false;
        }
        
        @SuppressWarnings("unchecked")
        List<Object> expectedSequence = (List<Object>) expectedResponse.get("sequence");
        @SuppressWarnings("unchecked")
        List<Object> userSequence = (List<Object>) userResponse.get("sequence");
        
        if (expectedSequence == null || userSequence == null) {
            return false;
        }
        
        return expectedSequence.equals(userSequence);
    }
    
    @Override
    public Map<String, Object> calculateExpectedResponse(Map<String, Object> challengeData) {
        @SuppressWarnings("unchecked")
        List<Object> sequence = (List<Object>) challengeData.get("sequence");
        
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("sequence", new ArrayList<>(sequence));
        
        return expectedResponse;
    }
    
    private String getSequenceType(int difficultyLevel) {
        if (difficultyLevel <= 2) {
            return "numeric";
        } else if (difficultyLevel <= 4) {
            return random.nextBoolean() ? "numeric" : "symbolic";
        } else {
            String[] types = {"numeric", "symbolic", "alphanumeric"};
            return types[random.nextInt(types.length)];
        }
    }
    
    private List<Object> generateSequence(int length, String type) {
        return switch (type) {
            case "numeric" -> IntStream.range(0, length)
                    .mapToObj(i -> random.nextInt(10))
                    .collect(Collectors.toList());
            case "symbolic" -> IntStream.range(0, length)
                    .mapToObj(i -> getRandomSymbol())
                    .collect(Collectors.toList());
            case "alphanumeric" -> IntStream.range(0, length)
                    .mapToObj(i -> random.nextBoolean() 
                            ? String.valueOf(random.nextInt(10))
                            : String.valueOf((char) ('A' + random.nextInt(26))))
                    .collect(Collectors.toList());
            default -> Collections.emptyList();
        };
    }
    
    private String getRandomSymbol() {
        String[] symbols = {"!", "@", "#", "$", "%", "&", "*", "+", "=", "?"};
        return symbols[random.nextInt(symbols.length)];
    }
    
    private int calculateDisplayDuration(int sequenceLength, int difficultyLevel) {
        // Base duration: 1000ms per item, adjusted by difficulty
        int baseDuration = sequenceLength * 1000;
        int difficultyAdjustment = (difficultyLevel - 1) * 200; // Harder = less time
        return Math.max(3000, baseDuration - difficultyAdjustment);
    }
}
