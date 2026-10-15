package com.example.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Note
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.data.model.IconShape
import com.example.data.model.IconSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppIconItem(
    app: AppInfo,
    iconShape: IconShape,
    iconSize: IconSize,
    showLabel: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shape = rememberIconShape(iconShape)
    val sizeDp = iconSize.dpSize.dp

    Column(
        modifier = modifier
            .padding(vertical = 6.dp, horizontal = 2.dp)
            .width(72.dp)
            .testTag("app_item_${app.packageName}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(sizeDp)
                .clip(shape)
                .background(
                    if (app.camouflageType != CamouflageType.NONE) {
                        Color(app.camouflageType.colorHex).copy(alpha = 0.2f)
                    } else if (iconShape == IconShape.MINIMAL_MONO) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        Color(0xFF1E222D)
                    }
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = (sizeDp / 2) + 4.dp),
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (app.camouflageType != CamouflageType.NONE) {
                // Biểu tượng ngụy trang (Camouflage stealth icon)
                CamouflageIconContent(
                    camouflageType = app.camouflageType,
                    size = sizeDp * 0.55f
                )
            } else {
                // Biểu tượng ứng dụng thông thường
                RealOrFallbackIcon(
                    app = app,
                    iconShape = iconShape,
                    accentColor = accentColor,
                    sizeDp = sizeDp
                )
            }

            // Dấu chấm nhỏ tinh tế nếu là app trong két hoặc có ghim
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
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
private fun RealOrFallbackIcon(
    app: AppInfo,
    iconShape: IconShape,
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

    if (appIconDrawable != null && iconShape != IconShape.MINIMAL_MONO) {
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
                modifier = Modifier.size(sizeDp * 0.85f)
            )
            return
        }
    }

    // Fallback vector glyph với màu gradient tinh tế
    val initialChar = app.displayLabel.firstOrNull()?.uppercase() ?: "A"
    val fallbackGradient = remember(app.packageName) {
        generateColorForPackage(app.packageName, accentColor)
    }

    Box(
        modifier = Modifier
            .size(sizeDp)
            .background(
                if (iconShape == IconShape.MINIMAL_MONO) {
                    Brush.verticalGradient(
                        listOf(Color(0xFF2A2E39), Color(0xFF1B1E26))
                    )
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
                fontSize = (sizeDp.value * 0.42f).sp
            ),
            color = if (iconShape == IconShape.MINIMAL_MONO) accentColor else Color.White
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
