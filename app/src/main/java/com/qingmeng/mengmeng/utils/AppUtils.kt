package com.qingmeng.mengmeng.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.util.FooterView
import java.io.FileInputStream
import java.io.FileNotFoundException



/**
 *  Description :view工具

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/11
 */
/**
 * 获取状态栏高度
 */
fun getBarHeight(context: Context): Int {
    val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    return context.resources.getDimensionPixelSize(resourceId)
}

/**
 * 设置view的margin
 */
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

/**
 * 数值转dp
 */
fun Context.dp2px(dpVal: Int): Int = dp2px(dpVal.toFloat()).toInt()

fun Context.dp2px(dpVal: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, this.resources.displayMetrics)

/**
 * 加载本地图片 转bitmap
 */
fun getLoacalBitmap(url: String): Bitmap? {
    try {
        val fis = FileInputStream(url)
        return BitmapFactory.decodeStream(fis)  ///把流转化为Bitmap图片
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        return null
    }
}

/**
 * 跳转到自己应用的设置页面
 */
fun toSelfSetting(context: Context) {
    val mIntent = Intent()
    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (Build.VERSION.SDK_INT >= 9) {
        mIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
        mIntent.data = Uri.fromParts("package", context.packageName, null)
    } else if (Build.VERSION.SDK_INT <= 8) {
        mIntent.action = Intent.ACTION_VIEW
        mIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails")
        mIntent.putExtra("com.android.settings.ApplicationPkgName", context.packageName)
    }
    context.startActivity(mIntent)
}

/**
 * 设置加载更多状态
 * style 1 加载中 2 加载结束 3 隐藏
 */
fun setFooterStatus(footerView: FooterView, style: Int) {
    when (style) {
        1 -> {
            footerView.findViewById<ProgressBar>(R.id.progressBar_footer).visibility = View.VISIBLE
            footerView.findViewById<TextView>(R.id.tv_foot_name).text = "加载中..."
        }
        2 -> {
            footerView.findViewById<ProgressBar>(R.id.progressBar_footer).visibility = View.GONE
            footerView.findViewById<TextView>(R.id.tv_foot_name).text = "---我是有底线的---"
        }
        3 -> {
            footerView.findViewById<ProgressBar>(R.id.progressBar_footer).visibility = View.GONE
            footerView.findViewById<TextView>(R.id.tv_foot_name).text = ""
        }
    }
}