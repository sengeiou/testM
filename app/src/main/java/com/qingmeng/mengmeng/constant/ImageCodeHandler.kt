package com.qingmeng.mengmeng.constant

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.widget.TextView
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import java.lang.ref.WeakReference

/**
 * Created by zq on 2018/9/8
 */
class ImageCodeHandler(activity: BaseActivity, val tv: TextView) : Handler() {
    private var mWeakActivity: WeakReference<BaseActivity> = WeakReference(activity)

    @SuppressLint("SetTextI18n")
    override fun handleMessage(msg: Message?) {
        val activity = mWeakActivity.get() ?: return
        when (msg?.what) {
            activity.timingOver -> {
                tv.setText(R.string.getMsg_again)
                tv.isClickable = true
                activity.totalTime = -1
            }
            activity.timing -> {
                if (activity.totalTime == 0) {
                    activity.imgHandler.sendEmptyMessageDelayed(activity.timingOver, 1000)
                } else {
                    if (activity.totalTime == -1) {
                        activity.totalTime = 60
                        tv.isClickable = false
                    } else {
                        activity.totalTime--
                    }
                    activity.imgHandler.sendEmptyMessageDelayed(activity.timing, 1000)
                }
                tv.text = "${activity.totalTime}秒后重试"
            }
        }
    }
}