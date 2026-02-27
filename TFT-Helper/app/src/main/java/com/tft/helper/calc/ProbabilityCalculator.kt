package com.tft.helper.calc

import kotlin.math.ceil
import kotlin.math.min

/**
 * 概率计算器
 * 
 * 计算金铲铲之战中的各种概率
 * 包括：碰对手概率、D牌概率、装备合成概率等
 * 
 * 注意：这是一个纯数学计算工具，不涉及任何游戏数据读取或自动操作
 */
object ProbabilityCalculator {

    /**
     * ========================================
     * 一、碰对手概率计算
     * ========================================
     */

    /**
     * 计算下一轮碰到特定对手的概率
     * 
     * 原理：
     * - 8人局，每个玩家在每回合会随机匹配一个对手
     * - 不会匹配到自己
     * - 不会连续两回合匹配到同一个对手（隐性规则）
     * 
     * @param totalPlayers 存活玩家总数（包括自己）
     * @param targetPlayerAlive 目标对手是否存活
     * @param lastOpponentId 上回合的对手ID（用于排除）
     * @return 碰到目标对手的概率（0-1）
     */
    fun calculateMeetProbability(
        totalPlayers: Int,
        targetPlayerAlive: Boolean,
        lastOpponentId: String? = null
    ): Double {
        // 如果目标对手已淘汰，概率为0
        if (!targetPlayerAlive) return 0.0
        
        // 只有自己存活的情况（特殊情况）
        if (totalPlayers <= 1) return 0.0
        
        // 基础概率：在剩余存活玩家中随机匹配
        // 排除自己，有 (totalPlayers - 1) 个可能对手
        val baseProbability = 1.0 / (totalPlayers - 1)
        
        // 如果上回合的对手是目标，根据规则这回合不会遇到
        // 但这种情况需要外部传入判断，这里仅计算基础概率
        
        return baseProbability
    }

    /**
     * 计算与所有存活对手相遇的概率列表
     * 
     * @param players 所有玩家列表
     * @param myId 自己的ID
     * @param lastOpponentId 上回合对手ID
     * @return 按概率排序的对手列表
     */
    fun calculateAllMeetProbabilities(
        players: List<PlayerInfo>,
        myId: String,
        lastOpponentId: String? = null
    ): List<PlayerMeetProbability> {
        // 过滤出存活的对手（不包括自己）
        val aliveOpponents = players.filter { 
            it.id != myId && it.isAlive 
        }
        
        // 如果上回合对手存活且不是唯一对手，降低其概率
        val lastOpponent = aliveOpponents.find { it.id == lastOpponentId }
        val hasLastOpponent = lastOpponent != null && aliveOpponents.size > 1
        
        // 计算基础概率
        val baseProbability = if (hasLastOpponent) {
            // 如果上回合对手在，其他人概率略微提高
            1.0 / (aliveOpponents.size - 1) * (aliveOpponents.size - 1.0) / aliveOpponents.size
        } else {
            1.0 / aliveOpponents.size
        }
        
        return aliveOpponents.map { opponent ->
            val probability = when {
                // 上回合对手且不止一个对手：降低概率
                hasLastOpponent && opponent.id == lastOpponentId -> {
                    baseProbability * 0.1  // 假设10%概率会连续遇到
                }
                // 其他对手：正常概率
                else -> baseProbability
            }
            
            PlayerMeetProbability(
                player = opponent,
                probability = probability,
                reason = generateMeetReason(opponent, probability)
            )
        }.sortedByDescending { it.probability }
    }

    /**
     * 计算多回合内碰到特定对手的概率
     * 
     * @param totalPlayers 存活玩家数
     * @param rounds 回合数
     * @return 至少碰到一次的概率
     */
    fun calculateMeetProbabilityOverRounds(
        totalPlayers: Int,
        rounds: Int
    ): Double {
        // 单回合不碰到某对手的概率
        val singleRoundNotMeet = 1.0 - (1.0 / (totalPlayers - 1))
        
        // rounds回合都不碰到的概率
        val notMeetAllRounds = Math.pow(singleRoundNotMeet, rounds.toDouble())
        
        // 至少碰到一次的概率
        return 1.0 - notMeetAllRounds
    }

