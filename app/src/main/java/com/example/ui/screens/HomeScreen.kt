package com.example.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.bluetooth.BluetoothAdapter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Cable
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.StayCurrentLandscape
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppCategory
import com.example.data.model.AppInfo
import com.example.data.model.DeviceRecord
import com.example.data.model.IconShape
import com.example.data.model.IconSize
import com.example.data.model.InfoPlacement
import com.example.data.model.LauncherSettings
import com.example.data.model.OrientationMode
import com.example.data.model.SmartWidgetType
import com.example.data.model.TvInputSource
import com.example.data.model.TvInputType
import com.example.data.model.WallpaperOption
import com.example.ui.components.AppIconItem
import com.example.ui.components.SmartWidgetContainer
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    settings: LauncherSettings,
    pinnedApps: List<AppInfo>,
    habitSuggestedApps: List<AppInfo>,
    allVisibleApps: List<AppInfo> = emptyList(),
    onLaunchApp: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenScreensaver: () -> Unit,
    onSetDefaultLauncher: () -> Unit,
    onSaveStickyNote: (String) -> Unit,
    onSwitchWidget: (SmartWidgetType) -> Unit,
    onOpenFamilyMonitor: () -> Unit = {},
    familyDevices: List<DeviceRecord> = emptyList(),
    currentDevice: DeviceRecord? = null,
    familyName: String = "Gia đình của tôi",
    userEmail: String? = null,
    tvInputSources: List<TvInputSource> = emptyList(),
    onSelectTvInput: (String) -> Unit = {},
    onSetAppPinned: (AppInfo, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val accentColor = Color(settings.accentColor.hex)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_root")
    ) {
        val isLandscape = (maxWidth > maxHeight) || (settings.orientationMode == OrientationMode.FORCE_LANDSCAPE)

        // Hình nền hiển thị (Tự động thích ứng, hỗ trợ ảnh tùy chỉnh từ thư viện)
        WallpaperBackground(
            wallpaper = settings.wallpaper,
            customWallpaperPath = settings.customWallpaperPath
        )

        // Lớp phủ tối nhẹ để tăng độ tương phản của các thẻ TV
        if (settings.wallpaper != WallpaperOption.AMOLED_BLACK && !settings.extremeBatterySaver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
            )
        }

        if (isLandscape) {
            // Giao diện ngang chuẩn TV Leanback Launcher
            TvLandscapeHomeScreen(
                settings = settings,
                pinnedApps = pinnedApps,
                habitSuggestedApps = habitSuggestedApps,
                allVisibleApps = allVisibleApps,
                accentColor = accentColor,
                onLaunchApp = onLaunchApp,
                onAppLongClick = onAppLongClick,
                onOpenDrawer = onOpenDrawer,
                onOpenSettings = onOpenSettings,
                onOpenVault = onOpenVault,
                onOpenScreensaver = onOpenScreensaver,
                onSetDefaultLauncher = onSetDefaultLauncher,
                onSaveStickyNote = onSaveStickyNote,
                onSwitchWidget = onSwitchWidget,
                onOpenFamilyMonitor = onOpenFamilyMonitor,
                familyDevices = familyDevices,
                currentDevice = currentDevice,
                familyName = familyName,
                userEmail = userEmail,
                tvInputSources = tvInputSources,
                onSelectTvInput = onSelectTvInput,
                onSetAppPinned = onSetAppPinned
            )
        } else {
            // Giao diện dọc thích ứng điện thoại, có nút chuyển nhanh sang TV Mode
            PortraitHomeScreen(
                settings = settings,
                pinnedApps = pinnedApps,
                habitSuggestedApps = habitSuggestedApps,
                allVisibleApps = allVisibleApps,
                accentColor = accentColor,
                onLaunchApp = onLaunchApp,
                onAppLongClick = onAppLongClick,
                onOpenDrawer = onOpenDrawer,
                onOpenSettings = onOpenSettings,
                onOpenVault = onOpenVault,
                onOpenScreensaver = onOpenScreensaver,
                onSetDefaultLauncher = onSetDefaultLauncher,
                onSaveStickyNote = onSaveStickyNote,
                onSwitchWidget = onSwitchWidget,
                onOpenFamilyMonitor = onOpenFamilyMonitor,
                familyDevices = familyDevices,
                currentDevice = currentDevice,
                familyName = familyName,
                userEmail = userEmail,
                tvInputSources = tvInputSources,
                onSelectTvInput = onSelectTvInput,
                onSetAppPinned = onSetAppPinned
            )
        }
    }
}

