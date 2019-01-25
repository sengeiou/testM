package com.qingmeng.mengmeng.adapter

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import cn.jzvd.JZMediaManager
import cn.jzvd.JzvdStd
import com.bumptech.glide.Glide
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.entity.ShopDetailImg
import com.qingmeng.mengmeng.view.photoview.EasePhotoView

@Suppress("DEPRECATION")
class DetailImgVpAdapter(val context: Context, val list: ArrayList<ShopDetailImg>) : PagerAdapter() {
    override fun isViewFromObject(view: View, any: Any): Boolean = view == any

    override fun getCount(): Int = list.size

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(any as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val detail = list[position]
        return if (detail.isVideo) {
            MainApplication.secondVideo?.let { second->
                second.textureViewContainer.removeView(JZMediaManager.textureView)
                MainApplication.firstVideo?.apply {
                    setUp(second.jzDataSource, JzvdStd.SCREEN_WINDOW_NORMAL)
                    setState(second.currentState)
                    addTextureView()
                }
            }
            MainApplication.firstVideo!!.textureViewContainer.removeView(JZMediaManager.textureView)
            val video = JzvdStd(context)
            video.setUp(MainApplication.firstVideo!!.jzDataSource, JzvdStd.SCREEN_WINDOW_NORMAL)
            video.setState(MainApplication.firstVideo!!.currentState)
            video.addTextureView()
            MainApplication.firstVideo!!.outFullscreen = {
                video.playOnThisJzvd()
            }
            MainApplication.secondVideo = video
            container.addView(video)
            JzvdStd.goOnPlayOnPause()
            JzvdStd.goOnPlayOnResume()
            video
        } else {
            val image = EasePhotoView(context)
            Glide.with(context).load(detail.path).into(image)
            container.addView(image)
            image
        }
    }
}