    /**
     * 生成相遇概率说明
     */
    private fun generateMeetReason(player: PlayerInfo, probability: Double): String {
        return buildString {
            append("基础概率 ${String.format("%.1f", probability * 100)}%")
            if (player.health < 30) {
                append(" · 该玩家血量较低(${player.health})，可能即将被淘汰")
            }
            if (player.recentMatchesAgainstMe >= 2) {
                append(" · 近期已遇到${player.recentMatchesAgainstMe}次")
            }
        }
    }

    /**
     * ========================================
     * 二、D牌/抽卡概率计算
     * ========================================
     */

    /**
     * 商店刷新概率表
     * 根据等级不同，各费卡的出现概率
     */
    private val SHOP_PROBABILITIES = mapOf(
        1 to listOf(1.0, 0.0, 0.0, 0.0, 0.0),    // 1级：100% 1费
        2 to listOf(1.0, 0.0, 0.0, 0.0, 0.0),    // 2级：100% 1费
        3 to listOf(0.75, 0.25, 0.0, 0.0, 0.0),  // 3级：75% 1费，25% 2费
        4 to listOf(0.55, 0.30, 0.15, 0.0, 0.0), // 4级
        5 to listOf(0.45, 0.33, 0.20, 0.02, 0.0),
        6 to listOf(0.25, 0.40, 0.30, 0.05, 0.0),
        7 to listOf(0.19, 0.30, 0.35, 0.15, 0.01),
        8 to listOf(0.15, 0.20, 0.35, 0.25, 0.05),
        9 to listOf(0.10, 0.15, 0.30, 0.30, 0.15),
        10 to listOf(0.05, 0.10, 0.20, 0.40, 0.25),
        11 to listOf(0.01, 0.02, 0.12, 0.50, 0.35)
    )

    /**
     * 各费卡在卡池中的数量
     */
    private val CARD_POOL_SIZES = mapOf(
        1 to 29,  // 1费卡每张29张
        2 to 22,  // 2费卡每张22张
        3 to 18,  // 3费卡每张18张
        4 to 12,  // 4费卡每张12张
        5 to 10   // 5费卡每张10张
    )

    /**
     * 计算特定英雄在商店中出现的概率
     * 
     * @param level 当前等级
     * @param heroCost 英雄费用
     * @param sameCostTaken 同费用卡已被拿走的数量
     * @param thisHeroTaken 该英雄已被拿走的数量
     * @return 单次刷新商店看到该英雄的概率
     */
    fun calculateHeroAppearanceProbability(
        level: Int,
        heroCost: Int,
        sameCostTaken: Int,
        thisHeroTaken: Int
    ): Double {
        val levelProbs = SHOP_PROBABILITIES[level] ?: return 0.0
        val costProbability = levelProbs.getOrElse(heroCost - 1) { 0.0 }
        
        // 该费用卡的卡池大小
        val poolSize = CARD_POOL_SIZES[heroCost] ?: return 0.0
        
        // 该费用剩余卡数
        val remainingSameCost = (poolSize * 13) - sameCostTaken  // 假设13张同费卡
        val remainingThisHero = poolSize - thisHeroTaken
        
        // 条件概率：先刷到该费用，再刷到该英雄
        return if (remainingSameCost > 0) {
            costProbability * (remainingThisHero.toDouble() / remainingSameCost)
        } else 0.0
    }

    /**
     * 计算D牌N次看到特定英雄的概率
     * 
     * @param shopRefreshCount D牌次数
     * @param singleProbability 单次看到概率
     * @return 至少看到一次的概率
     */
    fun calculateSeeHeroProbability(
        shopRefreshCount: Int,
        singleProbability: Double
    ): Double {
        // 单次看不到的概率
        val notSee = 1.0 - singleProbability
        // shopRefreshCount次都看不到的概率（每次5个格子）
        val notSeeAll = Math.pow(notSee, (shopRefreshCount * 5).toDouble())
        return 1.0 - notSeeAll
    }

