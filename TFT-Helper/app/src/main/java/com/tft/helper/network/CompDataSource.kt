package com.tft.helper.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tft.helper.model.Comp
import com.tft.helper.model.Hero
import com.tft.helper.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 阵容数据源
 * 
 * 从网络获取公开阵容数据
 * 支持多个数据源，带本地缓存机制
 * 
 * 注意：只读取公开网站数据，不涉及游戏客户端
 */
class CompDataSource private constructor() {

    // OkHttp客户端配置
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    // 本地缓存
    private var cachedComps: List<Comp>? = null
    private var cacheTime: Long = 0
    private val CACHE_DURATION = 5 * 60 * 1000  // 缓存5分钟

    companion object {
        @Volatile
        private var instance: CompDataSource? = null

        fun getInstance(): CompDataSource {
            return instance ?: synchronized(this) {
                instance ?: CompDataSource().also { instance = it }
            }
        }
    }

    /**
     * 获取阵容列表
     * 
     * @param forceRefresh 是否强制刷新缓存
     * @param sortBy 排序方式
     * @return 阵容列表
     */
    suspend fun getComps(
        forceRefresh: Boolean = false,
        sortBy: SortType = SortType.POPULARITY
    ): Result<List<Comp>> = withContext(Dispatchers.IO) {
        try {
            // 检查缓存
            if (!forceRefresh && cachedComps != null && 
                System.currentTimeMillis() - cacheTime < CACHE_DURATION) {
                return@withContext Result.success(sortComps(cachedComps!!, sortBy))
            }

            // 从网络获取数据
            val comps = fetchCompsFromNetwork()
            
            // 更新缓存
            cachedComps = comps
            cacheTime = System.currentTimeMillis()
            
            Result.success(sortComps(comps, sortBy))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从网络获取阵容数据
     * 这里使用模拟数据，实际项目中替换为真实API
     */
    private fun fetchCompsFromNetwork(): List<Comp> {
        // 模拟网络请求延迟
        Thread.sleep(500)
        
        // 返回模拟数据（实际项目中这里应该是HTTP请求）
        return getMockCompData()
    }

    /**
     * 获取模拟阵容数据
     * 包含当前版本热门阵容
     */
    private fun getMockCompData(): List<Comp> {
        return listOf(
            // 1. 剪纸仙灵 - 当前热门S级阵容
            Comp(
                id = "comp_001",
                name = "剪纸仙灵95",
                version = "S11",
                tier = Comp.Tier.S,
                popularity = 95.0,
                winRate = 28.5,
                top4Rate = 68.2,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h001", "凯尔", 5, listOf("剪纸仙灵", "决斗大师"), isCarry = true),
                        star = 2,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h002", "加里奥", 4, listOf("剪纸仙灵", "斗士"), isTank = true),
                        star = 2,
                        isTank = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h003", "希维尔", 1, listOf("剪纸仙灵", "迅捷射手")),
                        star = 3
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("剪纸仙灵", 7, 7),
                    Comp.CompTrait("决斗大师", 2, 2),
                    Comp.CompTrait("斗士", 2, 2)
                ),
                earlyGame = "开局抢攻速或大剑，优先做羊刀。前期用3剪纸仙灵过渡，希维尔带装备打工。",
                midGame = "4-1拉7人口，找加里奥和刀妹。有剪纸转职可以开7剪纸。",
                lateGame = "上8人口找凯尔，凯尔来之前用天使C。阵容成型后强度极高。",
                positioning = "加里奥单顶前排，凯尔站角落输出，其他单位包围保护。"
            ),

            // 2. 灵魂莲华 - A级阵容
            Comp(
                id = "comp_002",
                name = "灵魂莲华阿狸",
                version = "S11",
                tier = Comp.Tier.A,
                popularity = 82.0,
                winRate = 22.3,
                top4Rate = 61.5,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h004", "阿狸", 4, listOf("灵魂莲华", "法师"), isCarry = true),
                        star = 2,
                        isCarry = true
                    ),
                    Comp.CompHero(
                        hero = Hero("h005", "锤石", 4, listOf("灵魂莲华", "护卫"), isTank = true),
                        star = 2,
                        isTank = true
                    ),
                    Comp.CompHero(
                        hero = Hero("h006", "辛德拉", 5, listOf("灵魂莲华", "法师"))
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("灵魂莲华", 7, 7),
                    Comp.CompTrait("法师", 4, 4),
                    Comp.CompTrait("护卫", 2, 2)
                ),
                earlyGame = "开局抢眼泪或大棒，优先做蓝BUFF或法爆。用亚索带阿狸装备打工。",
                midGame = "4-1拉7人口，找阿狸和锤石。灵魂莲华羁绊提供团队增益。",
                lateGame = "上8人口找辛德拉，阵容大成。注意调整灵魂莲华链接关系。",
                positioning = "锤石前排承伤，阿狸站角落输出，注意防刺客。"
            ),

            // 3. 天将 - S级赌狗阵容
            Comp(
                id = "comp_003",
                name = "天将螳螂",
                version = "S11",
                tier = Comp.Tier.S,
                popularity = 88.0,
                winRate = 25.8,
                top4Rate = 65.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h007", "卡兹克", 1, listOf("天将", "死神"), isCarry = true),
                        star = 3,
                        isCarry = true
                    ),
                    Comp.CompHero(
                        hero = Hero("h008", "墨菲特", 1, listOf("天将", "擎天卫"), isTank = true),
                        star = 3,
                        isTank = true
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("天将", 5, 5),
                    Comp.CompTrait("死神", 2, 2),
                    Comp.CompTrait("擎天卫", 2, 2)
                ),
                earlyGame = "开局抢大剑或拳套，做饮血或正义。2阶段不升人口，卡利息D螳螂。",
                midGame = "3-1梭哈D三星螳螂，同时追墨菲特。螳螂3星后升人口补羁绊。",
                lateGame = "6人口或7人口停，追其他天将三星。上限靠三星数量和装备。",
                positioning = "螳螂跳对角C位，墨菲特前排抗伤。"
            ),

            // 4. 夜幽 - A级阵容
            Comp(
                id = "comp_004",
                name = "夜幽拉露恩",
                version = "S11",
                tier = Comp.Tier.A,
                popularity = 75.0,
                winRate = 20.5,
                top4Rate = 58.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h009", "拉露恩", 3, listOf("夜幽", "神谕者"), isCarry = true),
                        star = 3,
                        isCarry = true
                    ),
                    Comp.CompHero(
                        hero = Hero("h010", "塞拉斯", 4, listOf("夜幽", "斗士"))
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("夜幽", 6, 6),
                    Comp.CompTrait("神谕者", 4, 4),
                    Comp.CompTrait("斗士", 2, 2)
                ),
                earlyGame = "开局抢眼泪，做青龙刀。用诺手或亚索带装备打工。",
                midGame = "3-2拉6人口，D拉露恩三星。同时找塞拉斯和其他夜幽。",
                lateGame = "拉露恩3星后上人口补高费卡，可转夜幽95。",
                positioning = "拉露恩站后排中间，利用夜幽护盾保命。"
            ),

