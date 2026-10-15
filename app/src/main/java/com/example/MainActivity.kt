package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.OrientationMode
import com.example.ui.LauncherScreen
import com.example.ui.LauncherViewModel
import com.example.ui.components.AppCustomizeDialog
import com.example.ui.screens.AppDrawerScreen
import com.example.ui.screens.HiddenVaultScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ScreensaverScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ZenithLauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LauncherViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val visibleApps by viewModel.visibleApps.collectAsState()
            val pinnedApps by viewModel.pinnedApps.collectAsState()
            val habitSuggestedApps by viewModel.habitSuggestedApps.collectAsState()
            val hiddenApps by viewModel.hiddenApps.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val selectedCategory by viewModel.selectedCategory.collectAsState()
            val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
            val appToCustomize by viewModel.appToCustomize.collectAsState()

            val context = LocalContext.current
            val accentColor = Color(settings.accentColor.hex)

            // Áp dụng định hướng màn hình động (Hỗ trợ ép ngang kiểu TV hoặc tự động thích ứng)
            LaunchedEffect(settings.orientationMode) {
                requestedOrientation = when (settings.orientationMode) {
                    OrientationMode.FORCE_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    OrientationMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    OrientationMode.AUTO -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }

            ZenithLauncherTheme(
                themeStyle = settings.themeStyle,
                accentOption = settings.accentColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            when (targetState) {
                                LauncherScreen.APP_DRAWER -> {
                                    (slideInVertically { it / 4 } + fadeIn()) togetherWith (slideOutVertically { -it / 4 } + fadeOut())
                                }
                                LauncherScreen.HOME -> {
                                    (slideInVertically { -it / 4 } + fadeIn()) togetherWith (slideOutVertically { it / 4 } + fadeOut())
                                }
                                else -> fadeIn() togetherWith fadeOut()
                            }
                        },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            LauncherScreen.HOME -> {
                                HomeScreen(
                                    settings = settings,
                                    pinnedApps = pinnedApps,
                                    habitSuggestedApps = habitSuggestedApps,
                                    allVisibleApps = visibleApps,
                                    onLaunchApp = { app -> viewModel.launchApp(context, app) },
                                    onAppLongClick = { app -> viewModel.setAppToCustomize(app) },
                                    onOpenDrawer = { viewModel.navigateTo(LauncherScreen.APP_DRAWER) },
                                    onOpenSettings = { viewModel.navigateTo(LauncherScreen.SETTINGS) },
                                    onOpenVault = { viewModel.navigateTo(LauncherScreen.HIDDEN_VAULT) },
                                    onOpenScreensaver = { viewModel.navigateTo(LauncherScreen.SCREENSAVER) },
                                    onSetDefaultLauncher = { viewModel.openDefaultLauncherSettings(context) },
                                    onSaveStickyNote = { note -> viewModel.updateStickyNote(note) },
                                    onSwitchWidget = { widget -> viewModel.setSmartWidget(widget) }
                                )
                            }

                            LauncherScreen.APP_DRAWER -> {
                                AppDrawerScreen(
                                    apps = visibleApps,
                                    searchQuery = searchQuery,
                                    selectedCategory = selectedCategory,
                                    accentColor = accentColor,
                                    iconShape = settings.iconShape,
                                    iconSize = settings.iconSize,
                                    showAppLabels = settings.showAppLabels,
                                    gridColumns = settings.gridColumns,
                                    onSearchChanged = { viewModel.setSearchQuery(it) },
                                    onCategorySelected = { viewModel.selectCategory(it) },
                                    onLaunchApp = { app -> viewModel.launchApp(context, app) },
                                    onAppLongClick = { app -> viewModel.setAppToCustomize(app) },
                                    onOpenVault = { viewModel.navigateTo(LauncherScreen.HIDDEN_VAULT) },
                                    onBack = { viewModel.navigateTo(LauncherScreen.HOME) }
                                )
                            }

                            LauncherScreen.HIDDEN_VAULT -> {
                                HiddenVaultScreen(
                                    isUnlocked = isVaultUnlocked,
                                    hiddenApps = hiddenApps,
                                    allApps = visibleApps + hiddenApps,
                                    accentColor = accentColor,
                                    iconShape = settings.iconShape,
                                    iconSize = settings.iconSize,
                                    showAppLabels = settings.showAppLabels,
                                    currentPin = settings.vaultPin,
                                    onBack = { viewModel.navigateTo(LauncherScreen.HOME) },
                                    onVerifyPin = { pin -> viewModel.verifyVaultPin(pin) },
                                    onLockVault = { viewModel.lockVault() },
                                    onLaunchApp = { app -> viewModel.launchApp(context, app) },
                                    onUnhideApp = { app -> viewModel.toggleHidden(app, false) },
                                    onHideApp = { app -> viewModel.toggleHidden(app, true) },
                                    onChangePin = { newPin -> viewModel.setVaultPin(newPin) }
                                )
                            }

                            LauncherScreen.SETTINGS -> {
                                SettingsScreen(
                                    settings = settings,
                                    onSettingsChanged = { viewModel.updateSettings(it) },
                                    onOpenVault = { viewModel.navigateTo(LauncherScreen.HIDDEN_VAULT) },
                                    onOpenScreensaver = { viewModel.navigateTo(LauncherScreen.SCREENSAVER) },
                                    onSetDefaultLauncher = { viewModel.openDefaultLauncherSettings(context) },
                                    onBack = { viewModel.navigateTo(LauncherScreen.HOME) }
                                )
                            }

                            LauncherScreen.SCREENSAVER -> {
                                ScreensaverScreen(
                                    settings = settings,
                                    accentColor = accentColor,
                                    onWakeUp = { viewModel.navigateTo(LauncherScreen.HOME) }
                                )
                            }
                        }
                    }

                    // Dialog tùy chỉnh & ngụy trang ứng dụng
                    appToCustomize?.let { app ->
                        AppCustomizeDialog(
                            app = app,
                            accentColor = accentColor,
                            onDismiss = { viewModel.setAppToCustomize(null) },
                            onSave = { newLabel, camo ->
                                viewModel.customizeApp(app, newLabel, camo)
                            },
                            onTogglePin = { viewModel.togglePin(app) },
                            onToggleHidden = { hide -> viewModel.toggleHidden(app, hide) },
                            onOpenAppInfo = { viewModel.openAppInfo(context, app) }
                        )
                    }
                }
            }
        }
    }
}
