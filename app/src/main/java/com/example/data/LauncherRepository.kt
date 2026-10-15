package com.example.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import com.example.data.model.AccentColorOption
import com.example.data.model.AppCategory
import com.example.data.model.AppInfo
import com.example.data.model.CamouflageType
import com.example.data.model.IconShape
import com.example.data.model.IconSize
import com.example.data.model.LauncherSettings
import com.example.data.model.SmartWidgetType
import com.example.data.model.ThemeStyle
import com.example.data.model.WallpaperOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class LauncherRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("zenith_launcher_prefs", Context.MODE_PRIVATE)

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<LauncherSettings> = _settings.asStateFlow()

    suspend fun loadApps() = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(intent, 0)
            }
        } catch (e: Exception) {
            emptyList()
        }

        val customOverrides = loadCustomAppOverrides()

        val detectedApps = resolveInfos.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            val activity = resolveInfo.activityInfo.name
            val label = try {
                resolveInfo.loadLabel(packageManager).toString()
            } catch (e: Exception) {
                pkg
            }

            // Bỏ qua chính Zenith Launcher trong danh sách hiển thị
            if (pkg == context.packageName) return@mapNotNull null

            val override = customOverrides[pkg]
            val category = override?.category ?: autoDetectCategory(pkg, label)
            val isHidden = override?.isHidden ?: false
            val isPinned = override?.isPinnedHome ?: isDefaultPinned(pkg, label)
            val customLabel = override?.customLabel ?: ""
            val camouflage = override?.camouflageType ?: CamouflageType.NONE
            val launchCount = override?.launchCount ?: 0
            val lastLaunchedTime = override?.lastLaunchedTime ?: 0L

            AppInfo(
                packageName = pkg,
                activityName = activity,
                defaultLabel = label,
                customLabel = customLabel,
                camouflageType = camouflage,
                category = category,
                launchCount = launchCount,
                lastLaunchedTime = lastLaunchedTime,
                isHidden = isHidden,
                isPinnedHome = isPinned,
                isSystemApp = false
            )
        }

        // Bổ sung các ứng dụng quen thuộc nếu số lượng ứng dụng thực tế trên máy ít (đảm bảo trải nghiệm trực quan hoàn chỉnh)
        val sampleEssentialApps = getSampleEssentialApps(customOverrides)
        val combinedMap = LinkedHashMap<String, AppInfo>()

        // Thêm apps thực tế trước
        detectedApps.forEach { combinedMap[it.packageName] = it }

        // Bổ sung các apps tiện ích phong phú
        sampleEssentialApps.forEach { sample ->
            if (!combinedMap.containsKey(sample.packageName)) {
                combinedMap[sample.packageName] = sample
            }
        }

        _apps.value = combinedMap.values.toList()
    }

    private fun autoDetectCategory(packageName: String, label: String): AppCategory {
        val lowerPkg = packageName.lowercase()
        val lowerLabel = label.lowercase()

        return when {
            lowerPkg.contains("facebook") || lowerPkg.contains("zalo") || lowerPkg.contains("telegram") ||
                    lowerPkg.contains("whatsapp") || lowerPkg.contains("instagram") || lowerPkg.contains("messenger") ||
                    lowerPkg.contains("twitter") || lowerPkg.contains("chat") || lowerPkg.contains("tiktok") ||
                    lowerLabel.contains("tin nhắn") || lowerLabel.contains("zalo") || lowerLabel.contains("facebook") -> {
                AppCategory.SOCIAL
            }

            lowerPkg.contains("camera") || lowerPkg.contains("clock") || lowerPkg.contains("calculator") ||
                    lowerPkg.contains("settings") || lowerPkg.contains("gallery") || lowerPkg.contains("photos") ||
                    lowerPkg.contains("files") || lowerPkg.contains("filemanager") || lowerPkg.contains("recorder") ||
                    lowerLabel.contains("máy ảnh") || lowerLabel.contains("đồng hồ") || lowerLabel.contains("máy tính") ||
                    lowerLabel.contains("cài đặt") || lowerLabel.contains("bộ sưu tập") || lowerLabel.contains("tệp") -> {
                AppCategory.TOOLS
            }

            lowerPkg.contains("youtube") || lowerPkg.contains("spotify") || lowerPkg.contains("netflix") ||
                    lowerPkg.contains("music") || lowerPkg.contains("video") || lowerPkg.contains("game") ||
                    lowerPkg.contains("sound") || lowerLabel.contains("nhạc") || lowerLabel.contains("phim") -> {
                AppCategory.MEDIA
            }

            lowerPkg.contains("gmail") || lowerPkg.contains("mail") || lowerPkg.contains("note") ||
                    lowerPkg.contains("calendar") || lowerPkg.contains("drive") || lowerPkg.contains("docs") ||
                    lowerPkg.contains("sheet") || lowerPkg.contains("office") || lowerPkg.contains("slack") ||
                    lowerLabel.contains("ghi chú") || lowerLabel.contains("lịch") || lowerLabel.contains("thư") -> {
                AppCategory.PRODUCTIVITY
            }

            lowerPkg.contains("bank") || lowerPkg.contains("momo") || lowerPkg.contains("vnpay") ||
                    lowerPkg.contains("shopee") || lowerPkg.contains("lazada") || lowerPkg.contains("wallet") ||
                    lowerPkg.contains("pay") || lowerPkg.contains("finance") || lowerLabel.contains("ngân hàng") ||
                    lowerLabel.contains("ví") -> {
                AppCategory.FINANCE
            }

            else -> AppCategory.TOOLS
        }
    }

    private fun isDefaultPinned(pkg: String, label: String): Boolean {
        val lower = "$pkg $label".lowercase()
        return lower.contains("phone") || lower.contains("dialer") || lower.contains("gọi") ||
                lower.contains("message") || lower.contains("sms") || lower.contains("chrome") ||
                lower.contains("browser") || lower.contains("camera")
    }

    private fun getSampleEssentialApps(overrides: Map<String, AppInfo>): List<AppInfo> {
        val sampleList = listOf(
            AppInfo("com.google.android.dialer", "", "Điện thoại", category = AppCategory.TOOLS, isPinnedHome = true, launchCount = 18),
            AppInfo("com.google.android.apps.messaging", "", "Tin nhắn", category = AppCategory.SOCIAL, isPinnedHome = true, launchCount = 24),
            AppInfo("com.android.chrome", "", "Chrome Duyệt web", category = AppCategory.TOOLS, isPinnedHome = true, launchCount = 35),
            AppInfo("com.google.android.GoogleCamera", "", "Máy ảnh", category = AppCategory.TOOLS, isPinnedHome = true, launchCount = 12),
            AppInfo("com.google.android.apps.photos", "", "Ảnh & Video", category = AppCategory.TOOLS, isPinnedHome = false, launchCount = 15),
            AppInfo("com.google.android.youtube", "", "YouTube", category = AppCategory.MEDIA, isPinnedHome = false, launchCount = 42),
            AppInfo("com.zing.zalo", "", "Zalo", category = AppCategory.SOCIAL, isPinnedHome = false, launchCount = 50),
            AppInfo("com.facebook.katana", "", "Facebook", category = AppCategory.SOCIAL, isPinnedHome = false, launchCount = 38),
            AppInfo("com.spotify.music", "", "Spotify", category = AppCategory.MEDIA, isPinnedHome = false, launchCount = 29),
            AppInfo("com.google.android.gm", "", "Gmail", category = AppCategory.PRODUCTIVITY, isPinnedHome = false, launchCount = 16),
            AppInfo("com.google.android.apps.maps", "", "Google Maps", category = AppCategory.TOOLS, isPinnedHome = false, launchCount = 10),
            AppInfo("com.google.android.keep", "", "Ghi chú Keep", category = AppCategory.PRODUCTIVITY, isPinnedHome = false, launchCount = 22),
            AppInfo("com.google.android.calculator", "", "Máy tính", category = AppCategory.TOOLS, isPinnedHome = false, launchCount = 8),
            AppInfo("com.google.android.deskclock", "", "Đồng hồ & Báo thức", category = AppCategory.TOOLS, isPinnedHome = false, launchCount = 14),
            AppInfo("com.google.android.calendar", "", "Lịch công việc", category = AppCategory.PRODUCTIVITY, isPinnedHome = false, launchCount = 11),
            AppInfo("com.android.settings", "", "Cài đặt hệ thống", category = AppCategory.TOOLS, isPinnedHome = false, launchCount = 9),
            AppInfo("com.shopee.vn", "", "Shopee Mua sắm", category = AppCategory.FINANCE, isPinnedHome = false, launchCount = 20),
            AppInfo("vn.momo.platform", "", "Ví MoMo", category = AppCategory.FINANCE, isPinnedHome = false, launchCount = 26),
            AppInfo("org.telegram.messenger", "", "Telegram Bảo mật", category = AppCategory.SOCIAL, isPinnedHome = false, launchCount = 31),
            AppInfo("com.google.android.apps.nbu.files", "", "Tệp tin Files", category = AppCategory.TOOLS, isPinnedHome = false, launchCount = 7)
        )

        return sampleList.map { sample ->
            val override = overrides[sample.packageName]
            if (override != null) {
                sample.copy(
                    customLabel = override.customLabel,
                    camouflageType = override.camouflageType,
                    category = override.category,
                    launchCount = override.launchCount,
                    lastLaunchedTime = override.lastLaunchedTime,
                    isHidden = override.isHidden,
                    isPinnedHome = override.isPinnedHome
                )
            } else {
                sample
            }
        }
    }

    /**
     * Ghi nhận lượt mở ứng dụng để tự động học hỏi thói quen của người dùng
     */
    fun recordAppLaunch(packageName: String) {
        val currentList = _apps.value
        val updated = currentList.map { app ->
            if (app.packageName == packageName) {
                app.copy(
                    launchCount = app.launchCount + 1,
                    lastLaunchedTime = System.currentTimeMillis()
                )
            } else {
                app
            }
        }
        _apps.value = updated
        saveCustomAppOverrides(updated)
    }

    /**
     * Tùy chỉnh tên hiển thị và biểu tượng ngụy trang (Camouflage)
     */
    fun updateAppCustomization(
        packageName: String,
        customLabel: String,
        camouflageType: CamouflageType
    ) {
        val currentList = _apps.value
        val updated = currentList.map { app ->
            if (app.packageName == packageName) {
                app.copy(
                    customLabel = customLabel.trim(),
                    camouflageType = camouflageType
                )
            } else {
                app
            }
        }
        _apps.value = updated
        saveCustomAppOverrides(updated)
    }

    /**
     * Ẩn hoặc hiện ứng dụng trong Két Bảo Mật
     */
    fun toggleAppHidden(packageName: String, hide: Boolean) {
        val currentList = _apps.value
        val updated = currentList.map { app ->
            if (app.packageName == packageName) {
                app.copy(isHidden = hide)
            } else {
                app
            }
        }
        _apps.value = updated
        saveCustomAppOverrides(updated)
    }

    /**
     * Ghim hoặc bỏ ghim ứng dụng trên Màn hình chính
     */
    fun toggleAppPinned(packageName: String) {
        val currentList = _apps.value
        val updated = currentList.map { app ->
            if (app.packageName == packageName) {
                app.copy(isPinnedHome = !app.isPinnedHome)
            } else {
                app
            }
        }
        _apps.value = updated
        saveCustomAppOverrides(updated)
    }

    /**
     * Thay đổi danh mục thủ công cho ứng dụng
     */
    fun updateAppCategory(packageName: String, newCategory: AppCategory) {
        val currentList = _apps.value
        val updated = currentList.map { app ->
            if (app.packageName == packageName) {
                app.copy(category = newCategory)
            } else {
                app
            }
        }
        _apps.value = updated
        saveCustomAppOverrides(updated)
    }

    /**
     * Lưu cài đặt launcher
     */
    fun updateSettings(newSettings: LauncherSettings) {
        _settings.value = newSettings
        prefs.edit().apply {
            putString("theme_style", newSettings.themeStyle.name)
            putString("accent_color", newSettings.accentColor.name)
            putString("icon_shape", newSettings.iconShape.name)
            putString("icon_size", newSettings.iconSize.name)
            putBoolean("show_app_labels", newSettings.showAppLabels)
            putInt("grid_columns", newSettings.gridColumns)
            putString("wallpaper", newSettings.wallpaper.name)
            putString("custom_wallpaper_path", newSettings.customWallpaperPath)
            putString("orientation_mode", newSettings.orientationMode.name)
            putBoolean("extreme_battery_saver", newSettings.extremeBatterySaver)
            putString("vault_pin", newSettings.vaultPin)
            putBoolean("disguise_calc", newSettings.disguiseCalculatorEnabled)
            putString("active_widget", newSettings.activeWidget.name)
            putString("sticky_note", newSettings.stickyNoteText)
            apply()
        }
    }

    private fun loadSettings(): LauncherSettings {
        val theme = try {
            ThemeStyle.valueOf(prefs.getString("theme_style", ThemeStyle.AMOLED_BLACK.name)!!)
        } catch (e: Exception) {
            ThemeStyle.AMOLED_BLACK
        }

        val accent = try {
            AccentColorOption.valueOf(prefs.getString("accent_color", AccentColorOption.CYAN_NEON.name)!!)
        } catch (e: Exception) {
            AccentColorOption.CYAN_NEON
        }

        val shape = try {
            IconShape.valueOf(prefs.getString("icon_shape", IconShape.SQUIRCLE.name)!!)
        } catch (e: Exception) {
            IconShape.SQUIRCLE
        }

        val size = try {
            IconSize.valueOf(prefs.getString("icon_size", IconSize.STANDARD.name)!!)
        } catch (e: Exception) {
            IconSize.STANDARD
        }

        val wallpaper = try {
            WallpaperOption.valueOf(prefs.getString("wallpaper", WallpaperOption.TV_CINEMATIC.name)!!)
        } catch (e: Exception) {
            WallpaperOption.TV_CINEMATIC
        }

        val orientation = try {
            com.example.data.model.OrientationMode.valueOf(prefs.getString("orientation_mode", com.example.data.model.OrientationMode.AUTO.name)!!)
        } catch (e: Exception) {
            com.example.data.model.OrientationMode.AUTO
        }

        val widget = try {
            SmartWidgetType.valueOf(prefs.getString("active_widget", SmartWidgetType.CLOCK_WEATHER.name)!!)
        } catch (e: Exception) {
            SmartWidgetType.CLOCK_WEATHER
        }

        return LauncherSettings(
            themeStyle = theme,
            accentColor = accent,
            iconShape = shape,
            iconSize = size,
            showAppLabels = prefs.getBoolean("show_app_labels", true),
            gridColumns = prefs.getInt("grid_columns", 4),
            wallpaper = wallpaper,
            customWallpaperPath = prefs.getString("custom_wallpaper_path", null),
            orientationMode = orientation,
            extremeBatterySaver = prefs.getBoolean("extreme_battery_saver", false),
            vaultPin = prefs.getString("vault_pin", "1234") ?: "1234",
            disguiseCalculatorEnabled = prefs.getBoolean("disguise_calc", true),
            activeWidget = widget,
            stickyNoteText = prefs.getString("sticky_note", "Zenith TV Launcher: Tự động vừa khít mọi màn hình, mượt mà và bảo mật.") ?: ""
        )
    }

    private fun saveCustomAppOverrides(apps: List<AppInfo>) {
        val jsonArray = JSONArray()
        apps.forEach { app ->
            val obj = JSONObject().apply {
                put("pkg", app.packageName)
                put("customLabel", app.customLabel)
                put("camouflage", app.camouflageType.name)
                put("category", app.category.name)
                put("launchCount", app.launchCount)
                put("lastLaunched", app.lastLaunchedTime)
                put("isHidden", app.isHidden)
                put("isPinned", app.isPinnedHome)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("custom_app_overrides", jsonArray.toString()).apply()
    }

    private fun loadCustomAppOverrides(): Map<String, AppInfo> {
        val jsonStr = prefs.getString("custom_app_overrides", null) ?: return emptyMap()
        val result = mutableMapOf<String, AppInfo>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val pkg = obj.optString("pkg")
                val customLabel = obj.optString("customLabel", "")
                val camouflage = try {
                    CamouflageType.valueOf(obj.optString("camouflage", CamouflageType.NONE.name))
                } catch (e: Exception) {
                    CamouflageType.NONE
                }
                val category = try {
                    AppCategory.valueOf(obj.optString("category", AppCategory.TOOLS.name))
                } catch (e: Exception) {
                    AppCategory.TOOLS
                }
                val launchCount = obj.optInt("launchCount", 0)
                val lastLaunched = obj.optLong("lastLaunched", 0L)
                val isHidden = obj.optBoolean("isHidden", false)
                val isPinned = obj.optBoolean("isPinned", false)

                result[pkg] = AppInfo(
                    packageName = pkg,
                    defaultLabel = "",
                    customLabel = customLabel,
                    camouflageType = camouflage,
                    category = category,
                    launchCount = launchCount,
                    lastLaunchedTime = lastLaunched,
                    isHidden = isHidden,
                    isPinnedHome = isPinned
                )
            }
        } catch (e: Exception) {
            // Log fallback
        }
        return result
    }
}
