package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.camera.CameraViewModel
import com.example.camera.ui.AboutUsScreen
import com.example.camera.ui.CameraControlsOverlay
import com.example.camera.ui.CameraPreviewView
import com.example.camera.ui.PhotoReviewScreen
import com.example.ui.theme.CameraPitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SB7ProCameraTheme
import com.example.ui.theme.Yellow500
import com.example.ui.theme.Zinc100
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc700
import com.example.ui.theme.Zinc800
import com.example.ui.theme.Zinc900
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SB7ProCameraTheme {
                MainCameraApp()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainCameraApp(
    viewModel: CameraViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissions = buildList {
        add(Manifest.permission.CAMERA)
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = permissions)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CameraPitchBlack)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CameraPitchBlack)
        ) {
            if (permissionsState.allPermissionsGranted) {
                // Active Camera Viewfinder & Pro Controls
                CameraPreviewView(
                    viewModel = viewModel,
                    uiState = uiState,
                    modifier = Modifier.fillMaxSize()
                )

                CameraControlsOverlay(
                    viewModel = viewModel,
                    uiState = uiState,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Permission Request Screen with Sophisticated Dark Styling
                CameraPermissionScreen(
                    onRequestPermissions = { permissionsState.launchMultiplePermissionRequest() },
                    shouldShowRationale = permissionsState.shouldShowRationale,
                    onOpenAbout = { viewModel.setAboutUsOpen(true) },
                    onTryDemoCapture = { viewModel.capturePhoto(context) }
                )
            }

            // Post-Capture Photo Review Modal
            AnimatedVisibility(
                visible = uiState.activeReviewPhoto != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                uiState.activeReviewPhoto?.let { photo ->
                    PhotoReviewScreen(
                        photo = photo,
                        isProcessing = uiState.isProcessing,
                        onClose = { viewModel.closeReviewPhoto() },
                        onUpdateTuning = { sharpness, contrast, vibrance, aperture, applyBokeh ->
                            viewModel.updateActiveReviewPhoto(
                                context = context,
                                sharpness = sharpness,
                                contrast = contrast,
                                vibrance = vibrance,
                                aperture = aperture,
                                applyBokeh = applyBokeh
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Dedicated "About Us" Screen
            AnimatedVisibility(
                visible = uiState.isAboutUsOpen,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it })
            ) {
                AboutUsScreen(
                    onBackClick = { viewModel.setAboutUsOpen(false) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun CameraPermissionScreen(
    onRequestPermissions: () -> Unit,
    shouldShowRationale: Boolean,
    onOpenAbout: () -> Unit,
    onTryDemoCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CameraPitchBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("permission_prompt_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Zinc900),
            border = BorderStroke(1.dp, Zinc800)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Zinc800,
                    border = BorderStroke(1.dp, Yellow500.copy(alpha = 0.5f)),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Camera Icon",
                            tint = Yellow500,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SB7 Pro Camera",
                    color = Yellow500,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Camera & Media Access Required",
                    color = PureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (shouldShowRationale) {
                        "SB7 Pro Camera needs camera hardware access to capture high-definition photos, 20x zoom, and real-time portrait bokeh blur."
                    } else {
                        "To experience 20x zoom, DSLR bokeh blur, and professional image enhancement, please grant camera permissions."
                    },
                    color = Zinc400,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRequestPermissions,
                    colors = ButtonDefaults.buttonColors(containerColor = Yellow500),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("grant_permission_button")
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = CameraPitchBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enable Camera Hardware",
                        color = CameraPitchBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onTryDemoCapture,
                        colors = ButtonDefaults.buttonColors(containerColor = Zinc800),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Zinc700),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Demo Capture", color = Zinc100, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onOpenAbout,
                        colors = ButtonDefaults.buttonColors(containerColor = Zinc800),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Yellow500.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("About Us", color = Yellow500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
