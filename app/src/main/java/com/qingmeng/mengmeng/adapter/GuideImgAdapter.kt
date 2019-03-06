package com.qingmeng.mengmeng.adapter

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.qingmeng.mengmeng.R

class GuideImgAdapter : PagerAdapter() {
    private val imgIds = arrayOf(R.drawable.img_splash1, R.drawable.img_splash2, R.drawable.img_splash3)

    override fun isViewFromObject(view: View, any: Any): Boolean = view == any

    override fun getCount(): Int = imgIds.size

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(any as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val image = ImageView(container.context)
        image.scaleType = ImageView.ScaleType.FIT_XY
        image.setImageResource(imgIds[position])
        container.addView(image)
        return image
    }
}