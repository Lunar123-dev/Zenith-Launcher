package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.StayCurrentLandscape
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AppCategory
import com.example.data.model.AppInfo
import com.example.data.model.IconShape
import com.example.data.model.IconSize
import com.example.data.model.LauncherSettings
import com.example.data.model.OrientationMode
import com.example.data.model.SmartWidgetType
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
                    .background(Color.Black.copy(alpha = 0.4f))
            )
        }

        if (isLandscape) {
            // Giao diện ngang chuẩn TV / Tablet / Màn hình rộng
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
                onSetDefaultLauncher = onSetDefaultLauncher
            )
        } else {
            // Giao diện dọc thích ứng điện thoại, có nút chuyển nhanh sang TV Mode
            PortraitHomeScreen(
                settings = settings,
                pinnedApps = pinnedApps,
                habitSuggestedApps = habitSuggestedApps,
                accentColor = accentColor,
                onLaunchApp = onLaunchApp,
                onAppLongClick = onAppLongClick,
                onOpenDrawer = onOpenDrawer,
                onOpenSettings = onOpenSettings,
                onOpenVault = onOpenVault,
                onOpenScreensaver = onOpenScreensaver,
                onSetDefaultLauncher = onSetDefaultLauncher,
                onSaveStickyNote = onSaveStickyNote,
                onSwitchWidget = onSwitchWidget
            )
        }
    }
}

/**
 * Giao diện Ngang Chuẩn TV (Zenith TV Leanback Launcher)
 * Thiết kế cho TV Box, Smart TV, Màn hình ô tô, Máy tính bảng hoặc Điện thoại xoay ngang
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
    onSetDefaultLauncher: () -> Unit
) {
    var selectedChannelCategory by remember { mutableStateOf(AppCategory.ALL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        // 1. Thanh tiêu đề TV thông minh (TV Top Header Bar)
        TvTopHeader(
            accentColor = accentColor,
            onOpenDrawer = onOpenDrawer,
            onOpenSettings = onOpenSettings,
            onOpenVault = onOpenVault,
            onOpenScreensaver = onOpenScreensaver,
            onSetDefaultLauncher = onSetDefaultLauncher
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
            // Kênh 1: Ứng dụng thói quen hàng đầu (Smart Habits Shelf)
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
                                    onLongClick = { onAppLongClick(app) }
                                )
                            }
                        }
                    }
                }
            }

            // Kênh 2: Kênh yêu thích đã ghim (Pinned Favorites Shelf)
            if (pinnedApps.isNotEmpty()) {
                item {
                    TvShelfSection(
                        title = "KÊNH ỨNG DỤNG YÊU THÍCH",
                        icon = Icons.Rounded.Home,
                        accentColor = accentColor
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(pinnedApps) { app ->
                                TvAppCard(
                                    app = app,
                                    accentColor = accentColor,
                                    iconShape = settings.iconShape,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onAppLongClick(app) }
                                )
                            }
                        }
                    }
                }
            }

            // Kênh 3: Danh mục kênh ứng dụng (Category TV Channel Shelf)
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

                        // Các nút chọn danh mục kênh TV
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
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredChannelApps = remember(selectedChannelCategory, allVisibleApps) {
                        if (selectedChannelCategory == AppCategory.ALL) allVisibleApps
                        else allVisibleApps.filter { it.category == selectedChannelCategory }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(filteredChannelApps) { app ->
                            TvAppCard(
                                app = app,
                                accentColor = accentColor,
                                iconShape = settings.iconShape,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { onAppLongClick(app) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TvTopHeader(
    accentColor: Color,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenScreensaver: () -> Unit,
    onSetDefaultLauncher: () -> Unit
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
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
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bên trái: Đồng hồ lớn, thời tiết, logo TV
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Huy hiệu ZENITH TV
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tv,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "ZENITH TV",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = accentColor
                    )
                }

                // Giờ và ngày
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-1).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = ":$formattedSeconds",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Normal),
                        color = accentColor,
                        modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                    )
                }

                Text(
                    text = "• $formattedDate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Thời tiết
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "28°C",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Bên phải: Thanh công cụ thao tác nhanh cho TV
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Đặt làm mặc định
                Button(
                    onClick = onSetDefaultLauncher,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor.copy(alpha = 0.15f),
                        contentColor = accentColor
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Đặt mặc định", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Màn hình chờ Ambient
                IconButton(
                    onClick = onOpenScreensaver,
                    modifier = Modifier.testTag("tv_screensaver_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Nightlight,
                        contentDescription = "Màn hình chờ TV",
                        tint = accentColor
                    )
                }

                // Tìm kiếm
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.testTag("tv_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Tìm kiếm ứng dụng",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Két bảo mật
                IconButton(
                    onClick = onOpenVault,
                    modifier = Modifier.testTag("tv_vault_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Két bí mật",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Cài đặt
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("tv_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Cài đặt & Tùy biến",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
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

/**
 * Thẻ ứng dụng phong cách TV (TV App Card - Tỉ lệ ngang rộng với hiệu ứng viền sáng focus)
 */
