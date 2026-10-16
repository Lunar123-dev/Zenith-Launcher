package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Danh mục ứng dụng được phân loại thông minh theo thói quen và mục đích sử dụng.
 */
enum class AppCategory(val title: String, val icon: ImageVector) {
    ALL("Tất cả", Icons.Rounded.Apps),
    FREQUENT("Thói quen", Icons.Rounded.AutoAwesome),
    MEDIA("Giải trí & Phim", Icons.Rounded.PlayCircle),
    SOCIAL("Mạng xã hội", Icons.Rounded.Forum),
    TOOLS("Công cụ", Icons.Rounded.Build),
    PRODUCTIVITY("Năng suất", Icons.Rounded.WorkOutline),
    FINANCE("Tài chính & Ví", Icons.Rounded.AccountBalanceWallet)
}

/**
 * Kiểu hình dáng biểu tượng (Icon Shape)
 */
enum class IconShape(val label: String) {
    SQUIRCLE("Bo tròn tinh tế"),
    CIRCLE("Hình tròn"),
    ROUNDED_SQUARE("Vuông bo góc"),
    TEARDROP("Giọt nước"),
    MINIMAL_MONO("Đơn sắc tối giản")
}

/**
 * Kích thước icon
 */
enum class IconSize(val label: String, val dpSize: Int) {
    COMPACT("Nhỏ gọn (44dp)", 44),
    STANDARD("Tiêu chuẩn (50dp)", 50),
    LARGE("Lớn nổi bật (58dp)", 58)
}

/**
 * Chế độ giao diện (Dark mode & AMOLED tiết kiệm pin)
 */
enum class ThemeStyle(val label: String, val description: String) {
    AMOLED_BLACK("AMOLED Đen tuyền", "Tiết kiệm 100% điện năng màn hình OLED / TV"),
    CHARCOAL_DARK("Xám than chì", "Hiện đại, dịu mắt và tương phản cao"),
    MIDNIGHT_NAVY("Xanh bóng đêm", "Phong cách huyền bí, công nghệ"),
    CLEAN_LIGHT("Sáng tối giản", "Thanh lịch và sắc nét")
}

/**
 * Màu sắc điểm nhấn (Accent Colors)
 */
enum class AccentColorOption(val label: String, val hex: Long) {
    CYAN_NEON("Xanh Neon Cyan", 0xFF00E5FF),
    EMERALD_GREEN("Xanh Lục Bảo", 0xFF00E676),
    VIOLET_ELECTRIC("Tím Tương Lai", 0xFFB388FF),
    SUNSET_CORAL("Cam Hoàng Hôn", 0xFFFF6E40),
    GOLD_AMBER("Vàng Hổ Phách", 0xFFFFD700),
    SILVER_WHITE("Bạc Tinh Khiết", 0xFFECEFF1)
}

/**
 * Tùy chọn hướng màn hình
 */
enum class OrientationMode(val label: String) {
    AUTO("Tự động thích ứng thiết bị"),
    FORCE_LANDSCAPE("Luôn nằm ngang (Phong cách TV)"),
    PORTRAIT("Màn hình dọc điện thoại")
}

/**
 * Hình nền tích hợp phong cách AMOLED tối giản & TV Cinematic
 */
enum class WallpaperOption(val label: String, val resName: String?) {
    TV_CINEMATIC("Vũ Trụ Cực Quang TV", "wallpaper_tv_cinematic"),
    AMOLED_BLACK("Đen Tuyền AMOLED", null),
    NEON_HORIZON("Chân Trời Neon", "wallpaper_neon_zenith"),
    ZEN_MOON("Trăng Khuyết Tĩnh Lặng", "wallpaper_zen_moon"),
    AURORA_GRADIENT("Cực Quang Tối", null),
    CYBER_GRID("Lưới Ma Trận Tối", null),
    CUSTOM_IMAGE("Ảnh từ Thư viện / Tệp", null)
}

/**
 * Biểu tượng ngụy trang (Camouflage Icons) cho ứng dụng bí mật
 */
enum class CamouflageType(
    val disguiseTitle: String,
    val iconName: String,
    val colorHex: Long
) {
    NONE("Mặc định", "default", 0xFF00E5FF),
    CALCULATOR("Máy tính", "calculate", 0xFF4CAF50),
    NOTES("Ghi chú", "note", 0xFFFF9800),
    CLOCK("Đồng hồ", "schedule", 0xFF2196F3),
    WEATHER("Thời tiết", "cloud", 0xFF00BCD4),
    FILES("Tập tin", "folder", 0xFF9C27B0),
    COMPASS("La bàn", "explore", 0xFFE91E63),
    SETTINGS("Cài đặt phụ", "tune", 0xFF607D8B),
    RADIO("Đài FM", "radio", 0xFF795548)
}

/**
 * Bộ biểu tượng riêng có thể biến đổi theo từng chủ đề (Theme Morphing Icon Packs)
 */
