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
 * 金铲铲之战2026天选福星版本阵容数据
 * 更新日期：2026年2月
 * 
 * 2026年福星版本特色：
 * - 新增9名新春使者
 * - 新增福星守护神机制（不占用人口）
 * - 8福星/10福星羁绊
 * - 经典羁绊棱彩效果
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

        // 腾讯官方英雄图片CDN
        private const val HERO_IMAGE_BASE_URL = "https://game.gtimg.cn/images/yxzj/act/a20250116tftfuxing/hero/"
        
        /**
         * 获取英雄网络图片URL
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
        Thread.sleep(300)
        return get2026FuxingCompData()
    }

    /**
     * 2026天选福星版本阵容数据
     * 数据来源：2026年2月最新版本
     */
    private fun get2026FuxingCompData(): List<Comp> {
        return listOf(
            // 1. 福星九五 - S级版本T0
            Comp(
                id = "comp_001",
                name = "福星九五",
                version = "天选福星2026",
                tier = Comp.Tier.S,
                popularity = 98.0,
                winRate = 31.5,
                top4Rate = 72.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h001", "德莱文", 4, listOf("福星", "战神"), getHeroImageUrl("德莱文")),
                        star = 2,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h002", "瑟提", 5, listOf("霸王", "斗士"), getHeroImageUrl("瑟提")),
                        star = 2,
                        isCarry = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h003", "瑟庄妮", 4, listOf("福星", "重装战士"), getHeroImageUrl("瑟庄妮")),
                        star = 2,
                        isTank = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h004", "塔姆", 1, listOf("福星", "斗士"), getHeroImageUrl("塔姆")),
                        star = 3,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h005", "卡特琳娜", 3, listOf("福星", "三国猛将", "刺客"), getHeroImageUrl("卡特琳娜")),
                        star = 2,
                        position = Comp.Position.BACK
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("福星", 6, 6),
                    Comp.CompTrait("战神", 3, 3),
                    Comp.CompTrait("三国猛将", 3, 3),
                    Comp.CompTrait("重装战士", 2, 2)
                ),
                earlyGame = "开局抢大剑或攻速，优先做德莱文装备。前期用塔姆福星天选连败过渡，精致连败到野怪。",
                midGame = "3阶段有瑟庄妮就开6福星，推荐7-9连败收菜。7连败有概率出金锅/铲，可奔着10福星去玩。",
                lateGame = "收菜后拉9人口，找德莱文、瑟提等5费卡。德莱文带装备打钱，上10追三星5费。",
                positioning = "瑟庄妮单顶前排，德莱文站角落输出，福星单位分散站位。",
                imageUrl = "https://game.gtimg.cn/images/yxzj/act/a20250116tftfuxing/comp/fuxing95.jpg"
            ),

            // 2. 战神劫 - S级赌狗阵容
            Comp(
                id = "comp_002",
                name = "战神劫",
                version = "天选福星2026",
                tier = Comp.Tier.S,
                popularity = 94.0,
                winRate = 29.0,
                top4Rate = 69.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h006", "劫", 2, listOf("战神", "刺客"), getHeroImageUrl("劫")),
                        star = 3,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h007", "格温", 4, listOf("福星守护神"), getHeroImageUrl("格温")),
                        star = 2,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h008", "悠米", 3, listOf("灵魂莲华·明昼", "秘术师"), getHeroImageUrl("悠米")),
                        star = 2,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h009", "派克", 2, listOf("战神", "刺客"), getHeroImageUrl("派克"))
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("战神", 6, 6),
                    Comp.CompTrait("刺客", 2, 2),
                    Comp.CompTrait("秘术师", 2, 2),
                    Comp.CompTrait("福星守护神", 1, 1)
                ),
                earlyGame = "开局抢攻速或大剑，做夜刃+双羊刀。用体系牌过渡，2阶段不升人口。",
                midGame = "3-2拉6人口大D，找天选战神劫。追三星劫，同时找派克和悠米。",
                lateGame = "劫三星后上人口补高费卡，格温作为福星守护神提供额外战力。",
                positioning = "劫跳对角切C位，格温贴劫提供保护，明昼单位分散给攻速。",
                imageUrl = "https://game.gtimg.cn/images/yxzj/act/a20250116tftfuxing/comp/jie.jpg"
            ),

            // 3. 八斗双C - S级强力阵容
            Comp(
                id = "comp_003",
                name = "八斗双C",
                version = "天选福星2026",
                tier = Comp.Tier.S,
                popularity = 92.0,
                winRate = 28.0,
                top4Rate = 68.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h010", "努努", 3, listOf("永恒之森", "斗士"), getHeroImageUrl("努努")),
                        star = 3,
                        isCarry = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h011", "希瓦娜", 3, listOf("龙魂", "斗士"), getHeroImageUrl("希瓦娜")),
                        star = 3,
                        isCarry = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h012", "瑟提", 5, listOf("霸王", "斗士"), getHeroImageUrl("瑟提")),
                        star = 2,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h013", "塔姆", 1, listOf("福星", "斗士"), getHeroImageUrl("塔姆"))
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("斗士", 8, 8),
                    Comp.CompTrait("永恒之森", 3, 3),
                    Comp.CompTrait("龙魂", 3, 3),
                    Comp.CompTrait("霸王", 1, 1)
                ),
                earlyGame = "开局抢腰带或大棒，做科技枪或狂徒。用斗士或永恒之森过渡。",
                midGame = "4-1拉7人口大D，找努努和龙女天选。追三星努努和龙女。",
                lateGame = "三星后上人口找瑟提，新春使者选斗士可开8斗。努努一口一个。",
                positioning = "斗士全员顶前排，努努对准敌方C位。龙女站旁边补输出。",
                imageUrl = "https://game.gtimg.cn/images/yxzj/act/a20250116tftfuxing/comp/badou.jpg"
            ),

            // 4. 五秘术天使 - A级阵容
            Comp(
                id = "comp_004",
                name = "五秘术天使",
                version = "天选福星2026",
                tier = Comp.Tier.A,
                popularity = 88.0,
                winRate = 26.0,
                top4Rate = 64.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h014", "凯尔", 4, listOf("天神", "裁决使"), getHeroImageUrl("凯尔")),
                        star = 3,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h015", "悠米", 3, listOf("灵魂莲华·明昼", "秘术师"), getHeroImageUrl("悠米")),
                        star = 2,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h016", "慎", 4, listOf("忍者", "秘术师", "宗师"), getHeroImageUrl("慎")),
                        star = 2,
                        isTank = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h017", "李青", 5, listOf("天神", "决斗大师"), getHeroImageUrl("李青"))
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("秘术师", 5, 5),
                    Comp.CompTrait("天神", 4, 4),
                    Comp.CompTrait("裁决使", 4, 4),
                    Comp.CompTrait("宗师", 3, 3)
                ),
                earlyGame = "开局抢攻速，做羊刀+水银。用天神或决斗过渡，武器带装备打工。",
                midGame = "4-1拉7人口小D找2星天使，有秘术天选直接开4秘术。",
                lateGame = "上8开5秘术4裁决，天使站角落输出。李青踢走关键前排。",
                positioning = "天使站最角落，慎单顶前排，秘术单位保护后排。",
                imageUrl = "https://game.gtimg.cn/images/yxzj/act/a20250116tftfuxing/comp/tianshi.jpg"
            ),

            // 5. 战神奥拉夫 - A级阵容
            Comp(
                id = "comp_005",
                name = "战神奥拉夫",
                version = "天选福星2026",
                tier = Comp.Tier.A,
                popularity = 85.0,
                winRate = 25.0,
                top4Rate = 62.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h018", "奥拉夫", 4, listOf("龙魂", "战神"), getHeroImageUrl("奥拉夫")),
                        star = 2,
                        isCarry = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h019", "格温", 4, listOf("福星守护神"), getHeroImageUrl("格温")),
                        star = 2,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h020", "德莱文", 4, listOf("福星", "战神"), getHeroImageUrl("德莱文")),
                        star = 2,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h021", "剑魔", 4, listOf("腥红之月", "重装战士"), getHeroImageUrl("亚托克斯"))
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("战神", 6, 6),
                    Comp.CompTrait("龙魂", 3, 3),
                    Comp.CompTrait("猩红之月", 3, 3),
                    Comp.CompTrait("福星守护神", 1, 1)
                ),
                earlyGame = "开局抢攻速或拳套，做羊刀或正义。用三国射手或猩红过渡。",
                midGame = "4-1拉8人口，找奥拉夫和德莱文。开出6战神3龙魂即可锁血。",
                lateGame = "上9人口补高费卡，奥拉夫顶前排输出。格温提供额外战力。",
                positioning = "奥拉夫顶第一排吃伤害，德莱文站后排输出。剑魔前排抗伤。",
                imageUrl = "https://game.gtimg.cn/images/yxzj/act/a20250116tftfuxing/comp/aolafu.jpg"
            ),

            // 6. 福星守护神凯隐 - A级阵容
            Comp(
                id = "comp_006",
                name = "福星守护神-凯隐",
                version = "天选福星2026",
                tier = Comp.Tier.A,
                popularity = 82.0,
                winRate = 24.0,
                top4Rate = 60.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h022", "凯隐", 5, listOf("福星守护神"), getHeroImageUrl("凯隐")),
                        star = 2,
                        isCarry = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h023", "奥拉夫", 4, listOf("龙魂", "战神"), getHeroImageUrl("奥拉夫")),
                        star = 2,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h024", "格温", 4, listOf("福星守护神"), getHeroImageUrl("格温")),
                        star = 2,
                        position = Comp.Position.BACK
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("福星守护神", 1, 1),
                    Comp.CompTrait("战神", 3, 3),
                    Comp.CompTrait("龙魂", 3, 3)
                ),
                earlyGame = "前期用强力羁绊过渡，尽早解锁凯隐。福星守护神不占用人口。",
                midGame = "凯隐解锁后提供强力增益，根据凯隐类型调整阵容。",
                lateGame = "福星守护神提供独特羁绊效果，让阵容充满新的可能。",
                positioning = "凯隐作为守护神单挂，其他单位正常站位。",
                imageUrl = "https://game.gtimg.cn/images/yxzj/act/a20250116tftfuxing/comp/kaiyin.jpg"
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
        val comps = cachedComps ?: get2026FuxingCompData()
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
