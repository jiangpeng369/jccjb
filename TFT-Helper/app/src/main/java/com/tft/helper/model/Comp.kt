package com.tft.helper.model

import com.google.gson.annotations.SerializedName

/**
 * 阵容数据模型
 * 
 * 存储一套完整阵容的详细信息
 * 包括阵容名称、核心英雄、羁绊、运营思路等
 * 
 * @property id 阵容唯一标识
 * @property name 阵容名称
 * @property version 适用游戏版本
 * @property tier 阵容评级（S/A/B/C）
 * @property popularity 热度（用于排序）
 * @property winRate 胜率
 * @property top4Rate 前四率
 * @property coreHeroes 核心英雄列表
 * @property traits 核心羁绊列表
 * @property earlyGame 前期运营思路
 * @property midGame 中期运营思路
 * @property lateGame 后期运营思路
 * @property positioning 站位建议
 * @property imageUrl 阵容示意图URL
 * @property createTime 数据创建时间
 * @property updateTime 数据更新时间
 */
data class Comp(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("version")
    val version: String,  // 如 "S11"
    
    @SerializedName("tier")
    val tier: Tier,  // 阵容评级
    
    @SerializedName("popularity")
    val popularity: Double,  // 热度值（0-100）
    
    @SerializedName("win_rate")
    val winRate: Double,  // 胜率（0-100）
    
    @SerializedName("top4_rate")
    val top4Rate: Double,  // 前四率（0-100）
    
    @SerializedName("core_heroes")
    val coreHeroes: List<CompHero>,  // 阵容核心英雄
    
    @SerializedName("traits")
    val traits: List<CompTrait>,  // 核心羁绊
    
    @SerializedName("early_game")
    val earlyGame: String,  // 前期思路
    
    @SerializedName("mid_game")
    val midGame: String,  // 中期思路
    
    @SerializedName("late_game")
    val lateGame: String,  // 后期思路
    
    @SerializedName("positioning")
    val positioning: String,  // 站位建议
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("create_time")
    val createTime: Long = System.currentTimeMillis(),
    
    @SerializedName("update_time")
    val updateTime: Long = System.currentTimeMillis()
) {
    /**
     * 阵容评级枚举
     */
    enum class Tier {
        S,  // T0级阵容，最强
        A,  // T1级阵容，很强
        B,  // T2级阵容，可用
        C   // T3级阵容，较弱
    }
    
    /**
     * 阵容中的英雄信息
     * 包含英雄和其在阵容中的配置
     */
    data class CompHero(
        @SerializedName("hero")
        val hero: Hero,
        
        @SerializedName("star")
        val star: Int = 2,  // 推荐星级（1/2/3星）
        
        @SerializedName("is_carry")
        val isCarry: Boolean = false,  // 是否主C
        
        @SerializedName("is_tank")
        val isTank: Boolean = false,  // 是否主坦
        
        @SerializedName("position")
        val position: Position = Position.BACK  // 站位
    )
    
    /**
     * 站位位置
     */
    enum class Position {
        FRONT,   // 前排
        MIDDLE,  // 中排
        BACK     // 后排
    }
    
    /**
     * 羁绊信息
     */
    data class CompTrait(
        @SerializedName("name")
        val name: String,
        
        @SerializedName("count")
        val count: Int,  // 羁绊数量
        
        @SerializedName("active_level")
        val activeLevel: Int  // 激活等级（如6剪纸中的6）
    )
    
    /**
     * 获取阵容评级颜色
     */
    fun getTierColor(): String = when (tier) {
        Tier.S -> "#FF6B6B"  // 红色
        Tier.A -> "#FFA500"  // 橙色
        Tier.B -> "#4ECDC4"  // 青色
        Tier.C -> "#95A5A6"  // 灰色
    }
    
    /**
     * 获取主C英雄
     */
    fun getCarryHeroes(): List<CompHero> = coreHeroes.filter { it.isCarry }
    
    /**
     * 获取主坦英雄
     */
    fun getTankHeroes(): List<CompHero> = coreHeroes.filter { it.isTank }
}

/**
 * 阵容列表响应数据
 * 用于网络请求返回的列表数据
 */
data class CompListResponse(
    @SerializedName("code")
    val code: Int,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: List<Comp>,
    
    @SerializedName("total")
    val total: Int
)
