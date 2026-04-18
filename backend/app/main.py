from io import BytesIO
from pathlib import Path
from typing import Dict, List

import numpy as np
from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from PIL import Image, UnidentifiedImageError

try:
    from tensorflow.keras.models import load_model
except ImportError as exc:
    raise RuntimeError(
        "TensorFlow is not installed. Use Python 3.10-3.12, then run 'pip install -r backend/requirements.txt'."
    ) from exc

BASE_DIR = Path(__file__).resolve().parents[2]
MODEL_PATH = BASE_DIR / "pepperBell.keras"
CLASS_NAMES = [
    "Pepper Bell Bacterial Spot",
    "Pepper Bell Healthy",
]

app = FastAPI(title="Pepper Bell Disease Detection API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://127.0.0.1:5173"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


if not MODEL_PATH.exists():
    raise RuntimeError(f"Model not found at {MODEL_PATH}")

model = load_model(MODEL_PATH)

# Use the model's declared input resolution for inference preprocessing.
model_input_shape = getattr(model, "input_shape", None)
if (
    not model_input_shape
    or len(model_input_shape) != 4
    or model_input_shape[1] is None
    or model_input_shape[2] is None
):
    raise RuntimeError("Unable to determine model input shape. Expected 4D image input.")

IMAGE_SIZE = (int(model_input_shape[1]), int(model_input_shape[2]))


@app.get("/health")
def health() -> Dict[str, str]:
    return {"status": "ok"}


def preprocess_image(image_bytes: bytes) -> np.ndarray:
    try:
        image = Image.open(BytesIO(image_bytes)).convert("RGB")
    except UnidentifiedImageError as exc:
        raise HTTPException(status_code=400, detail="Uploaded file is not a valid image") from exc

    image = image.resize(IMAGE_SIZE)
    image_array = np.array(image, dtype=np.float32) / 255.0
    image_array = np.expand_dims(image_array, axis=0)
    return image_array


def build_confidence_map(probabilities: np.ndarray, labels: List[str]) -> Dict[str, float]:
    return {label: float(round(prob * 100, 2)) for label, prob in zip(labels, probabilities)}


@app.post("/predict")
async def predict(file: UploadFile = File(...)) -> Dict[str, object]:
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="Please upload an image file")

    image_bytes = await file.read()
    if not image_bytes:
        raise HTTPException(status_code=400, detail="Uploaded file is empty")

    image_batch = preprocess_image(image_bytes)
    raw_prediction = model.predict(image_batch, verbose=0)[0]

    if raw_prediction.ndim != 1:
        raise HTTPException(status_code=500, detail="Unexpected model output shape")

    class_index = int(np.argmax(raw_prediction))
    predicted_label = CLASS_NAMES[class_index] if class_index < len(CLASS_NAMES) else f"Class {class_index}"
    confidence = float(round(raw_prediction[class_index] * 100, 2))

    return {
        "prediction": predicted_label,
        "confidence": confidence,
        "all_confidences": build_confidence_map(raw_prediction, CLASS_NAMES),
    }
