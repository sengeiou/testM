package com.qingmeng.mengmeng.utils

import android.os.Handler
import android.os.Message
import android.widget.TextView
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import java.lang.ref.WeakReference

/**
 * handler 持有当前 Activity 的弱引用防止内存泄露
 */
class TimerHandler(activity: BaseActivity, private val tv: TextView) : Handler() {
    private var mWeakActivity = WeakReference<BaseActivity>(activity)
    override fun handleMessage(msg: Message?) {
        val activity = mWeakActivity.get() ?: return
        when (msg?.what) {
            activity.timingOver -> {
                tv.setText(R.string.getMsg)
                tv.setTextColor(activity.resources.getColor(R.color.colorPrimary))
                tv.isClickable = true
                activity.totalTime = -1
            }
            activity.timing -> {
                if (activity.totalTime == 1) {
                    activity.timerHandler.sendEmptyMessageDelayed(activity.timingOver, 1000)
                } else {
                    if (activity.totalTime == -1) {
                        activity.totalTime = 60
                        tv.isClickable = false
                    } else {
                        activity.totalTime--
                    }
                    activity.timerHandler.sendEmptyMessageDelayed(activity.timing, 1000)
                }
                tv.text = "${activity.totalTime}秒后重试"
                tv.setTextColor(activity.resources.getColor(R.color.color_999999))
            }
        }
    }
}