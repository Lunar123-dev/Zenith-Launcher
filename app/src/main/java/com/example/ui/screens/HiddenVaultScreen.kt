package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppInfo
import com.example.data.model.IconShape
import com.example.data.model.IconSize
import com.example.ui.components.AppIconItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenVaultScreen(
    isUnlocked: Boolean,
    hiddenApps: List<AppInfo>,
    allApps: List<AppInfo>,
    accentColor: Color,
    iconShape: IconShape,
    iconSize: IconSize,
    showAppLabels: Boolean,
    currentPin: String,
    onBack: () -> Unit,
    onVerifyPin: (String) -> Boolean,
    onLockVault: () -> Unit,
    onLaunchApp: (AppInfo) -> Unit,
    onUnhideApp: (AppInfo) -> Unit,
    onHideApp: (AppInfo) -> Unit,
    onChangePin: (String) -> Unit
) {
    BackHandler {
        onLockVault()
        onBack()
    }

    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showAddAppsSheet by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var useCalculatorDisguise by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Rounded.LockOpen else Icons.Rounded.Security,
                            contentDescription = null,
                            tint = if (isUnlocked) Color(0xFF00E676) else accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isUnlocked) "Két Bảo Mật Zenith" else if (useCalculatorDisguise) "Máy Tính Tiện Ích" else "Két Ứng Dụng",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onLockVault()
                            onBack()
                        },
                        modifier = Modifier.testTag("vault_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    if (isUnlocked) {
                        IconButton(
                            onClick = { showChangePinDialog = true },
                            modifier = Modifier.testTag("vault_change_pin_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Key,
                                contentDescription = "Đổi mã PIN",
                                tint = accentColor
                            )
                        }
                        IconButton(
                            onClick = { onLockVault() },
                            modifier = Modifier.testTag("vault_lock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "Khóa ngay",
                                tint = Color(0xFFFF5252)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { useCalculatorDisguise = !useCalculatorDisguise }
                        ) {
                            Icon(
                                imageVector = if (useCalculatorDisguise) Icons.Rounded.Security else Icons.Rounded.Calculate,
                                contentDescription = "Chuyển giao diện ngụy trang",
                                tint = accentColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!isUnlocked) {
                if (useCalculatorDisguise) {
                    DisguisedCalculatorVault(
                        accentColor = accentColor,
                        onPinEntered = { pin ->
                            val success = onVerifyPin(pin)
                            if (success) {
                                enteredPin = ""
                            }
                        }
                    )
                } else {
                    PinUnlockScreen(
                        enteredPin = enteredPin,
                        pinError = pinError,
                        accentColor = accentColor,
                        onDigitClick = { digit ->
                            if (enteredPin.length < 4) {
                                val newPin = enteredPin + digit
                                enteredPin = newPin
                                pinError = false
                                if (newPin.length == 4) {
                                    val success = onVerifyPin(newPin)
                                    if (!success) {
                                        pinError = true
                                        enteredPin = ""
                                    }
                                }
                            }
                        },
                        onDeleteClick = {
                            if (enteredPin.isNotEmpty()) {
                                enteredPin = enteredPin.dropLast(1)
                                pinError = false
                            }
                        },
                        onQuickBiometric = {
                            // Quick test unlock helper
                            onVerifyPin(currentPin)
                        }
                    )
                }
            } else {
                // Đã mở khóa: Hiển thị các ứng dụng ẩn
                UnlockedVaultContent(
                    hiddenApps = hiddenApps,
                    accentColor = accentColor,
                    iconShape = iconShape,
                    iconSize = iconSize,
                    showAppLabels = showAppLabels,
                    onLaunchApp = onLaunchApp,
                    onUnhideApp = onUnhideApp,
                    onAddMoreClick = { showAddAppsSheet = true }
                )
            }
        }
    }

    // Modal chọn thêm ứng dụng để ẩn
    if (showAddAppsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val unhiddenApps = remember(allApps) { allApps.filter { !it.isHidden } }

        ModalBottomSheet(
            onDismissRequest = { showAddAppsSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Chọn ứng dụng để ẩn vào Két",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ứng dụng được ẩn sẽ biến mất hoàn toàn khỏi màn hình chính và danh sách thông thường.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(unhiddenApps) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable {
                                    onHideApp(app)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AppIconItem(
                                    app = app,
                                    iconShape = iconShape,
                                    iconSize = IconSize.COMPACT,
                                    showLabel = false,
                                    accentColor = accentColor,
                                    onClick = {},
                                    onLongClick = {}
                                )
                                Column {
                                    Text(
                                        text = app.displayLabel,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = app.category.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accentColor
                                    )
                                }
                            }

                            Button(
                                onClick = { onHideApp(app) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                            ) {
                                Text("Ẩn ngay", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog đổi mã PIN
    if (showChangePinDialog) {
        var newPinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Đổi Mã PIN Két Bảo Mật",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 4) newPinInput = it },
                            label = { Text("Mã PIN mới (4 số)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = { if (it.length <= 4) confirmPinInput = it },
                            label = { Text("Xác nhận mã PIN") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = errorMessage, color = Color(0xFFFF5252), fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { showChangePinDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            ) {
                                Text("Hủy", color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newPinInput.length != 4) {
                                        errorMessage = "PIN phải có đúng 4 chữ số"
                                    } else if (newPinInput != confirmPinInput) {
                                        errorMessage = "Mã xác nhận không khớp"
                                    } else {
                                        onChangePin(newPinInput)
                                        showChangePinDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                            ) {
                                Text("Lưu PIN", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinUnlockScreen(
    enteredPin: String,
    pinError: Boolean,
    accentColor: Color,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onQuickBiometric: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Security,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(52.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Két Ứng Dụng Bảo Mật",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = if (pinError) "Mã PIN không đúng! Thử lại." else "Nhập mã PIN 4 chữ số (Mặc định: 1234)",
            style = MaterialTheme.typography.bodySmall,
            color = if (pinError) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4 Chấm chỉ báo PIN
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val filled = i < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (pinError) Color(0xFFFF5252)
                            else if (filled) accentColor
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (filled) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Bàn phím số Numpad
        val keypad = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("BIO", "0", "DEL")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            keypad.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { key ->
                        when (key) {
                            "DEL" -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable { onDeleteClick() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Backspace,
                                        contentDescription = "Xóa",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            "BIO" -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable { onQuickBiometric() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Fingerprint,
                                        contentDescription = "Vân tay",
                                        tint = accentColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable { onDigitClick(key) }
                                        .testTag("pin_key_$key"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 22.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DisguisedCalculatorVault(
    accentColor: Color,
    onPinEntered: (String) -> Unit
) {
    var calcDisplay by remember { mutableStateOf("0") }
    var currentInputPin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Màn hình hiển thị máy tính
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF101216))
                .padding(20.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Máy tính tiêu chuẩn",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = calcDisplay,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.White,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phím máy tính
        val buttons = listOf(
            listOf("C", "±", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { label ->
                        val isOp = label in listOf("÷", "×", "-", "+", "=")
                        val isSpecial = label in listOf("C", "±", "%")
                        val weight = if (label == "0") 2f else 1f

                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .height(60.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    when {
                                        label == "=" -> accentColor
                                        isOp -> Color(0xFF263238)
                                        isSpecial -> Color(0xFF37474F)
                                        else -> Color(0xFF1C202A)
                                    }
                                )
                                .clickable {
                                    when (label) {
                                        "C" -> {
                                            calcDisplay = "0"
                                            currentInputPin = ""
                                        }
                                        "=" -> {
                                            // Kiểm tra xem chuỗi nhập có chứa mã PIN bí mật không!
                                            if (currentInputPin.isNotEmpty()) {
                                                onPinEntered(currentInputPin)
                                            }
                                        }
                                        in "0".."9" -> {
                                            currentInputPin = (currentInputPin + label).takeLast(4)
                                            calcDisplay = if (calcDisplay == "0") label else (calcDisplay + label).takeLast(10)
                                        }
                                        else -> {
                                            calcDisplay = (calcDisplay + label).takeLast(10)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = if (label == "=") Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnlockedVaultContent(
    hiddenApps: List<AppInfo>,
    accentColor: Color,
    iconShape: IconShape,
    iconSize: IconSize,
    showAppLabels: Boolean,
    onLaunchApp: (AppInfo) -> Unit,
    onUnhideApp: (AppInfo) -> Unit,
    onAddMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Banner thông tin bảo mật
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF00E676).copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Két đang mở khóa an toàn",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF00E676)
                    )
                    Text(
                        text = "Các ứng dụng dưới đây hoàn toàn vô hình với bất kỳ ai khác dùng điện thoại.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Button(
                    onClick = onAddMoreClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thêm", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (hiddenApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Chưa có ứng dụng nào trong Két",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onAddMoreClick,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Thêm ứng dụng bảo mật ngay", color = Color.Black, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(hiddenApps) { app ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppIconItem(
                            app = app,
                            iconShape = iconShape,
                            iconSize = iconSize,
                            showLabel = showAppLabels,
                            accentColor = accentColor,
                            onClick = { onLaunchApp(app) },
                            onLongClick = { onUnhideApp(app) }
                        )

                        Text(
                            text = "Bấm để mở • Giữ để hiện",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
