package com.tft.helper.model

import com.google.gson.annotations.SerializedName

/**
 * 英雄数据模型
 * 
 * 存储金铲铲之战中英雄的基本信息
 * 包括名称、费用、特质、推荐装备等
 * 
 * @property id 英雄唯一标识
 * @property name 英雄名称（中文）
 * @property cost 英雄费用（1-5费）
 * @property traits 英雄特质/羁绊列表
 * @property imageUrl 英雄图片URL
 * @property recommendItems 推荐装备列表
 */
data class Hero(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("cost")
    val cost: Int,  // 1-5费卡
    
    @SerializedName("traits")
    val traits: List<String>,  // 如 ["剪纸仙灵", "圣贤", "武仙子"]
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("recommend_items")
    val recommendItems: List<String> = emptyList()
) {
    /**
     * 获取英雄费用对应的颜色
     * 用于UI展示时显示不同颜色的边框
     */
    fun getCostColor(): String = when (cost) {
        1 -> "#808080"  // 灰色 - 1费
        2 -> "#228B22"  // 绿色 - 2费
        3 -> "#1E90FF"  // 蓝色 - 3费
        4 -> "#9932CC"  // 紫色 - 4费
        5 -> "#FFD700"  // 金色 - 5费
        else -> "#808080"
    }
    
    /**
     * 检查英雄是否包含指定特质
     */
    fun hasTrait(trait: String): Boolean = traits.contains(trait)
}
