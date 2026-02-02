from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Dict, Optional
import logging

from app.services.inference import MLInferenceService

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="ML CAPTCHA Inference Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

ml_service = MLInferenceService()


class AnalyzeRequest(BaseModel):
    session_id: str
    challenge_type: str
    user_response: Dict
    mouse_movements: Optional[List[Dict]] = None
    keystroke_timings: Optional[List[Dict]] = None
    completion_time: Optional[int] = None


class AnalyzeResponse(BaseModel):
    is_human: bool
    confidence: float
    model_predictions: Dict


@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "ml-inference"}


@app.post("/api/v1/ml/analyze", response_model=AnalyzeResponse)
async def analyze_behavior(request: AnalyzeRequest):
    try:
        result = ml_service.analyze(
            session_id=request.session_id,
            challenge_type=request.challenge_type,
            user_response=request.user_response,
            mouse_movements=request.mouse_movements,
            keystroke_timings=request.keystroke_timings,
            completion_time=request.completion_time
        )
        
        return AnalyzeResponse(
            is_human=result["is_human"],
            confidence=result["confidence"],
            model_predictions=result.get("model_predictions", {})
        )
    except Exception as e:
        logger.error(f"Error in ML analysis: {str(e)}")
        raise HTTPException(status_code=500, detail=f"ML analysis failed: {str(e)}")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
