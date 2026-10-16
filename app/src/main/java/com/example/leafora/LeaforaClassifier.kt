package com.example.leafora

import android.content.Context
import android.graphics.Bitmap
import org.json.JSONArray
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class LeaforaClassifier(
    context: Context
) {

    companion object {
        private const val IMAGE_SIZE = 224
        private const val CLASS_COUNT = 65
    }

    private val interpreter: Interpreter
    private val labels: List<String>

    init {

        // Load TFLite model
        val modelBytes = context.assets
            .open("plant_disease_model.tflite")
            .use { it.readBytes() }

        val modelBuffer = ByteBuffer
            .allocateDirect(modelBytes.size)
            .order(ByteOrder.nativeOrder())

        modelBuffer.put(modelBytes)
        modelBuffer.rewind()

        interpreter = Interpreter(modelBuffer)

        // Load labels.json
        val jsonText = context.assets
            .open("labels.json")
            .bufferedReader()
            .use { it.readText() }

        val jsonArray = JSONArray(jsonText)

        labels = List(jsonArray.length()) { index ->
            jsonArray.getString(index)
        }

        require(labels.size == CLASS_COUNT) {
            "Expected 65 labels, found ${labels.size}"
        }

        // Verify model output
        val outputShape =
            interpreter
                .getOutputTensor(0)
                .shape()

        var outputSize = 1

        for (dimension in outputShape) {
            outputSize *= dimension
        }

        require(outputSize == CLASS_COUNT) {
            "Expected 65 model outputs, found $outputSize"
        }
    }

    fun classify(bitmap: Bitmap): Prediction {

        // Resize image to model input size
        val resizedBitmap = Bitmap.createScaledBitmap(
            bitmap,
            IMAGE_SIZE,
            IMAGE_SIZE,
            true
        )

        // Float32 input buffer
        val inputBuffer = ByteBuffer
            .allocateDirect(
                IMAGE_SIZE * IMAGE_SIZE * 3 * 4
            )
            .order(ByteOrder.nativeOrder())

        val pixels = IntArray(
            IMAGE_SIZE * IMAGE_SIZE
        )

        resizedBitmap.getPixels(
            pixels,
            0,
            IMAGE_SIZE,
            0,
            0,
            IMAGE_SIZE,
            IMAGE_SIZE
        )

        /*
         * IMPORTANT:
         * Testing in Colab confirmed that this model
         * gives the correct prediction when input values
         * are normalized to the 0-1 range.
         */
        for (pixel in pixels) {

            val red =
                ((pixel shr 16) and 0xFF) / 255.0f

            val green =
                ((pixel shr 8) and 0xFF) / 255.0f

            val blue =
                (pixel and 0xFF) / 255.0f

            inputBuffer.putFloat(red)
            inputBuffer.putFloat(green)
            inputBuffer.putFloat(blue)
        }

        inputBuffer.rewind()

        // Model output
        val output = Array(1) {
            FloatArray(CLASS_COUNT)
        }

        interpreter.run(
            inputBuffer,
            output
        )

        val scores = output[0]

        // Find highest-scoring class
        val bestIndex =
            scores.indices.maxByOrNull { scores[it] }
                ?: return Prediction(
                    crop = "Unknown",
                    disease = "Unknown",
                    confidence = 0f,
                    details = "Plant could not be identified."
                )

        val confidence =
            scores[bestIndex]

        val rawLabel =
            labels[bestIndex]

        // Extract crop name
        val crop =
            rawLabel.substringBefore("___")

        // Extract disease name
        val disease =
            cleanDiseaseName(rawLabel)

        val displayCrop =
            cleanCropName(crop)

        val details =
            "Crop: $displayCrop\n\n" +
                    "Disease: $disease\n\n" +
                    "Confidence: " +
                    "${(confidence * 100).toInt()}%"

        return Prediction(
            crop = displayCrop,
            disease = disease,
            confidence = confidence,
            details = details
        )
    }

    private fun cleanCropName(
        crop: String
    ): String {

        return when (crop) {

            "Pepper,_bell" ->
                "Bell Pepper"

            "Corn_(maize)" ->
                "Corn"

            "Cherry_(including_sour)" ->
                "Cherry"

            "Paddy" ->
                "Paddy / Rice"

            else ->
                crop.replace("_", " ")
        }
    }

    private fun cleanDiseaseName(
        label: String
    ): String {

        return label
            .substringAfter("___")
            .replace("_", " ")
            .replace("(", "")
            .replace(")", "")
    }

    fun close() {
        interpreter.close()
    }

    data class Prediction(
        val crop: String,
        val disease: String,
        val confidence: Float,
        val details: String
    )
}