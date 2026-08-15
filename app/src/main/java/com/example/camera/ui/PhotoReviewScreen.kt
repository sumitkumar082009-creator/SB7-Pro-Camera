package com.example.camera.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.camera.model.CapturedPhoto
import com.example.ui.theme.CameraPitchBlack
import com.example.ui.theme.LeicaRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Yellow500
import com.example.ui.theme.Zinc100
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc700
import com.example.ui.theme.Zinc800
import com.example.ui.theme.Zinc900

@Composable
fun PhotoReviewScreen(
    photo: CapturedPhoto,
    isProcessing: Boolean,
    onClose: () -> Unit,
    onUpdateTuning: (sharpness: Float, contrast: Float, vibrance: Float, aperture: Float, applyBokeh: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isShowOriginalHeld by remember { mutableStateOf(false) }
    var isTuningPanelOpen by remember { mutableStateOf(false) }

    var sharpness by remember { mutableFloatStateOf(photo.sharpness) }
    var contrast by remember { mutableFloatStateOf(photo.contrast) }
    var vibrance by remember { mutableFloatStateOf(1.2f) }
    var aperture by remember { mutableFloatStateOf(photo.apertureFStop) }
    var applyBokeh by remember { mutableStateOf(photo.isBokehApplied) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CameraPitchBlack)
            .testTag("photo_review_screen")
    ) {
        // Main Photo Canvas
        val displayBitmap = if (isShowOriginalHeld && photo.originalBitmap != null) {
            photo.originalBitmap
        } else {
            photo.bitmap ?: photo.originalBitmap
        }

        displayBitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Reviewed Photo",
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isShowOriginalHeld = true
                                tryAwaitRelease()
                                isShowOriginalHeld = false
                            }
                        )
                    },
                contentScale = ContentScale.Fit
            )
        }

        // Top Navigation & Compare Indicator
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Zinc900.copy(alpha = 0.85f))
                    .border(BorderStroke(1.dp, Zinc800), CircleShape)
                    .testTag("close_review_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Zinc100)
            }

            // Compare badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isShowOriginalHeld) LeicaRed else Zinc900,
                border = BorderStroke(1.dp, if (isShowOriginalHeld) LeicaRed else Yellow500.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Compare,
                        contentDescription = "Compare",
                        tint = if (isShowOriginalHeld) PureWhite else Yellow500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isShowOriginalHeld) "RAW UNEDITED (HOLDING)" else "DSLR PRO PROCESSED",
                        color = if (isShowOriginalHeld) PureWhite else Yellow500,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            IconButton(
                onClick = { isTuningPanelOpen = !isTuningPanelOpen },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isTuningPanelOpen) Yellow500 else Zinc900.copy(alpha = 0.85f))
                    .border(BorderStroke(1.dp, if (isTuningPanelOpen) Yellow500 else Zinc800), CircleShape)
                    .testTag("toggle_tuning_button")
            ) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = "Tune",
                    tint = if (isTuningPanelOpen) CameraPitchBlack else Zinc100
                )
            }
        }

        // Progress bar when re-tuning
        if (isProcessing) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = Yellow500,
                trackColor = Zinc900
            )
        }

        // Bottom Tuning or Action Panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Tuning Drawer
            AnimatedVisibility(visible = isTuningPanelOpen) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Zinc900.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, Zinc800)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "DSLR POST-PROCESSING ENGINE",
                            color = Yellow500,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sharpening Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Unsharp Mask Sharpening", color = Zinc100, fontSize = 12.sp)
                            Text(
                                String.format("%.2fx", sharpness),
                                color = Yellow500,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = sharpness,
                            onValueChange = {
                                sharpness = it
                                onUpdateTuning(sharpness, contrast, vibrance, aperture, applyBokeh)
                            },
                            valueRange = 0.5f..2.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = PureWhite,
                                activeTrackColor = Yellow500,
                                inactiveTrackColor = Zinc800
                            )
                        )

                        // Contrast S-Curve Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Dynamic Range S-Curve", color = Zinc100, fontSize = 12.sp)
                            Text(
                                String.format("%.2fx", contrast),
                                color = Yellow500,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = contrast,
                            onValueChange = {
                                contrast = it
                                onUpdateTuning(sharpness, contrast, vibrance, aperture, applyBokeh)
                            },
                            valueRange = 0.8f..1.8f,
                            colors = SliderDefaults.colors(
                                thumbColor = PureWhite,
                                activeTrackColor = Yellow500,
                                inactiveTrackColor = Zinc800
                            )
                        )

                        // Bokeh Aperture Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CenterFocusStrong, contentDescription = null, tint = Yellow500, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Portrait Bokeh Blur", color = Zinc100, fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    applyBokeh = !applyBokeh
                                    onUpdateTuning(sharpness, contrast, vibrance, aperture, applyBokeh)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (applyBokeh) Yellow500 else Zinc800
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = if (applyBokeh) "f/$aperture" else "OFF",
                                    color = if (applyBokeh) CameraPitchBlack else Zinc400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (applyBokeh) {
                            Slider(
                                value = aperture,
                                onValueChange = {
                                    aperture = it
                                    onUpdateTuning(sharpness, contrast, vibrance, aperture, applyBokeh)
                                },
                                valueRange = 1.4f..8.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = PureWhite,
                                    activeTrackColor = Yellow500,
                                    inactiveTrackColor = Zinc800
                                )
                            )
                        }
                    }
                }
            }

            // Bottom Actions: Save, Share, Info
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Zinc900.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, Zinc800)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SB7 Pro Ultra HD",
                            color = Zinc100,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mode: ${photo.mode.title} • Zoom: ${String.format("%.1fx", photo.zoomRatio)}",
                            color = Zinc400,
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Share
                        IconButton(
                            onClick = {
                                sharePhoto(context, photo)
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Zinc800)
                                .border(BorderStroke(1.dp, Zinc700), CircleShape)
                                .testTag("share_photo_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Zinc100)
                        }

                        // Save Button
                        Button(
                            onClick = {
                                Toast.makeText(context, "Saved to Gallery in HD quality!", Toast.LENGTH_SHORT).show()
                                onClose()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Yellow500),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("save_photo_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Save", tint = CameraPitchBlack, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save", color = CameraPitchBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun sharePhoto(context: Context, photo: CapturedPhoto) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        if (photo.uri != null) {
            putExtra(Intent.EXTRA_STREAM, photo.uri)
            type = "image/jpeg"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            putExtra(Intent.EXTRA_TEXT, "Captured with SB7 Pro Camera - 20x Zoom & DSLR Bokeh!")
            type = "text/plain"
        }
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share SB7 Pro Photo")
    context.startActivity(shareIntent)
}
