package com.qingmeng.mengmeng.adapter

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import cn.jzvd.JZUtils
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import com.bumptech.glide.Glide
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.entity.ShopDetailImg
import com.qingmeng.mengmeng.utils.GlideCacheUtils
import com.qingmeng.mengmeng.view.MyVideoView

@Suppress("DEPRECATION")
class ShopDetailVpAdapter(val context: Context, val list: ArrayList<ShopDetailImg>, private val videoClick: (Int) -> Unit) : PagerAdapter() {
    private var isFirst = true

    override fun isViewFromObject(view: View, any: Any): Boolean = view == any

    override fun getCount(): Int = list.size

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(any as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val detail = list[position]
        return if (detail.isVideo) {
            val video = MyVideoView(context)
            video.setUp(detail.path, "", JzvdStd.SCREEN_WINDOW_NORMAL)
            video.onVideoClick = { videoClick(position) }
            GlideCacheUtils.loadVideoScreenshot(context, detail.path, video.thumbImageView, 1)
            video.initTextureView()
            MainApplication.firstVideo = video
            container.addView(video)
            if (!detail.path.startsWith("file") && !detail.path.startsWith("/") &&
                    !JZUtils.isWifiConnected(context) && !Jzvd.WIFI_TIP_DIALOG_SHOWED && !isFirst) {
            } else {
                isFirst = false
//                video.startVideo()
            }
            video
        } else {
            val image = ImageView(context)
            image.setOnClickListener { videoClick(position) }
            Glide.with(context).load(detail.path).into(image)
            container.addView(image)
            image
        }
    }
}