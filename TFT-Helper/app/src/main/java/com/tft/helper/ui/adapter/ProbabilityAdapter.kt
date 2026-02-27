package com.tft.helper.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tft.helper.R
import com.tft.helper.calc.ProbabilityCalculator

/**
 * 概率列表适配器
 * 
 * 展示各玩家相遇概率，按概率从高到低排序
 */
class ProbabilityAdapter(
    private var probabilities: List<ProbabilityCalculator.PlayerMeetProbability> = emptyList()
) : RecyclerView.Adapter<ProbabilityAdapter.ProbabilityViewHolder>() {

    /**
     * 更新数据并排序
     */
    fun updateData(newProbabilities: List<ProbabilityCalculator.PlayerMeetProbability>) {
        // 按概率从高到低排序
        probabilities = newProbabilities.sortedByDescending { it.probability }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProbabilityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_probability, parent, false)
        return ProbabilityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProbabilityViewHolder, position: Int) {
        holder.bind(probabilities[position], position + 1)
    }

    override fun getItemCount(): Int = probabilities.size

    inner class ProbabilityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val tvRank: TextView = itemView.findViewById(R.id.tv_probability_rank)
        private val tvPlayerName: TextView = itemView.findViewById(R.id.tv_player_name)
        private val tvProbability: TextView = itemView.findViewById(R.id.tv_probability_value)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_probability)
        private val tvHealth: TextView = itemView.findViewById(R.id.tv_player_health)
        private val tvComp: TextView = itemView.findViewById(R.id.tv_player_comp)
        private val tvReason: TextView = itemView.findViewById(R.id.tv_probability_reason)

        fun bind(item: ProbabilityCalculator.PlayerMeetProbability, rank: Int) {
            val player = item.player
            
            // 设置排名
            tvRank.text = "#${rank}"
            
            // 设置玩家名称
            tvPlayerName.text = player.name
            
            // 设置概率值
            val probabilityPercent = (item.probability * 100).toInt()
            tvProbability.text = "${probabilityPercent}%"
            progressBar.progress = probabilityPercent
            
            // 设置血量
            tvHealth.text = "${player.health} HP"
            // 血量低于30显示警告色
            if (player.health < 30) {
                tvHealth.setTextColor(parseColor("#F44336"))
            } else {
                tvHealth.setTextColor(parseColor("#4CAF50"))
            }
            
            // 设置阵容信息
            if (!player.compName.isNullOrEmpty()) {
                tvComp.visibility = View.VISIBLE
                tvComp.text = player.compName
            } else {
                tvComp.visibility = View.GONE
            }
            
            // 设置概率说明
            tvReason.text = item.reason
            
            // 第一名高亮显示
            if (rank == 1) {
                itemView.setBackgroundColor(parseColor("#FFF3E0"))  // 淡橙色背景
            } else {
                itemView.setBackgroundColor(parseColor("#FFFFFF"))
            }
        }

        private fun parseColor(colorStr: String): Int {
            return try {
                android.graphics.Color.parseColor(colorStr)
            } catch (e: Exception) {
                android.graphics.Color.GRAY
            }
        }
    }
}
