package com.tft.helper.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tft.helper.R
import com.tft.helper.calc.ProbabilityCalculator
import com.tft.helper.ui.adapter.ProbabilityAdapter
import java.util.UUID

/**
 * 概率计算Activity
 * 
 * 提供各种概率计算功能：
 * 1. 碰对手概率计算 - 输入存活玩家信息，按概率排序
 * 2. D牌概率计算
 * 3. 装备概率计算
 */
class ProbabilityCalcActivity : AppCompatActivity() {

    // 视图组件 - 碰对手计算
    private lateinit var etTotalPlayers: EditText
    private lateinit var etMyHealth: EditText
    private lateinit var recyclerProbabilities: RecyclerView
    private lateinit var btnCalculateMeet: Button
    private lateinit var btnAddPlayer: Button
    private lateinit var tvHighestProbability: TextView

    // 适配器
    private lateinit var probabilityAdapter: ProbabilityAdapter

    // 玩家列表
    private val players = mutableListOf<ProbabilityCalculator.PlayerInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_probability_calc)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "概率计算器"

        // 初始化视图
        initViews()

        // 设置适配器
        setupAdapter()

        // 设置点击事件
        setupClickListeners()
    }

    /**
     * 初始化视图组件
     */
    private fun initViews() {
        etTotalPlayers = findViewById(R.id.et_total_players)
        etMyHealth = findViewById(R.id.et_my_health)
        recyclerProbabilities = findViewById(R.id.recycler_probabilities)
        btnCalculateMeet = findViewById(R.id.btn_calculate_meet)
        btnAddPlayer = findViewById(R.id.btn_add_player)
        tvHighestProbability = findViewById(R.id.tv_highest_probability)
    }

    /**
     * 设置RecyclerView适配器
     */
    private fun setupAdapter() {
        recyclerProbabilities.layoutManager = LinearLayoutManager(this)
        probabilityAdapter = ProbabilityAdapter()
        recyclerProbabilities.adapter = probabilityAdapter
    }

    /**
     * 设置点击事件
     */
    private fun setupClickListeners() {
        // 添加玩家按钮
        btnAddPlayer.setOnClickListener {
            showAddPlayerDialog()
        }

        // 计算概率按钮
        btnCalculateMeet.setOnClickListener {
            calculateMeetProbabilities()
        }
    }

    /**
     * 显示添加玩家对话框
     */
    private fun showAddPlayerDialog() {
        // 创建对话框
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("添加玩家")
            .setView(R.layout.dialog_add_player)
            .setPositiveButton("添加") { _, _ ->
                // 这里简化处理，实际应该获取对话框中的输入
                addMockPlayer()
            }
            .setNegativeButton("取消", null)
            .create()
        dialog.show()
    }

    /**
     * 添加模拟玩家数据
     */
    private fun addMockPlayer() {
        val mockNames = listOf("玩家A", "玩家B", "玩家C", "玩家D", "玩家E", "玩家F", "玩家G")
        val mockComps = listOf("剪纸仙灵", "灵魂莲华", "天将", "狙神护卫", "决斗大师", null, null)

        val player = ProbabilityCalculator.PlayerInfo(
            id = UUID.randomUUID().toString(),
            name = mockNames[players.size % mockNames.size],
            health = (20..100).random(),
            isAlive = true,
            recentMatchesAgainstMe = (0..3).random(),
            compName = mockComps[players.size % mockComps.size]
        )

        players.add(player)
        Toast.makeText(this, "已添加 ${player.name}", Toast.LENGTH_SHORT).show()
    }

    /**
     * 计算碰对手概率
     */
    private fun calculateMeetProbabilities() {
        if (players.isEmpty()) {
            Toast.makeText(this, "请先添加玩家", Toast.LENGTH_SHORT).show()
            return
        }

        val totalPlayers = players.size + 1  // +1 包括自己

        // 计算概率
        val myId = "my_id"
        val probabilities = ProbabilityCalculator.calculateAllMeetProbabilities(
            players = players,
            myId = myId,
            lastOpponentId = null
        )

        // 更新适配器
        probabilityAdapter.updateData(probabilities)

        // 显示最高概率
        if (probabilities.isNotEmpty()) {
            val highest = probabilities.first()
            tvHighestProbability.text = buildString {
                append("⚠️ 警告：")
                append(highest.player.name)
                append(" 是最可能遇到的对手")
                append(" (${(highest.probability * 100).toInt()}%)")
            }
            tvHighestProbability.visibility = View.VISIBLE
        }
    }

    // ==================== D牌概率计算 ====================

    /**
     * 计算追三星概率
     */
    private fun calculateThreeStarProbability() {
        // 获取输入
        val level = findViewById<EditText>(R.id.et_level)?.text?.toString()?.toIntOrNull() ?: 8
        val heroCost = findViewById<Spinner>(R.id.spinner_hero_cost)?.selectedItemPosition?.plus(1) ?: 3
        val currentCount = findViewById<EditText>(R.id.et_current_count)?.text?.toString()?.toIntOrNull() ?: 0
        val gold = findViewById<EditText>(R.id.et_gold)?.text?.toString()?.toIntOrNull() ?: 50

        // 计算概率
        val result = ProbabilityCalculator.calculateThreeStarProbability(
            level = level,
            heroCost = heroCost,
            currentCount = currentCount,
            gold = gold
        )

        // 显示结果
        val tvResult = findViewById<TextView>(R.id.tv_three_star_result)
        tvResult?.text = buildString {
            append("追到三星概率: ${(result.probability * 100).toInt()}%\n")
            append("预计需要金币: ${result.estimatedGold}\n")
            append("建议: ${result.suggestion}")
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
