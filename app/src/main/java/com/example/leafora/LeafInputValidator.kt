package com.example.leafora

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class LeafInputValidator(context: Context) {

    companion object {
        private const val IMAGE_SIZE = 224
        private const val NON_LEAF_THRESHOLD = 0.5f
        private const val TAG = "LeaforaValidator"
    }

    private val interpreter: Interpreter

    init {
        val modelBytes = context.assets
            .open("leaf_nonleaf_model.tflite")
            .use { it.readBytes() }

        val modelBuffer = ByteBuffer
            .allocateDirect(modelBytes.size)
            .order(ByteOrder.nativeOrder())

        modelBuffer.put(modelBytes)
        modelBuffer.rewind()

        interpreter = Interpreter(modelBuffer)

        Log.d(TAG, "Validator model loaded successfully")
        Log.d(
            TAG,
            "Input shape: ${
                interpreter.getInputTensor(0)
                    .shape().contentToString()
            }"
        )
        Log.d(
            TAG,
            "Input type: ${
                interpreter.getInputTensor(0).dataType()
            }"
        )
        Log.d(
            TAG,
            "Output shape: ${
                interpreter.getOutputTensor(0)
                    .shape().contentToString()
            }"
        )
        Log.d(
            TAG,
            "Output type: ${
                interpreter.getOutputTensor(0).dataType()
            }"
        )
    }

    fun isLeaf(bitmap: Bitmap): Boolean {
        val resizedBitmap = Bitmap.createScaledBitmap(
            bitmap,
            IMAGE_SIZE,
            IMAGE_SIZE,
            true
        )

        val inputBuffer = ByteBuffer
            .allocateDirect(IMAGE_SIZE * IMAGE_SIZE * 3 * 4)
            .order(ByteOrder.nativeOrder())

        val pixels = IntArray(IMAGE_SIZE * IMAGE_SIZE)

        resizedBitmap.getPixels(
            pixels,
            0,
            IMAGE_SIZE,
            0,
            0,
            IMAGE_SIZE,
            IMAGE_SIZE
        )

        // Existing preprocessing: RGB values in 0–255.
        for (pixel in pixels) {
            inputBuffer.putFloat(
                ((pixel shr 16) and 0xFF).toFloat()
            )
            inputBuffer.putFloat(
                ((pixel shr 8) and 0xFF).toFloat()
            )
            inputBuffer.putFloat(
                (pixel and 0xFF).toFloat()
            )
        }

        inputBuffer.rewind()

        val output = Array(1) {
            FloatArray(
                interpreter.getOutputTensor(0).shape().last()
            )
        }

        interpreter.run(inputBuffer, output)

        val values = output[0]

        Log.d(TAG, "Raw model output: ${values.contentToString()}")

        if (values.size != 1) {
            Log.e(
                TAG,
                "Expected one output value; got ${values.size}. " +
                        "Check model output interpretation."
            )
            throw IllegalStateException(
                "Validator output is not a single probability."
            )
        }

        val probability = values[0]

        Log.d(TAG, "Output value: $probability")

        // Assumes the single output represents non-leaf probability.
        // Verify this against the model's training configuration.
        val isLeaf = probability < NON_LEAF_THRESHOLD

        Log.d(TAG, "Accepted as leaf: $isLeaf")

        return isLeaf
    }

    fun close() {
        interpreter.close()
    }
}
