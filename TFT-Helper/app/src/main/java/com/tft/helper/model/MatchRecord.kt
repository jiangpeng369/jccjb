package com.tft.helper.model

import com.google.gson.annotations.SerializedName

/**
 * 对战记录数据模型
 * 
 * 用于玩家手动记录自己的对战数据
 * 帮助分析不同阵容的胜率
 * 
 * @property id 记录唯一标识
 * @property matchTime 对战时间
 * @property compUsed 使用的阵容
 * @property finalRank 最终排名（1-8）
 * @property healthRemaining 剩余血量
 * @property roundsSurvived 存活回合数
 * @property notes 备注
 * @property patchVersion 游戏版本
 * @property lobbyType 对局类型（排位/匹配）
 */
data class MatchRecord(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("match_time")
    val matchTime: Long = System.currentTimeMillis(),
    
    @SerializedName("comp_used")
    val compUsed: CompInfo,  // 使用的阵容信息
    
    @SerializedName("final_rank")
    val finalRank: Int,  // 1-8名
    
    @SerializedName("health_remaining")
    val healthRemaining: Int = 0,  // 剩余血量
    
    @SerializedName("rounds_survived")
    val roundsSurvived: Int = 0,  // 存活回合数
    
    @SerializedName("notes")
    val notes: String = "",  // 备注
    
    @SerializedName("patch_version")
    val patchVersion: String,  // 游戏版本
    
    @SerializedName("lobby_type")
    val lobbyType: LobbyType = LobbyType.RANKED
) {
    /**
     * 对局类型
     */
    enum class LobbyType {
        RANKED,     // 排位赛
        NORMAL,     // 匹配赛
        HYPER_ROLL, // 狂暴模式
        DOUBLE_UP   // 双人模式
    }
    
    /**
     * 阵容信息（简化版，用于记录）
     */
    data class CompInfo(
        @SerializedName("comp_id")
        val compId: String? = null,  // 阵容ID（如果是标准阵容）
        
        @SerializedName("comp_name")
        val compName: String,  // 阵容名称
        
        @SerializedName("core_traits")
        val coreTraits: List<String> = emptyList()  // 核心羁绊
    )
    
    /**
     * 判断是否吃鸡（第一名）
     */
    fun isFirstPlace(): Boolean = finalRank == 1
    
    /**
     * 判断是否前四
     */
    fun isTop4(): Boolean = finalRank <= 4
    
    /**
     * 获取排名对应的颜色
     */
    fun getRankColor(): String = when (finalRank) {
        1 -> "#FFD700"  // 金色
        2 -> "#C0C0C0"  // 银色
        3 -> "#CD7F32"  // 铜色
        4 -> "#4ECDC4"  // 青色
        else -> "#95A5A6"  // 灰色
    }
    
    /**
     * 获取LP变化估算（仅排位赛）
     * 这是一个简化的估算，实际LP变化取决于隐藏分
     */
    fun estimateLPChange(): Int = when (finalRank) {
        1 -> 40
        2 -> 25
        3 -> 15
        4 -> 5
        5 -> -10
        6 -> -20
        7 -> -30
        8 -> -40
        else -> 0
    }
}

/**
 * 对战统计数据
 * 用于展示某套阵容的历史表现
 */
data class MatchStatistics(
    @SerializedName("comp_id")
    val compId: String,
    
    @SerializedName("comp_name")
    val compName: String,
    
    @SerializedName("total_games")
    val totalGames: Int,
    
    @SerializedName("first_place_count")
    val firstPlaceCount: Int,
    
    @SerializedName("top4_count")
    val top4Count: Int,
    
    @SerializedName("average_rank")
    val averageRank: Double,  // 平均排名
    
    @SerializedName("win_rate")
    val winRate: Double,  // 吃鸡率
    
    @SerializedName("top4_rate")
    val top4Rate: Double  // 前四率
) {
    /**
     * 获取平均排名颜色
     */
    fun getAverageRankColor(): String = when {
        averageRank <= 2.0 -> "#4CAF50"  // 绿色
        averageRank <= 3.5 -> "#FFC107"  // 黄色
        else -> "#F44336"  // 红色
    }
}
