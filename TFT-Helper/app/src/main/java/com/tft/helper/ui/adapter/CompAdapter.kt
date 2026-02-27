package com.tft.helper.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tft.helper.R
import com.tft.helper.model.Comp
import com.tft.helper.utils.ScreenAdapter

/**
 * 阵容列表适配器
 * 
 * 展示阵容列表，支持按热度、胜率等排序
 * 自适应屏幕宽度，动态计算列数
 */
class CompAdapter(
    private var comps: List<Comp> = emptyList(),
    private val onItemClick: (Comp) -> Unit,
    private val onFavoriteClick: (Comp, Boolean) -> Unit
) : RecyclerView.Adapter<CompAdapter.CompViewHolder>() {

    // 记录收藏状态
    private val favoriteSet = mutableSetOf<String>()

    /**
     * 更新数据
     */
    fun updateData(newComps: List<Comp>) {
        comps = newComps
        notifyDataSetChanged()
    }

    /**
     * 设置收藏状态
     */
    fun setFavorites(favorites: Set<String>) {
        favoriteSet.clear()
        favoriteSet.addAll(favorites)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comp, parent, false)
        return CompViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompViewHolder, position: Int) {
        holder.bind(comps[position])
    }

    override fun getItemCount(): Int = comps.size

    inner class CompViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        // 视图组件
        private val cardView: CardView = itemView.findViewById(R.id.card_comp)
        private val ivCompImage: ImageView = itemView.findViewById(R.id.iv_comp_image)
        private val tvCompName: TextView = itemView.findViewById(R.id.tv_comp_name)
        private val tvTier: TextView = itemView.findViewById(R.id.tv_tier)
        private val tvPopularity: TextView = itemView.findViewById(R.id.tv_popularity)
        private val tvWinRate: TextView = itemView.findViewById(R.id.tv_win_rate)
        private val tvTop4Rate: TextView = itemView.findViewById(R.id.tv_top4_rate)
        private val tvTraits: TextView = itemView.findViewById(R.id.tv_traits)
        private val ivFavorite: ImageView = itemView.findViewById(R.id.iv_favorite)

        fun bind(comp: Comp) {
            // 设置阵容名称
            tvCompName.text = comp.name
            
            // 设置评级
            tvTier.text = "${comp.tier}级"
            tvTier.setTextColor(parseColor(comp.getTierColor()))
            
            // 设置热度
            tvPopularity.text = "热度 ${comp.popularity.toInt()}"
            
            // 设置胜率
            tvWinRate.text = "胜率 ${comp.winRate}%"
            
            // 设置前四率
            tvTop4Rate.text = "前四 ${comp.top4Rate}%"
            
            // 设置核心羁绊
            val traitsText = comp.traits.take(3).joinToString(" ") { 
                "${it.name}(${it.count})" 
            }
            tvTraits.text = traitsText
            
            // 加载图片
            if (!comp.imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(comp.imageUrl)
                    .placeholder(R.drawable.ic_comp_placeholder)
                    .error(R.drawable.ic_comp_placeholder)
                    .into(ivCompImage)
            } else {
                ivCompImage.setImageResource(R.drawable.ic_comp_placeholder)
            }
            
            // 设置收藏状态
            val isFavorite = favoriteSet.contains(comp.id)
            ivFavorite.setImageResource(
                if (isFavorite) R.drawable.ic_favorite_filled 
                else R.drawable.ic_favorite_border
            )
            
            // 点击事件
            itemView.setOnClickListener { onItemClick(comp) }
            
            ivFavorite.setOnClickListener {
                val newFavorite = !isFavorite
                if (newFavorite) {
                    favoriteSet.add(comp.id)
                } else {
                    favoriteSet.remove(comp.id)
                }
                onFavoriteClick(comp, newFavorite)
                notifyItemChanged(adapterPosition)
            }
        }

        /**
         * 解析颜色
         */
        private fun parseColor(colorStr: String): Int {
            return try {
                android.graphics.Color.parseColor(colorStr)
            } catch (e: Exception) {
                android.graphics.Color.GRAY
            }
        }
    }

    /**
     * 网格布局管理器
     * 根据屏幕宽度动态计算列数
     */
    class GridLayoutManager(context: android.content.Context) : 
        androidx.recyclerview.widget.GridLayoutManager(
            context, 
            calculateSpanCount(context)
        ) {
        companion object {
            private const val ITEM_MIN_WIDTH_DP = 160f

            fun calculateSpanCount(context: android.content.Context): Int {
                val screenWidthPx = ScreenAdapter.getScreenWidthPx(context)
                val itemWidthPx = ScreenAdapter.dp2px(ITEM_MIN_WIDTH_DP)
                return (screenWidthPx / itemWidthPx).coerceAtLeast(2)
            }
        }
    }
}
