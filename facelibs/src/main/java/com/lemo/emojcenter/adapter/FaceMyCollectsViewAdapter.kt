package com.lemo.emojcenter.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.lemo.emojcenter.R
import com.lemo.emojcenter.bean.CollectsBean

/**
 * Description:
 * Author:wxw
 * Date:2018/2/11.
 */
class FaceMyCollectsViewAdapter(private val context: Context,
                                private val lists: List<CollectsBean>//数据源
                                , private val mIndex: Int // 页数下标，标示第几页，从0开始
                                ,
                                private val mPargerSize: Int// 每页显示的最大的数量
) : BaseAdapter() {

    override fun getCount(): Int {
        return if (lists.size > (mIndex + 1) * mPargerSize)
            mPargerSize
        else
            lists.size - mIndex * mPargerSize
    }

    override fun getItem(position: Int): CollectsBean {
        return lists[position + mIndex * mPargerSize]
    }

    override fun getItemId(position: Int): Long {
        return (position + mIndex * mPargerSize).toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        var holder: ViewHolder? = null
        if (convertView == null) {
            holder = ViewHolder()
            convertView = View.inflate(context, R.layout.face_item_view, null)
            holder.mTvName = convertView!!.findViewById(R.id.item_name)
            holder.mImageUrl = convertView.findViewById(R.id.item_image)
            convertView.tag = holder
        } else {
            holder = convertView.tag as FaceMyCollectsViewAdapter.ViewHolder
        }
        //重新确定position因为拿到的总是数据源，数据源是分页加载到每页的GridView上的
        val pos = position + mIndex * mPargerSize//假设mPageSiez
        //假设mPagerSize=8，假如点击的是第二页（即mIndex=1）上的第二个位置item(position=1),那么这个item的实际位置就是pos=9
        //        holder.iv_nul.setImageResource(lists.get(pos).getUrl());
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.face_chat_img_bg)
        requestOptions.error(R.drawable.face_chat_img_bg)
        if (lists[pos].cover == null) {
            Glide.with(context).load(R.mipmap.face_tianjia).apply(requestOptions).into(holder.mImageUrl!!)
        } else {
            Glide.with(context).load(lists[pos].cover).apply(requestOptions).listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    return false
                }
            }).into(holder.mImageUrl!!)
        }
        return convertView
    }

    internal class ViewHolder {
        internal var mTvName: TextView? = null
        internal var mImageUrl: ImageView? = null

    }
}
