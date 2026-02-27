package com.tft.helper.calc

/**
 * 牌库计算器
 * 
 * 计算金铲铲之战卡池中各英雄的剩余数量
 * 帮助玩家判断是否适合追三星
 * 
 * 卡池规则：
 * - 1费卡：每张英雄29张，共13张不同1费卡 = 377张
 * - 2费卡：每张英雄22张，共13张不同2费卡 = 286张
 * - 3费卡：每张英雄18张，共13张不同3费卡 = 234张
 * - 4费卡：每张英雄12张，共12张不同4费卡 = 144张
 * - 5费卡：每张英雄10张，共8张不同5费卡 = 80张
 */
object CardPoolCalculator {

    /**
     * 各费用卡牌在卡池中的数量（每张英雄）
     */
    val CARD_POOL_COUNT = mapOf(
        1 to 29,  // 1费卡每张29张
        2 to 22,  // 2费卡每张22张
        3 to 18,  // 3费卡每张18张
        4 to 12,  // 4费卡每张12张
        5 to 10   // 5费卡每张10张
    )

    /**
     * 各费用英雄数量
     */
    val HERO_COUNT_PER_COST = mapOf(
        1 to 13,
        2 to 13,
        3 to 13,
        4 to 12,
        5 to 8
    )

    /**
     * 计算特定英雄在卡池中的剩余数量
     * 
     * @param cost 英雄费用（1-5）
     * @param takenCount 已被拿走的数量（包括所有玩家持有的、场上的、等待区的）
     * @return 卡池中剩余数量
     */
    fun calculateRemaining(cost: Int, takenCount: Int): Int {
        val totalInPool = CARD_POOL_COUNT[cost] ?: return 0
        return (totalInPool - takenCount).coerceAtLeast(0)
    }

    /**
     * 计算追到三星的可行性
     * 
     * @param cost 英雄费用
     * @param currentOwned 当前拥有数量
     * @param takenByOthers 被其他玩家拿走的数量
     * @return 追三星建议结果
     */
    fun calculateThreeStarFeasibility(
        cost: Int,
        currentOwned: Int,
        takenByOthers: Int
    ): ThreeStarFeasibility {
        val totalInPool = CARD_POOL_COUNT[cost] ?: 0
        val remaining = totalInPool - currentOwned - takenByOthers
        val needed = 9 - currentOwned

        return when {
            remaining <= 0 -> ThreeStarFeasibility(
                isFeasible = false,
                remainingCards = 0,
                neededCards = needed,
                probability = 0.0,
                suggestion = "卡池已空，无法追三",
                riskLevel = RiskLevel.IMPOSSIBLE
            )
            remaining < needed -> ThreeStarFeasibility(
                isFeasible = false,
                remainingCards = remaining,
                neededCards = needed,
                probability = 0.0,
                suggestion = "卡池剩余${remaining}张，不足以追三（需${needed}张）",
                riskLevel = RiskLevel.IMPOSSIBLE
            )
            remaining == needed -> ThreeStarFeasibility(
                isFeasible = true,
                remainingCards = remaining,
                neededCards = needed,
                probability = 0.3,
                suggestion = "卡池刚好够，但风险极高，需卖掉其他玩家手中的牌",
                riskLevel = RiskLevel.EXTREME_RISK
            )
            remaining <= needed + 3 -> ThreeStarFeasibility(
                isFeasible = true,
                remainingCards = remaining,
                neededCards = needed,
                probability = 0.5,
                suggestion = "卡池紧张，建议观察其他玩家是否持有该牌",
                riskLevel = RiskLevel.HIGH_RISK
            )
            remaining <= needed + 6 -> ThreeStarFeasibility(
                isFeasible = true,
                remainingCards = remaining,
                neededCards = needed,
                probability = 0.75,
                suggestion = "卡池尚可，可以尝试追三",
                riskLevel = RiskLevel.MEDIUM_RISK
            )
            else -> ThreeStarFeasibility(
                isFeasible = true,
                remainingCards = remaining,
                neededCards = needed,
                probability = 0.9,
                suggestion = "卡池充足，放心追三",
                riskLevel = RiskLevel.LOW_RISK
            )
        }
    }

