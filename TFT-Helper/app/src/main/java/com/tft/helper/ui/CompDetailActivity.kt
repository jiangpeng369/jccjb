package com.tft.helper.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.tft.helper.R
import com.tft.helper.model.Comp
import com.tft.helper.network.CompDataSource
import com.tft.helper.utils.ImageCacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 阵容详情Activity
 * 
 * 展示单个阵容的详细信息，包括：
 * - 阵容评级、胜率、热度
 * - 核心英雄及装备
 * - 核心羁绊
 * - 运营思路（前期/中期/后期）
 * - 站位建议
 */
class CompDetailActivity : AppCompatActivity() {

    // 视图组件
    private lateinit var ivCompImage: ImageView
    private lateinit var tvCompName: TextView
    private lateinit var tvTier: TextView
    private lateinit var tvPopularity: TextView
    private lateinit var tvWinRate: TextView
    private lateinit var tvTop4Rate: TextView
    private lateinit var chipGroupTraits: ChipGroup
    private lateinit var layoutHeroes: androidx.gridlayout.widget.GridLayout
    private lateinit var tvEarlyGame: TextView
    private lateinit var tvMidGame: TextView
    private lateinit var tvLateGame: TextView
    private lateinit var tvPositioning: TextView

    // 数据源
    private val dataSource = CompDataSource.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comp_detail)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "阵容详情"

        // 初始化视图
        initViews()

        // 加载阵容数据
        val compId = intent.getStringExtra("comp_id")
        if (compId != null) {
            loadCompDetail(compId)
        }
    }

    /**
     * 初始化视图组件
     */
    private fun initViews() {
        ivCompImage = findViewById(R.id.iv_comp_image_detail)
        tvCompName = findViewById(R.id.tv_comp_name_detail)
        tvTier = findViewById(R.id.tv_tier_detail)
        tvPopularity = findViewById(R.id.tv_popularity_detail)
        tvWinRate = findViewById(R.id.tv_win_rate_detail)
        tvTop4Rate = findViewById(R.id.tv_top4_rate_detail)
        chipGroupTraits = findViewById(R.id.chip_group_traits)
        layoutHeroes = findViewById(R.id.layout_heroes)
        tvEarlyGame = findViewById(R.id.tv_early_game)
        tvMidGame = findViewById(R.id.tv_mid_game)
        tvLateGame = findViewById(R.id.tv_late_game)
        tvPositioning = findViewById(R.id.tv_positioning)
    }

    /**
     * 加载阵容详情
     */
    private fun loadCompDetail(compId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // 获取阵容数据（这里简化处理，实际应该从缓存或数据库获取）
            val result = dataSource.getComps()
            
            withContext(Dispatchers.Main) {
                result.onSuccess { comps ->
                    val comp = comps.find { it.id == compId }
                    comp?.let { displayCompDetail(it) }
                }
            }
        }
    }

    /**
     * 展示阵容详情（使用本地缓存图片）
     */
    private fun displayCompDetail(comp: Comp) {
        // 设置标题
        supportActionBar?.title = comp.name

        // 加载本地缓存的阵容图片
        val imageCacheManager = ImageCacheManager.getInstance(this)
        val localImagePath = imageCacheManager.getCompImagePath(comp.id)
        
        if (localImagePath != null) {
            Glide.with(this)
                .load(File(localImagePath))
                .placeholder(R.drawable.ic_comp_placeholder)
                .error(R.drawable.ic_comp_placeholder)
                .into(ivCompImage)
        } else if (!comp.imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(comp.imageUrl)
                .placeholder(R.drawable.ic_comp_placeholder)
                .error(R.drawable.ic_comp_placeholder)
                .into(ivCompImage)
        }

        // 设置基本信息
        tvCompName.text = comp.name
        tvTier.text = "评级: ${comp.tier}"
        tvTier.setTextColor(parseColor(comp.getTierColor()))
        tvPopularity.text = "热度: ${comp.popularity}"
        tvWinRate.text = "胜率: ${comp.winRate}%"
        tvTop4Rate.text = "前四率: ${comp.top4Rate}%"

        // 设置羁绊
        chipGroupTraits.removeAllViews()
        comp.traits.forEach { trait ->
            val chip = Chip(this).apply {
                text = "${trait.name} ${trait.count}"
                isCheckable = false
                setChipBackgroundColorResource(R.color.colorPrimaryLight)
            }
            chipGroupTraits.addView(chip)
        }

        // 设置英雄
        displayHeroes(comp.coreHeroes)

        // 设置运营思路
        tvEarlyGame.text = comp.earlyGame
        tvMidGame.text = comp.midGame
        tvLateGame.text = comp.lateGame

        // 设置站位建议
        tvPositioning.text = comp.positioning
    }

    /**
     * 展示英雄列表（使用本地缓存图片）
     */
    private fun displayHeroes(heroes: List<Comp.CompHero>) {
        layoutHeroes.removeAllViews()
        val imageCacheManager = ImageCacheManager.getInstance(this)

        heroes.forEach { compHero ->
            val heroView = layoutInflater.inflate(R.layout.item_hero_detail, layoutHeroes, false)
            
            val ivHero = heroView.findViewById<ImageView>(R.id.iv_hero)
            val tvHeroName = heroView.findViewById<TextView>(R.id.tv_hero_name)
            val tvHeroCost = heroView.findViewById<TextView>(R.id.tv_hero_cost)
            val ivCarry = heroView.findViewById<ImageView>(R.id.iv_carry)
            val ivTank = heroView.findViewById<ImageView>(R.id.iv_tank)

            // 设置英雄信息
            tvHeroName.text = compHero.hero.name
            tvHeroCost.text = "${compHero.hero.cost}费"
            tvHeroCost.setTextColor(parseColor(compHero.hero.getCostColor()))

            // 显示主C/主坦标识
            ivCarry.visibility = if (compHero.isCarry) android.view.View.VISIBLE else android.view.View.GONE
            ivTank.visibility = if (compHero.isTank) android.view.View.VISIBLE else android.view.View.GONE

            // 加载本地缓存的英雄图片
            val localImagePath = imageCacheManager.getHeroImagePath(compHero.hero.name)
            if (localImagePath != null) {
                Glide.with(this)
                    .load(File(localImagePath))
                    .placeholder(R.drawable.ic_hero_placeholder)
                    .into(ivHero)
            } else {
                Glide.with(this)
                    .load(compHero.hero.imageUrl)
                    .placeholder(R.drawable.ic_hero_placeholder)
                    .into(ivHero)
            }

            layoutHeroes.addView(heroView)
        }
    }

    /**
     * 解析颜色
     */
    private fun parseColor(colorStr: String): Int {
        return try {
            android.graphics.Color.parseColor(colorStr)
        } catch (e: Exception) {
            android.graphics.Color.GRAY
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
