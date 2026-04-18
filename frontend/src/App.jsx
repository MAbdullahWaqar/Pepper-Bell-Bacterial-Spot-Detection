import { useMemo, useState } from "react";

const API_BASE_URL = "http://127.0.0.1:8000";

function App() {
  const [imageFile, setImageFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const sortedConfidences = useMemo(() => {
    if (!result?.all_confidences) {
      return [];
    }

    return Object.entries(result.all_confidences).sort((a, b) => b[1] - a[1]);
  }, [result]);

  const handleImageChange = (event) => {
    const selectedFile = event.target.files?.[0];
    setError("");
    setResult(null);

    if (!selectedFile) {
      setImageFile(null);
      setPreviewUrl("");
      return;
    }

    setImageFile(selectedFile);
    setPreviewUrl(URL.createObjectURL(selectedFile));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!imageFile) {
      setError("Please choose an image before predicting.");
      return;
    }

    setIsLoading(true);
    setError("");
    setResult(null);

    try {
      const formData = new FormData();
      formData.append("file", imageFile);

      const response = await fetch(`${API_BASE_URL}/predict`, {
        method: "POST",
        body: formData,
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.detail || "Prediction failed");
      }

      setResult(data);
    } catch (predictionError) {
      setError(predictionError.message || "Unable to fetch prediction");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <main className="page-shell">
      <section className="hero-card">
        <p className="chip">AI Crop Health</p>
        <h1>Pepper Bell Bacterial Spot Detector</h1>
        <p className="subtitle">
          Upload a leaf photo and get instant class prediction plus confidence from your trained model.
        </p>

        <form className="predict-form" onSubmit={handleSubmit}>
          <label className="upload-box" htmlFor="leaf-image">
            <input
              id="leaf-image"
              type="file"
              accept="image/*"
              onChange={handleImageChange}
            />
            <span>Choose Leaf Image</span>
          </label>

          <button type="submit" disabled={isLoading}>
            {isLoading ? "Analyzing..." : "Predict Disease"}
          </button>
        </form>

        {previewUrl && (
          <div className="preview-wrap">
            <img src={previewUrl} alt="Selected pepper leaf" />
          </div>
        )}

        {error && <p className="status error">{error}</p>}

        {result && (
          <article className="result-card">
            <h2>Prediction Result</h2>
            <p className="prediction">{result.prediction}</p>
            <p className="confidence">Confidence: {result.confidence}%</p>

            <div className="confidence-list">
              {sortedConfidences.map(([label, value]) => (
                <div key={label} className="confidence-row">
                  <div className="row-head">
                    <span>{label}</span>
                    <span>{value}%</span>
                  </div>
                  <div className="bar-track">
                    <div className="bar-fill" style={{ width: `${value}%` }} />
                  </div>
                </div>
              ))}
            </div>
          </article>
        )}
      </section>
    </main>
  );
}

export default App;
