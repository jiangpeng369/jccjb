package com.tft.helper

import android.app.Application
import com.tft.helper.utils.ScreenAdapter

/**
 * Application类
 * 
 * 应用启动时初始化全局配置
 */
class TFTApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // 初始化屏幕适配工具
        ScreenAdapter.init(this)
    }
}
