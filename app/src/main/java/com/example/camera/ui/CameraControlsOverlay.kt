package com.example.camera.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HdrOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.CameraViewModel
import com.example.camera.model.AspectRatioMode
import com.example.camera.model.CameraMode
import com.example.camera.model.CameraUiState
import com.example.camera.model.FlashMode
import com.example.camera.model.GridMode
import com.example.camera.model.TimerMode
import com.example.ui.theme.CameraPitchBlack
import com.example.ui.theme.LeicaRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Yellow500
import com.example.ui.theme.Zinc100
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc500
import com.example.ui.theme.Zinc700
import com.example.ui.theme.Zinc800
import com.example.ui.theme.Zinc900

@Composable
fun CameraControlsOverlay(
    viewModel: CameraViewModel,
    uiState: CameraUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isZoomSliderExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // TOP SOPHISTICATED PRO BAR
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SophisticatedHeaderBar(
                uiState = uiState,
                onFlashClick = { viewModel.cycleFlashMode() },
                onAspectClick = { viewModel.cycleAspectRatio() },
                onTimerClick = { viewModel.cycleTimer() },
                onGridClick = { viewModel.cycleGridMode() },
                onDslrAutoToggle = { viewModel.setAutoEnhanceDslr(!uiState.isAutoEnhanceDslr) },
                onAboutClick = { viewModel.setAboutUsOpen(true) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // FLOATING ACTIVE MODE STATUS PILL
            ActiveModeStatusPill(uiState = uiState)
        }

        // COUNTDOWN TIMER OVERLAY
        uiState.countdownRemaining?.let { count ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Zinc900.copy(alpha = 0.95f),
                    border = BorderStroke(2.dp, Yellow500),
                    modifier = Modifier.size(120.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$count",
                            color = Yellow500,
                            fontSize = 60.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // BOTTOM CONTROLS STACK
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // MODE SPECIFIC CONTROLS (PRO OR PORTRAIT)
            when (uiState.currentMode) {
                CameraMode.PRO -> {
                    ProManualControlsBar(
                        uiState = uiState,
                        onExposureChange = { viewModel.setExposureCompensation(it) },
                        onIsoChange = { viewModel.setProIso(it) },
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                CameraMode.PORTRAIT -> {
                    PortraitBokehControlsBar(
                        aperture = uiState.portraitAperture,
                        onApertureChange = { viewModel.setPortraitAperture(it) },
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                else -> {}
            }

            // 20X ZOOM SECTION
            SophisticatedZoomSection(
                zoomRatio = uiState.zoomRatio,
                isExpanded = isZoomSliderExpanded,
                onZoomChange = { viewModel.setZoomRatio(it) },
                onToggleExpand = { isZoomSliderExpanded = !isZoomSliderExpanded },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // CAMERA MODE SELECTOR CAROUSEL
            SophisticatedModeCarousel(
                currentMode = uiState.currentMode,
                onModeSelected = { viewModel.setMode(it) },
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // SHUTTER & ACTION BAR
            SophisticatedShutterBar(
                uiState = uiState,
                onCapture = { viewModel.capturePhoto(context) },
                onFlip = { viewModel.toggleCameraFacing() },
                onGalleryClick = {
                    uiState.lastCapturedPhoto?.let { photo ->
                        viewModel.openReviewPhoto(photo)
                    }
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // SIGNATURE DEVELOPER DOCKED CARD
            DeveloperSignatureCard(
                onProfileClick = { viewModel.setAboutUsOpen(true) },
                onInstagramClick = {
                    openInstagram(context, "https://instagram.com/sumit_bhumihar_7")
                }
            )
        }
    }
}

/**
 * Header matching the "Sophisticated Dark" header bar
 */
@Composable
fun SophisticatedHeaderBar(
    uiState: CameraUiState,
    onFlashClick: () -> Unit,
    onAspectClick: () -> Unit,
    onTimerClick: () -> Unit,
    onGridClick: () -> Unit,
    onDslrAutoToggle: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_pro_bar"),
        shape = RoundedCornerShape(20.dp),
        color = Zinc900.copy(alpha = 0.90f),
        border = BorderStroke(1.dp, Zinc800)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PRO Badge + App Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Zinc900)
                        .border(BorderStroke(1.dp, Zinc800), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PRO",
                        color = Yellow500,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = "SB7 Pro Camera",
                    color = Zinc100,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.2).sp
                )
            }

            // Quick Control Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Flash
                IconButton(
                    onClick = onFlashClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("flash_button")
                ) {
                    val icon = when (uiState.flashMode) {
                        FlashMode.OFF -> Icons.Default.FlashOff
                        FlashMode.ON -> Icons.Default.FlashOn
                        FlashMode.AUTO -> Icons.Default.FlashAuto
                        FlashMode.TORCH -> Icons.Default.FlashOn
                    }
                    val tint = if (uiState.flashMode != FlashMode.OFF) Yellow500 else Zinc400
                    Icon(icon, contentDescription = "Flash", tint = tint, modifier = Modifier.size(18.dp))
                }

                // HDR / DSLR Auto
                IconButton(
                    onClick = onDslrAutoToggle,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("dslr_enhance_toggle")
                ) {
                    Icon(
                        Icons.Default.HdrOn,
                        contentDescription = "DSLR HDR",
                        tint = if (uiState.isAutoEnhanceDslr) Yellow500 else Zinc400,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Aspect Ratio
                IconButton(
                    onClick = onAspectClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("aspect_ratio_button")
                ) {
                    Icon(
                        Icons.Default.Crop,
                        contentDescription = "Aspect Ratio",
                        tint = Zinc400,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Timer
                IconButton(
                    onClick = onTimerClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("timer_button")
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = if (uiState.timerMode != TimerMode.OFF) Yellow500 else Zinc400,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Settings / About Us
                IconButton(
                    onClick = onAboutClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("about_us_button")
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "About Us & Settings",
                        tint = Zinc400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Floating Active Mode Pill with pulsating dot indicator
 */
@Composable
fun ActiveModeStatusPill(
    uiState: CameraUiState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val labelText = when {
        uiState.currentMode == CameraMode.PORTRAIT -> "Portrait Mode Active (Bokeh)"
        uiState.currentMode == CameraMode.PRO -> "Pro Manual Calibration"
        uiState.currentMode == CameraMode.NIGHT -> "Night Sight Mode Active"
        uiState.zoomRatio >= 10.0f -> "20x Ultra Precision Zoom"
        uiState.isAutoEnhanceDslr -> "DSLR HD Post-Processing Active"
        else -> "Pro Camera Engine Ready"
    }

    Surface(
        modifier = modifier.testTag("active_mode_status_pill"),
        shape = RoundedCornerShape(50),
        color = Zinc800.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, Zinc700.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .alpha(pulseAlpha)
                    .background(LeicaRed)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = labelText.uppercase(),
                color = PureWhite,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }
    }
}

/**
 * Sophisticated Dark Zoom Section (Slider Track + 1x, 2x, 5x, 20.0x values)
 */
@Composable
fun SophisticatedZoomSection(
    zoomRatio: Float,
    isExpanded: Boolean,
    onZoomChange: (Float) -> Unit,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(1.0f, 2.0f, 5.0f, 10.0f, 20.0f)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Continuous Slider (when expanded)
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(bottom = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = Zinc900.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Zinc800)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("DEEP 20x ZOOM ENGINE", color = Zinc400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = String.format("%.1fx", zoomRatio),
                            color = Yellow500,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = zoomRatio,
                        onValueChange = onZoomChange,
                        valueRange = 1.0f..20.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = PureWhite,
                            activeTrackColor = Yellow500,
                            inactiveTrackColor = Zinc800
                        ),
                        modifier = Modifier.testTag("zoom_slider_continuous")
                    )
                }
            }
        }

        // Sophisticated Zoom Bar with Custom Track & Presets
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = Zinc900.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, Zinc800)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clickable { onToggleExpand() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Preset values list
                Row(
                    modifier = Modifier.padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    presets.forEach { preset ->
                        val isSelected = kotlin.math.abs(zoomRatio - preset) < 0.35f
                        Text(
                            text = if (preset >= 10f) "${preset.toInt()}.0x" else "${preset.toInt()}x",
                            color = if (isSelected) Yellow500 else Zinc400.copy(alpha = 0.7f),
                            fontSize = if (isSelected) 12.sp else 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            letterSpacing = (-0.5).sp,
                            modifier = Modifier
                                .clickable { onZoomChange(preset) }
                                .testTag("zoom_preset_${preset.toInt()}x")
                        )
                    }
                }

                // Mini Yellow Progress Bar Track
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    val progressFraction = ((zoomRatio - 1f) / 19f).coerceIn(0.05f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .height(3.dp)
                            .background(Yellow500, RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

/**
 * Mode selector carousel with uppercase typography and yellow underline
 */
@Composable
fun SophisticatedModeCarousel(
    currentMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = CameraMode.values()
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        modes.forEach { mode ->
            val isSelected = currentMode == mode
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 4.dp)
                    .testTag("mode_${mode.name.lowercase()}"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mode.title,
                    color = if (isSelected) PureWhite else Zinc500,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = 0.8.sp
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(width = 24.dp, height = 2.5.dp)
                            .background(Yellow500, RoundedCornerShape(2.dp))
                    )
                } else {
                    Spacer(modifier = Modifier.height(6.5.dp))
                }
            }
        }
    }
}

/**
 * Bottom Shutter Bar matching Sophisticated Dark specification:
 * - Square gallery thumbnail (rounded-xl)
 * - Shutter button with double white ring
 * - Lens flip button (rounded-full bg-zinc-800 border-zinc-700)
 */
@Composable
fun SophisticatedShutterBar(
    uiState: CameraUiState,
    onCapture: () -> Unit,
    onFlip: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isShutterPressed by remember { mutableStateOf(false) }
    val shutterScale by animateFloatAsState(
        targetValue = if (isShutterPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "shutter_scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gallery Thumbnail (w-12 h-12 rounded-xl bg-zinc-800 border border-zinc-700)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Zinc800)
                .border(BorderStroke(1.dp, Zinc700), RoundedCornerShape(14.dp))
                .clickable { onGalleryClick() }
                .testTag("gallery_thumbnail_button"),
            contentAlignment = Alignment.Center
        ) {
            val lastPhoto = uiState.lastCapturedPhoto
            if (lastPhoto?.bitmap != null) {
                Image(
                    bitmap = lastPhoto.bitmap.asImageBitmap(),
                    contentDescription = "Last captured photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = "Gallery",
                    tint = Zinc400,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (uiState.capturedPhotos.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .background(Yellow500, CircleShape)
                        .size(15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${minOf(99, uiState.capturedPhotos.size)}",
                        color = CameraPitchBlack,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Shutter Button (w-20 h-20 rounded-full border-4 border-white flex items-center justify-center)
        Box(
            modifier = Modifier
                .size(76.dp)
                .scale(shutterScale)
                .pointerInput(uiState.isCapturing) {
                    detectTapGestures(
                        onPress = {
                            isShutterPressed = true
                            tryAwaitRelease()
                            isShutterPressed = false
                            onCapture()
                        }
                    )
                }
                .testTag("shutter_button"),
            contentAlignment = Alignment.Center
        ) {
            // Outer white ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(BorderStroke(3.5.dp, PureWhite), CircleShape)
            )

            // Inner white/yellow core
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            uiState.isCapturing -> LeicaRed
                            uiState.currentMode == CameraMode.PORTRAIT -> Yellow500
                            else -> PureWhite
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isCapturing || uiState.isProcessing) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(CameraPitchBlack, CircleShape)
                    )
                }
            }
        }

        // Lens Flip Button (w-12 h-12 rounded-full bg-zinc-800 border border-zinc-700)
        IconButton(
            onClick = onFlip,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Zinc800)
                .border(BorderStroke(1.dp, Zinc700), CircleShape)
                .testTag("flip_camera_button")
        ) {
            Icon(
                Icons.Default.Cameraswitch,
                contentDescription = "Flip Camera",
                tint = Zinc100,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Signature Docked Developer Signature Card matching Sophisticated Dark spec:
 * `bg-zinc-900/80 backdrop-blur rounded-2xl p-3 border border-zinc-800 flex items-center gap-4`
 */
@Composable
fun DeveloperSignatureCard(
    onProfileClick: () -> Unit,
    onInstagramClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onProfileClick() }
            .testTag("developer_signature_dock"),
        shape = RoundedCornerShape(16.dp),
        color = Zinc900.copy(alpha = 0.90f),
        border = BorderStroke(1.dp, Zinc800)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Portrait Mini Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Zinc800)
                    .border(BorderStroke(1.dp, Zinc700), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Sumit Bhumihar",
                    tint = Yellow500,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Developer Name & Tagline
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SUMIT BHUMIHAR",
                    color = PureWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "DSLR-Quality Post-Processing Engine Active",
                    color = Zinc400,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Instagram Pill Button
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onInstagramClick() }
                    .testTag("dock_instagram_button"),
                shape = RoundedCornerShape(8.dp),
                color = Yellow500
            ) {
                Text(
                    text = "@sumit_bhumihar_7",
                    color = CameraPitchBlack,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }
        }
    }
}

/**
 * Dedicated Portrait Bokeh Aperture Selector
 */
@Composable
fun PortraitBokehControlsBar(
    aperture: Float,
    onApertureChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val fStops = listOf(1.4f, 2.0f, 2.8f, 4.0f, 5.6f, 8.0f)

    Surface(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .testTag("portrait_controls_bar"),
        shape = RoundedCornerShape(16.dp),
        color = Zinc900.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, Yellow500.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CenterFocusStrong,
                        contentDescription = "Bokeh",
                        tint = Yellow500,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DSLR BOKEH APERTURE", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "f/${aperture}",
                    color = Yellow500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                fStops.forEach { stop ->
                    val isSelected = aperture == stop
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onApertureChange(stop) }
                            .testTag("fstop_preset_${stop}"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Yellow500 else Zinc800,
                        border = if (isSelected) null else BorderStroke(1.dp, Zinc700)
                    ) {
                        Text(
                            text = "f/$stop",
                            color = if (isSelected) CameraPitchBlack else Zinc400,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Pro Manual Controls (EV Compensation, ISO)
 */
@Composable
fun ProManualControlsBar(
    uiState: CameraUiState,
    onExposureChange: (Int) -> Unit,
    onIsoChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isoList = listOf(100, 200, 400, 800, 1600, 3200)

    Surface(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .testTag("pro_manual_bar"),
        shape = RoundedCornerShape(16.dp),
        color = Zinc900.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, Zinc800)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            // EV slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("EXPOSURE (EV)", color = Zinc400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${if (uiState.exposureCompensation > 0) "+" else ""}${uiState.exposureCompensation} EV",
                    color = Yellow500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Slider(
                value = uiState.exposureCompensation.toFloat(),
                onValueChange = { onExposureChange(it.toInt()) },
                valueRange = -4f..4f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = PureWhite,
                    activeTrackColor = Yellow500,
                    inactiveTrackColor = Zinc800
                ),
                modifier = Modifier.testTag("ev_slider")
            )

            // ISO Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ISO SENSITIVITY", color = Zinc400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    isoList.forEach { iso ->
                        val isSelected = uiState.proIso == iso
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onIsoChange(iso) }
                                .testTag("iso_preset_$iso"),
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) Yellow500 else Zinc800,
                            border = if (isSelected) null else BorderStroke(1.dp, Zinc700)
                        ) {
                            Text(
                                text = "$iso",
                                color = if (isSelected) CameraPitchBlack else Zinc400,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun openInstagram(context: Context, url: String) {
    try {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.instagram.android")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    }
}