            // 5. 死神护卫 - B级阵容
            Comp(
                id = "comp_005",
                name = "死神护卫",
                version = "S11",
                tier = Comp.Tier.B,
                popularity = 60.0,
                winRate = 15.2,
                top4Rate = 50.5,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h011", "千珏", 4, listOf("死神", "青花瓷"), isCarry = true),
                        star = 2,
                        isCarry = true
                    ),
                    Comp.CompHero(
                        hero = Hero("h012", "阿木木", 3, listOf("护卫", "青花瓷"), isTank = true),
                        star = 3,
                        isTank = true
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("死神", 4, 4),
                    Comp.CompTrait("护卫", 4, 4),
                    Comp.CompTrait("青花瓷", 2, 2)
                ),
                earlyGame = "开局抢攻速，做羊刀或分裂弓。用赛娜或女警打工。",
                midGame = "4-1拉7人口，找千珏和阿木木。阿木木尽量追三。",
                lateGame = "上8人口补丽桑卓，青花瓷羁绊提供控制。",
                positioning = "护卫前排，死神后排，注意分散站位防AOE。"
            ),

            // 6. 决斗大师 - A级阵容
            Comp(
                id = "comp_006",
                name = "决斗大师狗熊",
                version = "S11",
                tier = Comp.Tier.A,
                popularity = 78.0,
                winRate = 21.0,
                top4Rate = 59.5,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h013", "沃利贝尔", 3, listOf("决斗大师", "墨之影"), isCarry = true),
                        star = 3,
                        isCarry = true
                    ),
                    Comp.CompHero(
                        hero = Hero("h014", "奇亚娜", 3, listOf("决斗大师", "吉星")),
                        star = 3
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("决斗大师", 6, 6),
                    Comp.CompTrait("墨之影", 3, 3),
                    Comp.CompTrait("吉星", 3, 3)
                ),
                earlyGame = "开局抢攻速或拳套，做羊刀或泰坦。用德莱厄斯带装备打工。",
                midGame = "3-2拉6人口，D三星狗熊和崔丝塔娜。决斗大师羁绊提供攻速。",
                lateGame = "三星后上人口补高费卡，墨之影提供额外伤害。",
                positioning = "分散站位，利用决斗大师的攻速叠加。"
            )
        )
    }

    /**
     * 对阵容列表进行排序
     */
    private fun sortComps(comps: List<Comp>, sortBy: SortType): List<Comp> {
        return when (sortBy) {
            SortType.POPULARITY -> comps.sortedByDescending { it.popularity }
            SortType.WIN_RATE -> comps.sortedByDescending { it.winRate }
            SortType.TOP4_RATE -> comps.sortedByDescending { it.top4Rate }
            SortType.TIER -> comps.sortedBy { it.tier.ordinal }
        }
    }

    /**
     * 获取英雄详情
     */
    suspend fun getHeroDetail(heroId: String): Result<Hero> = withContext(Dispatchers.IO) {
        // 实际项目中从网络获取
        Result.failure(NotImplementedError("待实现"))
    }

    /**
     * 获取装备详情
     */
    suspend fun getItemDetail(itemId: String): Result<Item> = withContext(Dispatchers.IO) {
        // 实际项目中从网络获取
        Result.failure(NotImplementedError("待实现"))
    }

    /**
     * 搜索阵容
     */
    suspend fun searchComps(keyword: String): Result<List<Comp>> = withContext(Dispatchers.IO) {
        val comps = cachedComps ?: getMockCompData()
        val filtered = comps.filter {
            it.name.contains(keyword) ||
            it.coreHeroes.any { hero -> hero.hero.name.contains(keyword) } ||
            it.traits.any { trait -> trait.name.contains(keyword) }
        }
        Result.success(filtered)
    }

    /**
     * 排序类型
     */
    enum class SortType {
        POPULARITY,  // 热度排序（默认）
        WIN_RATE,    // 胜率排序
        TOP4_RATE,   // 前四率排序
        TIER         // 评级排序
    }
}
