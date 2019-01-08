package com.qingmeng.mengmeng.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.bingoogolapple.bgabanner.BGABanner
import com.qingmeng.mengmeng.R

/**
 * Created by fyf on 2019/1/2
 * 头报适配器
 */

class NewspaperAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ITEM_BANNER = 0
    private val ITEM_NEWS = 1
    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> ITEM_BANNER
        else -> ITEM_NEWS
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item_type: Int) {
        holder.let {
            when (item_type) {
                ITEM_BANNER -> (it as NewspaperBannerViewHolder).bindViewHolder()
                ITEM_NEWS -> (it as NewspaperNewsViewHolder).bindViewHolder()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            ITEM_BANNER -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_news_bgabanner, parent, false)
                NewspaperBannerViewHolder(view)
            }
            else -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_news_paper, parent, false)
                NewspaperBannerViewHolder(view)
            }
        }

    }

    //以后改
    override fun getItemCount(): Int = 1

    inner class NewspaperBannerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val bgaBanner = itemView.findViewById<BGABanner>(R.id.news_pager_bgaBanner)
        fun bindViewHolder() {

        }
    }

    inner class NewspaperNewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon = itemView.findViewById<ImageView>(R.id.news_pager_icon)
        private val tittle = itemView.findViewById<TextView>(R.id.news_pager_tittle)
        private val content = itemView.findViewById<TextView>(R.id.news_pager_content)
        private val date = itemView.findViewById<TextView>(R.id.news_pager_date)

        fun bindViewHolder() {

        }
    }
}