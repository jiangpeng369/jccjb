package com.tft.helper.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tft.helper.R
import com.tft.helper.model.Comp
import com.tft.helper.network.CompDataSource
import com.tft.helper.ui.adapter.CompAdapter
import com.tft.helper.utils.LocalDataManager
import com.tft.helper.utils.ScreenAdapter
import kotlinx.coroutines.launch

/**
 * 主Activity - 阵容库
 * 
 * 展示全网热门阵容，支持：
 * 1. 按热度/胜率/前四率排序
 * 2. 搜索阵容
 * 3. 收藏阵容
 * 4. 查看阵容详情
 * 5. 自适应屏幕列数
 */
class MainActivity : AppCompatActivity() {

    // 视图组件
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var spinnerSort: Spinner
    private lateinit var fabRecord: FloatingActionButton
    private lateinit var fabProbability: FloatingActionButton
    private lateinit var fabCardPool: FloatingActionButton

    // 适配器
    private lateinit var compAdapter: CompAdapter

    // 数据源
    private val dataSource = CompDataSource.getInstance()
    private lateinit var localDataManager: LocalDataManager

    // 当前排序方式
    private var currentSortType = CompDataSource.SortType.POPULARITY

    // 当前阵容列表
    private var currentComps: List<Comp> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化屏幕适配
        ScreenAdapter.init(this)

        // 初始化本地数据管理器
        localDataManager = LocalDataManager.getInstance(this)

        // 初始化视图
        initViews()

        // 设置适配器
        setupAdapter()

        // 设置排序下拉框
        setupSortSpinner()

        // 加载数据
        loadComps()
    }

    override fun onResume() {
        super.onResume()
        // 刷新收藏状态
        updateFavoriteStatus()
    }

    /**
     * 初始化视图组件
     */
    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_comps)
        progressBar = findViewById(R.id.progress_bar)
        spinnerSort = findViewById(R.id.spinner_sort)
        fabRecord = findViewById(R.id.fab_record)
        fabProbability = findViewById(R.id.fab_probability)
        fabCardPool = findViewById(R.id.fab_card_pool)

        // 对战记录按钮点击
        fabRecord.setOnClickListener {
            startActivity(Intent(this, MatchRecordActivity::class.java))
        }

        // 概率计算按钮点击
        fabProbability.setOnClickListener {
            startActivity(Intent(this, ProbabilityCalcActivity::class.java))
        }

        // 牌库计算按钮点击
        fabCardPool.setOnClickListener {
            startActivity(Intent(this, CardPoolCalcActivity::class.java))
        }
    }

    /**
     * 设置RecyclerView适配器
     */
    private fun setupAdapter() {
        // 根据屏幕宽度计算列数
        val spanCount = calculateSpanCount()
        val layoutManager = GridLayoutManager(this, spanCount)
        recyclerView.layoutManager = layoutManager

        // 创建适配器
        compAdapter = CompAdapter(
            onItemClick = { comp ->
                // 跳转到阵容详情
                val intent = Intent(this, CompDetailActivity::class.java).apply {
                    putExtra("comp_id", comp.id)
                }
                startActivity(intent)
            },
            onFavoriteClick = { comp, isFavorite ->
                // 处理收藏/取消收藏
                if (isFavorite) {
                    localDataManager.addFavoriteComp(comp.id)
                    Toast.makeText(this, "已收藏 ${comp.name}", Toast.LENGTH_SHORT).show()
                } else {
                    localDataManager.removeFavoriteComp(comp.id)
                    Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show()
                }
            }
        )

        recyclerView.adapter = compAdapter
    }

    /**
     * 计算网格列数
     * 根据屏幕宽度动态计算
     */
    private fun calculateSpanCount(): Int {
        val screenWidthDp = ScreenAdapter.getScreenWidthDp()
        return when {
            screenWidthDp < 360 -> 1  // 小屏单列
            screenWidthDp < 600 -> 2  // 标准屏双列
            screenWidthDp < 900 -> 3  // 大屏三列
            else -> 4  // 平板四列
        }
    }

    /**
     * 设置排序下拉框
     */
    private fun setupSortSpinner() {
        val sortOptions = arrayOf("按热度", "按胜率", "按前四率", "按评级")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSort.adapter = adapter

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSortType = when (position) {
                    0 -> CompDataSource.SortType.POPULARITY
                    1 -> CompDataSource.SortType.WIN_RATE
                    2 -> CompDataSource.SortType.TOP4_RATE
                    3 -> CompDataSource.SortType.TIER
                    else -> CompDataSource.SortType.POPULARITY
                }
                loadComps()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * 加载阵容数据
     */
    private fun loadComps() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        lifecycleScope.launch {
            val result = dataSource.getComps(sortBy = currentSortType)
            
            result.onSuccess { comps ->
                currentComps = comps
                compAdapter.updateData(comps)
                updateFavoriteStatus()
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }.onFailure { error ->
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@MainActivity, 
                    "加载失败: ${error.message}", 
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * 更新收藏状态
     */
    private fun updateFavoriteStatus() {
        val favorites = localDataManager.getFavoriteComps()
        compAdapter.setFavorites(favorites)
    }

    /**
     * 搜索阵容
     */
    private fun searchComps(keyword: String) {
        if (keyword.isEmpty()) {
            compAdapter.updateData(currentComps)
            return
        }

        lifecycleScope.launch {
            val result = dataSource.searchComps(keyword)
            result.onSuccess { comps ->
                compAdapter.updateData(comps)
            }.onFailure {
                Toast.makeText(this@MainActivity, "搜索失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==================== 菜单 ====================

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // 设置搜索功能
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchComps(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchComps(it) }
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                // 刷新数据
                loadComps()
                true
            }
            R.id.action_favorites -> {
                // 显示收藏的阵容
                showFavoriteComps()
                true
            }
            R.id.action_statistics -> {
                // 查看统计数据
                startActivity(Intent(this, MatchRecordActivity::class.java))
                true
            }
            R.id.action_card_pool -> {
                // 打开牌库计算器
                startActivity(Intent(this, CardPoolCalcActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * 只显示收藏的阵容
     */
    private fun showFavoriteComps() {
        val favorites = localDataManager.getFavoriteComps()
        val favoriteComps = currentComps.filter { favorites.contains(it.id) }
        
        if (favoriteComps.isEmpty()) {
            Toast.makeText(this, "暂无收藏的阵容", Toast.LENGTH_SHORT).show()
        } else {
            compAdapter.updateData(favoriteComps)
        }
    }
}
