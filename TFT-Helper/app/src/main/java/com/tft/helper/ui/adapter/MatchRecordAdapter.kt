package com.tft.helper.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.tft.helper.R
import com.tft.helper.model.MatchRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 对战记录列表适配器
 * 
 * 展示玩家手动记录的对战数据
 */
class MatchRecordAdapter(
    private var records: List<MatchRecord> = emptyList(),
    private val onItemClick: (MatchRecord) -> Unit,
    private val onItemLongClick: (MatchRecord) -> Boolean
) : RecyclerView.Adapter<MatchRecordAdapter.RecordViewHolder>() {

    private val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    /**
     * 更新数据
     */
    fun updateData(newRecords: List<MatchRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val cardView: CardView = itemView.findViewById(R.id.card_record)
        private val tvRank: TextView = itemView.findViewById(R.id.tv_rank)
        private val tvCompName: TextView = itemView.findViewById(R.id.tv_comp_name)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val tvHealth: TextView = itemView.findViewById(R.id.tv_health)
        private val tvLPChange: TextView = itemView.findViewById(R.id.tv_lp_change)
        private val tvTraits: TextView = itemView.findViewById(R.id.tv_traits)

        fun bind(record: MatchRecord) {
            // 设置排名
            tvRank.text = "#${record.finalRank}"
            tvRank.setTextColor(parseColor(record.getRankColor()))
            
            // 设置阵容名称
            tvCompName.text = record.compUsed.compName
            
            // 设置时间
            tvTime.text = dateFormat.format(Date(record.matchTime))
            
            // 设置血量
            if (record.healthRemaining > 0) {
                tvHealth.visibility = View.VISIBLE
                tvHealth.text = "${record.healthRemaining}血"
            } else {
                tvHealth.visibility = View.GONE
            }
            
            // 设置LP变化
            if (record.lobbyType == MatchRecord.LobbyType.RANKED) {
                val lpChange = record.estimateLPChange()
                tvLPChange.visibility = View.VISIBLE
                tvLPChange.text = if (lpChange >= 0) "+${lpChange}LP" else "${lpChange}LP"
                tvLPChange.setTextColor(
                    if (lpChange >= 0) parseColor("#4CAF50") else parseColor("#F44336")
                )
            } else {
                tvLPChange.visibility = View.GONE
            }
            
            // 设置核心羁绊
            if (record.compUsed.coreTraits.isNotEmpty()) {
                tvTraits.visibility = View.VISIBLE
                tvTraits.text = record.compUsed.coreTraits.joinToString(" ")
            } else {
                tvTraits.visibility = View.GONE
            }
            
            // 点击事件
            itemView.setOnClickListener { onItemClick(record) }
            itemView.setOnLongClickListener { onItemLongClick(record) }
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
