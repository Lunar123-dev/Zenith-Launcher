package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LauncherRepository
import com.example.data.model.AppCategory
import com.example.data.model.AppInfo
import com.example.data.model.CamouflageType
import com.example.data.model.LauncherSettings
import com.example.data.model.SmartWidgetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LauncherScreen {
    HOME,
    APP_DRAWER,
    HIDDEN_VAULT,
    SETTINGS,
    SCREENSAVER
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LauncherRepository(application.applicationContext)

    val settings: StateFlow<LauncherSettings> = repository.settings

    private val _currentScreen = MutableStateFlow(LauncherScreen.HOME)
    val currentScreen: StateFlow<LauncherScreen> = _currentScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(AppCategory.ALL)
    val selectedCategory: StateFlow<AppCategory> = _selectedCategory.asStateFlow()

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _appToCustomize = MutableStateFlow<AppInfo?>(null)
    val appToCustomize: StateFlow<AppInfo?> = _appToCustomize.asStateFlow()

    private val _allApps = repository.apps

    // Các ứng dụng hiển thị trong App Drawer (loại bỏ ứng dụng bị ẩn trong Két)
    val visibleApps: StateFlow<List<AppInfo>> = combine(
        _allApps,
        _searchQuery,
        _selectedCategory
    ) { apps, query, category ->
        val unhidden = apps.filter { !it.isHidden }
        val filteredByCategory = when (category) {
            AppCategory.ALL -> unhidden.sortedBy { it.displayLabel.lowercase() }
            AppCategory.FREQUENT -> unhidden.sortedByDescending { it.launchCount }
            else -> unhidden.filter { it.category == category }.sortedBy { it.displayLabel.lowercase() }
        }

        if (query.isBlank()) {
            filteredByCategory
        } else {
            val q = query.trim().lowercase()
            unhidden.filter {
                it.displayLabel.lowercase().contains(q) ||
                        it.defaultLabel.lowercase().contains(q) ||
                        it.packageName.lowercase().contains(q)
            }.sortedBy { it.displayLabel.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ứng dụng ghim trên Màn hình chính (Home Screen)
    val pinnedApps: StateFlow<List<AppInfo>> = _allApps.combine(settings) { apps, _ ->
        apps.filter { it.isPinnedHome && !it.isHidden }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ứng dụng gợi ý thông minh dựa trên thói quen (top 5 ứng dụng mở nhiều nhất)
    val habitSuggestedApps: StateFlow<List<AppInfo>> = _allApps.combine(settings) { apps, _ ->
        apps.filter { !it.isHidden }
            .sortedByDescending { it.launchCount }
            .take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ứng dụng trong Két Bảo Mật
    val hiddenApps: StateFlow<List<AppInfo>> = _allApps.combine(_isVaultUnlocked) { apps, _ ->
        apps.filter { it.isHidden }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.loadApps()
        }
    }

    fun navigateTo(screen: LauncherScreen) {
        _currentScreen.value = screen
        if (screen != LauncherScreen.HIDDEN_VAULT && screen != LauncherScreen.APP_DRAWER) {
            _searchQuery.value = ""
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: AppCategory) {
        _selectedCategory.value = category
    }

    fun setAppToCustomize(app: AppInfo?) {
        _appToCustomize.value = app
    }

    fun launchApp(context: Context, app: AppInfo) {
        // Ghi nhận thói quen người dùng
        repository.recordAppLaunch(app.packageName)

        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(launchIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "Không thể mở: ${app.displayLabel}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Trường hợp ứng dụng mẫu hoặc không có launcher intent
            Toast.makeText(
                context,
                "Đang mở ${app.displayLabel} (Chế độ phản hồi nhanh Zenith)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val tvInputSources = repository.tvInputSources

    fun togglePin(app: AppInfo) {
        repository.toggleAppPinned(app.packageName)
    }

    fun setAppPinned(app: AppInfo, isPinned: Boolean) {
        repository.setAppPinned(app.packageName, isPinned)
    }

    fun setActiveTvInput(inputId: String, context: Context? = null) {
        updateSettings(settings.value.copy(activeTvInputId = inputId))
        if (context != null) {
            val input = tvInputSources.firstOrNull { it.id == inputId }
            Toast.makeText(context, "Đã chuyển sang cổng: ${input?.displayLabel ?: inputId}", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleHidden(app: AppInfo, hide: Boolean) {
        repository.toggleAppHidden(app.packageName, hide)
    }

    fun customizeApp(app: AppInfo, newLabel: String, camouflageType: CamouflageType, isPinned: Boolean? = null) {
        repository.updateAppCustomization(app.packageName, newLabel, camouflageType, isPinned)
        _appToCustomize.value = null
    }

    fun updateSettings(newSettings: LauncherSettings) {
        repository.updateSettings(newSettings)
    }

    fun verifyVaultPin(enteredPin: String): Boolean {
        val correct = (enteredPin == settings.value.vaultPin)
        if (correct) {
            _isVaultUnlocked.value = true
        }
        return correct
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    fun setVaultPin(newPin: String) {
        if (newPin.length == 4) {
            updateSettings(settings.value.copy(vaultPin = newPin))
        }
    }

    fun updateStickyNote(text: String) {
        updateSettings(settings.value.copy(stickyNoteText = text))
    }

    fun setSmartWidget(widget: SmartWidgetType) {
        updateSettings(settings.value.copy(activeWidget = widget))
    }

    fun setCustomWallpaper(path: String) {
        updateSettings(settings.value.copy(
            wallpaper = com.example.data.model.WallpaperOption.CUSTOM_IMAGE,
            customWallpaperPath = path
        ))
    }

    fun openDefaultLauncherSettings(context: Context) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(context, "Vui lòng vào Cài đặt > Ứng dụng mặc định > Màn hình chính", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun openAppInfo(context: Context, app: AppInfo) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${app.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Không thể xem thông tin ${app.displayLabel}", Toast.LENGTH_SHORT).show()
        }
    }
}
