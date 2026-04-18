# Pepper Bell Bacterial Spot Detection

An end-to-end plant disease detection project built around a trained deep learning model for pepper bell leaves.

The repository contains three runnable experiences built from the same trained model:

- A FastAPI backend for browser/web integration.
- A React frontend for desktop/browser uploads and live results.
- An Android app that runs TensorFlow Lite inference on-device.

The goal of the project is simple: upload a leaf image and receive a disease prediction with confidence scores.

## What This Project Does

The model classifies pepper bell leaf images into two classes:

- Pepper Bell Bacterial Spot
- Pepper Bell Healthy

The system can be used in three ways:

1. Web API mode through FastAPI.
2. Browser UI mode through React.
3. Offline mobile mode through Android + TensorFlow Lite.

## Repository Layout

```text
Pepper Bell Bacterial Spot Detection/
├── Training.ipynb
├── pepperBell.keras
├── data/
├── dataset/
├── backend/
├── frontend/
└── android/
```

### Top-Level Artifacts

- `Training.ipynb` contains the model training workflow, experiments, and validation steps.
- `pepperBell.keras` is the trained Keras model used by the backend and converted for Android.
- `data/` contains the original class folders used before splitting.
- `dataset/` contains train, validation, and test splits.

## Data Description

The project uses a binary image classification dataset structured into two labels:

- `Pepper__bell___Bacterial_spot`
- `Pepper__bell___healthy`

### Dataset Folders

- `data/Pepper__bell___Bacterial_spot/`
- `data/Pepper__bell___healthy/`
- `dataset/train/`
- `dataset/val/`
- `dataset/test/`

The `dataset/` directory is the split version used for training, validation, and testing.

## Model Overview

The trained model is stored as `pepperBell.keras`.

### Model Behavior

- Input: RGB leaf image
- Output: Two-class probability distribution
- Backend inference: Uses the Keras model directly
- Android inference: Uses a TensorFlow Lite version of the same model

### Input Size

The backend reads the model input shape dynamically. The trained model expects `256 x 256 x 3` input.

### Output Format

The model returns two probabilities that represent the confidence for each class. The app layers then convert these into a human-readable prediction and percentage confidence.

## Training Notebook

`Training.ipynb` is the place where the model was built and evaluated.

It typically covers:

- Dataset loading
- Image preprocessing
- Class label mapping
- Model architecture definition
- Training and validation
- Saving the final model as `pepperBell.keras`

If you want to retrain the model, this notebook is the starting point.

## Backend: FastAPI

The backend provides a lightweight prediction API for web clients.

### Location

- [backend/app/main.py](backend/app/main.py)

### What It Does

- Loads `pepperBell.keras` at startup.
- Reads the model input shape to determine the correct resize dimension.
- Accepts uploaded images.
- Converts images to RGB.
- Normalizes pixels to the range `0..1`.
- Runs inference.
- Returns predicted label, confidence, and per-class confidences.

### API Endpoints

#### `GET /health`

Simple health check used to verify the API is running.

Response:

```json
{ "status": "ok" }
```

#### `POST /predict`

Accepts multipart form-data with the uploaded image under the field name `file`.

Example response:

```json
{
  "prediction": "Pepper Bell Bacterial Spot",
  "confidence": 98.41,
  "all_confidences": {
    "Pepper Bell Bacterial Spot": 98.41,
    "Pepper Bell Healthy": 1.59
  }
}
```

### Backend Files

- `backend/app/main.py`: API logic and inference.
- `backend/requirements.txt`: Python dependencies.

### Backend Dependencies

The backend uses:

- FastAPI
- Uvicorn
- TensorFlow
- NumPy
- Pillow
- python-multipart

### Python Version Requirement

Use Python `3.10`, `3.11`, or `3.12` for the backend environment.

TensorFlow is not available for Python `3.14` in this project setup, so a newer interpreter will fail during installation.

### Backend Run Steps

From the repository root:

