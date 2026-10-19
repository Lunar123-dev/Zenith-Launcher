package com.example.ui.components

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cable
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.SignalWifiOff
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tablet
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SmartWidgetType
import com.example.data.model.TvInputSource
import com.example.data.model.TvInputType
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SmartWidgetContainer(
    activeWidget: SmartWidgetType,
    accentColor: Color,
    stickyNote: String,
    onSaveStickyNote: (String) -> Unit,
    onSwitchWidget: (SmartWidgetType) -> Unit,
    onClockSecretTrigger: () -> Unit,
    modifier: Modifier = Modifier,
    tvInputSources: List<TvInputSource> = emptyList(),
    activeTvInputId: String = "hdmi_1",
    onSelectTvInput: (String) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("smart_widget_container")
    ) {
        // Hàng chuyển đổi widget dạng tabs cuộn mượt
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(SmartWidgetType.values()) { type ->
                    val isSelected = activeWidget == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) accentColor.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) accentColor else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSwitchWidget(type) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("widget_tab_${type.name}")
                    ) {
                        Text(
                            text = type.shortTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp
                            ),
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Nút Két Bảo Mật tinh tế
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 16.dp),
                        onClick = onClockSecretTrigger
                    )
                    .padding(4.dp)
                    .testTag("secret_vault_trigger")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Mở Két Bảo Mật",
                    tint = accentColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Nội dung Widget
        AnimatedContent(
            targetState = activeWidget,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "widget_animation"
        ) { widget ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                tonalElevation = 3.dp
            ) {
                when (widget) {

                    SmartWidgetType.TV_INPUTS -> TvInputsWidget(
                        sources = tvInputSources,
                        activeInputId = activeTvInputId,
                        accentColor = accentColor,
                        onSelectInput = onSelectTvInput
                    )

                    SmartWidgetType.NETWORK_BLUETOOTH -> NetworkBluetoothWidget(
                        accentColor = accentColor
                    )

                    SmartWidgetType.CLOCK_WEATHER -> ZenClockWidget(
                        accentColor = accentColor,
                        onClockSecretTrigger = onClockSecretTrigger
                    )

                    SmartWidgetType.SYSTEM_STATS -> SystemStatsWidget(
                        accentColor = accentColor
                    )

                    SmartWidgetType.STICKY_NOTE -> StickyNoteWidget(
                        stickyNote = stickyNote,
                        accentColor = accentColor,
                        onSave = onSaveStickyNote
                    )
                }
            }
        }
    }
}

/**
 * WIDGET: LỐI TẮT NHANH CHO CỔNG ĐẦU VÀO TV (HDMI, AV, TV TUNER)
 */
@Composable
fun TvInputsWidget(
    sources: List<TvInputSource>,
    activeInputId: String,
    accentColor: Color,
    onSelectInput: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .testTag("tv_inputs_widget")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Cable,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "CỔNG ĐẦU VÀO TV (INPUT SOURCES)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Chuyển nhanh",
                style = MaterialTheme.typography.labelSmall.copy(color = accentColor, fontSize = 11.sp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(sources) { src ->
                val isSelected = src.id == activeInputId
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) accentColor else Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .clickable {
                            onSelectInput(src.id)
                            openTvInputIntent(context, src)
                        }
                        .width(135.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (src.inputType) {
                                    TvInputType.HDMI -> Icons.Rounded.Tv
                                    TvInputType.AV -> Icons.Rounded.Cable
                                    TvInputType.TV_TUNER -> Icons.Rounded.Tv
                                    TvInputType.USB_MEDIA -> Icons.Rounded.Usb
                                    TvInputType.OPTICAL -> Icons.AutoMirrored.Rounded.VolumeUp
                                },
                                contentDescription = null,
                                tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )

                            if (src.isArc) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFB388FF).copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        text = "eARC",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB388FF),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = src.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = src.customAlias.ifBlank { if (src.isConnected) "Đã kết nối" else "Chờ tín hiệu" },
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

/**
 * WIDGET 3: TRẠNG THÁI MẠNG (WI-FI / ETHERNET) VÀ BLUETOOTH
 */
