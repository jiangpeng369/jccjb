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
 * 閻楀苯绨遍崚妤勩€冮柅鍌炲帳閸?
 * 
 * 鐏炴洜銇氶崥鍕闂嗗嫮娈戦崡锛勫閸撯晙缍戦幆鍛枌
 */
class CardPoolAdapter(
    private var heroStatuses: List<CardPoolCalculator.HeroCardStatus> = emptyList(),
    private val onItemClick: (CardPoolCalculator.HeroCardStatus) -> Unit
) : RecyclerView.Adapter<CardPoolAdapter.CardPoolViewHolder>() {

    /**
     * 閺囧瓨鏌婇弫鐗堝祦
     */
    fun updateData(newData: List<CardPoolCalculator.HeroCardStatus>) {
        heroStatuses = newData
        notifyDataSetChanged()
    }

    /**
     * 閼惧嘲褰囪ぐ鎾冲閺佺増宓?
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
            // 閼婚亶娉熼崥宥囆?
            tvHeroName.text = heroStatus.heroName

            // 閸撯晙缍戦弫浼村櫤
            tvRemaining.text = "${heroStatus.remaining}"
            tvRemaining.setTextColor(android.graphics.Color.parseColor(heroStatus.getRemainingColor()))

            // 瀹稿弶瀣侀弫浼村櫤
            tvTaken.text = "瀹稿弶瀣? ${heroStatus.takenCount}"

            // 鏉╂稑瀹抽弶?
            val percent = heroStatus.getRemainingPercent()
            progressBar.progress = percent

            // 閻樿埖鈧焦鐖ｇ粵?
            tvStatus.text = when {
                heroStatus.remaining == 0 -> "瀹歌尙鈹?
                heroStatus.remaining <= 3 -> "缁毖冪炊"
                heroStatus.remaining <= 6 -> "鏉堝啫鐨?
                heroStatus.remaining <= 10 -> "閸忓懓鍐?
                else -> "瀵板牆顦?
            }

            tvStatus.setTextColor(android.graphics.Color.parseColor(heroStatus.getRemainingColor()))

            // 閻愮懓鍤禍瀣╂
            itemView.setOnClickListener { onItemClick(heroStatus) }

            // 闂€鎸庡瘻缂傛牞绶?
            itemView.setOnLongClickListener {
                // 闁氨鐓ctivity閺勫墽銇氱紓鏍帆鐎电鐦藉?
                (itemView.context as? com.tft.helper.ui.CardPoolCalcActivity)?
                    ?.showEditCardCountDialog(heroStatus)
                true
            }

            // 閺嶈宓侀崜鈺€缍戦弫浼村櫤鐠佸墽鐤嗛懗灞炬珯閼?
            val backgroundColor = when {
                heroStatus.remaining == 0 -> "#FFEBEE"  // 濞村懐瀛?
                heroStatus.remaining <= 3 -> "#FFF3E0"  // 濞村懏顭?
                heroStatus.remaining <= 6 -> "#FFFDE7"  // 濞村懘绮?
                else -> "#FFFFFF"  // 閻у€熷
            }
            cardView.setCardBackgroundColor(android.graphics.Color.parseColor(backgroundColor))
        }
    }
}
