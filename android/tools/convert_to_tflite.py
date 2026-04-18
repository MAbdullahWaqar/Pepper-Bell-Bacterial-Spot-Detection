from pathlib import Path

import tensorflow as tf
from tensorflow.keras.models import load_model


ROOT = Path(__file__).resolve().parents[2]
KERAS_MODEL_PATH = ROOT / "pepperBell.keras"
OUTPUT_DIR = ROOT / "android" / "app" / "src" / "main" / "assets"
TFLITE_MODEL_PATH = OUTPUT_DIR / "pepperBell.tflite"
LABELS_PATH = OUTPUT_DIR / "labels.txt"

CLASS_NAMES = [
    "Pepper Bell Bacterial Spot",
    "Pepper Bell Healthy",
]


def main() -> None:
    if not KERAS_MODEL_PATH.exists():
        raise FileNotFoundError(f"Model not found: {KERAS_MODEL_PATH}")

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    model = load_model(KERAS_MODEL_PATH)
    input_shape = model.input_shape
    if not input_shape or len(input_shape) != 4:
        raise ValueError(f"Expected image model input shape, got: {input_shape}")

    _, height, width, channels = input_shape

    @tf.function(
        input_signature=[
            tf.TensorSpec(shape=[1, int(height), int(width), int(channels)], dtype=tf.float32)
        ]
    )
    def serving_fn(inputs):
        return model(inputs, training=False)

    concrete_func = serving_fn.get_concrete_function()

    converter = tf.lite.TFLiteConverter.from_concrete_functions([concrete_func], model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_ops = [
        tf.lite.OpsSet.TFLITE_BUILTINS,
        tf.lite.OpsSet.SELECT_TF_OPS,
    ]
    tflite_model = converter.convert()

    TFLITE_MODEL_PATH.write_bytes(tflite_model)
    LABELS_PATH.write_text("\n".join(CLASS_NAMES) + "\n", encoding="utf-8")

    print(f"Saved: {TFLITE_MODEL_PATH}")
    print(f"Saved: {LABELS_PATH}")


if __name__ == "__main__":
    main()
