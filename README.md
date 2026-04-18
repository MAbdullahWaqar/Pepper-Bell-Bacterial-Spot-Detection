# Pepper Bell Bacterial Spot Detection

This project now includes:

- A FastAPI backend for image prediction and confidence scores.
- A React frontend (Vite) for uploading leaf images and viewing results.

## Project Structure

- `backend/app/main.py`: FastAPI API that loads `pepperBell.keras` and predicts class probabilities.
- `backend/requirements.txt`: Python dependencies for backend.
- `frontend/`: React app for the user interface.

## 1) Run Backend (FastAPI)

Create a TensorFlow-compatible virtual environment first (Python 3.10-3.12 required).

```bash
/opt/homebrew/bin/python3.10 -m venv venv
source venv/bin/activate
```

Then install and run backend:

```bash
cd backend
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Backend endpoints:

- `GET /health`
- `POST /predict` (multipart form-data key: `file`)

## 2) Run Frontend (React)

```bash
cd frontend
npm install
npm run dev
```

Open:

- `http://127.0.0.1:5173`

## API Response Example

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

## Notes

- The backend expects your model file at project root: `pepperBell.keras`.
- Current class labels are configured for 2 classes.
- If your model output classes differ, update `CLASS_NAMES` in `backend/app/main.py`.
- If you see `No matching distribution found for tensorflow`, your Python version is too new (for example 3.14). Recreate the venv with Python 3.10-3.12.
