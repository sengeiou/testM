package com.qingmeng.mengmeng.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.entity.MicroBlog
import com.qingmeng.mengmeng.entity.Qq
import com.qingmeng.mengmeng.entity.WeChat
import com.qingmeng.mengmeng.entity.WeChatCircle
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by fyf on 2019/1/7
 * 头报分享 dialog
 */
class ShareDialog(context: Context) : Dialog(context, R.style.dialog_share) {
    private val shareWx: LinearLayout
    private val shareMoments: LinearLayout
    private val shareQQ: LinearLayout
    private val shareSina: LinearLayout
    private val shareCancel: TextView

    init {
        setContentView(R.layout.dialog_share)
        val wlp = window.attributes
        wlp.width = context.resources.displayMetrics.widthPixels
        wlp.gravity = Gravity.BOTTOM
        shareWx = findViewById(R.id.share_wechat)
        shareMoments = findViewById(R.id.share_moments)
        shareQQ = findViewById(R.id.share_qq)
        shareSina = findViewById(R.id.share_sina)
        shareCancel = findViewById(R.id.share_cancel)
        initListener()
    }

    private fun initListener() {
        shareWx.setOnClickListener {
            ToastUtil.showShort("分享到微信")
            dismiss()
        }
        shareMoments.setOnClickListener {
            ToastUtil.showShort("分享到朋友圈")
            dismiss()
        }
        shareQQ.setOnClickListener {
            ToastUtil.showShort("分享到QQ")
            dismiss()
        }
        shareSina.setOnClickListener {
            ToastUtil.showShort("分享到新浪微博")
            dismiss()
        }
        shareCancel.setOnClickListener { dismiss() }
    }

}