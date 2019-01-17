package com.qingmeng.mengmeng.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 *  Description :viewPager适配器

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/01/10
 */
class MyFragmentPagerAdapter(fm: FragmentManager, var list: ArrayList<Fragment>, private var titles: Array<String>) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return list[position]
    }

    override fun getCount(): Int {
        return titles.size
    }

    //ViewPager与TabLayout绑定后，这里获取到PageTitle就是Tab的Text
    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }
}