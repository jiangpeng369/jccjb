package com.tft.helper.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tft.helper.R
import com.tft.helper.calc.CardPoolCalculator
import com.tft.helper.ui.adapter.CardPoolAdapter
import com.tft.helper.utils.ScreenAdapter

/**
 * 牌库计算器Activity
 * 
 * 功能：
 * 1. 查看各费用卡牌的剩余数量
 * 2. 输入自己和其他玩家持有的卡牌，计算剩余
 * 3. 追三星可行性分析
 * 4. 卡池分布可视化
 */
class CardPoolCalcActivity : AppCompatActivity() {

    // 视图组件
    private lateinit var spinnerCost: Spinner
    private lateinit var recyclerCardPool: RecyclerView
    private lateinit var layoutThreeStarCalc: LinearLayout
    private lateinit var etHeroName: EditText
    private lateinit var etCurrentOwned: EditText
    private lateinit var etTakenByOthers: EditText
    private lateinit var btnCalculate: Button
    private lateinit var btnReset: Button
    private lateinit var tvResult: TextView
    private lateinit var tvPoolSummary: TextView

    // 适配器
    private lateinit var cardPoolAdapter: CardPoolAdapter

    // 当前选中的费用
    private var currentCost = 3

    // 模拟卡池数据（实际项目中这些数据应从网络获取或用户输入）
    private val mockCardPoolData = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_pool_calc)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "牌库计算器"

        // 初始化模拟数据
        initMockData()

        // 初始化视图
        initViews()

        // 设置适配器
        setupAdapter()

        // 设置监听器
        setupListeners()

        // 初始加载
        loadCardPoolData(currentCost)
    }

    /**
     * 初始化模拟数据
     */
    private fun initMockData() {
        // 模拟各英雄的已拿数量（实际应由用户输入）
        // 1费卡
        for (i in 1..13) {
            mockCardPoolData["1费英雄$i"] = (0..15).random()
        }
        // 2费卡
        for (i in 1..13) {
            mockCardPoolData["2费英雄$i"] = (0..12).random()
        }
        // 3费卡
        for (i in 1..13) {
            mockCardPoolData["3费英雄$i"] = (0..10).random()
        }
        // 4费卡
        for (i in 1..12) {
            mockCardPoolData["4费英雄$i"] = (0..6).random()
        }
        // 5费卡
        for (i in 1..8) {
            mockCardPoolData["5费英雄$i"] = (0..5).random()
        }
    }

    /**
     * 初始化视图
     */
    private fun initViews() {
        spinnerCost = findViewById(R.id.spinner_cost)
        recyclerCardPool = findViewById(R.id.recycler_card_pool)
        layoutThreeStarCalc = findViewById(R.id.layout_three_star_calc)
        etHeroName = findViewById(R.id.et_hero_name)
        etCurrentOwned = findViewById(R.id.et_current_owned)
        etTakenByOthers = findViewById(R.id.et_taken_by_others)
        btnCalculate = findViewById(R.id.btn_calculate_feasibility)
        btnReset = findViewById(R.id.btn_reset_data)
        tvResult = findViewById(R.id.tv_feasibility_result)
        tvPoolSummary = findViewById(R.id.tv_pool_summary)

        // 设置费用选择器
        val costOptions = listOf("1费卡", "2费卡", "3费卡", "4费卡", "5费卡")
        spinnerCost.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, costOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    /**
     * 设置RecyclerView适配器
     */
    private fun setupAdapter() {
        // 根据屏幕宽度计算列数
        val spanCount = when {
            ScreenAdapter.getScreenWidthDp() < 360 -> 2
            ScreenAdapter.getScreenWidthDp() < 600 -> 3
            else -> 4
        }

        recyclerCardPool.layoutManager = GridLayoutManager(this, spanCount)
        cardPoolAdapter = CardPoolAdapter { heroStatus ->
            // 点击英雄，自动填充到追三星计算器中
            etHeroName.setText(heroStatus.heroName)
            etCurrentOwned.setText(heroStatus.takenCount.toString())
            etTakenByOthers.setText("0")

            // 滚动到计算器区域
            layoutThreeStarCalc.requestFocus()
        }
        recyclerCardPool.adapter = cardPoolAdapter
    }

    /**
     * 设置监听器
     */
    private fun setupListeners() {
        // 费用选择
        spinnerCost.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentCost = position + 1
                loadCardPoolData(currentCost)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 计算追三星可行性
        btnCalculate.setOnClickListener {
            calculateThreeStarFeasibility()
        }

        // 重置数据
        btnReset.setOnClickListener {
            showResetConfirmDialog()
        }
    }

    /**
     * 加载卡池数据
     */
    private fun loadCardPoolData(cost: Int) {
        // 获取该费用的所有英雄数据
        val heroCount = CardPoolCalculator.HERO_COUNT_PER_COST[cost] ?: 13
        val heroStatuses = (1..heroCount).map { index ->
            val heroName = "${cost}费英雄$index"
            val taken = mockCardPoolData[heroName] ?: 0
            val total = CardPoolCalculator.CARD_POOL_COUNT[cost] ?: 0

            CardPoolCalculator.HeroCardStatus(
                heroId = "hero_${cost}_$index",
                heroName = heroName,
                cost = cost,
                totalInPool = total,
                takenCount = taken,
                remaining = (total - taken).coerceAtLeast(0)
            )
        }.sortedByDescending { it.remaining }

        // 更新适配器
        cardPoolAdapter.updateData(heroStatuses)

        // 更新汇总信息
        updatePoolSummary(cost, heroStatuses)
    }

    /**
     * 更新卡池汇总信息
     */
    private fun updatePoolSummary(cost: Int, heroStatuses: List<CardPoolCalculator.HeroCardStatus>) {
        val totalPerHero = CardPoolCalculator.CARD_POOL_COUNT[cost] ?: 0
        val totalCards = totalPerHero * heroStatuses.size
        val totalRemaining = heroStatuses.sumOf { it.remaining }
        val totalTaken = totalCards - totalRemaining
        val emptyCount = heroStatuses.count { it.remaining == 0 }

        tvPoolSummary.text = buildString {
            append("【${cost}费卡】")
            append(" 总卡牌: ${totalCards}")
            append(" | 剩余: ${totalRemaining}")
            append(" | 已拿: ${totalTaken}")
            append(" | 已空: ${emptyCount}张")
        }
    }

    /**
     * 计算追三星可行性
     */
    private fun calculateThreeStarFeasibility() {
        val heroName = etHeroName.text.toString().trim()
        val currentOwned = etCurrentOwned.text.toString().toIntOrNull() ?: 0
        val takenByOthers = etTakenByOthers.text.toString().toIntOrNull() ?: 0

        if (heroName.isEmpty()) {
            Toast.makeText(this, "请输入英雄名称", Toast.LENGTH_SHORT).show()
            return
        }

        // 从当前显示的数据中找到该英雄
        val heroStatus = cardPoolAdapter.getData().find { it.heroName == heroName }
            ?: CardPoolCalculator.HeroCardStatus(
                heroId = "",
                heroName = heroName,
                cost = currentCost,
                totalInPool = CardPoolCalculator.CARD_POOL_COUNT[currentCost] ?: 0,
                takenCount = currentOwned + takenByOthers,
                remaining = (CardPoolCalculator.CARD_POOL_COUNT[currentCost] ?: 0) - currentOwned - takenByOthers
            )

        // 计算可行性
        val feasibility = CardPoolCalculator.calculateThreeStarFeasibility(
            cost = heroStatus.cost,
            currentOwned = currentOwned,
            takenByOthers = takenByOthers
        )

        // 显示结果
        displayFeasibilityResult(heroName, feasibility)
    }

    /**
     * 显示可行性结果
     */
    private fun displayFeasibilityResult(heroName: String, result: CardPoolCalculator.ThreeStarFeasibility) {
        val color = when (result.riskLevel) {
            CardPoolCalculator.RiskLevel.LOW_RISK -> "#4CAF50"
            CardPoolCalculator.RiskLevel.MEDIUM_RISK -> "#FFC107"
            CardPoolCalculator.RiskLevel.HIGH_RISK -> "#FF9800"
            CardPoolCalculator.RiskLevel.EXTREME_RISK -> "#F44336"
            CardPoolCalculator.RiskLevel.IMPOSSIBLE -> "#9E9E9E"
        }

        tvResult.text = buildString {
            appendLine("【${heroName}】追三星分析")
            appendLine()
            appendLine("当前拥有: ${9 - result.neededCards}/9")
            appendLine("卡池剩余: ${result.remainingCards}张")
            appendLine("还需: ${result.neededCards}张")
            appendLine()
            appendLine("成功率估计: ${(result.probability * 100).toInt()}%")
            appendLine()
            append("建议: ${result.suggestion}")
        }

        tvResult.setTextColor(android.graphics.Color.parseColor(color))
        tvResult.visibility = View.VISIBLE
    }

    /**
     * 显示重置确认对话框
     */
    private fun showResetConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("重置卡池数据")
            .setMessage("确定要重置所有卡池数据吗？这将清空所有已记录的卡牌数量。")
            .setPositiveButton("重置") { _, _ ->
                mockCardPoolData.clear()
                initMockData()
                loadCardPoolData(currentCost)
                tvResult.visibility = View.GONE
                Toast.makeText(this, "数据已重置", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 显示编辑卡牌数量的对话框
     */
    fun showEditCardCountDialog(heroStatus: CardPoolCalculator.HeroCardStatus) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_card_count, null)

        val tvHeroName = dialogView.findViewById<TextView>(R.id.tv_dialog_hero_name)
        val etTakenCount = dialogView.findViewById<EditText>(R.id.et_taken_count)

        tvHeroName.text = heroStatus.heroName
        etTakenCount.setText(heroStatus.takenCount.toString())

        AlertDialog.Builder(this)
            .setTitle("编辑卡牌数量")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val newCount = etTakenCount.text.toString().toIntOrNull() ?: 0
                mockCardPoolData[heroStatus.heroName] = newCount.coerceIn(0, heroStatus.totalInPool)
                loadCardPoolData(currentCost)
            }
            .setNegativeButton("取消", null)
            .show()
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
