package com.minicount.app.data.premade

import com.minicount.app.data.local.entity.EventCategory
import com.minicount.app.data.local.entity.EventTemplate
import com.minicount.app.data.local.entity.RepeatInterval
import com.minicount.app.data.local.entity.WidgetStyle
import androidx.compose.ui.graphics.Color

/**
 * Premade content including templates, color themes, and widget configurations
 */
object PremadeContent {

    /**
     * 15 Premade Event Templates
     */
    val EVENT_TEMPLATES = listOf(
        EventTemplate(
            id = 1,
            name = "Birthday",
            category = EventCategory.BIRTHDAY,
            description = "Someone's special day",
            color = 0xFFFF6B9D.toInt(),
            widgetStyle = WidgetStyle.CLASSIC,
            isRepeating = true,
            repeatInterval = RepeatInterval.YEARLY,
            defaultReminderDays = 7
        ),
        EventTemplate(
            id = 2,
            name = "Wedding",
            category = EventCategory.WEDDING,
            description = "The big day",
            color = 0xFFFFD700.toInt(),
            widgetStyle = WidgetStyle.ELEGANT,
            isRepeating = false,
            repeatInterval = RepeatInterval.NONE,
            defaultReminderDays = 14
        ),
        EventTemplate(
            id = 3,
            name = "Anniversary",
            category = EventCategory.ANNIVERSARY,
            description = "Celebrate love",
            color = 0xFFFF1744.toInt(),
            widgetStyle = WidgetStyle.ELEGANT,
            isRepeating = true,
            repeatInterval = RepeatInterval.YEARLY,
            defaultReminderDays = 7
        ),
        EventTemplate(
            id = 4,
            name = "Vacation",
            category = EventCategory.VACATION,
            description = "Time to relax",
            color = 0xFF00BCD4.toInt(),
            widgetStyle = WidgetStyle.MODERN,
            isRepeating = false,
            repeatInterval = RepeatInterval.NONE,
            defaultReminderDays = 30
        ),
        EventTemplate(
            id = 5,
            name = "Graduation",
            category = EventCategory.GRADUATION,
            description = "Academic milestone",
            color = 0xFF4CAF50.toInt(),
            widgetStyle = WidgetStyle.BOLD,
            isRepeating = false,
            repeatInterval = RepeatInterval.NONE,
            defaultReminderDays = 60
        ),
        EventTemplate(
            id = 6,
            name = "Baby Due Date",
            category = EventCategory.BABY,
            description = "New arrival expected",
            color = 0xFFFFAB91.toInt(),
            widgetStyle = WidgetStyle.MINIMAL,
            isRepeating = false,
            repeatInterval = RepeatInterval.NONE,
            defaultReminderDays = 14
        ),
        EventTemplate(
            id = 7,
            name = "Exam",
            category = EventCategory.EXAM,
            description = "Test preparation",
            color = 0xFFFF6F00.toInt(),
            widgetStyle = WidgetStyle.BOLD,
            isRepeating = false,
            repeatInterval = RepeatInterval.NONE,
            defaultReminderDays = 3
        ),
        EventTemplate(
            id = 8,
            name = "Concert",
            category = EventCategory.CONCERT,
            description = "Live music event",
            color = 0xFF9C27B0.toInt(),
            widgetStyle = WidgetStyle.GRADIENT,
            isRepeating = false,
            repeatInterval = RepeatInterval.NONE,
            defaultReminderDays = 7
        ),
        EventTemplate(
            id = 9,
            name = "Sports Event",
            category = EventCategory.SPORTS,
            description = "Game day",
            color = 0xFF2196F3.toInt(),
            widgetStyle = WidgetStyle.BOLD,
            isRepeating = false,
            repeatInterval = RepeatInterval.NONE,
            defaultReminderDays = 1
        ),
        EventTemplate(
            id = 10,
            name = "Holiday",
            category = EventCategory.HOLIDAY,
            description = "Festive celebration",
            color = 0xFFD32F2F.toInt(),
            widgetStyle = WidgetStyle.CLASSIC,
            isRepeating = true,
            repeatInterval = RepeatInterval.YEARLY,
            defaultReminderDays = 14
        ),
        EventTemplate(
            id = 11,
            name = "Project Deadline",
            category = EventCategory.MEETING,
            description = "Work deadline",
            color = 0xFFF57C00.toInt(),
            widgetStyle = WidgetStyle.MINIMAL,
            isRepeating = false,
            repeatInterval = RepeatInterval.NONE,
            defaultReminderDays = 3
        ),
        EventTemplate(
            id = 12,
            name = "Retirement",
            category = EventCategory.OTHER,
            description = "New chapter begins",
            color = 0xFF7B1FA2.toInt(),
            widgetStyle = WidgetStyle.ELEGANT,
            isRepeating = false,
            repeatInterval = RepeatInterval.NONE,
            defaultReminderDays = 30
        ),
        EventTemplate(
            id = 13,
            name = "Moving Day",
            category = EventCategory.OTHER,
            description = "New home, new start",
            color = 0xFF5D4037.toInt(),
            widgetStyle = WidgetStyle.MODERN,
            isRepeating = false,
            repeatInterval = RepeatInterval.NONE,
            defaultReminderDays = 14
        ),
        EventTemplate(
            id = 14,
            name = "New Job Start",
            category = EventCategory.OTHER,
            description = "Career milestone",
            color = 0xFF388E3C.toInt(),
            widgetStyle = WidgetStyle.BOLD,
            isRepeating = false,
            repeatInterval = RepeatInterval.NONE,
            defaultReminderDays = 7
        ),
        EventTemplate(
            id = 15,
            name = "Diet/Fitness Start",
            category = EventCategory.OTHER,
            description = "Health journey begins",
            color = 0xFF00897B.toInt(),
            widgetStyle = WidgetStyle.MODERN,
            isRepeating = false,
            repeatInterval = RepeatInterval.NONE,
            defaultReminderDays = 1
        )
    )

