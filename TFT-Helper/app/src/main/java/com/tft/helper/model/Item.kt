package com.tft.helper.model

import com.google.gson.annotations.SerializedName

/**
 * 装备数据模型
 * 
 * 存储装备的基本信息
 * 包括装备名称、效果、合成路径等
 * 
 * @property id 装备唯一标识
 * @property name 装备名称
 * @property description 装备效果描述
 * @property components 合成所需的小件装备
 * @property type 装备类型（AD/AP/防御/功能）
 * @property imageUrl 装备图片URL
 */
data class Item(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("components")
    val components: List<String> = emptyList(),  // 合成配方，如 ["大剑", "攻速"]
    
    @SerializedName("type")
    val type: ItemType,
    
    @SerializedName("image_url")
    val imageUrl: String? = null
) {
    /**
     * 装备类型枚举
     */
    enum class ItemType {
        AD,         // 物理攻击装备
        AP,         // 法术强度装备
        DEFENSE,    // 防御装备
        UTILITY,    // 功能装备
        SPECIAL     // 特殊装备（如转职）
    }
    
    /**
     * 获取装备类型对应的中文名称
     */
    fun getTypeName(): String = when (type) {
        ItemType.AD -> "物理"
        ItemType.AP -> "法术"
        ItemType.DEFENSE -> "防御"
        ItemType.UTILITY -> "功能"
        ItemType.SPECIAL -> "特殊"
    }
}

/**
 * 装备推荐数据
 * 用于表示某个英雄的核心装备推荐
 */
data class ItemRecommendation(
    @SerializedName("hero_id")
    val heroId: String,
    
    @SerializedName("core_items")
    val coreItems: List<String>,  // 核心装备（优先级最高）
    
    @SerializedName("alternative_items")
    val alternativeItems: List<String> = emptyList()  // 备选装备
)
