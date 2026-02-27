package com.tft.helper.network

/**
 * 网络配置
 * 
 * 配置网络请求相关参数
 * 包括API地址、超时设置、重试策略等
 */
object NetworkConfig {

    /**
     * API基础地址
     * 实际项目中替换为真实的数据源地址
     */
    const val BASE_URL = "https://api.example.com/tft/"

    /**
     * 阵容数据API
     */
    const val API_COMPS = "comps"

    /**
     * 英雄数据API
     */
    const val API_HEROES = "heroes"

    /**
     * 装备数据API
     */
    const val API_ITEMS = "items"

    /**
     * 连接超时时间（秒）
     */
    const val CONNECT_TIMEOUT = 15L

    /**
     * 读取超时时间（秒）
     */
    const val READ_TIMEOUT = 15L

    /**
     * 写入超时时间（秒）
     */
    const val WRITE_TIMEOUT = 15L

    /**
     * 最大重试次数
     */
    const val MAX_RETRY = 3

    /**
     * 支持的公开数据源
     * 用于从多个来源获取阵容数据
     */
    object DataSources {
        
        /**
         * 数据源信息
         */
        data class DataSource(
            val name: String,           // 数据源名称
            val baseUrl: String,        // 基础URL
            val description: String,    // 描述
            val isOfficial: Boolean     // 是否官方数据源
        )

        /**
         * 可用数据源列表
         */
        val SOURCES = listOf(
            DataSource(
                name = "掌上英雄联盟",
                baseUrl = "https://mlol.qt.qq.com/",
                description = "官方数据源，数据准确及时",
                isOfficial = true
            ),
            DataSource(
                name = "TFTactics",
                baseUrl = "https://tftactics.gg/",
                description = "国外知名数据站，阵容推荐专业",
                isOfficial = false
            ),
            DataSource(
                name = "Metatft",
                baseUrl = "https://metatft.com/",
                description = "实时胜率统计，数据丰富",
                isOfficial = false
            )
        )
    }
}
