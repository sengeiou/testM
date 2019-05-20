package com.qingmeng.mengmeng.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.entity.JoinRecommendBean

class JoinRecommendAdapter(val context: Context, val onItemClick: (JoinRecommendBean.JoinBean) -> Unit) : RecyclerView.Adapter<JoinRecommendAdapter.RecommendViewHolder>() {
    private val list = ArrayList<JoinRecommendBean.JoinBean>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendViewHolder {
        if (viewType == -1) {
            return RecommendViewHolder(LayoutInflater.from(context).inflate(R.layout.item_join_a, parent, false))
        }
        return RecommendViewHolder(LayoutInflater.from(context).inflate(R.layout.item_join_recommend, parent, false))
    }

    override fun getItemCount(): Int = if (list.size == 1) 2 else list.size

    override fun getItemViewType(position: Int): Int {
        if (position > list.size - 1) return -1
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(viewHolder: RecommendViewHolder, position: Int) {
        if (position > list.size - 1) return
        viewHolder.bindViewHolder(list[position])
    }

    fun isEmpty(): Boolean = list.isEmpty()

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
        private val recommendLogo by lazy { itemView.findViewById<ImageView>(R.id.recommend_logo) }
        private val recommendName by lazy { itemView.findViewById<TextView>(R.id.recommend_name) }
        private val recommendMoney by lazy { itemView.findViewById<TextView>(R.id.recommend_money) }

        @SuppressLint("SetTextI18n")
        fun bindViewHolder(joinBean: JoinRecommendBean.JoinBean) {
            recommendName.text = joinBean.name
            if (TextUtils.isEmpty(joinBean.capitalName)) {
                recommendMoney.text = "￥面议"
            } else {
                recommendMoney.text = "￥${joinBean.capitalName}"
            }
            itemView.setOnClickListener { onItemClick(joinBean) }
//            if (!TextUtils.isEmpty(joinBean.appCover)) {
//                Glide.with(context).load(joinBean.appCover).apply(RequestOptions()
//                        .placeholder(R.drawable.default_img_banner).error(R.drawable.default_img_banner)).into(recommendLogo)
//            } else {
            val logo = if(!TextUtils.isEmpty(joinBean.appCover)) joinBean.appCover else joinBean.logo
                if (!TextUtils.isEmpty(logo)) {
                    Glide.with(context).load(logo).apply(RequestOptions()
                            .placeholder(R.drawable.default_img_banner).error(R.drawable.default_img_banner)).into(recommendLogo)
                }
//            }
        }
    }
}