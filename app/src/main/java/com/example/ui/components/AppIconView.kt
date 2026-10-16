package com.example.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Note
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.model.AppInfo
import com.example.data.model.CamouflageType
import com.example.data.model.IconPackStyle
import com.example.data.model.IconShape
import com.example.data.model.IconSize
import com.example.data.model.ThemeStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconItem(
    app: AppInfo,
    iconShape: IconShape,
    iconSize: IconSize,
    showLabel: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconPackStyle: IconPackStyle = IconPackStyle.THEME_ADAPTIVE,
    themeStyle: ThemeStyle = ThemeStyle.AMOLED_BLACK
) {
    val sizeDp = iconSize.dpSize.dp
    val shape = rememberPackShape(iconPackStyle, iconShape)

    // Tính toán màu nền, viền và gradient biến đổi theo từng bộ icon & theme
    val (containerBackground, borderModifier) = rememberIconPackStyling(
        packStyle = iconPackStyle,
        themeStyle = themeStyle,
        accentColor = accentColor,
        isCamouflage = app.camouflageType != CamouflageType.NONE,
        camoColorHex = app.camouflageType.colorHex,
        shape = shape
    )

    Column(
        modifier = modifier
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .width(76.dp)
            .testTag("app_item_${app.packageName}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(sizeDp)
                .then(borderModifier)
                .clip(shape)
                .background(containerBackground)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = (sizeDp / 2) + 6.dp),
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (app.camouflageType != CamouflageType.NONE) {
                // Biểu tượng ngụy trang (Camouflage stealth icon)
                CamouflageIconContent(
                    camouflageType = app.camouflageType,
                    size = sizeDp * 0.52f
                )
            } else {
                // Biểu tượng ứng dụng thực tế hoặc fallback cách điệu
                RealOrFallbackIcon(
                    app = app,
                    iconShape = iconShape,
                    iconPackStyle = iconPackStyle,
                    themeStyle = themeStyle,
                    accentColor = accentColor,
                    sizeDp = sizeDp
                )
            }

            // Dấu ghim nhỏ tinh tế ở góc nếu app đã được Ghim vào Màn hình chính
            if (app.isPinnedHome) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(2.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PushPin,
                        contentDescription = "Đã ghim",
                        tint = Color.Black,
                        modifier = Modifier.size(9.dp)
                    )
                }
            }

            // Dấu chấm đỏ nhỏ nếu là ứng dụng ẩn trong Két
            if (app.isHidden) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5252))
                )
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = app.displayLabel,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = if (themeStyle == ThemeStyle.CLEAN_LIGHT) Color(0xFF1E293B) else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
private fun rememberIconPackStyling(
    packStyle: IconPackStyle,
    themeStyle: ThemeStyle,
    accentColor: Color,
    isCamouflage: Boolean,
    camoColorHex: Long,
    shape: Shape
): Pair<Brush, Modifier> {
    return remember(packStyle, themeStyle, accentColor, isCamouflage, camoColorHex) {
        if (isCamouflage) {
            val camoColor = Color(camoColorHex)
            val brush = Brush.verticalGradient(
                listOf(camoColor.copy(alpha = 0.28f), camoColor.copy(alpha = 0.12f))
            )
            val borderMod = Modifier.border(1.dp, camoColor.copy(alpha = 0.6f), shape)
            return@remember Pair(brush, borderMod)
        }

        when (packStyle) {
            IconPackStyle.THEME_ADAPTIVE -> {
                when (themeStyle) {
                    ThemeStyle.AMOLED_BLACK -> {
                        val brush = Brush.verticalGradient(listOf(Color(0xFF131720), Color(0xFF090B0E)))
                        val borderMod = Modifier.border(1.dp, accentColor.copy(alpha = 0.35f), shape)
                        Pair(brush, borderMod)
                    }
                    ThemeStyle.CHARCOAL_DARK -> {
                        val brush = Brush.verticalGradient(listOf(Color(0xFF262C36), Color(0xFF181C23)))
                        val borderMod = Modifier.border(1.dp, Color(0xFF3E4756), shape)
                        Pair(brush, borderMod)
                    }
                    ThemeStyle.MIDNIGHT_NAVY -> {
                        val brush = Brush.verticalGradient(listOf(Color(0xFF14223D), Color(0xFF0A1120)))
                        val borderMod = Modifier.border(1.dp, accentColor.copy(alpha = 0.5f), shape)
                        Pair(brush, borderMod)
                    }
                    ThemeStyle.CLEAN_LIGHT -> {
                        val brush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0)))
                        val borderMod = Modifier.border(1.dp, Color(0xFFCBD5E1), shape)
                        Pair(brush, borderMod)
                    }
                }
            }

            IconPackStyle.CYBER_NEON -> {
                val brush = Brush.verticalGradient(listOf(Color(0xFF0A0F1D), Color(0xFF03050A)))
                val borderBrush = Brush.linearGradient(
                    listOf(accentColor, Color(0xFFFF007F), accentColor)
                )
                val borderMod = Modifier.border(1.8.dp, borderBrush, shape)
                Pair(brush, borderMod)
            }

            IconPackStyle.GLASS_FROST -> {
                val brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.06f))
                )
                val borderMod = Modifier.border(1.2.dp, Color.White.copy(alpha = 0.4f), shape)
                Pair(brush, borderMod)
            }

            IconPackStyle.ANDROID_TV_CLASSIC -> {
                val brush = Brush.radialGradient(
                    listOf(Color(0xFF2B3445), Color(0xFF181E29))
                )
                val borderMod = Modifier.border(1.dp, Color.White.copy(alpha = 0.15f), shape)
                Pair(brush, borderMod)
            }

            IconPackStyle.MINIMAL_OUTLINE -> {
                val brush = Brush.verticalGradient(listOf(Color.Black, Color.Black))
                val borderMod = Modifier.border(1.dp, accentColor, shape)
                Pair(brush, borderMod)
            }

            IconPackStyle.RETRO_PIXEL -> {
                val brush = Brush.verticalGradient(listOf(Color(0xFF1F1A30), Color(0xFF0D0B16)))
                val borderMod = Modifier.border(2.dp, Color(0xFFFFD700), shape)
                Pair(brush, borderMod)
            }
        }
    }
}

