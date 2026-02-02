package com.mlcaptcha.service.challenge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Arithmetic CAPTCHA Strategy
 * Time-bound logic/math questions with adaptive difficulty.
 */
@Component
@Slf4j
public class ArithmeticStrategy implements ChallengeStrategy {
    
    private static final String CHALLENGE_TYPE = "arithmetic";
    private static final Random random = new Random();
    
    @Override
    public String getChallengeType() {
        return CHALLENGE_TYPE;
    }
    
    @Override
    public Map<String, Object> generateChallenge(int difficultyLevel) {
        String operation = getOperation(difficultyLevel);
        int[] operands = generateOperands(operation, difficultyLevel);
        int correctAnswer = calculateAnswer(operands[0], operands[1], operation);
        
        Map<String, Object> challenge = new HashMap<>();
        challenge.put("operand1", operands[0]);
        challenge.put("operand2", operands[1]);
        challenge.put("operation", operation);
        challenge.put("timeLimit", calculateTimeLimit(difficultyLevel));
        
        return challenge;
    }
    
    @Override
    public boolean verifyChallenge(Map<String, Object> expectedResponse, Map<String, Object> userResponse) {
        if (expectedResponse == null || userResponse == null) {
            return false;
        }
        
        Integer expectedAnswer = (Integer) expectedResponse.get("answer");
        Object userAnswerObj = userResponse.get("answer");
        
        if (expectedAnswer == null || userAnswerObj == null) {
            return false;
        }
        
        int userAnswer;
        if (userAnswerObj instanceof Number) {
            userAnswer = ((Number) userAnswerObj).intValue();
        } else {
            try {
                userAnswer = Integer.parseInt(userAnswerObj.toString());
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return expectedAnswer.equals(userAnswer);
    }
    
    @Override
    public Map<String, Object> calculateExpectedResponse(Map<String, Object> challengeData) {
        Integer operand1 = (Integer) challengeData.get("operand1");
        Integer operand2 = (Integer) challengeData.get("operand2");
        String operation = (String) challengeData.get("operation");
        
        int answer = calculateAnswer(operand1, operand2, operation);
        
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("answer", answer);
        
        return expectedResponse;
    }
    
    private String getOperation(int difficultyLevel) {
        if (difficultyLevel <= 2) {
            return "+";
        } else if (difficultyLevel == 3) {
            return random.nextBoolean() ? "+" : "-";
        } else if (difficultyLevel == 4) {
            String[] ops = {"+", "-", "*"};
            return ops[random.nextInt(ops.length)];
        } else {
            String[] ops = {"+", "-", "*", "/"};
            return ops[random.nextInt(ops.length)];
        }
    }
    
    private int[] generateOperands(String operation, int difficultyLevel) {
        int maxValue = 10 + (difficultyLevel * 10); // 20-60 range
        
        return switch (operation) {
            case "+" -> new int[]{
                    random.nextInt(maxValue),
                    random.nextInt(maxValue)
            };
            case "-" -> {
                int a = random.nextInt(maxValue) + maxValue / 2;
                int b = random.nextInt(a);
                yield new int[]{a, b};
            }
            case "*" -> new int[]{
                    random.nextInt(10) + 1,
                    random.nextInt(10) + 1
            };
            case "/" -> {
                int divisor = random.nextInt(10) + 1;
                int quotient = random.nextInt(10) + 1;
                int dividend = divisor * quotient;
                yield new int[]{dividend, divisor};
            }
            default -> new int[]{0, 0};
        };
    }
    
    private int calculateAnswer(int a, int b, String operation) {
        return switch (operation) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> a / b;
            default -> 0;
        };
    }
    
    private int calculateTimeLimit(int difficultyLevel) {
        // Base time: 15 seconds, reduced by difficulty
        return Math.max(5000, 15000 - (difficultyLevel * 1000));
    }
}