    /**
     * Color Themes
     */
    data class ColorTheme(
        val id: String,
        val name: String,
        val isPremium: Boolean = false,
        val price: String = "",
        val primary: Int,
        val secondary: Int,
        val accent: Int,
        val description: String
    )

    /**
     * Free Color Themes (8)
     */
    val FREE_THEMES = listOf(
        ColorTheme(
            id = "material_purple",
            name = "Material Purple",
            isPremium = false,
            primary = 0xFF6200EE.toInt(),
            secondary = 0xFF03DAC5.toInt(),
            accent = 0xFFBB86FC.toInt(),
            description = "Classic Material Design"
        ),
        ColorTheme(
            id = "ocean_blue",
            name = "Ocean Blue",
            isPremium = false,
            primary = 0xFF0277BD.toInt(),
            secondary = 0xFF00ACC1.toInt(),
            accent = 0xFF4DD0E1.toInt(),
            description = "Deep ocean vibes"
        ),
        ColorTheme(
            id = "forest_green",
            name = "Forest Green",
            isPremium = false,
            primary = 0xFF2E7D32.toInt(),
            secondary = 0xFF43A047.toInt(),
            accent = 0xFF66BB6A.toInt(),
            description = "Natural forest colors"
        ),
        ColorTheme(
            id = "sunset_orange",
            name = "Sunset Orange",
            isPremium = false,
            primary = 0xFFE65100.toInt(),
            secondary = 0xFFFF6F00.toInt(),
            accent = 0xFFFF9800.toInt(),
            description = "Warm sunset tones"
        ),
        ColorTheme(
            id = "rose_pink",
            name = "Rose Pink",
            isPremium = false,
            primary = 0xFFC2185B.toInt(),
            secondary = 0xFFE91E63.toInt(),
            accent = 0xFFF06292.toInt(),
            description = "Romantic rose shades"
        ),
        ColorTheme(
            id = "night_black",
            name = "Night Black",
            isPremium = false,
            primary = 0xFF212121.toInt(),
            secondary = 0xFF424242.toInt(),
            accent = 0xFF757575.toInt(),
            description = "Sleek dark theme"
        ),
        ColorTheme(
            id = "cloud_gray",
            name = "Cloud Gray",
            isPremium = false,
            primary = 0xFF546E7A.toInt(),
            secondary = 0xFF78909C.toInt(),
            accent = 0xFF90A4AE.toInt(),
            description = "Soft neutral tones"
        ),
        ColorTheme(
            id = "crimson_red",
            name = "Crimson Red",
            isPremium = false,
            primary = 0xFFC62828.toInt(),
            secondary = 0xFFE53935.toInt(),
            accent = 0xFFEF5350.toInt(),
            description = "Bold red accent"
        )
    )

