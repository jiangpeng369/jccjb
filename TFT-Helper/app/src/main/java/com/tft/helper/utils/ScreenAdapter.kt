package com.tft.helper.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * 屏幕适配工具类
 * 
 * 提供屏幕尺寸获取、dp/px转换等功能
 * 支持自适应不同屏幕尺寸
 */
object ScreenAdapter {

    // 设计稿基准尺寸（以常见手机尺寸为基准）
    private const val DESIGN_WIDTH = 375   // 设计稿宽度(dp)
    private const val DESIGN_HEIGHT = 812  // 设计稿高度(dp)

    // 实际屏幕尺寸
    private var screenWidthDp: Float = 0f
    private var screenHeightDp: Float = 0f
    private var screenDensity: Float = 0f
    private var screenDpi: Int = 0

    /**
     * 初始化屏幕参数
     * 应在Application或MainActivity中调用
     */
    fun init(context: Context) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)

        screenDensity = metrics.density
        screenDpi = metrics.densityDpi
        screenWidthDp = metrics.widthPixels / screenDensity
        screenHeightDp = metrics.heightPixels / screenDensity
    }

    /**
     * 获取屏幕宽度（px）
     */
    fun getScreenWidthPx(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    /**
     * 获取屏幕高度（px）
     */
    fun getScreenHeightPx(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    /**
     * 获取屏幕宽度（dp）
     */
    fun getScreenWidthDp(): Float = screenWidthDp

    /**
     * 获取屏幕高度（dp）
     */
    fun getScreenHeightDp(): Float = screenHeightDp

    /**
     * dp转px
     */
    fun dp2px(dp: Float): Int {
        return (dp * screenDensity + 0.5f).toInt()
    }

    /**
     * px转dp
     */
    fun px2dp(px: Float): Float {
        return px / screenDensity
    }

    /**
     * sp转px
     */
    fun sp2px(context: Context, sp: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (sp * fontScale + 0.5f).toInt()
    }

    /**
     * 根据屏幕宽度自适应尺寸
     * 按照设计稿比例缩放
     * 
     * @param designSize 设计稿上的尺寸(dp)
     * @return 适配后的尺寸(px)
     */
    fun adaptWidth(designSize: Float): Int {
        val ratio = screenWidthDp / DESIGN_WIDTH
        return dp2px(designSize * ratio)
    }

    /**
     * 根据屏幕高度自适应尺寸
     * 
     * @param designSize 设计稿上的尺寸(dp)
     * @return 适配后的尺寸(px)
     */
    fun adaptHeight(designSize: Float): Int {
        val ratio = screenHeightDp / DESIGN_HEIGHT
        return dp2px(designSize * ratio)
    }

    /**
     * 获取网格列数
     * 根据屏幕宽度自动计算
     * 
     * @param itemMinWidthDp 每个项目的最小宽度(dp)
     * @return 建议的列数
     */
    fun calculateSpanCount(itemMinWidthDp: Float): Int {
        val itemWidthPx = dp2px(itemMinWidthDp)
        val screenWidthPx = (screenWidthDp * screenDensity).toInt()
        return (screenWidthPx / itemWidthPx).coerceAtLeast(1)
    }

    /**
     * 判断是否为平板设备
     */
    fun isTablet(context: Context): Boolean {
        val configuration = context.resources.configuration
        val screenLayout = configuration.screenLayout
        return (screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK) >=
                android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    /**
     * 判断是否为横屏
     */
    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation ==
                android.content.res.Configuration.ORIENTATION_LANDSCAPE
    }

    /**
     * 获取状态栏高度
     */
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier(
            "status_bar_height", "dimen", "android"
        )
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            dp2px(24f)  // 默认值24dp
        }
    }

    /**
     * 获取导航栏高度
     */
    fun getNavigationBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier(
            "navigation_bar_height", "dimen", "android"
        )
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    /**
     * 屏幕尺寸分类
     */
    enum class ScreenSize {
        SMALL,      // 小屏 < 360dp
        NORMAL,     // 标准 360-400dp
        LARGE,      // 大屏 400-480dp
        XLARGE      // 超大屏 > 480dp
    }

    /**
     * 获取屏幕尺寸分类
     */
    fun getScreenSizeCategory(): ScreenSize {
        return when {
            screenWidthDp < 360 -> ScreenSize.SMALL
            screenWidthDp < 400 -> ScreenSize.NORMAL
            screenWidthDp < 480 -> ScreenSize.LARGE
            else -> ScreenSize.XLARGE
        }
    }

    /**
     * 根据屏幕尺寸获取字体缩放比例
     */
    fun getFontScale(): Float {
        return when (getScreenSizeCategory()) {
            ScreenSize.SMALL -> 0.9f
            ScreenSize.NORMAL -> 1.0f
            ScreenSize.LARGE -> 1.1f
            ScreenSize.XLARGE -> 1.2f
        }
    }
}
