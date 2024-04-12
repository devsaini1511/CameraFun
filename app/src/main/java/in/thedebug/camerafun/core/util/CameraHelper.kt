package `in`.thedebug.camerafun.core.util

import android.content.Context
import android.util.DisplayMetrics
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.guava.await
import java.io.File
import kotlin.math.abs

class CameraHelper(private val context: Context,val image:String) {
    private val imageFile by lazy { File(context.filesDir, image) }

    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null

    suspend fun startCamera(viewFinder: PreviewView, lifecycleOwner: LifecycleOwner) {
        cameraProvider = ProcessCameraProvider.getInstance(context).await()

        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        val aspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        val rotation = viewFinder.display.rotation

        val localCameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        preview = Preview.Builder()
            .setTargetAspectRatio(aspectRatio)
            .setTargetRotation(rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetAspectRatio(aspectRatio)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(aspectRatio)
            .setTargetRotation(rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        localCameraProvider.unbindAll()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        localCameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
            imageAnalyzer
        )

        preview?.setSurfaceProvider(viewFinder.surfaceProvider)
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = if (width > height) width.toDouble() / height.toDouble() else height.toDouble() / width.toDouble()
        return aspectRatio(previewRatio)
    }

    private fun aspectRatio(previewRatio: Double): Int {
        if (abs(previewRatio - 4.0 / 3.0) <= abs(previewRatio - 16.0 / 9.0)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    fun takePicture(onCapture:()->Unit) {
        val localImageCapture =
            imageCapture ?: throw IllegalStateException("Camera initialization failed.")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

        localImageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback(), ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    outputFileResults.savedUri?.path
                    onCapture()
                }
            }
        )
    }
}