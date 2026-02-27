package com.tft.helper.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tft.helper.R
import com.tft.helper.model.MatchRecord
import com.tft.helper.ui.adapter.MatchRecordAdapter
import com.tft.helper.utils.LocalDataManager
import java.util.Calendar
import java.util.UUID

/**
 * 对战记录Activity
 * 
 * 手动记录对战数据，包括：
 * - 使用阵容
 * - 最终排名
 * - 剩余血量
 * - 对局类型（排位/匹配）
 * 
 * 提供数据统计功能
 */
class MatchRecordActivity : AppCompatActivity() {

    // 视图组件
    private lateinit var recyclerRecords: RecyclerView
    private lateinit var fabAddRecord: FloatingActionButton
    private lateinit var tvEmpty: View
    private lateinit var tvStats: View

    // 适配器
    private lateinit var recordAdapter: MatchRecordAdapter

    // 数据管理器
    private lateinit var localDataManager: LocalDataManager

    // 当前记录列表
    private var currentRecords: List<MatchRecord> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_record)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "对战记录"

        // 初始化数据管理器
        localDataManager = LocalDataManager.getInstance(this)

        // 初始化视图
        initViews()

        // 设置适配器
        setupAdapter()

        // 加载数据
        loadRecords()
    }

    override fun onResume() {
        super.onResume()
        loadRecords()
    }

    /**
     * 初始化视图组件
     */
    private fun initViews() {
        recyclerRecords = findViewById(R.id.recycler_records)
        fabAddRecord = findViewById(R.id.fab_add_record)
        tvEmpty = findViewById(R.id.tv_empty)
        tvStats = findViewById(R.id.tv_stats)

        fabAddRecord.setOnClickListener {
            showAddRecordDialog()
        }
    }

    /**
     * 设置RecyclerView适配器
     */
    private fun setupAdapter() {
        recyclerRecords.layoutManager = LinearLayoutManager(this)
        recordAdapter = MatchRecordAdapter(
            onItemClick = { record ->
                // 查看详情
                showRecordDetailDialog(record)
            },
            onItemLongClick = { record ->
                // 长按删除
                showDeleteConfirmDialog(record)
                true
            }
        )
        recyclerRecords.adapter = recordAdapter
    }

    /**
     * 加载对战记录
     */
    private fun loadRecords() {
        currentRecords = localDataManager.getAllMatchRecords()
        recordAdapter.updateData(currentRecords)

        // 显示/隐藏空状态
        if (currentRecords.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerRecords.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerRecords.visibility = View.VISIBLE
        }

        // 更新统计信息
        updateStatistics()
    }

    /**
     * 更新统计信息
     */
    private fun updateStatistics() {
        if (currentRecords.isEmpty()) return

        val totalGames = currentRecords.size
        val winCount = currentRecords.count { it.isFirstPlace() }
        val top4Count = currentRecords.count { it.isTop4() }
        val avgRank = currentRecords.map { it.finalRank }.average()

        val statsText = findViewById<android.widget.TextView>(R.id.tv_stats_text)
        statsText?.text = buildString {
            append("总场次: ${totalGames}  |  ")
            append("吃鸡: ${winCount} (${winCount * 100 / totalGames}%)  |  ")
            append("前四: ${top4Count} (${top4Count * 100 / totalGames}%)  |  ")
            append("平均排名: ${String.format("%.1f", avgRank)}")
        }
    }

    /**
     * 显示添加记录对话框
     */
    private fun showAddRecordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_record, null)

        // 获取视图组件
        val spinnerComp = dialogView.findViewById<Spinner>(R.id.spinner_comp)
        val spinnerRank = dialogView.findViewById<Spinner>(R.id.spinner_rank)
        val etHealth = dialogView.findViewById<EditText>(R.id.et_health)
        val etRounds = dialogView.findViewById<EditText>(R.id.et_rounds)
        val spinnerLobbyType = dialogView.findViewById<Spinner>(R.id.spinner_lobby_type)
        val etNotes = dialogView.findViewById<EditText>(R.id.et_notes)
        val btnSelectTime = dialogView.findViewById<Button>(R.id.btn_select_time)
        val tvSelectedTime = dialogView.findViewById<android.widget.TextView>(R.id.tv_selected_time)

        // 阵容选项
        val comps = arrayOf(
            "剪纸仙灵95", "灵魂莲华阿狸", "天将螳螂", 
            "夜幽拉露恩", "狙神护卫", "决斗大师熊炮", "其他"
        )
        spinnerComp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, comps)

        // 排名选项
        val ranks = (1..8).map { "第${it}名" }.toTypedArray()
        spinnerRank.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ranks)

        // 对局类型
        val lobbyTypes = arrayOf("排位赛", "匹配赛", "狂暴模式", "双人模式")
        spinnerLobbyType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lobbyTypes)

        // 时间选择
        val calendar = Calendar.getInstance()
        btnSelectTime.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                TimePickerDialog(this, { _, hour, minute ->
                    calendar.set(year, month, day, hour, minute)
                    tvSelectedTime.text = "${month + 1}-${day} ${hour}:${String.format("%02d", minute)}"
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // 创建对话框
        AlertDialog.Builder(this)
            .setTitle("添加对战记录")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                // 创建记录
                val record = MatchRecord(
                    id = UUID.randomUUID().toString(),
                    matchTime = calendar.timeInMillis,
                    compUsed = MatchRecord.CompInfo(
                        compId = null,
                        compName = spinnerComp.selectedItem.toString(),
                        coreTraits = emptyList()
                    ),
                    finalRank = spinnerRank.selectedItemPosition + 1,
                    healthRemaining = etHealth.text.toString().toIntOrNull() ?: 0,
                    roundsSurvived = etRounds.text.toString().toIntOrNull() ?: 0,
                    notes = etNotes.text.toString(),
                    patchVersion = "S11",  // 当前版本
                    lobbyType = when (spinnerLobbyType.selectedItemPosition) {
                        0 -> MatchRecord.LobbyType.RANKED
                        1 -> MatchRecord.LobbyType.NORMAL
                        2 -> MatchRecord.LobbyType.HYPER_ROLL
                        else -> MatchRecord.LobbyType.DOUBLE_UP
                    }
                )

                // 保存记录
                if (localDataManager.saveMatchRecord(record)) {
                    Toast.makeText(this, "记录已保存", Toast.LENGTH_SHORT).show()
                    loadRecords()
                } else {
                    Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 显示记录详情对话框
     */
    private fun showRecordDetailDialog(record: MatchRecord) {
        val message = buildString {
            append("阵容: ${record.compUsed.compName}\n")
            append("排名: 第${record.finalRank}名\n")
            append("血量: ${record.healthRemaining}\n")
            append("存活回合: ${record.roundsSurvived}\n")
            append("对局类型: ${record.lobbyType.name}\n")
            if (record.notes.isNotEmpty()) {
                append("备注: ${record.notes}\n")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("对战详情")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }

    /**
     * 显示删除确认对话框
     */
    private fun showDeleteConfirmDialog(record: MatchRecord) {
        AlertDialog.Builder(this)
            .setTitle("删除记录")
            .setMessage("确定要删除这条记录吗？")
            .setPositiveButton("删除") { _, _ ->
                if (localDataManager.deleteMatchRecord(record.id)) {
                    Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show()
                    loadRecords()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_match_record, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_clear_all -> {
                // 清空所有记录
                AlertDialog.Builder(this)
                    .setTitle("清空记录")
                    .setMessage("确定要清空所有对战记录吗？此操作不可恢复。")
                    .setPositiveButton("清空") { _, _ ->
                        if (localDataManager.clearAllMatchRecords()) {
                            Toast.makeText(this, "已清空所有记录", Toast.LENGTH_SHORT).show()
                            loadRecords()
                        }
                    }
                    .setNegativeButton("取消", null)
                    .show()
                true
            }
            R.id.action_view_stats -> {
                // 查看阵容统计
                showCompStatistics()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * 显示阵容统计
     */
    private fun showCompStatistics() {
        val statistics = localDataManager.getAllCompStatistics()
        
        if (statistics.isEmpty()) {
            Toast.makeText(this, "暂无数据", Toast.LENGTH_SHORT).show()
            return
        }

        val message = statistics.joinToString("\n\n") { stat ->
            buildString {
                append("【${stat.compName}】\n")
                append("场次: ${stat.totalGames}  ")
                append("吃鸡: ${stat.firstPlaceCount}  ")
                append("前四: ${stat.top4Count}\n")
                append("前四率: ${String.format("%.1f", stat.top4Rate)}%  ")
                append("平均排名: ${String.format("%.1f", stat.averageRank)}")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("阵容统计数据")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }
}