/**
 * Giao diện Ngang Chuẩn TV (Zenith TV Leanback Launcher)
 */
@Composable
fun TvLandscapeHomeScreen(
    settings: LauncherSettings,
    pinnedApps: List<AppInfo>,
    habitSuggestedApps: List<AppInfo>,
    allVisibleApps: List<AppInfo>,
    accentColor: Color,
    onLaunchApp: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenScreensaver: () -> Unit,
    onSetDefaultLauncher: () -> Unit,
    onSaveStickyNote: (String) -> Unit,
    onSwitchWidget: (SmartWidgetType) -> Unit,
    onOpenFamilyMonitor: () -> Unit = {},
    familyDevices: List<DeviceRecord> = emptyList(),
    currentDevice: DeviceRecord? = null,
    familyName: String = "Gia đình của tôi",
    userEmail: String? = null,
    tvInputSources: List<TvInputSource> = emptyList(),
    onSelectTvInput: (String) -> Unit = {},
    onSetAppPinned: (AppInfo, Boolean) -> Unit = { _, _ -> }
) {
    var selectedChannelCategory by remember { mutableStateOf(AppCategory.ALL) }
    var showPinPicker by remember { mutableStateOf(false) }
    var showInputPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // 1. Thanh tiêu đề TV thông minh (TV Top Header Bar)
        TvTopHeader(
            settings = settings,
            accentColor = accentColor,
            onOpenDrawer = onOpenDrawer,
            onOpenSettings = onOpenSettings,
            onOpenVault = onOpenVault,
            onOpenScreensaver = onOpenScreensaver,
            onSetDefaultLauncher = onSetDefaultLauncher,
            onOpenFamilyMonitor = onOpenFamilyMonitor,
            familyDevicesCount = familyDevices.size,
            activeTvInput = tvInputSources.firstOrNull { it.id == settings.activeTvInputId }?.displayLabel ?: "HDMI 1",
            onOpenInputPicker = { showInputPicker = true }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Nội dung cuộn các dải kênh TV (TV Shelves / Rails)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Widget Hero Row nếu được cấu hình hiển thị
            if (settings.infoPlacement == InfoPlacement.HERO_WIDGET_ROW) {
                item {
                    SmartWidgetContainer(
                        activeWidget = settings.activeWidget,
                        accentColor = accentColor,
                        stickyNote = settings.stickyNoteText,
                        onSaveStickyNote = onSaveStickyNote,
                        onSwitchWidget = onSwitchWidget,
                        onClockSecretTrigger = onOpenVault,
                        familyDevices = familyDevices,
                        currentDevice = currentDevice,
                        familyName = familyName,
                        userEmail = userEmail,
                        onOpenFamilyMonitor = onOpenFamilyMonitor,
                        tvInputSources = tvInputSources,
                        activeTvInputId = settings.activeTvInputId,
                        onSelectTvInput = onSelectTvInput
                    )
                }
            }

            // Kênh 1: Kênh yêu thích đã ghim (Pinned Favorites Shelf) kèm nút Thêm Ghim
            item {
                TvShelfSection(
                    title = "KÊNH ỨNG DỤNG YÊU THÍCH (FAVORITES)",
                    icon = Icons.Rounded.Home,
                    accentColor = accentColor
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(pinnedApps, key = { it.packageName }) { app ->
                            TvAppCard(
                                app = app,
                                accentColor = accentColor,
                                iconShape = settings.iconShape,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { onAppLongClick(app) },
                                iconPackStyle = settings.iconPackStyle,
                                themeStyle = settings.themeStyle
                            )
                        }

                        // Nút thêm ghim phong cách Android TV
                        item {
                            TvAddAppCard(
                                accentColor = accentColor,
                                onClick = { showPinPicker = true }
                            )
                        }
                    }
                }
            }

            // Kênh 2: Lối tắt các cổng đầu vào TV (HDMI 1, HDMI 2 eARC, AV, TV Tuner)
            if (settings.showTvInputsShortcut && tvInputSources.isNotEmpty()) {
                item {
                    TvShelfSection(
                        title = "CỔNG ĐẦU VÀO TV (TV INPUTS)",
                        icon = Icons.Rounded.Cable,
                        accentColor = accentColor
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(tvInputSources) { src ->
                                val isSelected = src.id == settings.activeTvInputId
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) accentColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) accentColor else Color.White.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier
                                        .clickable { onSelectTvInput(src.id) }
                                        .width(150.dp)
                                        .height(72.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (src.inputType) {
                                                TvInputType.HDMI -> Icons.Rounded.Tv
                                                TvInputType.AV -> Icons.Rounded.Cable
                                                TvInputType.TV_TUNER -> Icons.Rounded.Tv
                                                TvInputType.USB_MEDIA -> Icons.Rounded.Usb
                                                else -> Icons.Rounded.Cable
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Column {
                                            Text(
                                                text = src.name,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = src.customAlias.ifBlank { if (src.isConnected) "Sẵn sàng" else "Chờ cắm" },
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Kênh 3: Gợi ý hàng đầu theo thói quen
            if (habitSuggestedApps.isNotEmpty()) {
                item {
                    TvShelfSection(
                        title = "GỢI Ý HÀNG ĐẦU • DỰA TRÊN THÓI QUEN",
                        icon = Icons.Rounded.AutoAwesome,
                        accentColor = accentColor
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(habitSuggestedApps) { app ->
                                TvAppCard(
                                    app = app,
                                    accentColor = accentColor,
                                    iconShape = settings.iconShape,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onAppLongClick(app) },
                                    iconPackStyle = settings.iconPackStyle,
                                    themeStyle = settings.themeStyle
                                )
                            }
                        }
                    }
                }
            }

            // Kênh 4: Danh mục kênh ứng dụng (Category TV Channels)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TvShelfHeader(
                            title = "KHÁM PHÁ THEO DANH MỤC",
                            icon = Icons.Rounded.Tv,
                            accentColor = accentColor
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf(
                                AppCategory.ALL,
                                AppCategory.MEDIA,
                                AppCategory.SOCIAL,
                                AppCategory.TOOLS,
                                AppCategory.PRODUCTIVITY,
                                AppCategory.FINANCE
                            )) { cat ->
                                val isSelected = selectedChannelCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedChannelCategory = cat },
                                    label = { Text(cat.title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentColor,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredApps = remember(selectedChannelCategory, allVisibleApps) {
                        if (selectedChannelCategory == AppCategory.ALL) allVisibleApps
                        else allVisibleApps.filter { it.category == selectedChannelCategory }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(filteredApps) { app ->
                            TvAppCard(
                                app = app,
                                accentColor = accentColor,
                                iconShape = settings.iconShape,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { onAppLongClick(app) },
                                iconPackStyle = settings.iconPackStyle,
                                themeStyle = settings.themeStyle
                            )
                        }
                    }
                }
            }
        }

        // Vị trí BOTTOM_BAR nếu người dùng chọn đặt widget thông tin ở thanh dưới
        if (settings.infoPlacement == InfoPlacement.BOTTOM_BAR) {
            Spacer(modifier = Modifier.height(6.dp))
            SmartWidgetContainer(
                activeWidget = settings.activeWidget,
                accentColor = accentColor,
                stickyNote = settings.stickyNoteText,
                onSaveStickyNote = onSaveStickyNote,
                onSwitchWidget = onSwitchWidget,
                onClockSecretTrigger = onOpenVault,
                familyDevices = familyDevices,
                currentDevice = currentDevice,
                familyName = familyName,
                userEmail = userEmail,
                onOpenFamilyMonitor = onOpenFamilyMonitor,
                tvInputSources = tvInputSources,
                activeTvInputId = settings.activeTvInputId,
                onSelectTvInput = onSelectTvInput
            )
        }
    }

    // Modal chọn ghim ứng dụng yêu thích
    if (showPinPicker) {
        TvPinAppPickerDialog(
            allApps = allVisibleApps,
            pinnedApps = pinnedApps,
            accentColor = accentColor,
            iconShape = settings.iconShape,
            iconPackStyle = settings.iconPackStyle,
            themeStyle = settings.themeStyle,
            onTogglePin = onSetAppPinned,
            onDismiss = { showPinPicker = false }
        )
    }

    // Modal chọn cổng đầu vào TV
    if (showInputPicker) {
        TvInputSourceChooserDialog(
            sources = tvInputSources,
            activeInputId = settings.activeTvInputId,
            accentColor = accentColor,
            onSelect = {
                onSelectTvInput(it)
                showInputPicker = false
            },
            onDismiss = { showInputPicker = false }
        )
    }
}

/**
 * Thanh Tiêu Đề TV Mở Rộng: Hiển thị Live Clock, Mạng, Bluetooth, Pin, Huy Hiệu Thiết Bị Gia Đình
 */
@Composable
fun TvTopHeader(
    settings: LauncherSettings,
    accentColor: Color,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenScreensaver: () -> Unit,
    onSetDefaultLauncher: () -> Unit,
    onOpenFamilyMonitor: () -> Unit = {},
    familyDevicesCount: Int = 0,
    activeTvInput: String = "HDMI 1",
    onOpenInputPicker: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var wifiSsid by remember { mutableStateOf(getWifiSsid(context)) }
    var hasNet by remember { mutableStateOf(checkHasNetwork(context)) }
    var bluetoothOn by remember { mutableStateOf(checkBluetoothEnabled(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            wifiSsid = getWifiSsid(context)
            hasNet = checkHasNetwork(context)
            bluetoothOn = checkBluetoothEnabled(context)
            delay(1000L)
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val secondsFormat = remember { SimpleDateFormat("ss", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d 'tháng' M", Locale("vi", "VN")) }

    val formattedTime = timeFormat.format(Date(currentTime))
    val formattedSeconds = secondsFormat.format(Date(currentTime))
    val formattedDate = dateFormat.format(Date(currentTime)).replaceFirstChar { it.uppercase() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bên trái: Biểu tượng TV, Giờ gọn, Thời tiết, Mạng, Bluetooth (chỉ icon nhỏ gọn)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Biểu tượng TV
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tv,
                        contentDescription = "Zenith TV",
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Giờ (chỉ số giờ:phút nhỏ gọn)
                if (settings.showClock) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Thời tiết (chỉ icon)
                if (settings.showWeather) {
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = "Thời tiết",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Trạng thái mạng Wi-Fi (chỉ icon với màu trực quan)
                if (settings.showNetworkStatus) {
                    Icon(
                        imageVector = Icons.Rounded.Wifi,
                        contentDescription = if (hasNet) "Mạng: $wifiSsid" else "Mất mạng",
                        tint = if (hasNet) Color(0xFF00E676) else Color(0xFFFF5252),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Trạng thái Bluetooth (chỉ icon)
                if (settings.showBluetoothStatus && bluetoothOn) {
                    Icon(
                        imageVector = Icons.Rounded.Bluetooth,
                        contentDescription = "Bluetooth đã bật",
                        tint = Color(0xFF2979FF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Bên phải: Toàn bộ nút chức năng thu gọn chỉ còn mỗi icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Lối tắt Cổng đầu vào TV (chỉ icon)
                if (settings.showTvInputsShortcut) {
                    IconButton(
                        onClick = onOpenInputPicker,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.12f))
                            .testTag("header_tv_inputs_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Cable,
                            contentDescription = "Cổng TV: $activeTvInput",
                            tint = accentColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Huy hiệu Theo Dõi Thiết Bị Gia Đình (chỉ icon + chấm online/offline)
                if (settings.showFamilyDevicesWidget) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (hasNet) accentColor.copy(alpha = 0.14f) else Color(0xFFFF5252).copy(alpha = 0.14f))
                            .clickable(onClick = onOpenFamilyMonitor)
                            .testTag("header_family_devices_pill"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.People,
                            contentDescription = "Thiết bị: $familyDevicesCount",
                            tint = if (hasNet) accentColor else Color(0xFFFF5252),
                            modifier = Modifier.size(17.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(3.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (hasNet) Color(0xFF00E676) else Color(0xFFFF5252))
                        )
                    }
                }

                // Đặt làm mặc định (chỉ icon)
                IconButton(
                    onClick = onSetDefaultLauncher,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.14f))
                        .testTag("header_default_launcher_icon")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = "Đặt làm launcher mặc định",
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Màn hình chờ (Screensaver)
                IconButton(
                    onClick = onOpenScreensaver,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Nightlight,
                        contentDescription = "Màn hình chờ",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Két bí mật
                IconButton(
                    onClick = onOpenVault,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Két ứng dụng ẩn",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Tất cả ứng dụng (App Drawer)
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Apps,
                        contentDescription = "Tất cả ứng dụng",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Cài đặt
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Cài đặt launcher",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

/**
 * Thẻ Ứng Dụng Chuẩn Android TV (TV App Card): Hỗ trợ combinedClickable chạm và giữ lâu
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvAppCard(
    app: AppInfo,
    accentColor: Color,
    iconShape: IconShape,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    iconPackStyle: com.example.data.model.IconPackStyle = com.example.data.model.IconPackStyle.THEME_ADAPTIVE,
    themeStyle: com.example.data.model.ThemeStyle = com.example.data.model.ThemeStyle.AMOLED_BLACK
) {
    Surface(
        modifier = Modifier
            .width(172.dp)
            .height(82.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = if (app.isPinnedHome) accentColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("tv_card_${app.packageName}"),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppIconItem(
                app = app,
                iconShape = iconShape,
                iconSize = IconSize.COMPACT,
                showLabel = false,
                accentColor = accentColor,
                onClick = onClick,
                onLongClick = onLongClick,
                iconPackStyle = iconPackStyle,
                themeStyle = themeStyle
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = app.displayLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (app.isPinnedHome) "Đã ghim • ${app.category.title}" else app.category.title,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = if (app.isPinnedHome) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Thẻ Thêm Ứng Dụng Ghim vào TV Favorites
 */
@Composable
fun TvAddAppCard(
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(135.dp)
            .height(82.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .testTag("tv_add_favorite_card"),
        color = accentColor.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Thêm ghim",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Thêm ghim", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}

/**
 * Dialog Chọn Ghim / Bỏ Ghim Ứng Dụng Trực Quan
 */
@Composable
fun TvPinAppPickerDialog(
    allApps: List<AppInfo>,
    pinnedApps: List<AppInfo>,
    accentColor: Color,
    iconShape: IconShape,
    iconPackStyle: com.example.data.model.IconPackStyle,
    themeStyle: com.example.data.model.ThemeStyle,
    onTogglePin: (AppInfo, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, allApps) {
        if (query.isBlank()) allApps
        else allApps.filter { it.displayLabel.lowercase().contains(query.trim().lowercase()) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Rounded.PushPin, contentDescription = null, tint = accentColor)
                    Text("Ghim Vào Hàng Ưa Thích TV", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "Đóng")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Tìm ứng dụng...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                    items(filtered) { app ->
                        val isPinned = pinnedApps.any { it.packageName == app.packageName }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPinned) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isPinned) accentColor.copy(alpha = 0.4f) else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { onTogglePin(app, !isPinned) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    AppIconItem(
                                        app = app,
                                        iconShape = iconShape,
                                        iconSize = IconSize.COMPACT,
                                        showLabel = false,
                                        accentColor = accentColor,
                                        onClick = { onTogglePin(app, !isPinned) },
                                        onLongClick = {},
                                        iconPackStyle = iconPackStyle,
                                        themeStyle = themeStyle
                                    )
                                    Column {
                                        Text(
                                            text = app.displayLabel,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isPinned) "Đang ghim trên màn hình chính" else "Nhấn để ghim",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = if (isPinned) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                Switch(
                                    checked = isPinned,
                                    onCheckedChange = { onTogglePin(app, it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Hoàn tất", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * Dialog Chọn Cổng Đầu Vào TV
 */
@Composable
fun TvInputSourceChooserDialog(
    sources: List<TvInputSource>,
    activeInputId: String,
    accentColor: Color,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Rounded.Cable, contentDescription = null, tint = accentColor)
                Text("Chọn Cổng Đầu Vào TV", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sources.forEach { src ->
                    val isSelected = src.id == activeInputId
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) accentColor else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(src.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(
                                    imageVector = when (src.inputType) {
                                        TvInputType.HDMI -> Icons.Rounded.Tv
                                        TvInputType.AV -> Icons.Rounded.Cable
                                        TvInputType.TV_TUNER -> Icons.Rounded.Tv
                                        TvInputType.USB_MEDIA -> Icons.Rounded.Usb
                                        else -> Icons.Rounded.Cable
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                                )
                                Column {
                                    Text(text = src.name, fontWeight = FontWeight.Bold, color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface)
                                    Text(text = src.customAlias.ifBlank { "Tín hiệu TV" }, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }

                            if (isSelected) {
                                Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = accentColor)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = accentColor)
            }
        }
    )
}

/**
 * Giao diện Dọc cho Điện Thoại & Máy Tính Bảng (Portrait)
 */
@Composable
fun PortraitHomeScreen(
    settings: LauncherSettings,
    pinnedApps: List<AppInfo>,
    habitSuggestedApps: List<AppInfo>,
    allVisibleApps: List<AppInfo> = emptyList(),
    accentColor: Color,
    onLaunchApp: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenScreensaver: () -> Unit,
    onSetDefaultLauncher: () -> Unit,
    onSaveStickyNote: (String) -> Unit,
    onSwitchWidget: (SmartWidgetType) -> Unit,
    onOpenFamilyMonitor: () -> Unit = {},
    familyDevices: List<DeviceRecord> = emptyList(),
    currentDevice: DeviceRecord? = null,
    familyName: String = "Gia đình của tôi",
    userEmail: String? = null,
    tvInputSources: List<TvInputSource> = emptyList(),
    onSelectTvInput: (String) -> Unit = {},
    onSetAppPinned: (AppInfo, Boolean) -> Unit = { _, _ -> }
) {
    var showPinPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Thanh đỉnh đầu nhanh
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Tv, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                Text("Zenith TV Leanback", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Nút mở Family Monitor
                IconButton(onClick = onOpenFamilyMonitor) {
                    Icon(imageVector = Icons.Rounded.People, contentDescription = "Gia đình", tint = accentColor)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(imageVector = Icons.Rounded.Settings, contentDescription = "Cài đặt")
                }
            }
        }

        // Widget Thông minh chính
        SmartWidgetContainer(
            activeWidget = settings.activeWidget,
            accentColor = accentColor,
            stickyNote = settings.stickyNoteText,
            onSaveStickyNote = onSaveStickyNote,
            onSwitchWidget = onSwitchWidget,
            onClockSecretTrigger = onOpenVault,
            familyDevices = familyDevices,
            currentDevice = currentDevice,
            familyName = familyName,
            userEmail = userEmail,
            onOpenFamilyMonitor = onOpenFamilyMonitor,
            tvInputSources = tvInputSources,
            activeTvInputId = settings.activeTvInputId,
            onSelectTvInput = onSelectTvInput
        )

        // Hàng ứng dụng ghim yêu thích
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ỨNG DỤNG ĐÃ GHIM",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
            Text(
                text = "+ Ghim thêm",
                style = MaterialTheme.typography.labelSmall.copy(color = accentColor, fontWeight = FontWeight.Bold),
                modifier = Modifier.clickable { showPinPicker = true }
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(pinnedApps, key = { it.packageName }) { app ->
                AppIconItem(
                    app = app,
                    iconShape = settings.iconShape,
                    iconSize = settings.iconSize,
                    showLabel = settings.showAppLabels,
                    accentColor = accentColor,
                    onClick = { onLaunchApp(app) },
                    onLongClick = { onAppLongClick(app) },
                    iconPackStyle = settings.iconPackStyle,
                    themeStyle = settings.themeStyle
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Gợi ý theo thói quen
        if (habitSuggestedApps.isNotEmpty()) {
            HabitSuggestionRow(
                apps = habitSuggestedApps,
                accentColor = accentColor,
                settings = settings,
                onLaunchApp = onLaunchApp,
                onAppLongClick = onAppLongClick
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        BottomDockBar(
            accentColor = accentColor,
            onOpenDrawer = onOpenDrawer,
            onOpenSettings = onOpenSettings,
            onOpenVault = onOpenVault
        )
    }

    if (showPinPicker) {
        TvPinAppPickerDialog(
            allApps = allVisibleApps,
            pinnedApps = pinnedApps,
            accentColor = accentColor,
            iconShape = settings.iconShape,
            iconPackStyle = settings.iconPackStyle,
            themeStyle = settings.themeStyle,
            onTogglePin = onSetAppPinned,
            onDismiss = { showPinPicker = false }
        )
    }
}

@Composable
fun TvShelfSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TvShelfHeader(title = title, icon = icon, accentColor = accentColor)
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
fun TvShelfHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 11.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
        )
    }
}

@Composable
fun HabitSuggestionRow(
    apps: List<AppInfo>,
    accentColor: Color,
    settings: LauncherSettings,
    onLaunchApp: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Gợi ý theo thói quen của bạn",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                    color = accentColor
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(apps) { app ->
                    AppIconItem(
                        app = app,
                        iconShape = settings.iconShape,
                        iconSize = IconSize.COMPACT,
                        showLabel = settings.showAppLabels,
                        accentColor = accentColor,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onAppLongClick(app) },
                        iconPackStyle = settings.iconPackStyle,
                        themeStyle = settings.themeStyle
                    )
                }
            }
        }
    }
}

@Composable
fun BottomDockBar(
    accentColor: Color,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVault: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(26.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("home_settings_button")) {
                Icon(imageVector = Icons.Rounded.Settings, contentDescription = "Cá nhân hóa")
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .clickable { onOpenDrawer() }
                    .padding(horizontal = 18.dp, vertical = 8.dp)
                    .testTag("home_open_drawer_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Apps, contentDescription = "Tất cả ứng dụng", tint = accentColor, modifier = Modifier.size(18.dp))
                Text(text = "Ngăn ứng dụng", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = accentColor)
                Icon(imageVector = Icons.Rounded.KeyboardArrowUp, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }

            IconButton(onClick = onOpenVault, modifier = Modifier.testTag("home_vault_button")) {
                Icon(imageVector = Icons.Rounded.Lock, contentDescription = "Két bí mật")
            }
        }
    }
}

@Composable
fun WallpaperBackground(
    wallpaper: WallpaperOption,
    customWallpaperPath: String? = null
) {
    when (wallpaper) {
        WallpaperOption.CUSTOM_IMAGE -> {
            if (!customWallpaperPath.isNullOrBlank() && File(customWallpaperPath).exists()) {
                val bitmap = remember(customWallpaperPath) {
                    try {
                        BitmapFactory.decodeFile(customWallpaperPath)?.asImageBitmap()
                    } catch (e: Exception) {
                        null
                    }
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Hình nền tùy chọn",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)))
            }
        }

        WallpaperOption.TV_CINEMATIC -> {
            Image(
                painter = painterResource(id = R.drawable.wallpaper_tv_cinematic),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        WallpaperOption.AMOLED_BLACK -> {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        }

        WallpaperOption.NEON_HORIZON -> {
            Image(
                painter = painterResource(id = R.drawable.wallpaper_neon_zenith),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        WallpaperOption.ZEN_MOON -> {
            Image(
                painter = painterResource(id = R.drawable.wallpaper_zen_moon),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        WallpaperOption.AURORA_GRADIENT -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                        )
                    )
            )
        }

        WallpaperOption.CYBER_GRID -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            listOf(Color(0xFF1A1F36), Color(0xFF0A0C16))
                        )
                    )
            )
        }
    }
}

private fun checkHasNetwork(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val activeNet = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(activeNet) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

private fun getWifiSsid(context: Context): String {
    return try {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val info = wm?.connectionInfo
        val ssid = info?.ssid ?: ""
        if (ssid.isNotBlank() && ssid != "<unknown ssid>") ssid.replace("\"", "") else "Wi-Fi TV"
    } catch (e: Exception) {
        "Wi-Fi TV"
    }
}

private fun checkBluetoothEnabled(context: Context): Boolean {
    return try {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        adapter != null && adapter.isEnabled
    } catch (e: Exception) {
        false
    }
}
