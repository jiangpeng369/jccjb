package com.tft.helper.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tft.helper.model.MatchRecord
import com.tft.helper.model.MatchStatistics

/**
 * 本地数据管理器
 * 
 * 管理对战记录的本地存储
 * 使用SharedPreferences和本地文件存储
 */
class LocalDataManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "tft_helper_prefs"
        private const val KEY_MATCH_RECORDS = "match_records"
        private const val KEY_FAVORITE_COMPS = "favorite_comps"
        private const val KEY_USER_SETTINGS = "user_settings"

        @Volatile
        private var instance: LocalDataManager? = null

        fun getInstance(context: Context): LocalDataManager {
            return instance ?: synchronized(this) {
                instance ?: LocalDataManager(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }

    // ==================== 对战记录 ====================

    /**
     * 保存对战记录
     */
    fun saveMatchRecord(record: MatchRecord): Boolean {
        val records = getAllMatchRecords().toMutableList()
        records.add(0, record)  // 新记录添加到开头
        
        return try {
            val json = gson.toJson(records)
            prefs.edit().putString(KEY_MATCH_RECORDS, json).apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 获取所有对战记录
     */
    fun getAllMatchRecords(): List<MatchRecord> {
        val json = prefs.getString(KEY_MATCH_RECORDS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<MatchRecord>>() {}.type
            gson.fromJson<List<MatchRecord>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 删除对战记录
     */
    fun deleteMatchRecord(recordId: String): Boolean {
        val records = getAllMatchRecords().toMutableList()
        val removed = records.removeAll { it.id == recordId }
        
        if (removed) {
            val json = gson.toJson(records)
            prefs.edit().putString(KEY_MATCH_RECORDS, json).apply()
        }
        return removed
    }

    /**
     * 清空所有对战记录
     */
    fun clearAllMatchRecords(): Boolean {
        return prefs.edit().remove(KEY_MATCH_RECORDS).commit()
    }

    /**
     * 获取某套阵容的统计数据
     */
    fun getCompStatistics(compId: String): MatchStatistics? {
        val records = getAllMatchRecords().filter { 
            it.compUsed.compId == compId 
        }
        
        if (records.isEmpty()) return null

        val totalGames = records.size
        val firstPlaceCount = records.count { it.isFirstPlace() }
        val top4Count = records.count { it.isTop4() }
        val averageRank = records.map { it.finalRank }.average()

        return MatchStatistics(
            compId = compId,
            compName = records.first().compUsed.compName,
            totalGames = totalGames,
            firstPlaceCount = firstPlaceCount,
            top4Count = top4Count,
            averageRank = averageRank,
            winRate = firstPlaceCount * 100.0 / totalGames,
            top4Rate = top4Count * 100.0 / totalGames
        )
    }

    /**
     * 获取所有阵容的统计数据
     */
    fun getAllCompStatistics(): List<MatchStatistics> {
        val records = getAllMatchRecords()
        return records.groupBy { it.compUsed.compId ?: it.compUsed.compName }
            .map { (key, recordList) ->
                val totalGames = recordList.size
                val firstPlaceCount = recordList.count { it.isFirstPlace() }
                val top4Count = recordList.count { it.isTop4() }
                val averageRank = recordList.map { it.finalRank }.average()

                MatchStatistics(
                    compId = key,
                    compName = recordList.first().compUsed.compName,
                    totalGames = totalGames,
                    firstPlaceCount = firstPlaceCount,
                    top4Count = top4Count,
                    averageRank = averageRank,
                    winRate = firstPlaceCount * 100.0 / totalGames,
                    top4Rate = top4Count * 100.0 / totalGames
                )
            }.sortedByDescending { it.top4Rate }
    }

    // ==================== 收藏阵容 ====================

    /**
     * 收藏阵容
     */
    fun addFavoriteComp(compId: String): Boolean {
        val favorites = getFavoriteComps().toMutableSet()
        favorites.add(compId)
        return prefs.edit().putStringSet(KEY_FAVORITE_COMPS, favorites).commit()
    }

    /**
     * 取消收藏
     */
    fun removeFavoriteComp(compId: String): Boolean {
        val favorites = getFavoriteComps().toMutableSet()
        favorites.remove(compId)
        return prefs.edit().putStringSet(KEY_FAVORITE_COMPS, favorites).commit()
    }

    /**
     * 获取所有收藏的阵容ID
     */
    fun getFavoriteComps(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITE_COMPS, emptySet()) ?: emptySet()
    }

    /**
     * 判断是否已收藏
     */
    fun isFavoriteComp(compId: String): Boolean {
        return getFavoriteComps().contains(compId)
    }

    // ==================== 用户设置 ====================

    /**
     * 保存用户设置
     */
    fun saveSetting(key: String, value: String) {
        prefs.edit().putString("${KEY_USER_SETTINGS}_$key", value).apply()
    }

    /**
     * 获取用户设置
     */
    fun getSetting(key: String, defaultValue: String = ""): String {
        return prefs.getString("${KEY_USER_SETTINGS}_$key", defaultValue) ?: defaultValue
    }

    /**
     * 保存布尔设置
     */
    fun saveBooleanSetting(key: String, value: Boolean) {
        prefs.edit().putBoolean("${KEY_USER_SETTINGS}_$key", value).apply()
    }

    /**
     * 获取布尔设置
     */
    fun getBooleanSetting(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean("${KEY_USER_SETTINGS}_$key", defaultValue)
    }

    /**
     * 清除所有数据
     */
    fun clearAllData(): Boolean {
        return prefs.edit().clear().commit()
    }
}
