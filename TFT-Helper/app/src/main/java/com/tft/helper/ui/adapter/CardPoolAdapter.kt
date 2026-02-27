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
 * 鐗屽簱鍒楄〃閫傞厤鍣?
 * 
 * 灞曠ず鍚勮嫳闆勭殑鍗＄墝鍓╀綑鎯呭喌
 */
class CardPoolAdapter(
    private var heroStatuses: List<CardPoolCalculator.HeroCardStatus> = emptyList(),
    private val onItemClick: (CardPoolCalculator.HeroCardStatus) -> Unit
) : RecyclerView.Adapter<CardPoolAdapter.CardPoolViewHolder>() {

    /**
     * 鏇存柊鏁版嵁
     */
    fun updateData(newData: List<CardPoolCalculator.HeroCardStatus>) {
        heroStatuses = newData
        notifyDataSetChanged()
    }

    /**
     * 鑾峰彇褰撳墠鏁版嵁
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
            // 鑻遍泟鍚嶇О
            tvHeroName.text = heroStatus.heroName

            // 鍓╀綑鏁伴噺
            tvRemaining.text = "${heroStatus.remaining}"
            tvRemaining.setTextColor(android.graphics.Color.parseColor(heroStatus.getRemainingColor()))

            // 宸叉嬁鏁伴噺
            tvTaken.text = "宸叉嬁: ${heroStatus.takenCount}"

            // 杩涘害鏉?
            val percent = heroStatus.getRemainingPercent()
            progressBar.progress = percent

            // 鐘舵€佹爣绛?
            tvStatus.text = when {
                heroStatus.remaining == 0 -> "宸茬┖"
                heroStatus.remaining <= 3 -> "绱у紶"
                heroStatus.remaining <= 6 -> "杈冨皯"
                heroStatus.remaining <= 10 -> "鍏呰冻"
                else -> "寰堝"
            }

            tvStatus.setTextColor(android.graphics.Color.parseColor(heroStatus.getRemainingColor()))

            // 鐐瑰嚮浜嬩欢
            itemView.setOnClickListener { onItemClick(heroStatus) }

            // 闀挎寜缂栬緫
            itemView.setOnLongClickListener {
                // 閫氱煡Activity鏄剧ず缂栬緫瀵硅瘽妗?
                (itemView.context as? com.tft.helper.ui.CardPoolCalcActivity)?
                    ?.showEditCardCountDialog(heroStatus)
                true
            }

            // 鏍规嵁鍓╀綑鏁伴噺璁剧疆鑳屾櫙鑹?
            val backgroundColor = when {
                heroStatus.remaining == 0 -> "#FFEBEE"  // 娴呯孩
                heroStatus.remaining <= 3 -> "#FFF3E0"  // 娴呮
                heroStatus.remaining <= 6 -> "#FFFDE7"  // 娴呴粍
                else -> "#FFFFFF"  // 鐧借壊
            }
            cardView.setCardBackgroundColor(android.graphics.Color.parseColor(backgroundColor))
        }
    }
}
