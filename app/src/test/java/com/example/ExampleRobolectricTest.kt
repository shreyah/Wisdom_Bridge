package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ActivityScenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Diagnostic rerun trigger (cached rerun force)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `debug asset files v3`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val assetManager = context.assets
    val assetsToTest = listOf(
      "hero-learn.jpg",
      "topics/yoga.png",
      "topics/maths.jpg"
    )
    // SYSTEM FILE SCANNER
    println("=== STARTING IMAGE SCAN ===")
    val drawableDir = java.io.File("./src/main/res/drawable")
    if (drawableDir.exists()) {
        drawableDir.listFiles()?.forEach { file ->
            println("DRAWABLE_FILE: ${file.name} size=${file.length()}")
        }
    }
    println("=== END IMAGE SCAN ===")
    assetsToTest.forEach { assetName ->
      try {
        assetManager.open(assetName).use { inputStream ->
          val bytes = inputStream.readBytes()
          println("ASSET DEBUG: File '$assetName' exists, size is ${bytes.size} bytes.")
          if (bytes.isEmpty()) {
            println("ASSET DEBUG: WARNING! File is empty!")
          } else {
            val decoded = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (decoded != null) {
              println("ASSET DEBUG: Successfully decoded image '$assetName' (width=${decoded.width}, height=${decoded.height})")
            } else {
              println("ASSET DEBUG: ERROR! Failed to decode image '$assetName'")
            }
          }
        }
      } catch (e: Exception) {
        println("ASSET DEBUG: EXCEPTION for '$assetName':")
        e.printStackTrace()
      }
    }
  }

  @Test
  fun `launch MainActivity`() {
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    scenario.onActivity { activity ->
      assertNotNull(activity)
    }
  }
}
