package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Tablet
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.model.DeviceRecord
import com.example.ui.FamilyMonitorViewModel
import com.example.ui.auth.GoogleAuthHelper
import com.example.ui.auth.GoogleSignInButton

@Composable
fun FamilyMonitorScreen(
    viewModel: FamilyMonitorViewModel,
    accentColor: Color,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val user by viewModel.currentUser.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val familyId by viewModel.familyId.collectAsState()
    val familyCode by viewModel.familyCode.collectAsState()
    val familyName by viewModel.familyName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var targetDeviceForMessage by remember { mutableStateOf<DeviceRecord?>(null) }
    var inputMessageText by remember { mutableStateOf("") }
    var inputNewFamilyName by remember { mutableStateOf("") }
    var inputJoinCode by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090C10))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Theo Dõi Thiết Bị Gia Đình",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Giám sát pin, RAM, trạng thái & gửi tin nhắn từ xa",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                if (user != null) {
                    IconButton(
                        onClick = { viewModel.startTelemetrySync() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.06f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Làm mới",
                            tint = accentColor
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = {
                            GoogleAuthHelper.signOut(
                                context = context,
                                credentialManager = credentialManager,
                                onSignOutComplete = {
                                    Toast.makeText(context, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                                },
                                scope = coroutineScope
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text("Đăng xuất", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            if (user == null) {
                // Màn hình yêu cầu đăng nhập tài khoản Google
                UnauthenticatedFamilyView(accentColor = accentColor)
            } else {
                // Family Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131822)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.People,
                                        contentDescription = "Gia đình",
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = familyName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${devices.size} thiết bị kết nối",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            if (familyCode.isNotBlank()) {
                                Row(
                                    modifier = Modifier
                                        .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(familyCode))
                                            Toast.makeText(context, "Đã sao chép mã: $familyCode", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Mã: $familyCode",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = accentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showCreateDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tạo nhóm", fontSize = 12.sp, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = { showJoinDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Rounded.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Nhập mã", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }

                // Danh sách thiết bị
                if (devices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.Tv,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Đang đồng bộ thiết bị trong nhà...",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 280.dp),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(devices, key = { it.id }) { device ->
                            FamilyDeviceCard(
                                device = device,
                                accentColor = accentColor,
                                onSendMessage = { targetDeviceForMessage = device },
                                onRingDevice = {
                                    viewModel.ringTargetDevice(device.id)
                                    Toast.makeText(context, "Đã gửi tín hiệu chuông tới ${device.deviceName}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Dialog gửi tin nhắn từ xa
        targetDeviceForMessage?.let { target ->
            AlertDialog(
                onDismissRequest = { targetDeviceForMessage = null },
                containerColor = Color(0xFF161B22),
                title = {
                    Text(
                        text = "Gửi thông báo tới ${target.deviceName}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Tin nhắn sẽ hiển thị tức thì trên màn hình thiết bị.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = inputMessageText,
                            onValueChange = { inputMessageText = it },
                            placeholder = { Text("VD: Đến giờ cơm rồi!", color = Color.White.copy(alpha = 0.4f)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputMessageText.isNotBlank()) {
                                viewModel.sendRemoteMessage(target.id, inputMessageText)
                                Toast.makeText(context, "Đã gửi thông báo!", Toast.LENGTH_SHORT).show()
                                inputMessageText = ""
                                targetDeviceForMessage = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Gửi ngay", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { targetDeviceForMessage = null }) {
                        Text("Hủy", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }

        // Dialog Tạo Nhóm Gia Đình
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = Color(0xFF161B22),
                title = { Text("Tạo Nhóm Gia Đình Mới", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = inputNewFamilyName,
                        onValueChange = { inputNewFamilyName = it },
                        label = { Text("Tên gia đình", color = Color.White.copy(alpha = 0.6f)) },
                        placeholder = { Text("VD: Gia đình Bác Nam", color = Color.White.copy(alpha = 0.4f)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.createFamily(inputNewFamilyName)
                            showCreateDialog = false
                            inputNewFamilyName = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Tạo", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Hủy", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }

        // Dialog Gia Nhập Qua Mã
        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false },
                containerColor = Color(0xFF161B22),
                title = { Text("Gia Nhập Bằng Mã", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = inputJoinCode,
                        onValueChange = { inputJoinCode = it.uppercase() },
                        label = { Text("Mã gia đình (6 ký tự)", color = Color.White.copy(alpha = 0.6f)) },
                        placeholder = { Text("VD: ZEN889", color = Color.White.copy(alpha = 0.4f)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.joinFamily(inputJoinCode)
                            showJoinDialog = false
                            inputJoinCode = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Gia nhập", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinDialog = false }) {
                        Text("Hủy", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }

        // Loading Overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = accentColor)
            }
        }
    }
}

@Composable
fun UnauthenticatedFamilyView(accentColor: Color) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131822)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.People,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Giám Sát Thiết Bị Gia Đình",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Đăng nhập tài khoản Google để theo dõi mức pin, mạng WiFi, bộ nhớ RAM và điều khiển gửi tin nhắn / phát chuông tìm kiếm từ xa giữa TV, điện thoại và máy tính bảng trong nhà.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                GoogleSignInButton(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = accentColor,
                    onAuthSuccess = {
                        Toast.makeText(context, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show()
                    },
                    onAuthError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
}

@Composable
fun FamilyDeviceCard(
    device: DeviceRecord,
    accentColor: Color,
    onSendMessage: () -> Unit,
    onRingDevice: () -> Unit
) {
    val deviceIcon = when (device.deviceType) {
        "TV" -> Icons.Rounded.Tv
        "TABLET" -> Icons.Rounded.Tablet
        else -> Icons.Rounded.Smartphone
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131822)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top row: Type icon, Name, Online badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = deviceIcon,
                        contentDescription = device.deviceType,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.deviceName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = device.deviceModel,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        maxLines = 1
                    )
                }

                // Online indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            if (device.isOnline) Color(0xFF10B981).copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (device.isOnline) Color(0xFF10B981) else Color.Red, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (device.isOnline) "Trực tuyến" else "Ngoại tuyến",
                        fontSize = 10.sp,
                        color = if (device.isOnline) Color(0xFF10B981) else Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics row: Battery & RAM
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pin
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (device.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryFull,
                            contentDescription = "Pin",
                            tint = if (device.batteryLevel <= 20) Color.Red else Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "${device.batteryLevel}%" + if (device.isCharging) " ⚡" else "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (device.isCharging) "Đang sạc" else "Pin",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                // RAM
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Memory,
                            contentDescription = "RAM",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            val ramUsedGb = "%.1f".format(device.ramUsedMb / 1024f)
                            val ramTotalGb = "%.1f".format(device.ramTotalMb / 1024f)
                            Text(
                                text = "$ramUsedGb / $ramTotalGb GB",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Bộ nhớ RAM",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Network info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Wifi,
                    contentDescription = "Mạng",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${device.networkName} (${device.networkType})",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSendMessage,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Rounded.Message, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nhắn tin", fontSize = 11.sp, color = Color.White)
                }

                OutlinedButton(
                    onClick = onRingDevice,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Rounded.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp), tint = accentColor)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Phát chuông", fontSize = 11.sp, color = accentColor)
                }
            }
        }
    }
}
