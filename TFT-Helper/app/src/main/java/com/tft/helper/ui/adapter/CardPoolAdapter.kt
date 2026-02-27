package com.tft.helper.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.tft.helper.R
import com.tft.helper.calc.CardPoolCalculator

/**
 * 卡池状态列表适配器
 * 
 * 显示英雄剩余数量和已被拿取数量
 */
class CardPoolAdapter(
    private var heroStatuses: List<CardPoolCalculator.HeroCardStatus> = emptyList(),
    private val onItemClick: (CardPoolCalculator.HeroCardStatus) -> Unit
) : RecyclerView.Adapter<CardPoolAdapter.CardPoolViewHolder>() {

    /**
     * 更新数据
     */
    fun updateData(newData: List<CardPoolCalculator.HeroCardStatus>) {
        heroStatuses = newData
        notifyDataSetChanged()
    }

    /**
     * 获取当前数据
     */
    fun getData(): List<CardPoolCalculator.HeroCardStatus> = heroStatuses

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardPoolViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_pool, parent, false)
        return CardPoolViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardPoolViewHolder, position: Int) {
        holder.bind(heroStatuses[position])
    }

    override fun getItemCount(): Int = heroStatuses.size

    inner class CardPoolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cardView: CardView = itemView.findViewById(R.id.card_hero_pool)
        private val tvHeroName: TextView = itemView.findViewById(R.id.tv_hero_name_pool)
        private val tvRemaining: TextView = itemView.findViewById(R.id.tv_remaining)
        private val tvTaken: TextView = itemView.findViewById(R.id.tv_taken)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_remaining)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_pool_status)

        fun bind(heroStatus: CardPoolCalculator.HeroCardStatus) {
            // 设置英雄名
            tvHeroName.text = heroStatus.heroName

            // 设置剩余数量
            tvRemaining.text = "${heroStatus.remaining}"
            tvRemaining.setTextColor(android.graphics.Color.parseColor(heroStatus.getRemainingColor()))

            // 被拿取数量
            tvTaken.text = "被拿取：${heroStatus.takenCount}"

            // 进度条
            val percent = heroStatus.getRemainingPercent()
            progressBar.progress = percent

            // 设置状态文字
            tvStatus.text = when {
                heroStatus.remaining == 0 -> "已清空"
                heroStatus.remaining <= 3 -> "极度稀缺"
                heroStatus.remaining <= 6 -> "比较稀缺"
                heroStatus.remaining <= 10 -> "库存充足"
                else -> "大量剩余"
            }

            tvStatus.setTextColor(android.graphics.Color.parseColor(heroStatus.getRemainingColor()))

            // 设置点击监听
            itemView.setOnClickListener { onItemClick(heroStatus) }

            // 设置长按监听
            itemView.setOnLongClickListener {
                // 调用Activity方法显示编辑对话框
                (itemView.context as? com.tft.helper.ui.CardPoolCalcActivity)?
                    ?.showEditCardCountDialog(heroStatus)
                true
            }

            // 根据剩余数量设置背景颜色
            val backgroundColor = when {
                heroStatus.remaining == 0 -> "#FFEBEE"  // 淡红色
                heroStatus.remaining <= 3 -> "#FFF3E0"  // 淡橙色
                heroStatus.remaining <= 6 -> "#FFFDE7"  // 淡黄色
                else -> "#FFFFFF"  // 纯白色
            }
            cardView.setCardBackgroundColor(android.graphics.Color.parseColor(backgroundColor))
        }
    }
}
