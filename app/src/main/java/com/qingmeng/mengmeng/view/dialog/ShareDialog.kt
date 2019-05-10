package com.qingmeng.mengmeng.view.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.qingmeng.mengmeng.R

/**
 * Created by fyf on 2019/1/7
 * 头报分享 dialog
 */
class ShareDialog(context: Context,val onWechatClick:() -> Unit,val onPengyouClick:() -> Unit,val onQQClick:() -> Unit,val onSinaClick:() -> Unit) : Dialog(context, R.style.dialog_share) {
    private val shareWx: LinearLayout
    private val shareMoments: LinearLayout
    private val shareQQ: LinearLayout
    private val shareSina: LinearLayout
    private val shareCancel: TextView

    // private lateinit var content: String
    // var wxList: ArrayList<WeChat>, var monentsList: ArrayList<WeChatCircle>,
    // var qqList: ArrayList<QQ>,var sinaList: ArrayList<MicroBlog>
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
            onWechatClick()
            dismiss()
        }
        shareMoments.setOnClickListener {
            onPengyouClick()
            dismiss()
        }
        shareQQ.setOnClickListener {
            onQQClick()
            dismiss()
        }
        shareSina.setOnClickListener {
            onSinaClick()
            dismiss()
        }
        shareCancel.setOnClickListener { dismiss() }
    }
}