    /**
     * 计算追到三星的概率
     * 
     * @param level 当前等级
     * @param heroCost 英雄费用
     * @param currentCount 当前拥有数量
     * @param gold 可用金币
     * @param shopRefreshCount 可D牌次数（由金币决定）
     * @return 追到三星的概率
     */
    fun calculateThreeStarProbability(
        level: Int,
        heroCost: Int,
        currentCount: Int,
        gold: Int,
        shopRefreshCount: Int = gold / 2
    ): TripleStarProbability {
        val needed = 9 - currentCount  // 还需要多少张
        if (needed <= 0) return TripleStarProbability(1.0, 0, "已满")
        
        val singleProb = calculateHeroAppearanceProbability(level, heroCost, 0, 0)
        val expectedPerRefresh = singleProb * 5  // 每次刷新期望看到数量
        val expectedTotal = expectedPerRefresh * shopRefreshCount
        
        // 简化的泊松分布估算
        val probability = if (expectedTotal >= needed) {
            min(0.95, expectedTotal / (needed + expectedTotal))
        } else {
            expectedTotal / (2 * needed)
        }
        
        val estimatedGold = ceil(needed / expectedPerRefresh * 2).toInt()
        
        return TripleStarProbability(
            probability = probability,
            estimatedGold = estimatedGold,
            suggestion = when {
                probability > 0.7 -> "概率较高，建议追三"
                probability > 0.4 -> "概率一般，看情况追"
                else -> "概率较低，不建议追"
            }
        )
    }

    /**
     * ========================================
     * 三、装备合成概率
     * ========================================
     */

    /**
     * 计算获得特定装备的概率
     * 
     * @param rounds 游戏回合数（影响装备数量）
     * @param desiredItems 想要的装备列表
     * @param totalItemTypes 装备总数（用于计算基础概率）
     */
    fun calculateItemProbability(
        rounds: Int,
        desiredItems: List<String>,
        totalItemTypes: Int = 36  // 基础装备组合数
    ): ItemProbability {
        // 估算获得装备数量（简化模型）
        val expectedItems = when {
            rounds < 10 -> 2
            rounds < 20 -> 4
            rounds < 30 -> 7
            else -> 10
        }
        
        val singleProb = desiredItems.size.toDouble() / totalItemTypes
        val notGetSingle = 1.0 - singleProb
        val notGetAny = Math.pow(notGetSingle, expectedItems.toDouble())
        val getAtLeastOne = 1.0 - notGetAny
        
        return ItemProbability(
            probability = getAtLeastOne,
            expectedItems = expectedItems,
            suggestion = if (getAtLeastOne > 0.5) "有较大概率获得目标装备" else "建议准备备选装备"
        )
    }

    /**
     * ========================================
     * 四、海克斯/强化符文概率
     * ========================================
     */

    /**
     * 海克斯等级概率（根据阶段）
     */
    fun calculateAugmentProbabilities(stage: Int): Map<AugmentTier, Double> {
        return when (stage) {
            1 -> mapOf(
                AugmentTier.SILVER to 0.80,
                AugmentTier.GOLD to 0.19,
                AugmentTier.PRISMATIC to 0.01
            )
            3 -> mapOf(
                AugmentTier.SILVER to 0.35,
                AugmentTier.GOLD to 0.55,
                AugmentTier.PRISMATIC to 0.10
            )
            5 -> mapOf(
                AugmentTier.SILVER to 0.15,
                AugmentTier.GOLD to 0.55,
                AugmentTier.PRISMATIC to 0.30
            )
            else -> mapOf(
                AugmentTier.SILVER to 0.33,
                AugmentTier.GOLD to 0.33,
                AugmentTier.PRISMATIC to 0.34
            )
        }
    }

    // ==================== 数据类 ====================

    /**
     * 玩家信息
     */
    data class PlayerInfo(
        val id: String,
        val name: String,
        val health: Int,
        val isAlive: Boolean,
        val recentMatchesAgainstMe: Int = 0,  // 近期与我匹配次数
        val compName: String? = null  // 已知阵容
    )

    /**
     * 玩家相遇概率
     */
    data class PlayerMeetProbability(
        val player: PlayerInfo,
        val probability: Double,
        val reason: String
    )

    /**
     * 三星概率结果
     */
    data class TripleStarProbability(
        val probability: Double,
        val estimatedGold: Int,
        val suggestion: String
    )

    /**
     * 装备概率结果
     */
    data class ItemProbability(
        val probability: Double,
        val expectedItems: Int,
        val suggestion: String
    )

    /**
     * 海克斯等级
     */
    enum class AugmentTier {
        SILVER,     // 银色
        GOLD,       // 金色
        PRISMATIC   // 彩色
    }
}
