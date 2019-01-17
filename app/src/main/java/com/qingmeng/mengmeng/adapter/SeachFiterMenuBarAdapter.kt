package com.qingmeng.mengmeng.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Created by fyf on 2019/1/15
 * 搜索页筛选菜单栏适配器
 */
class SeachFiterMenuBarAdapter(fm: FragmentManager, var list: ArrayList<Fragment>, private var titles: Array<String>) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return list[position]
    }

    override fun getCount(): Int {
        return titles.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }

}