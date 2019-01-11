package com.qingmeng.mengmeng.utils

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout

/**
 *  Description :view工具

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/11
 */
//获取状态栏高度
fun getBarHeight(context: Context): Int {
    val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    return context.resources.getDimensionPixelSize(resourceId)
}

//设置view的margin
fun View.setMarginExt(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    val layout = when (layoutParams) {
        is RelativeLayout.LayoutParams -> layoutParams as RelativeLayout.LayoutParams
        is LinearLayout.LayoutParams -> layoutParams as LinearLayout.LayoutParams
        is FrameLayout.LayoutParams -> layoutParams as FrameLayout.LayoutParams
        is RecyclerView.LayoutParams -> layoutParams as RecyclerView.LayoutParams
        is ViewGroup.MarginLayoutParams -> layoutParams as ViewGroup.MarginLayoutParams
        else -> null
    }
    if (layout == null) return
    val leftResult = left ?: layout.leftMargin
    val rightResult = right ?: layout.rightMargin
    val topResult = top ?: layout.topMargin
    val bottomResult = bottom ?: layout.bottomMargin

    layout.let {
        it.setMargins(leftResult, topResult, rightResult, bottomResult)
        layoutParams = it
    }
}

//数值转dp
fun Context.dp2px(dpVal: Int): Int = dp2px(dpVal.toFloat()).toInt()

fun Context.dp2px(dpVal: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, this.resources.displayMetrics)