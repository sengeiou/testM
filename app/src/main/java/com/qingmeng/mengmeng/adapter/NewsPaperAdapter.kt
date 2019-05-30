package com.qingmeng.mengmeng.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.bingoogolapple.bgabanner.BGABanner
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.entity.Banner
import com.qingmeng.mengmeng.entity.NewsPagerList
import com.qingmeng.mengmeng.view.GlideRoundTransformCenterCrop
import com.youke.yingba.base.utils.TimeConversion.getDateToString

/**
 * Created by fyf on 2019/1/2
 * 头报适配器
 */
open class NewsPaperAdapter(val context: Context, var mImgsList: ArrayList<Banner>,
                            val onItemClickListener: (newsPagerList: NewsPagerList) -> Unit,
                            private val onBannerClick: (Banner) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(), BGABanner.Delegate<ImageView, Banner>, BGABanner.Adapter<ImageView, Banner> {
    val mList = ArrayList<NewsPagerList>()
    private val ITEM_BANNER = 0
    private val ITEM_NEWS = 1
    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> ITEM_BANNER
        else -> ITEM_NEWS
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.let {
            when (getItemViewType(position = position)) {
                ITEM_BANNER -> (it as NewspaperBannerViewHolder).bindViewHolder()
                ITEM_NEWS -> (it as NewspaperNewsViewHolder).bindViewHolder(mList[position - 1])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            ITEM_BANNER -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.news_bgabanner_item, parent, false)
                NewspaperBannerViewHolder(view)
            }
            else -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.news_paper_item, parent, false)
                NewspaperNewsViewHolder(view)
            }
        }

    }

    override fun getItemCount(): Int = mList.size + 1
    fun isEmpty(): Boolean = mList.isEmpty()
    /** 更新数据，替换原有数据  */
    fun updateItems(items: ArrayList<NewsPagerList>) {
        if (!mList.isEmpty()) {
            mList.clear()
        }
        mList.addAll(items)
//        notifyDataSetChanged()
    }

    /** 在列表尾添加一串数据  */
    fun addItems(items: List<NewsPagerList>) {
        mList.addAll(items)
//        notifyDataSetChanged()
    }

    inner class NewspaperBannerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val bgaBanner = itemView.findViewById<BGABanner>(R.id.news_pager_bgaBanner)
        fun bindViewHolder() {
            bgaBanner.setAdapter(this@NewsPaperAdapter) //必须设置此适配器，否则方法不会调用接口来填充图片
            bgaBanner.setDelegate(this@NewsPaperAdapter) //设置点击事件，重写点击回调方法
            bgaBanner.setData(mImgsList, null)
            bgaBanner.setAutoPlayAble(mImgsList.size > 1)
            if (mImgsList.isEmpty()) {
                bgaBanner.showPlaceholder()
            }
        }
    }

    inner class NewspaperNewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val mImgView = itemView.findViewById<ImageView>(R.id.news_pager_icon)
        private val mTittle = itemView.findViewById<TextView>(R.id.news_pager_tittle)
        private val mContext = itemView.findViewById<TextView>(R.id.news_pager_content)
        private val mDate = itemView.findViewById<TextView>(R.id.news_pager_date)
        private lateinit var url: String
        fun bindViewHolder(newsPagerList: NewsPagerList) {
            mTittle.text = newsPagerList.title
            mContext.text = newsPagerList.content
            mDate.text = getDateToString(newsPagerList.formatTime.toLong())
            url = newsPagerList.articleUrl
            Glide.with(context).load(newsPagerList.banner).apply(RequestOptions()
                    .placeholder(R.drawable.default_img_banner)
                    .transform(GlideRoundTransformCenterCrop())
            )
                    .into(mImgView)
            itemView.setOnClickListener { onItemClickListener(newsPagerList) }
        }
    }

    //Banner 点击事件    跳转链接
    override fun onBannerItemClick(banner: BGABanner?, itemView: ImageView?, model: Banner?, position: Int) {
        model?.let { onBannerClick(it) }
    }

    //Banner 加载图片
    override fun fillBannerItem(banner: BGABanner?, itemView: ImageView, model: Banner?, position: Int) {
        model?.let {
            Glide.with(context).load(it.imgUrl).apply(RequestOptions()
                    .placeholder(R.drawable.default_img_banner).error(R.drawable.default_img_banner)
                    .centerCrop()).into(itemView)
        }
    }
}