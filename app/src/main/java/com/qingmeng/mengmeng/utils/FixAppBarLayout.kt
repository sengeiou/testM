package com.qingmeng.mengmeng.utils

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.OverScroller
import java.lang.reflect.Field
import android.view.KeyEvent.ACTION_DOWN

/**
 * 解决AppBarLayout滑动时抖动问题
 */
class FixAppBarLayout : AppBarLayout.Behavior {

    constructor() : super() {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: AppBarLayout, ev: MotionEvent): Boolean {
        if (ev.action == ACTION_DOWN) {
            val scroller = getSuperSuperField(this, "mScroller")
            if (scroller != null && scroller is OverScroller) {
                val overScroller = scroller as OverScroller?
                overScroller!!.abortAnimation()
            }
        }

        return super.onInterceptTouchEvent(parent, child, ev)
    }

    private fun getSuperSuperField(paramClass: Any, paramString: String): Any? {
        var field: Field? = null
        var `object`: Any? = null
        try {
            field = paramClass.javaClass.superclass.superclass.getDeclaredField(paramString)
            field!!.isAccessible = true
            `object` = field.get(paramClass)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return `object`
    }

    companion object {
        private val TAG = "AppBarLayoutBehavior"
    }
}