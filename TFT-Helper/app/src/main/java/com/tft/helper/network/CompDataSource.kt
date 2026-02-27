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
 * 闃靛鏁版嵁婧?
 * 
 * 浠庣綉缁滆幏鍙栧叕寮€闃靛鏁版嵁
 * 鏀寔澶氫釜鏁版嵁婧愶紝甯︽湰鍦扮紦瀛樻満鍒?
 * 
 * 娉ㄦ剰锛氬彧璇诲彇鍏紑缃戠珯鏁版嵁锛屼笉娑夊強娓告垙瀹㈡埛绔?
 */
class CompDataSource private constructor() {

    // OkHttp瀹㈡埛绔厤缃?
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    // 鏈湴缂撳瓨
    private var cachedComps: List<Comp>? = null
    private var cacheTime: Long = 0
    private val CACHE_DURATION = 5 * 60 * 1000  // 缂撳瓨5鍒嗛挓

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
     * 鑾峰彇闃靛鍒楄〃
     * 
     * @param forceRefresh 鏄惁寮哄埗鍒锋柊缂撳瓨
     * @param sortBy 鎺掑簭鏂瑰紡
     * @return 闃靛鍒楄〃
     */
    suspend fun getComps(
        forceRefresh: Boolean = false,
        sortBy: SortType = SortType.POPULARITY
    ): Result<List<Comp>> = withContext(Dispatchers.IO) {
        try {
            // 妫€鏌ョ紦瀛?
            if (!forceRefresh && cachedComps != null && 
                System.currentTimeMillis() - cacheTime < CACHE_DURATION) {
                return@withContext Result.success(sortComps(cachedComps!!, sortBy))
            }

            // 浠庣綉缁滆幏鍙栨暟鎹?
            val comps = fetchCompsFromNetwork()
            
            // 鏇存柊缂撳瓨
            cachedComps = comps
            cacheTime = System.currentTimeMillis()
            
            Result.success(sortComps(comps, sortBy))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 浠庣綉缁滆幏鍙栭樀瀹规暟鎹?
     * 杩欓噷浣跨敤妯℃嫙鏁版嵁锛屽疄闄呴」鐩腑鏇挎崲涓虹湡瀹濧PI
     */
    private fun fetchCompsFromNetwork(): List<Comp> {
        // 妯℃嫙缃戠粶璇锋眰寤惰繜
        Thread.sleep(500)
        
        // 杩斿洖妯℃嫙鏁版嵁锛堝疄闄呴」鐩腑杩欓噷搴旇鏄疕TTP璇锋眰锛?
        return getMockCompData()
    }

    /**
     * 鑾峰彇妯℃嫙闃靛鏁版嵁
     * 鍖呭惈褰撳墠鐗堟湰鐑棬闃靛
     */
    private fun getMockCompData(): List<Comp> {
        return listOf(
            // 1. 鍓焊浠欑伒 - 褰撳墠鐑棬S绾ч樀瀹?
            Comp(
                id = "comp_001",
                name = "鍓焊浠欑伒95",
                version = "S11",
                tier = Comp.Tier.S,
                popularity = 95.0,
                winRate = 28.5,
                top4Rate = 68.2,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h001", "鑹剧憺鑾夊▍", 5, listOf("鍓焊浠欑伒", "鍐虫枟澶у笀"), isCarry = true),
                        star = 2,
                        isCarry = true,
                        position = Comp.Position.BACK
                    ),
                    Comp.CompHero(
                        hero = Hero("h002", "鍔犻噷濂?, 4, listOf("鍓焊浠欑伒", "鏂楀＋"), isTank = true),
                        star = 2,
                        isTank = true,
                        position = Comp.Position.FRONT
                    ),
                    Comp.CompHero(
                        hero = Hero("h003", "甯岀淮灏?, 1, listOf("鍓焊浠欑伒", "杩呮嵎灏勬墜")),
                        star = 3
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("鍓焊浠欑伒", 7, 7),
                    Comp.CompTrait("鍐虫枟澶у笀", 2, 2),
                    Comp.CompTrait("鏂楀＋", 2, 2)
                ),
                earlyGame = "寮€灞€鎶㈡敾閫熸垨澶у墤锛屼紭鍏堝仛缇婂垁銆傚墠鏈熺敤3鍓焊浠欑伒杩囨浮锛屽笇缁村皵甯﹁澶囨墦宸ャ€?,
                midGame = "4-1鎷?浜哄彛锛屾壘鍔犻噷濂ュ拰鍗¤帋銆傛湁鍓焊杞亴鍙互寮€7鍓焊銆?,
                lateGame = "涓?浜哄彛鎵捐壘鐟炶帀濞咃紝鑹剧憺鑾夊▍鏉ヤ箣鍓嶇敤鍗¤帋C銆傞樀瀹规垚鍨嬪悗寮哄害鏋侀珮銆?,
                positioning = "鍔犻噷濂ュ崟椤跺墠鎺掞紝鑹剧憺鑾夊▍绔欒钀斤紝鍏朵粬鍗曚綅鍖呭洿淇濇姢銆?
            ),

            // 2. 鐏甸瓊鑾插崕 - A绾ч樀瀹?
            Comp(
                id = "comp_002",
                name = "鐏甸瓊鑾插崕闃跨嫺",
                version = "S11",
                tier = Comp.Tier.A,
                popularity = 82.0,
                winRate = 22.3,
                top4Rate = 61.5,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h004", "闃跨嫺", 4, listOf("鐏甸瓊鑾插崕", "娉曞笀"), isCarry = true),
                        star = 2,
                        isCarry = true
                    ),
                    Comp.CompHero(
                        hero = Hero("h005", "閿ょ煶", 4, listOf("鐏甸瓊鑾插崕", "鎶ゅ崼"), isTank = true),
                        star = 2,
                        isTank = true
                    ),
                    Comp.CompHero(
                        hero = Hero("h006", "杈涘痉鎷?, 5, listOf("鐏甸瓊鑾插崕", "娉曞笀"))
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("鐏甸瓊鑾插崕", 7, 7),
                    Comp.CompTrait("娉曞笀", 4, 4),
                    Comp.CompTrait("鎶ゅ崼", 2, 2)
                ),
                earlyGame = "寮€灞€鎶㈢溂娉垨澶ф锛屼紭鍏堝仛钃滲UFF鎴栨硶鐖嗐€傜敤浜氱储甯﹂樋鐙歌澶囨墦宸ャ€?,
                midGame = "4-1鎷?浜哄彛锛屾壘闃跨嫺鍜岄敜鐭炽€傜伒榄傝幉鍗庣緛缁婃彁渚涘洟闃熷鐩娿€?,
                lateGame = "涓?浜哄彛鎵捐緵寰锋媺锛岄樀瀹瑰ぇ鎴愩€傛敞鎰忚皟鏁寸伒榄傝幉鍗庨摼鎺ュ叧绯汇€?,
                positioning = "閿ょ煶鍓嶆帓鎶椾激锛岄樋鐙哥珯瑙掕惤杈撳嚭锛屾敞鎰忛槻鍒哄銆?
            ),

            // 3. 澶╁皢 - S绾ц祵鐙楅樀瀹?
            Comp(
                id = "comp_003",
                name = "澶╁皢铻宠瀭",
                version = "S11",
                tier = Comp.Tier.S,
                popularity = 88.0,
                winRate = 25.8,
                top4Rate = 65.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h007", "鍗″吂鍏?, 1, listOf("澶╁皢", "姝荤"), isCarry = true),
                        star = 3,
                        isCarry = true
                    ),
                    Comp.CompHero(
                        hero = Hero("h008", "澧ㄨ彶鐗?, 1, listOf("澶╁皢", "鎿庡ぉ鍗?), isTank = true),
                        star = 3,
                        isTank = true
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("澶╁皢", 5, 5),
                    Comp.CompTrait("姝荤", 2, 2),
                    Comp.CompTrait("鎿庡ぉ鍗?, 2, 2)
                ),
                earlyGame = "寮€灞€鎶㈠ぇ鍓戞垨鎷冲锛屽仛楗鎴栨涔夈€?闃舵涓嶅崌浜哄彛锛屽崱鍒╂伅D铻宠瀭銆?,
                midGame = "3-1姊搱D涓夋槦铻宠瀭锛屽悓鏃惰拷澧ㄨ彶鐗广€傝灣铻?鍚庡崌浜哄彛琛ョ緛缁娿€?,
                lateGame = "6浜哄彛鎴?浜哄彛鍋滐紝杩藉叾浠栧ぉ灏嗕笁鏄熴€備笂闄愰潬涓夋槦鏁伴噺鍜岃澶囥€?,
                positioning = "铻宠瀭璺冲闈浣嶏紝澧ㄨ彶鐗瑰墠鎺掓姉浼ゃ€?
            ),

            // 4. 澶滃菇 - A绾ч樀瀹?
            Comp(
                id = "comp_004",
                name = "澶滃菇鎷夐湶鎭?,
                version = "S11",
                tier = Comp.Tier.A,
                popularity = 75.0,
                winRate = 20.5,
                top4Rate = 58.0,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h009", "鎷夐湶鎭?, 3, listOf("澶滃菇", "绁炶皶鑰?), isCarry = true),
                        star = 3,
                        isCarry = true
                    ),
                    Comp.CompHero(
                        hero = Hero("h010", "濉炴媺鏂?, 4, listOf("澶滃菇", "鏂楀＋"))
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("澶滃菇", 6, 6),
                    Comp.CompTrait("绁炶皶鑰?, 4, 4),
                    Comp.CompTrait("鏂楀＋", 2, 2)
                ),
                earlyGame = "寮€灞€鎶㈢溂娉紝鍋氶潚榫欏垁銆傜敤璇烘墜鎴栦簹绱㈠甫瑁呭鎵撳伐銆?,
                midGame = "3-2鎷?浜哄彛锛孌鎷夐湶鎭╀笁鏄熴€傚悓鏃舵壘濉炴媺鏂拰鍏朵粬澶滃菇銆?,
                lateGame = "鎷夐湶鎭?鍚庝笂浜哄彛琛ラ珮璐瑰崱锛屽彲杞骞?5銆?,
                positioning = "鎷夐湶鎭╃珯鍚庢帓涓棿锛屽埄鐢ㄥ骞芥姢鐩句繚鍛姐€?
            ),

            // 5. 鐙欑鎶ゅ崼 - B绾ч樀瀹?
            Comp(
                id = "comp_005",
                name = "鐙欑鎶ゅ崼",
                version = "S11",
                tier = Comp.Tier.B,
                popularity = 60.0,
                winRate = 15.2,
                top4Rate = 50.5,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h011", "鑹惧笇", 4, listOf("鐙欑", "闈掕姳鐡?), isCarry = true),
                        star = 2,
                        isCarry = true
                    ),
                    Comp.CompHero(
                        hero = Hero("h012", "闃挎湪鏈?, 3, listOf("鎶ゅ崼", "闈掕姳鐡?), isTank = true),
                        star = 3,
                        isTank = true
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("鐙欑", 4, 4),
                    Comp.CompTrait("鎶ゅ崼", 4, 4),
                    Comp.CompTrait("闈掕姳鐡?, 2, 2)
                ),
                earlyGame = "寮€灞€鎶㈡敾閫燂紝鍋氱緤鍒€鎴栧垎瑁傚紦銆傜敤璧涘鎴栧コ璀︽墦宸ャ€?,
                midGame = "4-1鎷?浜哄彛锛屾壘鑹惧笇鍜岄樋鏈ㄦ湪銆傞樋鏈ㄦ湪灏介噺杩戒笁銆?,
                lateGame = "涓?浜哄彛琛ヤ附妗戝崜锛岄潚鑺辩摲缇佺粖鎻愪緵鎺у埗銆?,
                positioning = "鎶ゅ崼鍓嶆帓锛岀嫏绁炲悗鎺掞紝娉ㄦ剰鍒嗘暎绔欎綅闃睞OE銆?
            ),

            // 6. 鍐虫枟澶у笀 - A绾ч樀瀹?
            Comp(
                id = "comp_006",
                name = "鍐虫枟澶у笀鐔婄偖",
                version = "S11",
                tier = Comp.Tier.A,
                popularity = 78.0,
                winRate = 21.0,
                top4Rate = 59.5,
                coreHeroes = listOf(
                    Comp.CompHero(
                        hero = Hero("h013", "娌冨埄璐濆皵", 3, listOf("鍐虫枟澶у笀", "澧ㄤ箣褰?), isCarry = true),
                        star = 3,
                        isCarry = true
                    ),
                    Comp.CompHero(
                        hero = Hero("h014", "宕斾笣濉斿", 3, listOf("鍐虫枟澶у笀", "鍚夋槦")),
                        star = 3
                    )
                ),
                traits = listOf(
                    Comp.CompTrait("鍐虫枟澶у笀", 6, 6),
                    Comp.CompTrait("澧ㄤ箣褰?, 3, 3),
                    Comp.CompTrait("鍚夋槦", 3, 3)
                ),
                earlyGame = "寮€灞€鎶㈡敾閫熸垨鎷冲锛屽仛缇婂垁鎴栨嘲鍧︺€傜敤寰疯幈鍘勬柉甯﹁澶囨墦宸ャ€?,
                midGame = "3-2鎷?浜哄彛锛孌涓夋槦鐙楃唺鍜屽磾涓濆濞溿€傚喅鏂楀ぇ甯堢緛缁婃彁渚涙敾閫熴€?,
                lateGame = "涓夋槦鍚庝笂浜哄彛琛ラ珮璐瑰崱锛屽ⅷ涔嬪奖鎻愪緵棰濆浼ゅ銆?,
                positioning = "鍒嗘暎绔欎綅锛屽埄鐢ㄥ喅鏂楀ぇ甯堢殑鏀婚€熷彔鍔犮€?
            )
        )
    }

    /**
     * 瀵归樀瀹瑰垪琛ㄨ繘琛屾帓搴?
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
     * 鑾峰彇鑻遍泟璇︽儏
     */
    suspend fun getHeroDetail(heroId: String): Result<Hero> = withContext(Dispatchers.IO) {
        // 瀹為檯椤圭洰涓粠缃戠粶鑾峰彇
        Result.failure(NotImplementedError("寰呭疄鐜?))
    }

    /**
     * 鑾峰彇瑁呭璇︽儏
     */
    suspend fun getItemDetail(itemId: String): Result<Item> = withContext(Dispatchers.IO) {
        // 瀹為檯椤圭洰涓粠缃戠粶鑾峰彇
        Result.failure(NotImplementedError("寰呭疄鐜?))
    }

    /**
     * 鎼滅储闃靛
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
     * 鎺掑簭绫诲瀷
     */
    enum class SortType {
        POPULARITY,  // 鐑害鎺掑簭锛堥粯璁わ級
        WIN_RATE,    // 鑳滅巼鎺掑簭
        TOP4_RATE,   // 鍓嶅洓鐜囨帓搴?
        TIER         // 璇勭骇鎺掑簭
    }
}
