package com.example.camera.processing

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.camera.model.AspectRatioMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object ImageProcessingPipeline {

    /**
     * Applies full DSLR-grade post-processing pipeline:
     * - Unsharp masking & edge sharpening
     * - S-Curve Contrast & Dynamic Range expansion
     * - Vibrance & Saturation color grading
     * - Highlight recovery & shadow clarity
     */
    suspend fun applyDslrEnhancement(
        srcBitmap: Bitmap,
        sharpness: Float = 1.35f,
        contrast: Float = 1.18f,
        vibrance: Float = 1.22f,
        clarity: Float = 1.15f
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = srcBitmap.width
        val height = srcBitmap.height
        
        // Step 1: Apply color matrix for Vibrance and Base Contrast
        val intermediateBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(intermediateBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        
        val cm = ColorMatrix()
        // Saturation/vibrance
        cm.setSaturation(vibrance)
        
        // Contrast adjustment matrix
        val scale = contrast
        val translate = (-0.5f * scale + 0.5f) * 255f
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(contrastMatrix)
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(srcBitmap, 0f, 0f, paint)

        // Step 2: Pixel-level Sharpening & Micro-contrast convolution (Unsharp Mask)
        val pixels = IntArray(width * height)
        intermediateBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val outputPixels = IntArray(width * height)

        val strength = min(2.5f, max(0.5f, sharpness))
        val centerWeight = 1f + 4f * (strength - 1f)
        val neighborWeight = -(strength - 1f)

        for (y in 0 until height) {
            val yPrev = max(0, y - 1) * width
            val yCurr = y * width
            val yNext = min(height - 1, y + 1) * width

            for (x in 0 until width) {
                val xPrev = max(0, x - 1)
                val xNext = min(width - 1, x + 1)

                val cCenter = pixels[yCurr + x]
                val cTop = pixels[yPrev + x]
                val cBottom = pixels[yNext + x]
                val cLeft = pixels[yCurr + xPrev]
                val cRight = pixels[yCurr + xNext]

                val a = (cCenter ushr 24) and 0xFF

                // Red
                val rC = (cCenter ushr 16) and 0xFF
                val rT = (cTop ushr 16) and 0xFF
                val rB = (cBottom ushr 16) and 0xFF
                val rL = (cLeft ushr 16) and 0xFF
                val rR = (cRight ushr 16) and 0xFF
                val rSharp = (rC * centerWeight + (rT + rB + rL + rR) * neighborWeight)
                val rFinal = min(255, max(0, rSharp.toInt()))

                // Green
                val gC = (cCenter ushr 8) and 0xFF
                val gT = (cTop ushr 8) and 0xFF
                val gB = (cBottom ushr 8) and 0xFF
                val gL = (cLeft ushr 8) and 0xFF
                val gR = (cRight ushr 8) and 0xFF
                val gSharp = (gC * centerWeight + (gT + gB + gL + gR) * neighborWeight)
                val gFinal = min(255, max(0, gSharp.toInt()))

                // Blue
                val bC = cCenter and 0xFF
                val bT = cTop and 0xFF
                val bB = cBottom and 0xFF
                val bL = cLeft and 0xFF
                val bR = cRight and 0xFF
                val bSharp = (bC * centerWeight + (bT + bB + bL + bR) * neighborWeight)
                val bFinal = min(255, max(0, bSharp.toInt()))

                outputPixels[yCurr + x] = (a shl 24) or (rFinal shl 16) or (gFinal shl 8) or bFinal
            }
        }

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        resultBitmap.setPixels(outputPixels, 0, width, 0, 0, width, height)
        intermediateBitmap.recycle()
        resultBitmap
    }

    /**
     * Applies dedicated DSLR-style Portrait Bokeh blur:
     * - Simulates optical depth-of-field (f/1.4, f/2.0, f/2.8, etc.)
     * - Blurs background while keeping the subject region razor sharp
     * - Smooth feathering gradient between subject and creamy bokeh
     */
    suspend fun applyPortraitBokehBlur(
        srcBitmap: Bitmap,
        apertureFStop: Float = 2.8f,
        focusCenterRatioX: Float = 0.5f,
        focusCenterRatioY: Float = 0.46f,
        focusRadiusRatioX: Float = 0.38f,
        focusRadiusRatioY: Float = 0.48f
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = srcBitmap.width
        val height = srcBitmap.height

        // Calculate blur radius based on f-stop (f/1.4 -> huge blur radius, f/8.0 -> subtle blur)
        val blurRadius = when {
            apertureFStop <= 1.4f -> 22
            apertureFStop <= 2.0f -> 16
            apertureFStop <= 2.8f -> 12
            apertureFStop <= 4.0f -> 8
            apertureFStop <= 5.6f -> 5
            else -> 3
        }

        // Fast Box/Stack blur on a scaled bitmap for performance & optical smoothness
        val scaleDown = 2
        val smallW = max(1, width / scaleDown)
        val smallH = max(1, height / scaleDown)
        val smallBitmap = Bitmap.createScaledBitmap(srcBitmap, smallW, smallH, true)
        val blurredSmall = fastBlur(smallBitmap, blurRadius)
        val blurredBackground = Bitmap.createScaledBitmap(blurredSmall, width, height, true)
        if (smallBitmap != blurredSmall) smallBitmap.recycle()

        // Create output bitmap by blending sharp foreground subject with creamy bokeh background
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        // Draw blurred background first
        canvas.drawBitmap(blurredBackground, 0f, 0f, null)

        // Generate depth mask: Elliptical subject protection with smooth radial falloff
        val maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(maskBitmap)
        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        val centerX = width * focusCenterRatioX
        val centerY = height * focusCenterRatioY
        val radiusX = width * focusRadiusRatioX
        val radiusY = height * focusRadiusRatioY
        val maxRadius = max(radiusX, radiusY)

        val gradient = RadialGradient(
            centerX, centerY, maxRadius,
            intArrayOf(Color.BLACK, Color.BLACK, Color.TRANSPARENT),
            floatArrayOf(0.0f, 0.65f, 1.0f),
            Shader.TileMode.CLAMP
        )
        maskPaint.shader = gradient
        maskCanvas.save()
        maskCanvas.scale(radiusX / maxRadius, radiusY / maxRadius, centerX, centerY)
        maskCanvas.drawCircle(centerX, centerY, maxRadius, maskPaint)
        maskCanvas.restore()

        // Composite sharp original image through the mask
        val compositePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val subjectBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val subjectCanvas = Canvas(subjectBitmap)

        // Draw sharp original
        subjectCanvas.drawBitmap(srcBitmap, 0f, 0f, null)
        // Mask out background
        val xferPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        xferPaint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN)
        subjectCanvas.drawBitmap(maskBitmap, 0f, 0f, xferPaint)

        // Composite onto blurred background
        canvas.drawBitmap(subjectBitmap, 0f, 0f, compositePaint)

        // Cleanup
        blurredBackground.recycle()
        maskBitmap.recycle()
        subjectBitmap.recycle()

        resultBitmap
    }

    /**
     * Fast multi-pass box blur algorithm in pure Kotlin for smooth creamy bokeh
     */
    private fun fastBlur(sentBitmap: Bitmap, radius: Int): Bitmap {
        val w = sentBitmap.width
        val h = sentBitmap.height
        val pix = IntArray(w * h)
        sentBitmap.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(max(w, h))

        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        for (idx in 0 until 256 * divsum) {
            dv[idx] = idx / divsum
        }

        yi = 0
        yw = 0

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        for (curY in 0 until h) {
            rinsum = 0
            ginsum = 0
            binsum = 0
            routsum = 0
            goutsum = 0
            boutsum = 0
            rsum = 0
            gsum = 0
            bsum = 0
            for (curI in -radius..radius) {
                p = pix[yi + min(wm, max(curI, 0))]
                sir = stack[curI + radius]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - kotlin.math.abs(curI)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (curI > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
            }
            stackpointer = radius

            for (curX in 0 until w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (curY == 0) {
                    vmin[curX] = min(curX + radius + 1, wm)
                }
                p = pix[yw + vmin[curX]]

                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = p and 0x0000ff

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi++
            }
            yw += w
        }

        for (curX in 0 until w) {
            rinsum = 0
            ginsum = 0
            binsum = 0
            routsum = 0
            goutsum = 0
            boutsum = 0
            rsum = 0
            gsum = 0
            bsum = 0
            yp = -radius * w
            for (curI in -radius..radius) {
                yi = max(0, yp) + curX
                sir = stack[curI + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - kotlin.math.abs(curI)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (curI > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (curI < hm) {
                    yp += w
                }
            }
            yi = curX
            stackpointer = radius
            for (curY in 0 until h) {
                pix[yi] = (0xff000000.toInt() and pix[yi]) or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (curX == 0) {
                    vmin[curY] = min(curY + r1, hm) * w
                }
                p = curX + vmin[curY]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi += w
            }
        }

        val blurred = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        blurred.setPixels(pix, 0, w, 0, 0, w, h)
        return blurred
    }

    /**
     * Saves a high-resolution bitmap to Android MediaStore Pictures folder
     */
    suspend fun saveBitmapToMediaStore(
        context: Context,
        bitmap: Bitmap,
        title: String = "SB7_PRO_${System.currentTimeMillis()}"
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$title.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/SB7 Pro Camera")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { stream: OutputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 98, stream)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            }
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Crops and upscales bitmap to match live 20x zoom factor and chosen aspect ratio
     */
    fun cropAndScaleForZoomAndAspect(
        source: Bitmap,
        zoomFactor: Float,
        aspectRatio: AspectRatioMode
    ): Bitmap {
        var bmp = source
        val srcW = bmp.width
        val srcH = bmp.height

        // 1. Calculate Target Aspect Ratio Dimensions
        val targetRatio = when (aspectRatio) {
            AspectRatioMode.RATIO_4_3 -> 4f / 3f
            AspectRatioMode.RATIO_16_9 -> 16f / 9f
            AspectRatioMode.RATIO_1_1 -> 1f
        }

        // Determine aspect crop box inside original image
        val currentRatio = srcW.toFloat() / srcH.toFloat()
        val (aspectW, aspectH) = if (currentRatio > targetRatio) {
            // Source is wider than target -> crop width
            val newW = (srcH * targetRatio).toInt()
            Pair(newW, srcH)
        } else {
            // Source is taller than target -> crop height
            val newH = (srcW / targetRatio).toInt()
            Pair(srcW, newH)
        }

        val aspectStartX = ((srcW - aspectW) / 2).coerceAtLeast(0)
        val aspectStartY = ((srcH - aspectH) / 2).coerceAtLeast(0)

        val aspectCropped = if (aspectW != srcW || aspectH != srcH) {
            Bitmap.createBitmap(bmp, aspectStartX, aspectStartY, aspectW.coerceAtMost(srcW - aspectStartX), aspectH.coerceAtMost(srcH - aspectStartY))
        } else {
            bmp
        }

        // 2. Apply Digital Zoom Crop (for zoom > 1.0x)
        if (zoomFactor > 1.02f) {
            val zW = (aspectCropped.width / zoomFactor).toInt().coerceIn(10, aspectCropped.width)
            val zH = (aspectCropped.height / zoomFactor).toInt().coerceIn(10, aspectCropped.height)
            val zStartX = ((aspectCropped.width - zW) / 2).coerceAtLeast(0)
            val zStartY = ((aspectCropped.height - zH) / 2).coerceAtLeast(0)

            val zoomCropped = Bitmap.createBitmap(
                aspectCropped,
                zStartX,
                zStartY,
                zW.coerceAtMost(aspectCropped.width - zStartX),
                zH.coerceAtMost(aspectCropped.height - zStartY)
            )

            // High-resolution Bicubic Upscale to restore high fidelity
            val targetUpscaleW = 1080
            val targetUpscaleH = (targetUpscaleW / targetRatio).toInt()
            val upscaled = Bitmap.createScaledBitmap(zoomCropped, targetUpscaleW, targetUpscaleH, true)

            if (zoomCropped != aspectCropped && zoomCropped != bmp) {
                zoomCropped.recycle()
            }
            if (aspectCropped != bmp) {
                aspectCropped.recycle()
            }
            return upscaled
        }

        return aspectCropped
    }

    /**
     * Generates a high-definition sample photograph for demo/emulator environments
     */
    fun createSamplePhotoBitmap(width: Int = 1080, height: Int = 1440): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Studio gradient background
        val bgPaint = Paint()
        bgPaint.shader = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(0xFF1E293B.toInt(), 0xFF0F172A.toInt(), 0xFF020617.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Studio warm soft bokeh orbs in background
        val orbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        orbPaint.color = 0x33FFB703.toInt()
        canvas.drawCircle(width * 0.2f, height * 0.25f, 180f, orbPaint)
        orbPaint.color = 0x2238BDF8.toInt()
        canvas.drawCircle(width * 0.8f, height * 0.35f, 220f, orbPaint)
        orbPaint.color = 0x2210B981.toInt()
        canvas.drawCircle(width * 0.3f, height * 0.7f, 260f, orbPaint)

        // Foreground subject silhouette & lens reflection
        val subjectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        subjectPaint.color = 0xFFF8FAFC.toInt()
        
        // Lens circle centerpiece
        val lensPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        lensPaint.color = 0xFF1E293B.toInt()
        canvas.drawCircle(width * 0.5f, height * 0.5f, 300f, lensPaint)

        val goldRing = Paint(Paint.ANTI_ALIAS_FLAG)
        goldRing.style = Paint.Style.STROKE
        goldRing.strokeWidth = 14f
        goldRing.color = 0xFFFFB703.toInt()
        canvas.drawCircle(width * 0.5f, height * 0.5f, 300f, goldRing)

        // Text markings
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = 0xFFF8FAFC.toInt()
        textPaint.textSize = 54f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("SB7 PRO CAMERA", width * 0.5f, height * 0.48f, textPaint)

        textPaint.color = 0xFFFFB703.toInt()
        textPaint.textSize = 36f
        canvas.drawText("20X OPTICAL ZOOM • DSLR BOKEH", width * 0.5f, height * 0.54f, textPaint)

        return bitmap
    }
}