```bash
/opt/homebrew/bin/python3.10 -m venv venv
source venv/bin/activate
cd backend
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### Why the Resize Is Dynamic

The backend reads the model's declared input shape instead of hardcoding a fixed resize target. That prevents shape mismatch errors if the trained model architecture changes.

## Frontend: React + Vite

The frontend is a web UI for uploading a leaf image and showing the prediction result visually.

### Location

- [frontend/src/App.jsx](frontend/src/App.jsx)

### What It Does

- Lets the user choose an image.
- Shows a local preview of the selected image.
- Sends the image to the backend `/predict` endpoint.
- Displays the model prediction.
- Displays confidence bars for each class.

### Frontend Stack

- React 18
- Vite
- CSS-based custom UI

### How It Communicates With the Backend

The frontend sends a `POST` request to:

```text
http://127.0.0.1:8000/predict
```

The image is posted as multipart form-data with the key `file`.

### Frontend Run Steps

```bash
cd frontend
npm install
npm run dev
```

Open:

```text
http://127.0.0.1:5173
```

### Frontend UI Details

The interface includes:

- A title and subtitle
- File picker for image upload
- Live image preview
- Predict button
- Prediction result card
- Confidence percentage
- Per-class confidence bars

## Android App: TensorFlow Lite On-Device Inference

The Android app now runs the model locally on the device using TensorFlow Lite.

That means:

- No backend call is required for predictions.
- It can work offline once the model is packaged into the app.
- The app converts selected leaf images into tensors and predicts locally.

### Android App Location

- [android/app/src/main/java/com/example/pepperbell/MainActivity.kt](android/app/src/main/java/com/example/pepperbell/MainActivity.kt)
- [android/app/src/main/java/com/example/pepperbell/tflite/TfLitePepperClassifier.kt](android/app/src/main/java/com/example/pepperbell/tflite/TfLitePepperClassifier.kt)

### Android App Behavior

1. User selects an image from device storage.
2. The image is copied into app cache.
3. The image is resized to the TFLite model input size.
4. Pixel values are normalized.
5. TensorFlow Lite runs inference locally.
6. The app shows the predicted class and confidence scores.

### Android UI

The Android app uses Jetpack Compose and includes:

- Hero header
- Image picker card
- Predict button
- Preview of selected image
- Prediction result card
- Confidence bars
- Error display if something fails

### Android Model Asset

The app expects these assets:

- `android/app/src/main/assets/pepperBell.tflite`
- `android/app/src/main/assets/labels.txt`

These are generated from the Keras model using:

- `android/tools/convert_to_tflite.py`

### Android Dependencies

The Android project includes:

- Jetpack Compose
- Kotlin
- TensorFlow Lite
- TensorFlow Lite Support
- TensorFlow Lite Select TF Ops
- Coil for image preview

### Why Select TF Ops Are Included

The converted model uses TensorFlow operations that are not always supported by the smallest native TFLite runtime. To keep the converted model usable, the Android app includes the Select TF Ops runtime and the TFLite converter exports with Select TF Ops enabled.

### Android Run Steps

1. Open the `android/` folder in Android Studio.
2. Sync Gradle.
3. Make sure the TFLite model asset exists.
4. Run on an emulator or physical device.

### Android Notes

- Emulator localhost access is not needed because inference is local.
- If you want to change labels, update both the asset labels and the classifier labels.
- If you replace the model, regenerate `pepperBell.tflite` and verify the input shape.

## Project Structure In Detail

### Backend

- `backend/app/main.py`
- `backend/requirements.txt`

### Frontend

- `frontend/package.json`
- `frontend/vite.config.js`
- `frontend/index.html`
- `frontend/src/main.jsx`
- `frontend/src/App.jsx`
- `frontend/src/styles.css`

### Android

- `android/build.gradle.kts`
- `android/settings.gradle.kts`
- `android/app/build.gradle.kts`
- `android/app/src/main/AndroidManifest.xml`
- `android/app/src/main/java/com/example/pepperbell/MainActivity.kt`
- `android/app/src/main/java/com/example/pepperbell/ui/PredictionViewModel.kt`
- `android/app/src/main/java/com/example/pepperbell/ui/screens/PredictionScreen.kt`
- `android/app/src/main/java/com/example/pepperbell/tflite/TfLitePepperClassifier.kt`
- `android/app/src/main/assets/pepperBell.tflite`
- `android/app/src/main/assets/labels.txt`

## How the Full Stack Fits Together

### Option 1: Backend + Frontend

This is the easiest way to test the model in a browser.

Flow:

1. Run backend.
2. Run frontend.
3. Upload an image in the browser.
4. Frontend calls the backend.
5. Backend returns prediction JSON.
6. Frontend renders the result.

### Option 2: Android Only

This is the mobile/offline mode.

Flow:

1. Open Android app.
2. Select image.
3. TensorFlow Lite predicts locally.
4. No network request is needed.

## Setup Checklist

Before running anything, make sure you have:

- Python 3.10-3.12 for the backend
- Node.js 16+ for the frontend
- Android Studio for the mobile app
- The trained model file `pepperBell.keras` at repo root

## Troubleshooting

### Backend: `No matching distribution found for tensorflow`

Cause:

- Python version is too new.

Fix:

- Recreate the virtual environment with Python 3.10, 3.11, or 3.12.

### Backend: Prediction Shape Error

Cause:

- Image resize size does not match the model input shape.

Fix:

- The backend now reads the model input shape dynamically.

### Frontend: `npm ERR! ENOENT package.json`

Cause:

- You are running `npm install` from the wrong folder.

Fix:

- Run it from `frontend/`.

### Android: Model or Classifier Issues

Cause:

- Missing `pepperBell.tflite` or incompatible input shape.

Fix:

- Regenerate the model using `android/tools/convert_to_tflite.py`.
- Rebuild the Android project.

### Android: App Cannot Open Selected Images

Cause:

- Storage permissions or unsupported image decoding.

Fix:

- Ensure the device allows file access.
- Use common image formats like JPG or PNG.

## Important Implementation Notes

- The backend and frontend still form a web stack.
- The Android app is intentionally separate and does not require the backend for inference.
- The Android app uses the same label set as the backend.
- The TFLite model was exported from the trained Keras model.

## Example Usage Scenarios

### Web Demo

- Start backend.
- Start frontend.
- Upload a pepper leaf image.
- View prediction and confidence.

### Mobile Demo

- Install Android app.
- Choose a leaf image from gallery.
- Get an on-device prediction.

### Model Validation

- Use `dataset/test/` images against the backend or Android app.



## Short Summary

This project is a full-stack pepper leaf disease detection system built from one trained model and exposed through:

- a FastAPI backend,
- a React frontend,
- and an Android TensorFlow Lite app.

The project now supports both web and mobile usage with confidence-based predictions.
