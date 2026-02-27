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
 * 牌库列表适配器
 * 
 * 展示各英雄的卡牌剩余情况
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
            // 英雄名称
            tvHeroName.text = heroStatus.heroName

            // 剩余数量
            tvRemaining.text = "${heroStatus.remaining}"
            tvRemaining.setTextColor(android.graphics.Color.parseColor(heroStatus.getRemainingColor()))

            // 已拿数量
            tvTaken.text = "已拿: ${heroStatus.takenCount}"

            // 进度条
            val percent = heroStatus.getRemainingPercent()
            progressBar.progress = percent

            // 状态标签
            tvStatus.text = when {
                heroStatus.remaining == 0 -> "已空"
                heroStatus.remaining <= 3 -> "紧张"
                heroStatus.remaining <= 6 -> "较少"
                heroStatus.remaining <= 10 -> "充足"
                else -> "很多"
            }

            tvStatus.setTextColor(android.graphics.Color.parseColor(heroStatus.getRemainingColor()))

            // 点击事件
            itemView.setOnClickListener { onItemClick(heroStatus) }

            // 长按编辑
            itemView.setOnLongClickListener {
                // 通知Activity显示编辑对话框
                (itemView.context as? com.tft.helper.ui.CardPoolCalcActivity)?
                    ?.showEditCardCountDialog(heroStatus)
                true
            }

            // 根据剩余数量设置背景色
            val backgroundColor = when {
                heroStatus.remaining == 0 -> "#FFEBEE"  // 浅红
                heroStatus.remaining <= 3 -> "#FFF3E0"  // 浅橙
                heroStatus.remaining <= 6 -> "#FFFDE7"  // 浅黄
                else -> "#FFFFFF"  // 白色
            }
            cardView.setCardBackgroundColor(android.graphics.Color.parseColor(backgroundColor))
        }
    }
}