@Composable
fun TvAppCard(
    app: AppInfo,
    accentColor: Color,
    iconShape: IconShape,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .height(78.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            }
            .testTag("tv_card_${app.packageName}"),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
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
                onLongClick = onLongClick
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
                    text = app.category.title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Bố cục dọc cho điện thoại, hỗ trợ chuyển nhanh sang TV Mode
 */
@Composable
fun PortraitHomeScreen(
    settings: LauncherSettings,
    pinnedApps: List<AppInfo>,
    habitSuggestedApps: List<AppInfo>,
    accentColor: Color,
    onLaunchApp: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenScreensaver: () -> Unit,
    onSetDefaultLauncher: () -> Unit,
    onSaveStickyNote: (String) -> Unit,
    onSwitchWidget: (SmartWidgetType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Thanh chuyển sang Chế độ Ngang TV nhanh
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .clickable { onOpenScreensaver() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Nightlight,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Màn hình chờ TV",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = accentColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .clickable { onSetDefaultLauncher() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Đặt làm Mặc định",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            SmartWidgetContainer(
                activeWidget = settings.activeWidget,
                accentColor = accentColor,
                stickyNote = settings.stickyNoteText,
                onSaveStickyNote = onSaveStickyNote,
                onSwitchWidget = onSwitchWidget,
                onClockSecretTrigger = onOpenVault
            )

            if (habitSuggestedApps.isNotEmpty() && !settings.extremeBatterySaver) {
                Spacer(modifier = Modifier.height(4.dp))
                HabitSuggestionRow(
                    apps = habitSuggestedApps,
                    accentColor = accentColor,
                    settings = settings,
                    onLaunchApp = onLaunchApp,
                    onAppLongClick = onAppLongClick
                )
            }
        }

        // Pinned Apps Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.Bottom
        ) {
            if (pinnedApps.isNotEmpty()) {
                Text(
                    text = "Ứng dụng thường dùng",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(settings.gridColumns),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Vuốt lên để xem tất cả ứng dụng • Giữ ứng dụng để ghim vào đây",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        BottomDockBar(
            accentColor = accentColor,
            onOpenDrawer = onOpenDrawer,
            onOpenSettings = onOpenSettings,
            onOpenVault = onOpenVault
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
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
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
                        onLongClick = { onAppLongClick(app) }
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
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(26.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("home_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Cá nhân hóa",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
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
                Icon(
                    imageVector = Icons.Rounded.Apps,
                    contentDescription = "Tất cả ứng dụng",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Ngăn ứng dụng",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onOpenVault,
                modifier = Modifier.testTag("home_vault_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Két bí mật",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
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
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF030D1B),
                                Color(0xFF0A2239),
                                Color(0xFF00382B),
                                Color(0xFF051B14)
                            )
                        )
                    )
            )
        }

        WallpaperOption.CYBER_GRID -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF1D1135),
                                Color(0xFF0C081E),
                                Color(0xFF040209)
                            )
                        )
                    )
            )
        }
    }
}
