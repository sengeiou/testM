package com.qingmeng.mengmeng.adapter

import android.content.Context
import android.graphics.Color
import android.support.v4.view.ViewPager
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.entity.StaticBean
import com.qingmeng.mengmeng.view.ScaleTransitionPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import java.util.*

/**
 * Created by tgy on 2017/2/26.
 */
@Suppress("DEPRECATION")
class UnderLineNavigatorAdapter(var tagList: ArrayList<StaticBean>) : CommonNavigatorAdapter() {
    private lateinit var viewPager: ViewPager

    fun setRelateViewPager(viewPager: ViewPager) {
        this.viewPager = viewPager
    }

    override fun getCount(): Int = tagList.size

    override fun getTitleView(context: Context, index: Int): IPagerTitleView {
        // 缩放 + 颜色渐变
        val simplePagerTitleView = ScaleTransitionPagerTitleView(context)
        simplePagerTitleView.text = tagList[index].title
        simplePagerTitleView.minScale = 1f
        simplePagerTitleView.textSize = 16f
        simplePagerTitleView.normalColor = context.resources.getColor(R.color.color_666666)
        simplePagerTitleView.selectedColor = context.resources.getColor(R.color.main_theme)
        simplePagerTitleView.setOnClickListener {
            if (viewPager.adapter?.count ?: 0 > index) {
                viewPager.setCurrentItem(index, true)
            }
        }
        return simplePagerTitleView
    }

    override fun getIndicator(context: Context): IPagerIndicator {
        val indicator = LinePagerIndicator(context)
        indicator.mode = LinePagerIndicator.MODE_WRAP_CONTENT
        indicator.setColors(Color.WHITE)
        return indicator
    }
}