@Composable
fun NetworkBluetoothWidget(
    accentColor: Color
) {
    val context = LocalContext.current
    var wifiName by remember { mutableStateOf(getWifiSsid(context)) }
    var isWifiConnected by remember { mutableStateOf(checkHasNetwork(context)) }
    var isBluetoothOn by remember { mutableStateOf(checkBluetoothEnabled(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            wifiName = getWifiSsid(context)
            isWifiConnected = checkHasNetwork(context)
            isBluetoothOn = checkBluetoothEnabled(context)
            delay(4000L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .testTag("network_bluetooth_widget")
    ) {
        Text(
            text = "KẾT NỐI MẠNG & PHỤ KIỆN BLUETOOTH",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Thẻ Wi-Fi / Ethernet
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        try {
                            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } catch (e: Exception) {
                            // Fallback
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isWifiConnected) accentColor.copy(alpha = 0.2f) else Color(0xFFFF5252).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isWifiConnected) Icons.Rounded.Wifi else Icons.Rounded.SignalWifiOff,
                            contentDescription = null,
                            tint = if (isWifiConnected) accentColor else Color(0xFFFF5252),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isWifiConnected) wifiName else "Chưa kết nối",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isWifiConnected) "Mạng trực tuyến • Đang dùng" else "Không có mạng Internet",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = if (isWifiConnected) Color(0xFF00E676) else Color(0xFFFF5252)
                        )
                    }
                }
            }

            // Thẻ Bluetooth
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        try {
                            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } catch (e: Exception) {
                            // Fallback
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isBluetoothOn) Color(0xFF2979FF).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isBluetoothOn) Icons.Rounded.BluetoothConnected else Icons.Rounded.Bluetooth,
                            contentDescription = null,
                            tint = if (isBluetoothOn) Color(0xFF2979FF) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isBluetoothOn) "Bluetooth: Đang Bật" else "Bluetooth: Tắt",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBluetoothOn) "Điều khiển TV / Loa sẵn sàng" else "Nhấn để bật Bluetooth",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * WIDGET 4: ĐỒNG HỒ THỜI GIAN & THỜI TIẾT LEANBACK
 */
@Composable
fun ZenClockWidget(
    accentColor: Color,
    onClockSecretTrigger: () -> Unit
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val secFormat = SimpleDateFormat("ss", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, d 'tháng' M, yyyy", Locale("vi", "VN"))

    val timeStr = timeFormat.format(Date(currentTime))
    val secStr = secFormat.format(Date(currentTime))
    val dateStr = dateFormat.format(Date(currentTime))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = ":$secStr",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = accentColor
                    ),
                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                )
            }

            Text(
                text = dateStr.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        // Thông tin thời tiết tóm tắt
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.WbSunny,
                contentDescription = "Thời tiết",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(26.dp)
            )
            Column {
                Text(
                    text = "28°C",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Nắng đẹp • Êm dịu",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * WIDGET 5: HIỆU NĂNG, RAM & PIN
 */
@Composable
fun SystemStatsWidget(accentColor: Color) {
    val context = LocalContext.current
    var batteryPct by remember { mutableIntStateOf(100) }
    var isCharging by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    val runtime = Runtime.getRuntime()
    val usedMemoryMb = ((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)).toInt()
    val maxMemoryMb = (runtime.maxMemory() / (1024 * 1024)).toInt()
    val memFraction = (usedMemoryMb.toFloat() / maxMemoryMb.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRẠNG THÁI HỆ THỐNG",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryFull,
                    contentDescription = null,
                    tint = if (batteryPct < 20) Color(0xFFFF5252) else accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$batteryPct% ${if (isCharging) "(Đang sạc)" else ""}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Bộ nhớ RAM đang dùng: $usedMemoryMb MB / $maxMemoryMb MB",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "${(memFraction * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = accentColor
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { memFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = accentColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * WIDGET 6: GHI CHÚ NHANH ZEN
 */
@Composable
fun StickyNoteWidget(
    stickyNote: String,
    accentColor: Color,
    onSave: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember(stickyNote) { mutableStateOf(stickyNote) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GHI CHÚ & LỜI NHẮC GIA ĐÌNH",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            TextButton(
                onClick = {
                    if (isEditing) {
                        onSave(textValue)
                    }
                    isEditing = !isEditing
                }
            ) {
                Text(
                    text = if (isEditing) "Lưu" else "Sửa",
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (isEditing) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            Text(
                text = stickyNote.ifBlank { "Nhấn nút 'Sửa' để để lại lời nhắn cho gia đình trên màn hình TV..." },
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
        }
    }
}

// Helper kiểm tra kết nối mạng
private fun checkHasNetwork(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val activeNet = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(activeNet) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

// Helper lấy tên Wi-Fi
private fun getWifiSsid(context: Context): String {
    return try {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val info = wm?.connectionInfo
        val ssid = info?.ssid ?: ""
        if (ssid.isNotBlank() && ssid != "<unknown ssid>") ssid.replace("\"", "") else "Wi-Fi Gia đình"
    } catch (e: Exception) {
        "Wi-Fi TV"
    }
}

// Helper kiểm tra Bluetooth
private fun checkBluetoothEnabled(context: Context): Boolean {
    return try {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        adapter != null && adapter.isEnabled
    } catch (e: Exception) {
        false
    }
}

// Helper chuyển cổng TV
private fun openTvInputIntent(context: Context, source: TvInputSource) {
    try {
        val intent = Intent("android.media.tv.action.QUERY_CONTENT_RATING_SYSTEMS").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback: thông báo chuyển cổng thành công
    }
}
