package com.qingmeng.mengmeng.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.entity.JoinRecommendBean

class JoinRecommendAdapter(val context: Context, val list: ArrayList<JoinRecommendBean.JoinBean>,
                           val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<JoinRecommendAdapter.RecommendViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendViewHolder {
        return RecommendViewHolder(LayoutInflater.from(context).inflate(R.layout.item_join_recommend, parent, false))
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(viewHolder: RecommendViewHolder, position: Int) {
        viewHolder.bindViewHolder(list[position], position)
    }

    /** 更新数据，替换原有数据  */
    fun updateItems(items: ArrayList<JoinRecommendBean.JoinBean>) {
        if (!list.isEmpty()) {
            list.clear()
        }
        list.addAll(items)
        notifyDataSetChanged()
    }

    /** 在列表尾添加一串数据  */
    fun addItems(items: List<JoinRecommendBean.JoinBean>) {
        list.addAll(items)
        notifyDataSetChanged()
    }

    inner class RecommendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recommendLogo: ImageView = itemView.findViewById(R.id.recommend_logo)
        val recommendName: TextView = itemView.findViewById(R.id.recommend_name)
        val recommendMoney: TextView = itemView.findViewById(R.id.recommend_money)

        fun bindViewHolder(joinBean: JoinRecommendBean.JoinBean, position: Int) {
            recommendName.text = joinBean.name
            recommendMoney.text = joinBean.capitalName
            itemView.setOnClickListener { onItemClick(position) }
        }
    }
}