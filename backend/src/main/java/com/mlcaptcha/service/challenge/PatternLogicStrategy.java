package com.mlcaptcha.service.challenge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Pattern & Logic CAPTCHA Strategy
 * Users must continue a pattern (numbers, shapes, rotations).
 */
@Component
@Slf4j
public class PatternLogicStrategy implements ChallengeStrategy {
    
    private static final String CHALLENGE_TYPE = "pattern_logic";
    private static final Random random = new Random();
    
    @Override
    public String getChallengeType() {
        return CHALLENGE_TYPE;
    }
    
    @Override
    public Map<String, Object> generateChallenge(int difficultyLevel) {
        String patternType = getPatternType(difficultyLevel);
        int sequenceLength = 4 + difficultyLevel;
        
        List<Object> pattern = generatePattern(sequenceLength, patternType, difficultyLevel);
        Object nextItem = calculateNextItem(pattern, patternType, difficultyLevel);
        
        Map<String, Object> challenge = new HashMap<>();
        challenge.put("pattern", pattern);
        challenge.put("patternType", patternType);
        challenge.put("nextItem", nextItem);
        
        return challenge;
    }
    
    @Override
    public boolean verifyChallenge(Map<String, Object> expectedResponse, Map<String, Object> userResponse) {
        if (expectedResponse == null || userResponse == null) {
            return false;
        }
        
        Object expectedAnswer = expectedResponse.get("answer");
        Object userAnswer = userResponse.get("answer");
        
        return expectedAnswer != null && expectedAnswer.equals(userAnswer);
    }
    
    @Override
    public Map<String, Object> calculateExpectedResponse(Map<String, Object> challengeData) {
        @SuppressWarnings("unchecked")
        List<Object> pattern = (List<Object>) challengeData.get("pattern");
        String patternType = (String) challengeData.get("patternType");
        Integer difficultyLevel = (Integer) challengeData.getOrDefault("difficultyLevel", 1);
        
        Object nextItem = calculateNextItem(pattern, patternType, difficultyLevel);
        
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("answer", nextItem);
        
        return expectedResponse;
    }
    
    private String getPatternType(int difficultyLevel) {
        if (difficultyLevel <= 2) {
            return "numeric_sequence";
        } else if (difficultyLevel <= 4) {
            return random.nextBoolean() ? "numeric_sequence" : "rotation";
        } else {
            String[] types = {"numeric_sequence", "rotation", "geometric"};
            return types[random.nextInt(types.length)];
        }
    }
    
    private List<Object> generatePattern(int length, String patternType, int difficultyLevel) {
        return switch (patternType) {
            case "numeric_sequence" -> generateNumericSequence(length, difficultyLevel);
            case "rotation" -> generateRotationPattern(length);
            case "geometric" -> generateGeometricPattern(length);
            default -> Collections.emptyList();
        };
    }
    
    private List<Object> generateNumericSequence(int length, int difficultyLevel) {
        List<Object> sequence = new ArrayList<>();
        int start = random.nextInt(10);
        int step = 1 + difficultyLevel; // 2-6 step size
        
        for (int i = 0; i < length; i++) {
            sequence.add(start + (i * step));
        }
        return sequence;
    }
    
    private List<Object> generateRotationPattern(int length) {
        List<Object> pattern = new ArrayList<>();
        String[] directions = {"↑", "→", "↓", "←"};
        int startIndex = random.nextInt(directions.length);
        
        for (int i = 0; i < length; i++) {
            pattern.add(directions[(startIndex + i) % directions.length]);
        }
        return pattern;
    }
    
    private List<Object> generateGeometricPattern(int length) {
        List<Object> pattern = new ArrayList<>();
        String[] shapes = {"●", "■", "▲", "◆"};
        
        for (int i = 0; i < length; i++) {
            pattern.add(shapes[i % shapes.length]);
        }
        return pattern;
    }
    
    private Object calculateNextItem(List<Object> pattern, String patternType, int difficultyLevel) {
        if (pattern == null || pattern.isEmpty()) {
            return null;
        }
        
        return switch (patternType) {
            case "numeric_sequence" -> {
                int last = (Integer) pattern.get(pattern.size() - 1);
                int secondLast = (Integer) pattern.get(pattern.size() - 2);
                int step = last - secondLast;
                yield last + step;
            }
            case "rotation" -> {
                String[] directions = {"↑", "→", "↓", "←"};
                String last = (String) pattern.get(pattern.size() - 1);
                int lastIndex = Arrays.asList(directions).indexOf(last);
                yield directions[(lastIndex + 1) % directions.length];
            }
            case "geometric" -> {
                String[] shapes = {"●", "■", "▲", "◆"};
                String last = (String) pattern.get(pattern.size() - 1);
                int lastIndex = Arrays.asList(shapes).indexOf(last);
                yield shapes[(lastIndex + 1) % shapes.length];
            }
            default -> null;
        };
    }
}
