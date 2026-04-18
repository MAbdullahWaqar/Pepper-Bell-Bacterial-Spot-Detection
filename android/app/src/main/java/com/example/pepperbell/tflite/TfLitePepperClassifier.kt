package com.example.pepperbell.tflite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.pepperbell.data.PredictionResponse
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TfLitePepperClassifier(context: Context) {
    private val classNames = listOf(
        "Pepper Bell Bacterial Spot",
        "Pepper Bell Healthy"
    )

    private val interpreter: Interpreter by lazy {
        val options = Interpreter.Options().apply {
            addDelegate(FlexDelegate())
        }
        Interpreter(loadModelBuffer(context, "pepperBell.tflite"), options)
    }

    fun predict(imageFile: File): PredictionResponse {
        val sourceBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            ?: throw IllegalArgumentException("Unable to decode selected image")

        val inputShape = interpreter.getInputTensor(0).shape()
        val inputHeight = inputShape[1]
        val inputWidth = inputShape[2]
        val inputChannels = inputShape[3]

        if (inputChannels != 3) {
            throw IllegalStateException("Model expects $inputChannels channels. Only RGB models are supported.")
        }

        val resizedBitmap = Bitmap.createScaledBitmap(sourceBitmap, inputWidth, inputHeight, true)
        val inputBuffer = bitmapToInputBuffer(resizedBitmap, inputWidth, inputHeight)

        val outputShape = interpreter.getOutputTensor(0).shape()
        val classCount = outputShape.last()
        val output = Array(1) { FloatArray(classCount) }

        interpreter.run(inputBuffer, output)

        val probabilities = output[0]
        val winningIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
        val predictedLabel = classNames.getOrElse(winningIndex) { "Class $winningIndex" }

        val confidenceMap = probabilities.mapIndexed { index, value ->
            classNames.getOrElse(index) { "Class $index" } to (value * 100.0)
        }.toMap()

        val confidence = probabilities[winningIndex] * 100.0

        return PredictionResponse(
            prediction = predictedLabel,
            confidence = confidence,
            allConfidences = confidenceMap
        )
    }

    private fun bitmapToInputBuffer(bitmap: Bitmap, width: Int, height: Int): ByteBuffer {
        val inputBuffer = ByteBuffer.allocateDirect(4 * width * height * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        pixels.forEach { pixel ->
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }

        inputBuffer.rewind()
        return inputBuffer
    }

    private fun loadModelBuffer(context: Context, modelAssetName: String): ByteBuffer {
        context.assets.open(modelAssetName).use { inputStream ->
            val modelBytes = inputStream.readBytes()
            return ByteBuffer.allocateDirect(modelBytes.size).apply {
                order(ByteOrder.nativeOrder())
                put(modelBytes)
                rewind()
            }
        }
    }

    fun close() {
        interpreter.close()
    }
}
