package com.lemo.emojcenter.adapter

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup

/**
 * Description:
 * Author:wxw
 * Date:2018/1/29.
 */
class FaceMyViewPagerAdapter(private var viewList: List<View>?//View就二十GridView
) : PagerAdapter() {

    fun setData(viewList: List<View>) {
        this.viewList = viewList
    }

    override fun getCount(): Int {
        return if (viewList != null) viewList!!.size else 0
    }

    override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
        return arg0 === arg1
    }

    /**
     * 将当前的View添加到ViewGroup容器中
     * 这个方法，return一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPage上
     */
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(viewList!![position])
        return viewList!![position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}
