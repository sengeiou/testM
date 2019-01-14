package com.qingmeng.mengmeng.view


import android.content.Context
import android.util.AttributeSet
import android.view.View

import com.aspsine.swipetoloadlayout.SwipeToLoadLayout
import com.qingmeng.mengmeng.R

/**
 * 用固定视图封装过的滑动控件。
 */
class SwipeLayout : SwipeToLoadLayout {

    interface ISwipeEndListener {
        fun refreshEnd(succ: Boolean)

        fun loadMoreEnd(succ: Boolean)
    }

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    private fun initView(context: Context) {
        View.inflate(context, R.layout.swipe_head, this)
        View.inflate(context, R.layout.swipe_foot, this)
    }

    /**
     * 结束刷新动作
     */
    fun endRefresh() {
        isRefreshing = false
    }

    /**
     * 结束加载更多动作
     */
    fun endLoadMore() {
        isLoadingMore = false
    }

    /**
     * 当加载至尾页时调用
     */
    fun noMore(noMore: Boolean) {
        (findViewById<View>(R.id.swipe_load_more_footer) as SwipeFootView).noMore(noMore)
    }

    companion object {
        val STATE_NORMAL = 0x00
        val STATE_REFRESH = 0x01
        val STATE_LOADMORE = 0x02
    }
}
