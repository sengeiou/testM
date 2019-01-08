package com.qingmeng.mengmeng.view.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.ToastUtil

/**
 * Created by fyf on 2019/1/7
 * 头报分享 dialog
 */
class ShareDialog(context: Context) : Dialog(context, R.style.dialog_share) {
    private val shareWx: LinearLayout
    private val shareMoments: LinearLayout
    private val shareQQ: LinearLayout
    private val shareSina: LinearLayout
    private val shareCancle: TextView

    init {
        setContentView(R.layout.dialog_share)
        val wlp = window.attributes
        wlp.width = context.resources.displayMetrics.widthPixels
        wlp.gravity = Gravity.BOTTOM
        shareWx = findViewById(R.id.share_wechat)
        shareMoments = findViewById(R.id.share_moments)
        shareQQ = findViewById(R.id.share_qq)
        shareSina = findViewById(R.id.share_sina)
        shareCancle = findViewById(R.id.share_cancel)
        initListener()
    }

    private fun initListener() {
        shareWx.setOnClickListener { ToastUtil.showShort("分享到微信") }
        shareMoments.setOnClickListener { ToastUtil.showShort("分享到朋友圈") }
        shareQQ.setOnClickListener { ToastUtil.showShort("分享到QQ") }
        shareSina.setOnClickListener { ToastUtil.showShort("分享到新浪微博") }
        shareCancle.setOnClickListener { dismiss() }
    }

}