@Composable
private fun rememberPackShape(packStyle: IconPackStyle, baseShape: IconShape): Shape {
    return remember(packStyle, baseShape) {
        when (packStyle) {
            IconPackStyle.RETRO_PIXEL -> CutCornerShape(8.dp)
            IconPackStyle.ANDROID_TV_CLASSIC -> RoundedCornerShape(28)
            else -> when (baseShape) {
                IconShape.SQUIRCLE -> RoundedCornerShape(26)
                IconShape.CIRCLE -> CircleShape
                IconShape.ROUNDED_SQUARE -> RoundedCornerShape(18)
                IconShape.TEARDROP -> RoundedCornerShape(topStart = 4.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 24.dp)
                IconShape.MINIMAL_MONO -> RoundedCornerShape(20)
            }
        }
    }
}

@Composable
private fun RealOrFallbackIcon(
    app: AppInfo,
    iconShape: IconShape,
    iconPackStyle: IconPackStyle,
    themeStyle: ThemeStyle,
    accentColor: Color,
    sizeDp: androidx.compose.ui.unit.Dp
) {
    val context = LocalContext.current
    val appIconDrawable by produceState<Drawable?>(initialValue = null, key1 = app.packageName) {
        value = withContext(Dispatchers.IO) {
            try {
                context.packageManager.getApplicationIcon(app.packageName)
            } catch (e: Exception) {
                null
            }
        }
    }

    if (appIconDrawable != null && iconShape != IconShape.MINIMAL_MONO && iconPackStyle != IconPackStyle.MINIMAL_OUTLINE) {
        val bitmap = remember(appIconDrawable) {
            try {
                appIconDrawable?.toBitmap(96, 96)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap,
                contentDescription = app.displayLabel,
                modifier = Modifier.size(sizeDp * 0.82f)
            )
            return
        }
    }

    // Fallback vector glyph với màu gradient tinh tế thích ứng theo chủ đề
    val initialChar = app.displayLabel.firstOrNull()?.uppercase() ?: "A"
    val fallbackGradient = remember(app.packageName, accentColor) {
        generateColorForPackage(app.packageName, accentColor)
    }

    Box(
        modifier = Modifier
            .size(sizeDp)
            .background(
                if (iconShape == IconShape.MINIMAL_MONO || iconPackStyle == IconPackStyle.MINIMAL_OUTLINE) {
                    Brush.verticalGradient(listOf(Color(0xFF1E222D), Color(0xFF11141C)))
                } else {
                    Brush.verticalGradient(fallbackGradient)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initialChar,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = (sizeDp.value * 0.44f).sp
            ),
            color = if (iconShape == IconShape.MINIMAL_MONO || iconPackStyle == IconPackStyle.MINIMAL_OUTLINE) accentColor else Color.White
        )
    }
}

@Composable
fun CamouflageIconContent(
    camouflageType: CamouflageType,
    size: androidx.compose.ui.unit.Dp
) {
    val vector: ImageVector = when (camouflageType) {
        CamouflageType.CALCULATOR -> Icons.Rounded.Calculate
        CamouflageType.NOTES -> Icons.Rounded.Note
        CamouflageType.CLOCK -> Icons.Rounded.Schedule
        CamouflageType.WEATHER -> Icons.Rounded.Cloud
        CamouflageType.FILES -> Icons.Rounded.Folder
        CamouflageType.COMPASS -> Icons.Rounded.Explore
        CamouflageType.SETTINGS -> Icons.Rounded.Tune
        CamouflageType.RADIO -> Icons.Rounded.Radio
        CamouflageType.NONE -> Icons.Rounded.Shield
    }

    Icon(
        imageVector = vector,
        contentDescription = camouflageType.disguiseTitle,
        tint = Color(camouflageType.colorHex),
        modifier = Modifier.size(size)
    )
}

@Composable
fun rememberIconShape(shape: IconShape): Shape {
    return remember(shape) {
        when (shape) {
            IconShape.SQUIRCLE -> RoundedCornerShape(26)
            IconShape.CIRCLE -> CircleShape
            IconShape.ROUNDED_SQUARE -> RoundedCornerShape(18)
            IconShape.TEARDROP -> RoundedCornerShape(topStart = 4.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 24.dp)
            IconShape.MINIMAL_MONO -> RoundedCornerShape(22)
        }
    }
}

private fun generateColorForPackage(pkg: String, accentColor: Color): List<Color> {
    val hash = pkg.hashCode()
    return when (Math.abs(hash % 6)) {
        0 -> listOf(Color(0xFF2196F3), Color(0xFF1565C0))
        1 -> listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
        2 -> listOf(Color(0xFFFF9800), Color(0xFFE65100))
        3 -> listOf(Color(0xFFE91E63), Color(0xFFAD1457))
        4 -> listOf(Color(0xFF9C27B0), Color(0xFF6A1B9A))
        else -> listOf(Color(0xFF00BCD4), Color(0xFF00838F))
    }
}
