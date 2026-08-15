package com.example.camera.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CameraPitchBlack
import com.example.ui.theme.LeicaRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.Yellow500
import com.example.ui.theme.Zinc100
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc500
import com.example.ui.theme.Zinc700
import com.example.ui.theme.Zinc800
import com.example.ui.theme.Zinc900

@Composable
fun AboutUsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val instagramId = "@sumit_bhumihar_7"
    val instagramUrl = "https://instagram.com/sumit_bhumihar_7"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CameraPitchBlack)
            .testTag("about_us_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Zinc900)
                        .border(BorderStroke(1.dp, Zinc800), CircleShape)
                        .testTag("about_back_button")
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Zinc100
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "ABOUT US",
                        color = Zinc400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "SB7 Pro Camera",
                        color = Yellow500,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DEVELOPER PROFILE CARD WITH CUSTOM STUDIO PORTRAIT
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("developer_profile_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Zinc900),
                border = BorderStroke(1.dp, Zinc800)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Custom Profile Artwork (Sumit Bhumihar sitting on green sofa in studio setup)
                    DeveloperStudioPortraitArt(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .border(BorderStroke(3.dp, Yellow500), CircleShape)
                            .testTag("developer_profile_picture")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Developer Name & Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Sumit Bhumihar",
                            color = PureWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Verified Creator",
                            tint = Yellow500,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Lead Developer & UI/UX Architect",
                        color = Zinc400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Clickable Instagram Profile Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                openInstagramProfile(context, instagramUrl)
                            }
                            .testTag("instagram_link_button"),
                        shape = RoundedCornerShape(16.dp),
                        color = Zinc800,
                        border = BorderStroke(1.dp, Yellow500.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
                                        ),
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = instagramId,
                                color = Yellow500,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.OpenInNew,
                                contentDescription = "Open Instagram",
                                tint = Zinc400,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Created by Sumit Bhumihar in a studio environment to bring true optical DSLR quality, 20x zoom precision, and creamy portrait bokeh to mobile creators.",
                        color = Zinc400,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // APP OVERVIEW & MISSION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Zinc900),
                border = BorderStroke(1.dp, Zinc800)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Yellow500, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "APP ARCHITECTURE",
                            color = Yellow500,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "SB7 Pro Camera is engineered with modern Android CameraX, deep 20x optical/digital zoom interpolation, and a high-performance native post-processing pipeline. It delivers razor-sharp clarity, cinematic dynamic range, and studio-grade depth blurring in real-time.",
                        color = Zinc100,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // KEY FEATURE HIGHLIGHTS
            Text(
                text = "ENGINEERED CAPABILITIES",
                color = Zinc400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            FeatureCard(
                icon = Icons.Default.ZoomIn,
                title = "20x Precision Zoom Engine",
                description = "Deep optical and algorithmic digital zoom ratio up to 20x with instant preset toggles (1x, 2x, 5x, 10x, 20x) and fine slider calibration.",
                accentColor = Yellow500
            )

            Spacer(modifier = Modifier.height(10.dp))

            FeatureCard(
                icon = Icons.Default.CenterFocusStrong,
                title = "DSLR Portrait Bokeh Studio",
                description = "Simulates variable optical lens apertures from f/1.4 to f/8.0 with intelligent subject contour isolation and buttery soft background blur.",
                accentColor = StudioGreen
            )

            Spacer(modifier = Modifier.height(10.dp))

            FeatureCard(
                icon = Icons.Default.AutoAwesome,
                title = "DSLR Image Post-Processing",
                description = "High-speed multi-pass unsharp mask convolution, S-curve dynamic contrast expansion, vibrance grading, and instant side-by-side comparison.",
                accentColor = LeicaRed
            )

            Spacer(modifier = Modifier.height(10.dp))

            FeatureCard(
                icon = Icons.Default.Tune,
                title = "Pro Manual Exposure & Metering",
                description = "Full manual control over Exposure Value (-4 to +4 EV), ISO sensitivity up to 3200, rule of thirds / golden ratio gridlines, and tap-to-meter focus.",
                accentColor = Yellow500
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Version Footer
            Text(
                text = "SB7 Pro Camera • Version 1.0.0 Pro Edition",
                color = Zinc500,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Engineered with Jetpack Compose & CameraX",
                color = Zinc500,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Custom High-Fidelity Vector/Canvas Artwork representing Sumit Bhumihar sitting on a green sofa in studio setup
 */
@Composable
fun DeveloperStudioPortraitArt(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Studio backdrop with warm ambient gradient
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020617)),
                center = Offset(w * 0.5f, h * 0.35f),
                radius = w * 0.7f
            )
        )

        // 2. Warm Studio ambient light glow
        drawCircle(
            color = Color(0x40FFB703),
            radius = w * 0.35f,
            center = Offset(w * 0.75f, h * 0.2f)
        )

        // 3. Studio Emerald Green Sofa
        val sofaGreen = Color(0xFF059669)
        val sofaGreenDark = Color(0xFF047857)
        val sofaShadow = Color(0xFF064E3B)

        drawRoundRect(
            color = sofaGreenDark,
            topLeft = Offset(w * 0.12f, h * 0.48f),
            size = Size(w * 0.76f, h * 0.35f),
            cornerRadius = CornerRadius(16.dp.toPx())
        )

        // Sofa Armrests (Left & Right)
        drawRoundRect(
            color = sofaGreen,
            topLeft = Offset(w * 0.08f, h * 0.54f),
            size = Size(w * 0.18f, h * 0.28f),
            cornerRadius = CornerRadius(12.dp.toPx())
        )
        drawRoundRect(
            color = sofaGreen,
            topLeft = Offset(w * 0.74f, h * 0.54f),
            size = Size(w * 0.18f, h * 0.28f),
            cornerRadius = CornerRadius(12.dp.toPx())
        )

        // Sofa Cushions with depth stitching
        drawRoundRect(
            color = sofaGreen,
            topLeft = Offset(w * 0.18f, h * 0.62f),
            size = Size(w * 0.64f, h * 0.24f),
            cornerRadius = CornerRadius(10.dp.toPx())
        )

        // Sofa Cushion Line
        drawLine(
            color = sofaShadow,
            start = Offset(w * 0.5f, h * 0.62f),
            end = Offset(w * 0.5f, h * 0.86f),
            strokeWidth = 2.dp.toPx()
        )

        // 4. Developer Sumit Bhumihar Portrait (Sitting in center of green sofa)
        val jacketColor = Color(0xFF1E2430)
        val shirtColor = Color(0xFFF1F5F9)
        val skinTone = Color(0xFFD99B73)
        val hairColor = Color(0xFF1E1B18)

        // Torso / Shoulders
        val torsoPath = Path().apply {
            moveTo(w * 0.32f, h * 0.85f)
            lineTo(w * 0.34f, h * 0.58f)
            cubicTo(w * 0.38f, h * 0.50f, w * 0.62f, h * 0.50f, w * 0.66f, h * 0.58f)
            lineTo(w * 0.68f, h * 0.85f)
            close()
        }
        drawPath(torsoPath, color = jacketColor)

        // Inner shirt collar V-neck
        val collarPath = Path().apply {
            moveTo(w * 0.44f, h * 0.52f)
            lineTo(w * 0.5f, h * 0.64f)
            lineTo(w * 0.56f, h * 0.52f)
            close()
        }
        drawPath(collarPath, color = shirtColor)

        // Neck
        drawRect(
            color = skinTone,
            topLeft = Offset(w * 0.46f, h * 0.46f),
            size = Size(w * 0.08f, h * 0.08f)
        )

        // Head / Face
        drawOval(
            color = skinTone,
            topLeft = Offset(w * 0.38f, h * 0.28f),
            size = Size(w * 0.24f, h * 0.24f)
        )

        // Hair
        val hairPath = Path().apply {
            moveTo(w * 0.36f, h * 0.34f)
            cubicTo(w * 0.36f, h * 0.22f, w * 0.64f, h * 0.22f, w * 0.64f, h * 0.34f)
            cubicTo(w * 0.62f, h * 0.27f, w * 0.42f, h * 0.27f, w * 0.36f, h * 0.34f)
            close()
        }
        drawPath(hairPath, color = hairColor)
        drawCircle(color = hairColor, radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.32f))

        // Re-draw face contour cleanly
        drawOval(
            color = skinTone,
            topLeft = Offset(w * 0.39f, h * 0.31f),
            size = Size(w * 0.22f, h * 0.20f)
        )

        // Eyebrows & Eyes
        drawLine(Color(0xFF0F172A), Offset(w * 0.43f, h * 0.37f), Offset(w * 0.47f, h * 0.37f), strokeWidth = 2.dp.toPx())
        drawLine(Color(0xFF0F172A), Offset(w * 0.53f, h * 0.37f), Offset(w * 0.57f, h * 0.37f), strokeWidth = 2.dp.toPx())
        drawCircle(Color(0xFF0F172A), radius = 2.dp.toPx(), center = Offset(w * 0.45f, h * 0.40f))
        drawCircle(Color(0xFF0F172A), radius = 2.dp.toPx(), center = Offset(w * 0.55f, h * 0.40f))

        // Smile
        drawArc(
            color = Color(0xFF8B5E3C),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.47f, h * 0.44f),
            size = Size(w * 0.06f, h * 0.03f),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // 5. Studio Camera Gear Silhouette on Tripod
        drawRect(Color(0x8094A3B8), topLeft = Offset(w * 0.15f, h * 0.30f), size = Size(w * 0.08f, h * 0.06f))
        drawCircle(Color(0x80FFB703), radius = w * 0.02f, center = Offset(w * 0.19f, h * 0.33f))
        drawLine(Color(0x6094A3B8), Offset(w * 0.19f, h * 0.36f), Offset(w * 0.14f, h * 0.55f), strokeWidth = 1.5.dp.toPx())
        drawLine(Color(0x6094A3B8), Offset(w * 0.19f, h * 0.36f), Offset(w * 0.24f, h * 0.55f), strokeWidth = 1.5.dp.toPx())

        // 6. Pro Gold Lens Badge
        drawCircle(
            color = Yellow500,
            radius = w * 0.06f,
            center = Offset(w * 0.85f, h * 0.85f)
        )
        drawCircle(
            color = CameraPitchBlack,
            radius = w * 0.045f,
            center = Offset(w * 0.85f, h * 0.85f)
        )
    }
}

@Composable
fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Zinc900),
        border = BorderStroke(1.dp, Zinc800)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = PureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = Zinc400,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

private fun openInstagramProfile(context: Context, url: String) {
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
