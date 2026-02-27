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
 * 从天选福星版本获取阵容数据
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
     * 天选福星版本热门阵容
     */
    private fun getMockCompData(): List<Comp> {
        return listOf(
            // 1. 福星三国 - S级阵容
            Comp(
                id = "comp_001",
                name = "福星三国",
                version = "福星",
                tier = Comp.Tier.S,
                popularity = 95.0,
                winRate = 28.5,
                top4Rate = 68.2,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h001", "卡特琳娜", 3, listOf("福星", "三国猛将", "刺客")),
                        star = 3,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h002", "瑟提", 5, listOf("霸王", "斗士")),
                        star = 2,
                        isCarry = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h003", "塔姆", 1, listOf("福星", "斗士")),
                        star = 3,
                        isTank = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h004", "安妮", 2, listOf("福星", "法师")),
                        star = 3,
                        position = Comp.Position.BACK
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("福星", 6, 6),
                    Comp.CompTrait("三国猛将", 3, 3),
                    Comp.CompTrait("刺客", 2, 2),
                    Comp.CompTrait("斗士", 2, 2)
                ),
                earlyGame = "开局抢大棒或眼泪，优先做卡特装备。前期用3福星过渡，连败攒经济。",
                midGame = "3-2拉6人口，D出卡特琳娜三星。福星连败收菜，获取大量装备和经济。",
                lateGame = "上8人口找瑟提和永恩，福星提供妮蔻和装备。9人口可补5费卡提升上限。",
                positioning = "卡特跳后排切C位，瑟提单独前排，福星单位分散站位。"
            ),

            // 2. 龙魂战神 - S级阵容
            Comp(
                id = "comp_002",
                name = "龙魂战神",
                version = "福星",
                tier = Comp.Tier.S,
                popularity = 88.0,
                winRate = 26.5,
                top4Rate = 65.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h005", "奥拉夫", 4, listOf("龙魂", "战神")),
                        star = 2,
                        isCarry = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h006", "斯维因", 3, listOf("龙魂", "神使")),
                        star = 3,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h007", "奥恩", 5, listOf("铁匠", "永恒之森", "重装战士")),
                        star = 2,
                        isTank = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h008", "崔丝塔娜", "1", listOf("龙魂", "神射手")),
                        star = 3,
                        position = Comp.Position.BACK
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("龙魂", 6, 6),
                    Comp.CompTrait("战神", 3, 3),
                    Comp.CompTrait("神使", 2, 2),
                    Comp.CompTrait("重装战士", 2, 2)
                ),
                earlyGame = "开局抢攻速或拳套，做分裂弓或正义。用龙魂小炮或神射过渡。",
                midGame = "4-1拉7人口，找奥拉夫和斯维因。奥拉夫两星后锁血，龙魂加持伤害爆炸。",
                lateGame = "上8人口找莎弥拉和奥恩，战神提供续航。龙魂祷言一响，清场无敌。",
                positioning = "奥拉夫顶第一排吃龙魂，斯维因站角落输出，注意防刺客。"
            ),

            // 3. 明昼劫 - A级赌狗阵容
            Comp(
                id = "comp_003",
                name = "明昼劫",
                version = "福星",
                tier = Comp.Tier.A,
                popularity = 85.0,
                winRate = 24.5,
                top4Rate = 62.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h009", "劫", 2, listOf("夜影", "忍者")),
                        star = 3,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h010", "伊芙琳", 3, listOf("夜影", "猩红之月")),
                        star = 3,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h011", "悠米", 3, listOf("灵魂莲华·明昼", "秘术师")),
                        star = 2,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h012", "黛安娜", 1, listOf("灵魂莲华·明昼", "刺客")),
                        star = 3,
                        position = Comp.Position.BACK
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("夜影", 4, 4),
                    Comp.CompTrait("灵魂莲华·明昼", 4, 4),
                    Comp.CompTrait("忍者", 1, 1),
                    Comp.CompTrait("秘术师", 2, 2)
                ),
                earlyGame = "开局抢攻速或大剑，做火炮或饮血。2阶段不升人口，只拿劫和皎月。",
                midGame = "3-1梭哈D三星劫和黛安娜。明昼提供攻速加成，夜影跳后排切C。",
                lateGame = "劫三星后升人口补4明昼。伊芙琳劫双C，后期可上凯隐。",
                positioning = "夜影单位沉底站位，跳对角切后排。明昼单位分散提供攻速。"
            ),

            // 4. 森林法 - A级阵容
            Comp(
                id = "comp_004",
                name = "森林小法",
                version = "福星",
                tier = Comp.Tier.A,
                popularity = 78.0,
                winRate = 22.0,
                top4Rate = 60.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h013", "维迦", "3", listOf("永恒之森", "魔法师")),
                        star = 3,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h014", "璐璐", "2", listOf("永恒之森", "魔法师")),
                        star = 3,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h015", "努努", "3", listOf("永恒之森", "斗士")),
                        star = 3,
                        isTank = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h016", "奥恩", "5", listOf("铁匠", "永恒之森", "重装战士")),
                        star = 2,
                        position = Comp.Position.FRONT
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("永恒之森", 9, 9),
                    Comp.CompTrait("魔法师", 3, 3),
                    Comp.CompTrait("斗士", 2, 2),
                    Comp.CompTrait("重装战士", 2, 2)
                ),
                earlyGame = "开局抢眼泪或大棒，做蓝BUFF或法爆。用森林或法师打工。",
                midGame = "3-2拉6人口，D三星小法和璐璐。森林羁绊层数叠起来后很肉。",
                lateGame = "上8人口找奥恩打装备，9森林成型后属性爆炸。小法点名秒人。",
                positioning = "努努和奥恩前排，小法站角落输出。森林单位靠近互相叠层。"
            ),

            // 5. 重秘神射 - A级阵容
            Comp(
                id = "comp_005",
                name = "重秘神射",
                version = "福星",
                tier = Comp.Tier.A,
                popularity = 75.0,
                winRate = 21.5,
                top4Rate = 58.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h017", "希维尔", "3", listOf("猩红之月", "神射手")),
                        star = 3,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h018", "提莫", "2", listOf("灵魂莲华·明昼", "神射手")),
                        star = 3,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h019", "瑟庄妮", "4", listOf("福星", "重装战士")),
                        star = 2,
                        isTank = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h020", "亚托克斯", "4", listOf("腥红之月", "重装战士")),
                        star = 2,
                        position = Comp.Position.FRONT
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("神射手", 4, 4),
                    Comp.CompTrait("重装战士", 4, 4),
                    Comp.CompTrait("秘术师", 2, 2),
                    Comp.CompTrait("猩红之月", 3, 3)
                ),
                earlyGame = "开局抢攻速，做分裂弓或轻语。用神射或猩红过渡。",
                midGame = "4-1拉7人口，找希维尔和重装前排。希维尔两星后锁血。",
                lateGame = "上8人口补秘术和高质量前排。神射弹射清场，重装提供坦度。",
                positioning = "重装顶前排，神射站后排。希维尔站中间让弹射收益最大化。"
            ),

            // 6. 八斗森林 - B级阵容
            Comp(
                id = "comp_006",
                name = "八斗努努",
                version = "福星",
                tier = Comp.Tier.B,
                popularity = 65.0,
                winRate = 18.5,
                top4Rate = 52.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h021", "努努", "3", listOf("永恒之森", "斗士")),
                        star = 3,
                        isCarry = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h022", "瑟提", "5", listOf("霸王", "斗士")),
                        star = 2,
                        isCarry = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h023", "塔姆", "1", listOf("福星", "斗士")),
                        star = 3,
                        isTank = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h024", "茂凯", "1", listOf("永恒之森", "斗士")),
                        star = 3,
                        position = Comp.Position.FRONT
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("斗士", 8, 8),
                    Comp.CompTrait("永恒之森", "3", "3"),
                    Comp.CompTrait("霸王", 1, 1)
                ),
                earlyGame = "开局抢腰带或大棒，做科技枪或狂徒。用斗士或森林过渡。",
                midGame = "3-2拉6人口，D三星努努和塔姆。八斗士提供高额生命值。",
                lateGame = "上8人口找瑟提和奥恩。努努一口一个，瑟提抱摔清场。",
                positioning = "斗士全员顶前排，努努对准敌方C位。注意瑟提做俯卧撑位置。"
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
