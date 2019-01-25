@file:Suppress("DEPRECATION")

package com.qingmeng.mengmeng.utils

import android.widget.TextView

/**
 * Created by zq on 2018/9/26
 */
fun TextView.setDrawableLeft(resId: Int) {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(resources.getDrawable(resId), null, null, null)
}

fun TextView.setDrawableRight(resId: Int) {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, resources.getDrawable(resId), null)
}

fun TextView.setDrawableTop(resId: Int) {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(null, resources.getDrawable(resId), null, null)
}

fun TextView.setDrawableBottom(resId: Int) {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, resources.getDrawable(resId))
}