package com.example.camera.ui

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.camera.CameraViewModel
import com.example.camera.model.AspectRatioMode
import com.example.camera.model.CameraUiState
import com.example.camera.model.GridMode
import com.example.ui.theme.CameraPitchBlack
import com.example.ui.theme.GridLineColor
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Yellow500
import com.example.ui.theme.Zinc800
import com.example.ui.theme.Zinc900
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun CameraPreviewView(
    viewModel: CameraViewModel,
    uiState: CameraUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var tapCoords by remember { mutableStateOf<Offset?>(null) }
    val focusAnimScale = remember { Animatable(1.4f) }
    val focusAnimAlpha = remember { Animatable(0f) }

    LaunchedEffect(tapCoords) {
        tapCoords?.let {
            focusAnimScale.snapTo(1.4f)
            focusAnimAlpha.snapTo(1.0f)
            focusAnimScale.animateTo(1.0f, tween(250))
            focusAnimAlpha.animateTo(0f, tween(1500))
            tapCoords = null
        }
    }

    // Bind camera lifecycle cleanly when previewView or camera facing changes
    LaunchedEffect(previewView, uiState.isBackCamera, lifecycleOwner) {
        val currentPreviewView = previewView ?: return@LaunchedEffect
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = withContext(Dispatchers.Main) {
            cameraProviderFuture.get()
        }

        try {
            cameraProvider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = currentPreviewView.surfaceProvider
            }

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            val cameraSelector = if (uiState.isBackCamera) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            if (cameraProvider.hasCamera(cameraSelector)) {
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                viewModel.onCameraBound(cameraProvider, camera, imageCapture)
            }
        } catch (e: Exception) {
            Log.e("CameraPreviewView", "Error binding camera lifecycle: ${e.message}", e)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CameraPitchBlack),
        contentAlignment = Alignment.Center
    ) {
        // Viewfinder aspect ratio container
        val ratioValue = when (uiState.aspectRatio) {
            AspectRatioMode.RATIO_4_3 -> 3f / 4f
            AspectRatioMode.RATIO_16_9 -> 9f / 16f
            AspectRatioMode.RATIO_1_1 -> 1f
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .aspectRatio(ratioValue)
                .clip(RoundedCornerShape(32.dp))
                .background(Zinc900)
                .border(BorderStroke(1.dp, Zinc800.copy(alpha = 0.8f)), RoundedCornerShape(32.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        val newRatio = (uiState.zoomRatio * zoom).coerceIn(1.0f, 20.0f)
                        viewModel.setZoomRatio(newRatio)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        tapCoords = offset
                        val pView = previewView
                        if (pView != null && pView.meteringPointFactory != null) {
                            viewModel.triggerFocus(
                                pView.meteringPointFactory,
                                offset.x,
                                offset.y
                            )
                        }
                    }
                }
                .testTag("camera_preview_container")
        ) {
            // Android CameraX Preview View with dynamic digital zoom scaling up to 20x
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        previewView = this
                    }
                },
                update = { pView ->
                    previewView = pView
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = uiState.digitalZoomFactor
                        scaleY = uiState.digitalZoomFactor
                        transformOrigin = TransformOrigin.Center
                    }
            )

            // Sophisticated Dark Cinematic Vignette Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.55f)
                            )
                        )
                    )
            )

            // Grid Overlay
            if (uiState.gridMode != GridMode.OFF) {
                CameraGridOverlay(gridMode = uiState.gridMode)
            }

            // Central Subtle Focus Ring Reticle (Sophisticated Dark signature)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.Center)
                    .border(BorderStroke(1.dp, PureWhite.copy(alpha = 0.22f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .background(PureWhite.copy(alpha = 0.7f), CircleShape)
                )
            }

            // Animated Dynamic Focus Tap Reticle
            tapCoords?.let { pt ->
                Box(
                    modifier = Modifier
                        .offset { IntOffset((pt.x - 36.dp.toPx()).roundToInt(), (pt.y - 36.dp.toPx()).roundToInt()) }
                        .size(72.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val scale = focusAnimScale.value
                        val alpha = focusAnimAlpha.value
                        drawCircle(
                            color = Yellow500.copy(alpha = alpha),
                            radius = size.width / 2 * scale,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = Yellow500.copy(alpha = alpha),
                            radius = 3.dp.toPx()
                        )
                    }
                }
            }

            // Shutter Flash Animation Overlay
            AnimatedVisibility(
                visible = uiState.isShutterFlashing,
                enter = fadeIn(tween(50)),
                exit = fadeOut(tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.85f))
                )
            }

            // Live Captured Toast / Pill Confirmation Banner
            AnimatedVisibility(
                visible = uiState.captureNotification != null,
                enter = fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                uiState.captureNotification?.let { note ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Zinc900.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, Yellow500.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Yellow500,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = note,
                                color = PureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraGridOverlay(
    gridMode: GridMode,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when (gridMode) {
            GridMode.RULE_OF_THIRDS -> {
                drawLine(GridLineColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 1.dp.toPx())
                drawLine(GridLineColor, Offset(2 * w / 3f, 0f), Offset(2 * w / 3f, h), strokeWidth = 1.dp.toPx())
                drawLine(GridLineColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 1.dp.toPx())
                drawLine(GridLineColor, Offset(0f, 2 * h / 3f), Offset(w, 2 * h / 3f), strokeWidth = 1.dp.toPx())
            }
            GridMode.GOLDEN_RATIO -> {
                val phi = 0.618f
                drawLine(GridLineColor, Offset(w * (1 - phi), 0f), Offset(w * (1 - phi), h), strokeWidth = 1.dp.toPx())
                drawLine(GridLineColor, Offset(w * phi, 0f), Offset(w * phi, h), strokeWidth = 1.dp.toPx())
                drawLine(GridLineColor, Offset(0f, h * (1 - phi)), Offset(w, h * (1 - phi)), strokeWidth = 1.dp.toPx())
                drawLine(GridLineColor, Offset(0f, h * phi), Offset(w, h * phi), strokeWidth = 1.dp.toPx())
            }
            GridMode.CROSSHAIR -> {
                val cx = w / 2f
                val cy = h / 2f
                val len = 20.dp.toPx()
                drawLine(Yellow500.copy(alpha = 0.6f), Offset(cx - len, cy), Offset(cx + len, cy), strokeWidth = 1.2.dp.toPx())
                drawLine(Yellow500.copy(alpha = 0.6f), Offset(cx, cy - len), Offset(cx, cy + len), strokeWidth = 1.2.dp.toPx())
                drawCircle(Yellow500.copy(alpha = 0.4f), radius = 28.dp.toPx(), style = Stroke(1.dp.toPx()))
            }
            GridMode.OFF -> {}
        }
    }
}
