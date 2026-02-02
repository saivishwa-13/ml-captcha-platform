import { createApiClient } from './api';
import { CaptchaChallenge, CaptchaResponse, BehaviorMetrics } from '../types/captcha';

export const captchaService = {
  async generateChallenge(
    apiKey: string,
    challengeType?: string,
    difficultyLevel?: number
  ): Promise<CaptchaChallenge> {
    const apiClient = createApiClient(apiKey);
    const response = await apiClient.post('/captcha/generate', {
      challengeType,
      difficultyLevel,
    });
    return response.data;
  },

  async verifyChallenge(
    apiKey: string,
    sessionToken: string,
    response: Record<string, any>,
    behaviorMetrics?: BehaviorMetrics
  ): Promise<CaptchaResponse> {
    const apiClient = createApiClient(apiKey);
    const verifyResponse = await apiClient.post('/captcha/verify', {
      sessionToken,
      response,
      behaviorMetrics,
    });
    return verifyResponse.data;
  },
};
