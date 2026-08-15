package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.camera.processing.ImageProcessingPipeline
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SB7 Pro Camera", appName)
  }

  @Test
  fun `test dslr post processing pipeline`() = runBlocking {
    val sample = ImageProcessingPipeline.createSamplePhotoBitmap(300, 400)
    assertNotNull(sample)
    val enhanced = ImageProcessingPipeline.applyDslrEnhancement(sample)
    assertNotNull(enhanced)
    assertEquals(sample.width, enhanced.width)
    assertEquals(sample.height, enhanced.height)
  }

  @Test
  fun `test portrait bokeh blur processing`() = runBlocking {
    val sample = ImageProcessingPipeline.createSamplePhotoBitmap(300, 400)
    val bokeh = ImageProcessingPipeline.applyPortraitBokehBlur(sample, apertureFStop = 1.4f)
    assertNotNull(bokeh)
    assertEquals(sample.width, bokeh.width)
    assertEquals(sample.height, bokeh.height)
  }

  @Test
  fun `test 20x zoom and aspect ratio cropping`() {
    val sample = ImageProcessingPipeline.createSamplePhotoBitmap(600, 800)
    val croppedZoom = ImageProcessingPipeline.cropAndScaleForZoomAndAspect(
      source = sample,
      zoomFactor = 20.0f,
      aspectRatio = com.example.camera.model.AspectRatioMode.RATIO_1_1
    )
    assertNotNull(croppedZoom)
    assertEquals(croppedZoom.width, croppedZoom.height)
  }
}
