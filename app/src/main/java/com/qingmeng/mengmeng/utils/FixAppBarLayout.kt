package com.qingmeng.mengmeng.utils

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.OverScroller
import java.lang.reflect.Field
import android.view.KeyEvent.ACTION_DOWN
import android.view.View

/**
 * 解决AppBarLayout滑动时抖动问题
 */
class FixAppBarLayout : AppBarLayout.Behavior {

    constructor() : super() {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    // --------------begin added by shaopx -------------
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

    // --------------------------- end

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View,
                                dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, type)
        stopNestedScrollIfNeeded(dyUnconsumed, child, target, type)
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout,
                                   target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        stopNestedScrollIfNeeded(dy, child, target, type)
    }

    private fun stopNestedScrollIfNeeded(dy: Int, child: AppBarLayout, target: View?, type: Int) {
        if (type == ViewCompat.TYPE_NON_TOUCH) {
            val currOffset = topAndBottomOffset
            if (dy < 0 && currOffset == 0 || dy > 0 && currOffset == -child.totalScrollRange) {
                ViewCompat.stopNestedScroll(target!!, ViewCompat.TYPE_NON_TOUCH)
            }
        }
    }

    companion object {
        private val TAG = "AppBarLayoutBehavior"
    }
}