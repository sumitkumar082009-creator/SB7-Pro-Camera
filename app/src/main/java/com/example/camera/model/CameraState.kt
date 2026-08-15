package com.example.camera.model

import android.graphics.Bitmap
import android.net.Uri

enum class CameraMode(val title: String, val badge: String) {
    PRO("PRO", "MANUAL"),
    PHOTO("PHOTO", "HD"),
    PORTRAIT("PORTRAIT", "BOKEH"),
    NIGHT("NIGHT", "LOW-LIGHT"),
    MACRO("MACRO", "DETAIL")
}

enum class FlashMode(val iconName: String) {
    OFF("Off"),
    ON("On"),
    AUTO("Auto"),
    TORCH("Torch")
}

enum class AspectRatioMode(val title: String, val ratio: Float) {
    RATIO_4_3("4:3", 4f / 3f),
    RATIO_16_9("16:9", 16f / 9f),
    RATIO_1_1("1:1", 1f)
}

enum class TimerMode(val seconds: Int, val label: String) {
    OFF(0, "Off"),
    SEC_3(3, "3s"),
    SEC_5(5, "5s"),
    SEC_10(10, "10s")
}

enum class GridMode(val label: String) {
    OFF("Off"),
    RULE_OF_THIRDS("3x3 Grid"),
    GOLDEN_RATIO("Golden Ratio"),
    CROSSHAIR("Center Cross")
}

data class CapturedPhoto(
    val id: String = System.currentTimeMillis().toString(),
    val uri: Uri? = null,
    val bitmap: Bitmap? = null,
    val originalBitmap: Bitmap? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: CameraMode = CameraMode.PHOTO,
    val zoomRatio: Float = 1.0f,
    val apertureFStop: Float = 2.8f,
    val sharpness: Float = 1.2f,
    val contrast: Float = 1.15f,
    val isDslrEnhanced: Boolean = true,
    val isBokehApplied: Boolean = false
)

data class CameraUiState(
    val currentMode: CameraMode = CameraMode.PHOTO,
    val isBackCamera: Boolean = true,
    val zoomRatio: Float = 1.0f,
    val minZoomRatio: Float = 1.0f,
    val maxZoomRatio: Float = 20.0f,
    val digitalZoomFactor: Float = 1.0f,
    val flashMode: FlashMode = FlashMode.OFF,
    val aspectRatio: AspectRatioMode = AspectRatioMode.RATIO_4_3,
    val timerMode: TimerMode = TimerMode.OFF,
    val gridMode: GridMode = GridMode.RULE_OF_THIRDS,
    val isHdrEnabled: Boolean = true,
    val exposureCompensation: Int = 0,
    val exposureRange: IntRange = -4..4,
    // Pro manual parameters
    val proIso: Int = 100, // 100, 200, 400, 800, 1600, 3200
    val proShutterSpeed: String = "1/125s",
    val proWhitebalance: String = "Auto",
    // Portrait mode parameters
    val portraitAperture: Float = 2.8f, // f/1.4 to f/16
    val portraitBlurStrength: Float = 14f,
    // Post processing switches
    val isAutoEnhanceDslr: Boolean = true,
    val isCapturing: Boolean = false,
    val isProcessing: Boolean = false,
    val isShutterFlashing: Boolean = false,
    val countdownRemaining: Int? = null,
    val focusPoint: Pair<Float, Float>? = null,
    val lastCapturedPhoto: CapturedPhoto? = null,
    val capturedPhotos: List<CapturedPhoto> = emptyList(),
    val activeReviewPhoto: CapturedPhoto? = null,
    val isAboutUsOpen: Boolean = false,
    val captureNotification: String? = null
)
