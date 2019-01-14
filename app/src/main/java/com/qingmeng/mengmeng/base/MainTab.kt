package com.qingmeng.mengmeng.base

import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.fragment.JoinFragment
import com.qingmeng.mengmeng.fragment.MyFragment
import com.qingmeng.mengmeng.fragment.NewspaperFragment
import com.qingmeng.mengmeng.fragment.RedShopFragment

/**
 * Created by zq on 2018/8/6
 */
enum class MainTab private constructor(var idx: Int, var resName: Int, var resIcon: Int, var clz: Class<*>) {

    NEWS(0, R.string.tab_name_love_to_join, R.drawable.main_icon_join, JoinFragment::class.java),
    SECOND(1, R.string.tab_name_red_shop, R.drawable.main_icon_red, RedShopFragment::class.java),
    THRID(2, R.string.tab_name_head_newspaper, R.drawable.main_icon_newspaper, NewspaperFragment::class.java),
    FOUR(3, R.string.tab_name_my, R.drawable.main_icon_my, MyFragment::class.java)
}