    /**
     * 计算某费用所有卡牌的分布情况
     * 
     * @param cost 费用
     * @param allHerosTaken 所有英雄的已拿数量 Map<英雄ID, 已拿数量>
     * @return 该费用所有卡牌的分布
     */
    fun calculateCostDistribution(
        cost: Int,
        allHerosTaken: Map<String, Int>
    ): CostDistribution {
        val totalPerHero = CARD_POOL_COUNT[cost] ?: 0
        val heroCount = HERO_COUNT_PER_COST[cost] ?: 0
        
        val heroStatuses = allHerosTaken.map { (heroId, taken) ->
            HeroCardStatus(
                heroId = heroId,
                heroName = getHeroNameById(heroId),
                cost = cost,
                totalInPool = totalPerHero,
                takenCount = taken,
                remaining = (totalPerHero - taken).coerceAtLeast(0)
            )
        }.sortedByDescending { it.remaining }

        val totalCards = totalPerHero * heroCount
        val totalTaken = allHerosTaken.values.sum()
        val totalRemaining = totalCards - totalTaken

        return CostDistribution(
            cost = cost,
            totalCards = totalCards,
            totalRemaining = totalRemaining,
            heroStatuses = heroStatuses
        )
    }

    /**
     * 统计其他玩家持有某英雄的总数
     * 
     * @param heroId 英雄ID
     * @param otherPlayersHeros 其他玩家持有的英雄列表 List<Map<英雄ID, 数量>>
     * @return 其他玩家持有的总数
     */
    fun countTakenByOthers(
        heroId: String,
        otherPlayersHeros: List<Map<String, Int>>
    ): Int {
        return otherPlayersHeros.sumOf { playerHeros ->
            playerHeros[heroId] ?: 0
        }
    }

    /**
     * 获取商店中某费用卡牌的出现概率（根据等级）
     */
    fun getShopProbability(level: Int, cost: Int): Double {
        val probabilities = when (level) {
            1 -> listOf(1.0, 0.0, 0.0, 0.0, 0.0)
            2 -> listOf(1.0, 0.0, 0.0, 0.0, 0.0)
            3 -> listOf(0.75, 0.25, 0.0, 0.0, 0.0)
            4 -> listOf(0.55, 0.30, 0.15, 0.0, 0.0)
            5 -> listOf(0.45, 0.33, 0.20, 0.02, 0.0)
            6 -> listOf(0.25, 0.40, 0.30, 0.05, 0.0)
            7 -> listOf(0.19, 0.30, 0.35, 0.15, 0.01)
            8 -> listOf(0.15, 0.20, 0.35, 0.25, 0.05)
            9 -> listOf(0.10, 0.15, 0.30, 0.30, 0.15)
            10 -> listOf(0.05, 0.10, 0.20, 0.40, 0.25)
            11 -> listOf(0.01, 0.02, 0.12, 0.50, 0.35)
            else -> listOf(0.0, 0.0, 0.0, 0.0, 0.0)
        }
        return probabilities.getOrElse(cost - 1) { 0.0 }
    }

    /**
     * 根据英雄ID获取名称（简化版，实际应从数据库查询）
     */
    private fun getHeroNameById(heroId: String): String {
        // 实际项目中应从数据库或配置文件查询
        return heroId
    }

    // ==================== 数据类 ====================

    /**
     * 追三星可行性结果
     */
    data class ThreeStarFeasibility(
        val isFeasible: Boolean,        // 是否可行
        val remainingCards: Int,        // 卡池剩余数量
        val neededCards: Int,           // 还需要多少张
        val probability: Double,        // 成功概率估计
        val suggestion: String,         // 建议文本
        val riskLevel: RiskLevel        // 风险等级
    )

    /**
     * 风险等级
     */
    enum class RiskLevel {
        LOW_RISK,       // 低风险 - 绿色
        MEDIUM_RISK,    // 中风险 - 黄色
        HIGH_RISK,      // 高风险 - 橙色
        EXTREME_RISK,   // 极高风险 - 红色
        IMPOSSIBLE      // 不可能 - 灰色
    }

    /**
     * 某费用的卡牌分布
     */
    data class CostDistribution(
        val cost: Int,                          // 费用
        val totalCards: Int,                    // 该费用总卡牌数
        val totalRemaining: Int,                // 该费用总剩余
        val heroStatuses: List<HeroCardStatus>  // 各英雄状态
    )

    /**
     * 单个英雄的卡牌状态
     */
    data class HeroCardStatus(
        val heroId: String,         // 英雄ID
        val heroName: String,       // 英雄名称
        val cost: Int,              // 费用
        val totalInPool: Int,       // 卡池总数量
        val takenCount: Int,        // 已被拿走数量
        val remaining: Int          // 剩余数量
    ) {
        /**
         * 获取剩余数量颜色
         */
        fun getRemainingColor(): String = when {
            remaining == 0 -> "#F44336"  // 红色 - 已空
            remaining <= 3 -> "#FF9800"  // 橙色 - 极少
            remaining <= 6 -> "#FFC107"  // 黄色 - 较少
            remaining <= 10 -> "#4CAF50" // 绿色 - 充足
            else -> "#2196F3"            // 蓝色 - 很多
        }

        /**
         * 获取剩余百分比
         */
        fun getRemainingPercent(): Int {
            return (remaining * 100 / totalInPool)
        }
    }
}
