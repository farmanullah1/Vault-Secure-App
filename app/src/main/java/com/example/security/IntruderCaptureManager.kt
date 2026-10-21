package com.example.security

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LifecycleOwner
import com.example.data.db.VaultDatabase
import com.example.data.model.IntruderEntryPoint
import com.example.data.model.IntruderEventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Headless front-camera capture for failed authentication attempts.
 * Conforms strictly to Master Build Spec v3 (Section 32).
 *
 * Rules:
 * - Front camera only, headless (no preview).
 * - Opt-in (default OFF).
 * - Respects runtime CAMERA permission.
 * - Minimum 3-second gap between captures to prevent rapid-burst flooding.
 * - Never keeps camera open: captures and unbinds immediately (~1.5s).
 * - Photos saved in protected `.pv_intruders/` containing a `.nomedia` file.
 * - Stored in Room (IntruderEventEntity), pruned according to retention setting.
 */
class IntruderCaptureManager(
    private val context: Context,
    private val securityManager: PinSecurityManager
) {

    private val db = VaultDatabase.getInstance(context)
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var lastCaptureElapsedRealtime: Long = 0L

    companion object {
        private const val TAG = "IntruderCapture"
        private const val MIN_CAPTURE_GAP_MS = 3000L // 3 seconds per Spec 32.1
        private const val FOLDER_NAME = ".pv_intruders"
        private const val NO_MEDIA_FILE = ".nomedia"
    }

    /**
     * Records a failed attempt and quietly triggers front-camera capture if criteria are met.
     */
    fun onAuthFailure(
        lifecycleOwner: LifecycleOwner,
        attemptNumber: Int,
        entryPoint: IntruderEntryPoint = IntruderEntryPoint.COVER,
        isDeviceLocked: Boolean = false
    ) {
        // Check if Intruder Photo is enabled by user
        val isEnabled = securityManager.isIntruderPhotoEnabled
        val threshold = securityManager.intruderThreshold

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        // If disabled or below threshold or permission missing: log without photo
        if (!isEnabled || attemptNumber < threshold || !hasPermission) {
            logEventWithoutPhoto(attemptNumber, entryPoint, isDeviceLocked)
            return
        }

        // Rate limiting: 3s gap
        val now = SystemClock.elapsedRealtime()
        if (now - lastCaptureElapsedRealtime < MIN_CAPTURE_GAP_MS) {
            logEventWithoutPhoto(attemptNumber, entryPoint, isDeviceLocked)
            return
        }
        lastCaptureElapsedRealtime = now

        // Trigger capture on background
        capturePhoto(lifecycleOwner, attemptNumber, entryPoint, isDeviceLocked)
    }

    /**
     * Executes test capture for Settings -> "Test intruder photo".
     */
    fun testCapture(
        lifecycleOwner: LifecycleOwner,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            onComplete(false, "Camera permission not granted")
            return
        }

        capturePhoto(lifecycleOwner, 1, IntruderEntryPoint.SETTINGS, false) { success, uri ->
            onComplete(success, uri)
        }
    }

    private fun capturePhoto(
        lifecycleOwner: LifecycleOwner,
        attemptNumber: Int,
        entryPoint: IntruderEntryPoint,
        isDeviceLocked: Boolean,
        callback: ((Boolean, String?) -> Unit)? = null
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                // Check front camera availability
                if (!cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                    Log.w(TAG, "No front camera available on device")
                    logEventWithoutPhoto(attemptNumber, entryPoint, isDeviceLocked)
                    callback?.invoke(false, "No front camera found")
                    return@addListener
                }

                val imageCapture = ImageCapture.Builder()
                    .setTargetResolution(Size(1280, 960))
                    .setJpegQuality(80)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    imageCapture
                )

                // Prepare output file
                val photoFile = prepareOutputFile()
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                imageCapture.takePicture(
                    outputOptions,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            try {
                                cameraProvider.unbindAll()
                            } catch (e: Exception) {
                                // Ignore unbind error
                            }

                            // Save into SD card folder if configured
                            copyToVaultFolderIfPossible(photoFile)

                            val photoUri = Uri.fromFile(photoFile).toString()
                            saveEventToDatabase(attemptNumber, entryPoint, photoUri, isDeviceLocked)
                            callback?.invoke(true, photoUri)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            try {
                                cameraProvider.unbindAll()
                            } catch (e: Exception) {
                                // Ignore unbind error
                            }
                            Log.e(TAG, "Headless capture failed: ${exception.message}", exception)
                            logEventWithoutPhoto(attemptNumber, entryPoint, isDeviceLocked)
                            callback?.invoke(false, exception.message)
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "CameraX initialization error: ${e.message}", e)
                logEventWithoutPhoto(attemptNumber, entryPoint, isDeviceLocked)
                callback?.invoke(false, e.message)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun prepareOutputFile(): File {
        val internalDir = File(context.filesDir, FOLDER_NAME)
        if (!internalDir.exists()) {
            internalDir.mkdirs()
        }
        val noMedia = File(internalDir, NO_MEDIA_FILE)
        if (!noMedia.exists()) {
            try { noMedia.createNewFile() } catch (e: Exception) { /* Ignore */ }
        }

        val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(internalDir, "intruder_${timestampStr}.jpg")
    }

    private fun copyToVaultFolderIfPossible(internalPhoto: File) {
        val vaultUriStr = securityManager.vaultFolderUri ?: return
        try {
            val vaultTreeUri = Uri.parse(vaultUriStr)
            val rootDoc = DocumentFile.fromTreeUri(context, vaultTreeUri) ?: return
            var intruderDir = rootDoc.findFile(FOLDER_NAME)
            if (intruderDir == null || !intruderDir.isDirectory) {
                intruderDir = rootDoc.createDirectory(FOLDER_NAME) ?: return
            }
            if (intruderDir.findFile(NO_MEDIA_FILE) == null) {
                intruderDir.createFile("application/octet-stream", NO_MEDIA_FILE)
            }

            val targetDoc = intruderDir.createFile("image/jpeg", internalPhoto.name) ?: return
            context.contentResolver.openOutputStream(targetDoc.uri)?.use { outStream ->
                internalPhoto.inputStream().use { inStream ->
                    inStream.copyTo(outStream)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not mirror intruder photo to SD card folder: ${e.message}")
        }
    }

    private fun saveEventToDatabase(
        attemptNumber: Int,
        entryPoint: IntruderEntryPoint,
        photoUri: String?,
        isDeviceLocked: Boolean
    ) {
        coroutineScope.launch {
            val event = IntruderEventEntity(
                timestamp = System.currentTimeMillis(),
                attemptNumber = attemptNumber,
                entryPoint = entryPoint.name,
                photoUri = photoUri,
                seen = false,
                deviceLockedState = isDeviceLocked
            )
            db.intruderEventDao().insertEvent(event)

            // Auto prune oldest events
            val retention = securityManager.intruderRetentionCount
            if (retention > 0) {
                db.intruderEventDao().pruneOldEvents(retention)
            }
        }
    }

    private fun logEventWithoutPhoto(
        attemptNumber: Int,
        entryPoint: IntruderEntryPoint,
        isDeviceLocked: Boolean
    ) {
        saveEventToDatabase(attemptNumber, entryPoint, null, isDeviceLocked)
    }
}
