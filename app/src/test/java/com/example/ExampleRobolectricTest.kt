package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.LauncherRepository
import com.example.data.model.CamouflageType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Zenith", appName)
  }

  @Test
  fun `test launcher repository app customization and camouflage`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = LauncherRepository(context)
    repository.loadApps()

    val apps = repository.apps.value
    assertTrue("Apps list should not be empty", apps.isNotEmpty())

    val firstApp = apps.first()
    repository.updateAppCustomization(firstApp.packageName, "Ngụy trang", CamouflageType.CALCULATOR)

    val updatedApps = repository.apps.value
    val customizedApp = updatedApps.first { it.packageName == firstApp.packageName }
    assertEquals("Ngụy trang", customizedApp.displayLabel)
    assertEquals(CamouflageType.CALCULATOR, customizedApp.camouflageType)

    // Test ẩn vào két
    repository.toggleAppHidden(firstApp.packageName, true)
    val hiddenApps = repository.apps.value
    assertTrue(hiddenApps.first { it.packageName == firstApp.packageName }.isHidden)

    // Test cập nhật hình nền và chế độ TV
    val defaultSettings = repository.settings.value
    assertEquals(com.example.data.model.WallpaperOption.TV_CINEMATIC, defaultSettings.wallpaper)

    repository.updateSettings(defaultSettings.copy(
        wallpaper = com.example.data.model.WallpaperOption.CUSTOM_IMAGE,
        customWallpaperPath = "/data/user/0/com.example/files/custom.jpg",
        orientationMode = com.example.data.model.OrientationMode.FORCE_LANDSCAPE
    ))
    val updatedSettings = repository.settings.value
    assertEquals(com.example.data.model.WallpaperOption.CUSTOM_IMAGE, updatedSettings.wallpaper)
    assertEquals("/data/user/0/com.example/files/custom.jpg", updatedSettings.customWallpaperPath)
    assertEquals(com.example.data.model.OrientationMode.FORCE_LANDSCAPE, updatedSettings.orientationMode)
  }
}