    /**
     * Premium Color Themes (12) - $0.49 each or $2.99 bundle
     */
    val PREMIUM_THEMES = listOf(
        ColorTheme(
            id = "aurora",
            name = "Aurora Borealis",
            isPremium = true,
            price = "$0.49",
            primary = 0xFF1A237E.toInt(),
            secondary = 0xFF00E5FF.toInt(),
            accent = 0xFF76FF03.toInt(),
            description = "Northern lights inspired"
        ),
        ColorTheme(
            id = "cherry_blossom",
            name = "Cherry Blossom",
            isPremium = true,
            price = "$0.49",
            primary = 0xFFFCE4EC.toInt(),
            secondary = 0xFFF8BBD0.toInt(),
            accent = 0xFFF48FB1.toInt(),
            description = "Soft Japanese sakura"
        ),
        ColorTheme(
            id = "tropical",
            name = "Tropical Paradise",
            isPremium = true,
            price = "$0.49",
            primary = 0xFF006064.toInt(),
            secondary = 0xFF00BFA5.toInt(),
            accent = 0xFF1DE9B6.toInt(),
            description = "Vibrant tropical colors"
        ),
        ColorTheme(
            id = "autumn",
            name = "Autumn Harvest",
            isPremium = true,
            price = "$0.49",
            primary = 0xFFBF360C.toInt(),
            secondary = 0xFFE64A19.toInt(),
            accent = 0xFFFF6E40.toInt(),
            description = "Warm fall colors"
        ),
        ColorTheme(
            id = "midnight",
            name = "Midnight Galaxy",
            isPremium = true,
            price = "$0.49",
            primary = 0xFF1A237E.toInt(),
            secondary = 0xFF512DA8.toInt(),
            accent = 0xFF7E57C2.toInt(),
            description = "Deep space purples"
        ),
        ColorTheme(
            id = "lemon_lime",
            name = "Lemon Lime",
            isPremium = true,
            price = "$0.49",
            primary = 0xFFC0CA33.toInt(),
            secondary = 0xFFD4E157.toInt(),
            accent = 0xFFFFEE58.toInt(),
            description = "Citrus fresh"
        ),
        ColorTheme(
            id = "lavender",
            name = "Lavender Dreams",
            isPremium = true,
            price = "$0.49",
            primary = 0xFF7B1FA2.toInt(),
            secondary = 0xFF9C27B0.toInt(),
            accent = 0xFFBA68C8.toInt(),
            description = "Soft purple tones"
        ),
        ColorTheme(
            id = "coral",
            name = "Coral Reef",
            isPremium = true,
            price = "$0.49",
            primary = 0xFFD84315.toInt(),
            secondary = 0xFFFF5722.toInt(),
            accent = 0xFFFF7043.toInt(),
            description = "Underwater coral"
        ),
        ColorTheme(
            id = "desert",
            name = "Desert Sunset",
            isPremium = true,
            price = "$0.49",
            primary = 0xFF6D4C41.toInt(),
            secondary = 0xFF8D6E63.toInt(),
            accent = 0xFFA1887F.toInt(),
            description = "Earthy sand tones"
        ),
        ColorTheme(
            id = "arctic",
            name = "Arctic Ice",
            isPremium = true,
            price = "$0.49",
            primary = 0xFF01579B.toInt(),
            secondary = 0xFF0277BD.toInt(),
            accent = 0xFF29B6F6.toInt(),
            description = "Cool icy blues"
        ),
        ColorTheme(
            id = "candy",
            name = "Candy Pop",
            isPremium = true,
            price = "$0.49",
            primary = 0xFFAD1457.toInt(),
            secondary = 0xFFEC407A.toInt(),
            accent = 0xFFFF4081.toInt(),
            description = "Bright candy colors"
        ),
        ColorTheme(
            id = "monochrome",
            name = "Monochrome Pro",
            isPremium = true,
            price = "$0.49",
            primary = 0xFF263238.toInt(),
            secondary = 0xFF455A64.toInt(),
            accent = 0xFF607D8B.toInt(),
            description = "Elegant grayscale"
        )
    )

    /**
     * In-App Purchase Product IDs
     */
    object ProductIds {
        // Premium Unlock
        const val PREMIUM_UNLIMITED = "premium_unlimited"

        // Widget Styles
        const val WIDGET_NEON = "widget_neon"
        const val WIDGET_GLASS = "widget_glass"
        const val WIDGET_NEURO = "widget_neuro"
        const val WIDGET_RETRO = "widget_retro"
        const val WIDGET_COSMIC = "widget_cosmic"
        const val WIDGET_NATURE = "widget_nature"
        const val WIDGET_LUXURY = "widget_luxury"
        const val WIDGET_HANDWRITTEN = "widget_handwritten"
        const val WIDGET_CYBERPUNK = "widget_cyberpunk"
        const val WIDGET_MINIMALIST_PRO = "widget_minimalist_pro"

        // Bundles
        const val WIDGET_BUNDLE = "widget_premium_bundle"
        const val THEME_BUNDLE = "theme_premium_bundle"
        const val ULTIMATE_BUNDLE = "ultimate_bundle"

        // Color Themes
        const val THEME_AURORA = "theme_aurora"
        const val THEME_CHERRY = "theme_cherry_blossom"
        const val THEME_TROPICAL = "theme_tropical"
        const val THEME_AUTUMN = "theme_autumn"
        const val THEME_MIDNIGHT = "theme_midnight"
        const val THEME_LEMON = "theme_lemon_lime"
        const val THEME_LAVENDER = "theme_lavender"
        const val THEME_CORAL = "theme_coral"
        const val THEME_DESERT = "theme_desert"
        const val THEME_ARCTIC = "theme_arctic"
        const val THEME_CANDY = "theme_candy"
        const val THEME_MONOCHROME = "theme_monochrome"
    }

    /**
     * Product pricing
     */
    object Pricing {
        const val PREMIUM_UNLIMITED = 1_990_000L // $1.99 in micros
        const val WIDGET_STYLE = 990_000L // $0.99 in micros
        const val COLOR_THEME = 490_000L // $0.49 in micros
        const val WIDGET_BUNDLE = 4_990_000L // $4.99 in micros (50% off)
        const val THEME_BUNDLE = 2_990_000L // $2.99 in micros (50% off)
        const val ULTIMATE_BUNDLE = 9_990_000L // $9.99 in micros (50% off all)
    }
}
