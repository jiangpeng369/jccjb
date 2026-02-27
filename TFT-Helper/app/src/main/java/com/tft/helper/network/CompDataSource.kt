package com.tft.helper.network

import com.tft.helper.model.Comp
import com.tft.helper.model.Hero
import com.tft.helper.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * 阵容数据源
 * 
 * 金铲铲之战2025天选福星版本阵容数据
 * 数据来源：2025年1月最新版本
 */
class CompDataSource private constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // 本地缓存
    private var cachedComps: List<Comp>? = null
    private var cacheTime: Long = 0
    private val CACHE_DURATION = 5 * 60 * 1000

    companion object {
        @Volatile
        private var instance: CompDataSource? = null

        fun getInstance(): CompDataSource {
            return instance ?: synchronized(this) {
                instance ?: CompDataSource().also { instance = it }
            }
        }

        // 英雄图片基础URL（使用金铲铲官方图片CDN）
        private const val HERO_IMAGE_BASE_URL = "https://game.gtimg.cn/images/yxzj/act/a20250116tftfuxing/hero/"
        
        /**
         * 获取英雄图片URL
         */
        fun getHeroImageUrl(heroName: String): String {
            val encodedName = java.net.URLEncoder.encode(heroName, "UTF-8")
            return "${HERO_IMAGE_BASE_URL}${encodedName}.png"
        }
    }

    suspend fun getComps(
        forceRefresh: Boolean = false,
        sortBy: SortType = SortType.POPULARITY
    ): Result<List<Comp>> = withContext(Dispatchers.IO) {
        try {
            if (!forceRefresh && cachedComps != null && 
                System.currentTimeMillis() - cacheTime < CACHE_DURATION) {
                return@withContext Result.success(sortComps(cachedComps!!, sortBy))
            }
            val comps = fetchCompsFromNetwork()
            cachedComps = comps
            cacheTime = System.currentTimeMillis()
            Result.success(sortComps(comps, sortBy))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchCompsFromNetwork(): List<Comp> {
        Thread.sleep(500)
        return getMockCompData()
    }

    /**
     * 2025天选福星版本热门阵容
     * 数据来源：2025年1月最新版本环境
     */
    private fun getMockCompData(): List<Comp> {
        return listOf(
            // 1. 五秘术天使 - S级版本T0
            Comp(
                id = "comp_001",
                name = "五秘术天使",
                version = "天选福星",
                tier = Comp.Tier.S,
                popularity = 96.0,
                winRate = 29.5,
                top4Rate = 70.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h001", "凯尔", 4, listOf("天神", "裁决使"), getHeroImageUrl("凯尔")),
                        star = 3,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h002", "悠米", 3, listOf("灵魂莲华·明昼", "秘术师"), getHeroImageUrl("悠米")),
                        star = 2,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h003", "慎", 4, listOf("忍者", "秘术师", "宗师"), getHeroImageUrl("慎")),
                        star = 2,
                        isTank = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h004", "李青", 5, listOf("天神", "决斗大师"), getHeroImageUrl("李青")),
                        star = 2,
                        position = Comp.Position.FRONT
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("天神", 4, 4),
                    Comp.CompTrait("裁决使", 4, 4),
                    Comp.CompTrait("秘术师", 5, 5),
                    Comp.CompTrait("灵魂莲华·明昼", 2, 2)
                ),
                earlyGame = "开局抢攻速或大棒，做羊刀或水银。前期用天神或决斗过渡，天使装备给武器大师打工。",
                midGame = "3-2拉6人口，4-1拉7人口小D找2星天使。有秘术天选可以直接开4秘术。",
                lateGame = "上8人口开5秘术4裁决，天使站角落输出。李青踢走对面关键前排。",
                positioning = "天使站最角落，悠米贴天使提供攻速。慎单顶前排，秘术单位保护后排。",
                imageUrl = "https://game.gtimg.cn/images/yxzj/act/a20250116tftfuxing/comp/tianshi.jpg"
            ),

            // 2. 八斗双C - S级强力阵容
            Comp(
                id = "comp_002",
                name = "八斗双C",
                version = "天选福星",
                tier = Comp.Tier.S,
                popularity = 92.0,
                winRate = 27.5,
                top4Rate = 68.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h005", "瑟提", 5, listOf("霸王", "斗士"), getHeroImageUrl("瑟提")),
                        star = 2,
                        isCarry = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h006", "努努", 3, listOf("永恒之森", "斗士"), getHeroImageUrl("努努")),
                        star = 3,
                        isCarry = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h007", "塔姆", 1, listOf("福星", "斗士"), getHeroImageUrl("塔姆")),
                        star = 3,
                        isTank = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h008", "茂凯", 1, listOf("永恒之森", "斗士"), getHeroImageUrl("茂凯")),
                        star = 3,
                        position = Comp.Position.FRONT
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("斗士", 8, 8),
                    Comp.CompTrait("永恒之森", 3, 3),
                    Comp.CompTrait("霸王", 1, 1),
                    Comp.CompTrait("福星", 1, 1)
                ),
                earlyGame = "开局抢腰带或大棒，做科技枪或狂徒。用斗士或永恒之森过渡，优先找努努装备。",
                midGame = "3-2拉6人口，D三星努努和塔姆。八斗士提供高额生命值，努努一口一个。",
                lateGame = "上8人口找瑟提，斗士转给瑟提。新春使者选斗士可以开8斗，血量上万。",
                positioning = "斗士全员顶前排，努努对准敌方C位。瑟提做俯卧撑位置要安全。",
                imageUrl = "https://game.gtimg.cn/images/yxzj/act/a20250116tftfuxing/comp/badou.jpg"
            ),

            // 3. 福星三国 - A级赌狗阵容
            Comp(
                id = "comp_003",
                name = "福星三国",
                version = "天选福星",
                tier = Comp.Tier.A,
                popularity = 88.0,
                winRate = 25.0,
                top4Rate = 62.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h009", "卡特琳娜", 3, listOf("福星", "三国猛将", "刺客"), getHeroImageUrl("卡特琳娜")),
                        star = 3,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h010", "塔姆", 1, listOf("福星", "斗士"), getHeroImageUrl("塔姆")),
                        star = 3,
                        isTank = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h011", "瑟庄妮", 4, listOf("福星", "重装战士"), getHeroImageUrl("瑟庄妮")),
                        star = 2,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h012", "德莱文", 4, listOf("福星", "战神"), getHeroImageUrl("德莱文"))
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("福星", 6, 6),
                    Comp.CompTrait("三国猛将", 6, 6),
                    Comp.CompTrait("刺客", 2, 2),
                    Comp.CompTrait("战神", 3, 3)
                ),
                earlyGame = "开局抢大棒或眼泪，优先做卡特装备。前期用3福星过渡，连败攒经济和奖励。",
                midGame = "3-2拉6人口，D出卡特琳娜三星。福星连败收菜，获取大量装备和妮蔻。",
                lateGame = "上8人口找德莱文和瑟庄妮，福星提供奖励。9人口可追三星五费。",
                positioning = "卡特跳后排切C位，福星单位分散站位。注意收菜时机。",
                imageUrl = "https://game.gtimg.cn/images/yxzj/act/a20250116tftfuxing/comp/fuxing.jpg"
            ),

            // 4. 万盾风女 - A级铁王八
            Comp(
                id = "comp_004",
                name = "万盾风女",
                version = "天选福星",
                tier = Comp.Tier.A,
                popularity = 82.0,
                winRate = 23.5,
                top4Rate = 60.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h013", "迦娜", 2, listOf("玉剑仙", "秘术师", "神使"), getHeroImageUrl("迦娜")),
                        star = 3,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h014", "慎", 4, listOf("忍者", "秘术师", "宗师"), getHeroImageUrl("慎")),
                        star = 2,
                        isTank = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h015", "悠米", 3, listOf("灵魂莲华·明昼", "秘术师"), getHeroImageUrl("悠米")),
                        star = 2,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h016", "莫甘娜", 4, listOf("玉剑仙", "耀光使"), getHeroImageUrl("莫甘娜"))
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("秘术师", 5, 5),
                    Comp.CompTrait("玉剑仙", 4, 4),
                    Comp.CompTrait("神使", 2, 2),
                    Comp.CompTrait("灵魂莲华·明昼", 2, 2)
                ),
                earlyGame = "开局抢眼泪，做青龙刀或大天使。用秘术或玉剑仙过渡，风女装备给娜美打工。",
                midGame = "6级小D找风女秘术天选，7级大D追三星风女和找慎。风女盾厚到打不破。",
                lateGame = "上8人口开5秘术，风女站中间给盾。中期赢下回合快速上分。",
                positioning = "风女站中间给所有人套盾，慎单顶前排。铁王八阵型缩角。",
                imageUrl = "https://game.gtimg.cn/images/yxzj/act/a20250116tftfuxing/comp/fengnv.jpg"
            ),

            // 5. 神射希维尔 - A级运营阵容
            Comp(
                id = "comp_005",
                name = "神射希维尔",
                version = "天选福星",
                tier = Comp.Tier.A,
                popularity = 78.0,
                winRate = 22.0,
                top4Rate = 58.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h017", "希维尔", 3, listOf("猩红之月", "神射手"), getHeroImageUrl("希维尔")),
                        star = 3,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h018", "提莫", 2, listOf("灵魂莲华·明昼", "神射手"), getHeroImageUrl("提莫")),
                        star = 3,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h019", "瑟庄妮", 4, listOf("福星", "重装战士"), getHeroImageUrl("瑟庄妮")),
                        star = 2,
                        isTank = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h020", "亚托克斯", 4, listOf("腥红之月", "重装战士"), getHeroImageUrl("亚托克斯"))
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("神射手", 4, 4),
                    Comp.CompTrait("重装战士", 4, 4),
                    Comp.CompTrait("猩红之月", 4, 4),
                    Comp.CompTrait("秘术师", 2, 2)
                ),
                earlyGame = "开局抢攻速，做分裂弓或轻语。用神射或猩红过渡，希维尔装备给薇恩打工。",
                midGame = "4-1拉7人口，找希维尔和重装前排。希维尔两星后锁血，弹射清场。",
                lateGame = "上8人口补秘术和高质量前排。希维尔站中间让弹射收益最大化。",
                positioning = "重装顶前排，神射站后排中间。希维尔分裂弓弹射清全场。",
                imageUrl = "https://game.gtimg.cn/images/yxzj/act/a20250116tftfuxing/comp/shenshe.jpg"
            ),

            // 6. 裁决天使 - A级赌狗阵容
            Comp(
                id = "comp_006",
                name = "裁决天使",
                version = "天选福星",
                tier = Comp.Tier.A,
                popularity = 75.0,
                winRate = 21.5,
                top4Rate = 57.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h021", "霞", 4, listOf("永恒之森", "裁决使", "神盾使"), getHeroImageUrl("霞")),
                        star = 2,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h022", "凯尔", 4, listOf("天神", "裁决使"), getHeroImageUrl("凯尔")),
                        star = 2,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h023", "千珏", 3, listOf("永恒之森", "猎人", "裁决使"), getHeroImageUrl("千珏")),
                        star = 3,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h024", "悠米", 3, listOf("灵魂莲华·明昼", "秘术师"), getHeroImageUrl("悠米"))
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("裁决使", 4, 4),
                    Comp.CompTrait("永恒之森", 4, 4),
                    Comp.CompTrait("天神", 2, 2),
                    Comp.CompTrait("灵魂莲华·明昼", 2, 2)
                ),
                earlyGame = "开局抢攻速或大剑，做羊刀或正义。用天神或裁决使过渡，天使装备给武器打工。",
                midGame = "4-1拉7人口，找凯尔和霞。天神天选可以下霞开6天神，伤害爆炸。",
                lateGame = "上8人口开4裁决4森林，双C输出。天神天选优先开6天神。",
                positioning = "天使和霞站后排，千珏保护。天神羁绊提供攻速和减伤。",
                imageUrl = "https://game.gtimg.cn/images/yxzj/act/a20250116tftfuxing/comp/caijue.jpg"
            )
        )
    }

    private fun sortComps(comps: List<Comp>, sortBy: SortType): List<Comp> {
        return when (sortBy) {
            SortType.POPULARITY -> comps.sortedByDescending { it.popularity }
            SortType.WIN_RATE -> comps.sortedByDescending { it.winRate }
            SortType.TOP4_RATE -> comps.sortedByDescending { it.top4Rate }
            SortType.TIER -> comps.sortedBy { it.tier.ordinal }
        }
    }

    suspend fun getHeroDetail(heroId: String): Result<Hero> = withContext(Dispatchers.IO) {
        Result.failure(NotImplementedError("待实现"))
    }

    suspend fun getItemDetail(itemId: String): Result<Item> = withContext(Dispatchers.IO) {
        Result.failure(NotImplementedError("待实现"))
    }

    suspend fun searchComps(keyword: String): Result<List<Comp>> = withContext(Dispatchers.IO) {
        val comps = cachedComps ?: getMockCompData()
        val filtered = comps.filter {
            it.name.contains(keyword) ||
            it.coreHeroes.any { hero -> hero.hero.name.contains(keyword) } ||
            it.traits.any { trait -> trait.name.contains(keyword) }
        }
        Result.success(filtered)
    }

    enum class SortType {
        POPULARITY,
        WIN_RATE,
        TOP4_RATE,
        TIER
    }
}