enum class IconPackStyle(val label: String, val description: String) {
    THEME_ADAPTIVE("Tự động theo Chủ đề", "Icon đổi màu, viền neon/gradient và độ mờ ăn khớp trực tiếp theo Theme & Accent đã chọn"),
    ANDROID_TV_CLASSIC("Android TV Cổ điển", "Khung squircle gradient mờ tinh tế phong cách Leanback TV nguyên bản"),
    CYBER_NEON("Cyberpunk Neon Glow", "Viền phát quang dạ quang sắc nét với hiệu ứng đổ bóng neon rực rỡ"),
    GLASS_FROST("Thủy tinh Mờ Frost", "Kính mờ xuyên thấu sang trọng phong cách Frosted Glassmorphism"),
    MINIMAL_OUTLINE("Đơn sắc Tối giản", "Biểu tượng vector phẳng hiện đại trên nền đen AMOLED tiết kiệm pin"),
    RETRO_PIXEL("Arcade 8-Bit Retro", "Đậm chất hoài niệm máy chơi game TV cổ điển")
}

/**
 * Vị trí đặt thông tin hệ thống và widget trên màn hình
 */
enum class InfoPlacement(val label: String, val description: String) {
    TOP_BAR("Thanh trên cùng (Android TV)", "Đồng hồ, mạng, bluetooth và thiết bị hiển thị gọn trên thanh tiêu đề"),
    HERO_WIDGET_ROW("Dải Widget trung tâm", "Hiển thị dạng thẻ lớn nổi bật ngay trên các hàng ứng dụng"),
    BOTTOM_BAR("Thanh thông tin bên dưới", "Widget và trạng thái đặt cố định ở thanh đáy màn hình"),
    SIDE_PANEL("Cột thông tin bên góc", "Cột tóm tắt nhanh trạng thái hệ thống và gia đình"),
    MINIMAL_HIDDEN("Ẩn bớt để tối giản", "Chỉ hiển thị các biểu tượng cần thiết nhất")
}

/**
 * Các nguồn cổng đầu vào TV (TV Input Sources)
 */
data class TvInputSource(
    val id: String,
    val name: String,
    val customAlias: String = "",
    val isConnected: Boolean = true,
    val isArc: Boolean = false,
    val inputType: TvInputType = TvInputType.HDMI
) {
    val displayLabel: String
        get() = if (customAlias.isNotBlank()) customAlias else name
}

enum class TvInputType(val label: String) {
    HDMI("Cổng HDMI"),
    AV("Cổng AV / RCA"),
    TV_TUNER("Truyền hình DVB-T2 / Anten"),
    USB_MEDIA("Ổ đĩa USB / Ổ cứng"),
    OPTICAL("Cổng âm thanh quang")
}

/**
 * Widget thông minh trên màn hình chính
 */
enum class SmartWidgetType(val label: String, val shortTitle: String) {
    FAMILY_DEVICES("Thiết bị Gia đình từ xa", "Gia đình"),
    TV_INPUTS("Cổng đầu vào TV (HDMI/AV)", "Cổng TV"),
    NETWORK_BLUETOOTH("Mạng & Bluetooth", "Kết nối"),
    CLOCK_WEATHER("Đồng hồ & Thời tiết", "Thời tiết"),
    SYSTEM_STATS("Hiệu năng & Pin", "Hệ thống"),
    STICKY_NOTE("Ghi chú nhanh Zen", "Ghi chú")
}

/**
 * Thông tin chi tiết một ứng dụng trong Launcher
 */
data class AppInfo(
    val packageName: String,
    val activityName: String = "",
    val defaultLabel: String,
    val customLabel: String = "",
    val camouflageType: CamouflageType = CamouflageType.NONE,
    val category: AppCategory = AppCategory.TOOLS,
    val launchCount: Int = 0,
    val lastLaunchedTime: Long = 0L,
    val isHidden: Boolean = false,
    val isPinnedHome: Boolean = false,
    val homeOrder: Int = 0,
    val isSystemApp: Boolean = false
) {
    val displayLabel: String
        get() = when {
            camouflageType != CamouflageType.NONE && customLabel.isBlank() -> camouflageType.disguiseTitle
            customLabel.isNotBlank() -> customLabel
            else -> defaultLabel
        }
}

/**
 * Tùy chọn cài đặt toàn diện cho Launcher
 */
data class LauncherSettings(
    val themeStyle: ThemeStyle = ThemeStyle.AMOLED_BLACK,
    val accentColor: AccentColorOption = AccentColorOption.CYAN_NEON,
    val iconShape: IconShape = IconShape.SQUIRCLE,
    val iconSize: IconSize = IconSize.STANDARD,
    val iconPackStyle: IconPackStyle = IconPackStyle.THEME_ADAPTIVE,
    val infoPlacement: InfoPlacement = InfoPlacement.TOP_BAR,
    val showAppLabels: Boolean = true,
    val gridColumns: Int = 4,
    val wallpaper: WallpaperOption = WallpaperOption.TV_CINEMATIC,
    val customWallpaperPath: String? = null,
    val orientationMode: OrientationMode = OrientationMode.AUTO,
    val extremeBatterySaver: Boolean = false,
    val vaultPin: String = "1234",
    val disguiseCalculatorEnabled: Boolean = true,
    val activeWidget: SmartWidgetType = SmartWidgetType.FAMILY_DEVICES,
    val stickyNoteText: String = "Zenith TV Launcher: Tự động vừa khít mọi màn hình, mượt mà và bảo mật.",
    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val showWeather: Boolean = true,
    val showNetworkStatus: Boolean = true,
    val showBluetoothStatus: Boolean = true,
    val showBatteryStatus: Boolean = true,
    val showFamilyDevicesWidget: Boolean = true,
    val showTvInputsShortcut: Boolean = true,
    val activeTvInputId: String = "hdmi_1"
)
