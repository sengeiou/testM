package com.qingmeng.mengmeng.view.dialog

import android.app.Dialog
import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.GeetestUtil
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

@Suppress("IMPLICIT_BOXING_IN_IDENTITY_EQUALS", "DEPRECATED_IDENTITY_EQUALS")
class DialogCustom(private var mContext: Context?) {
    private var remindDialog: Dialog? = null
    private var loadingDialog: Dialog? = null
    private var screenWidth: Int = mContext?.resources?.displayMetrics?.widthPixels ?: 0

    fun unBindContext() {
        screenWidth = 0
        mContext = null
        remindDialog?.let { remindDialog = null }
        loadingDialog?.let { loadingDialog = null }
    }

    fun showImageCodeDialog(phone: String, type: Int, sureClick: (disposable: Disposable) -> Unit, startTiming: () -> Unit) {
        remindDialog?.let {
            if (it.isShowing) {
                return@let
            }
        }
        remindDialog = Dialog(mContext!!, R.style.commondialogstyle)
        val view = View.inflate(mContext, R.layout.layout_dialog_imageedtext, null)
        val banCancel = view.findViewById<TextView>(R.id.dialog_image_cancel)
        val btnSure = view.findViewById<TextView>(R.id.dialog_image_confirm)
        val imageView = view.findViewById<ImageView>(R.id.dialog_image_image)
        val layout = view.findViewById<LinearLayout>(R.id.dialog_image_layout)
        val editText = view.findViewById<EditText>(R.id.dialog_image_edtext)
        mContext?.let { it ->
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.white))
            Glide.with(it).load(IConstants.GET_IMAGE_CODE + phone)
                    .apply(RequestOptions().skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)).into(imageView)
            layout.setOnClickListener {
                Glide.with(it).load(IConstants.GET_IMAGE_CODE + phone)
                        .apply(RequestOptions().skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)).into(imageView)
            }
        }
        banCancel.setOnClickListener { remindDialog?.dismiss() }
        btnSure.setOnClickListener { _ ->
            if (editText.text.length !== 4) {
                mContext?.let {
                    ToastUtil.showShort(it.getString(R.string.scuuess_code))
                }
                return@setOnClickListener
            }
            ApiUtils.getApi().sendSms(phone, type, editText.text.toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        if (it.code == 12000) {
                            startTiming()
                            remindDialog?.dismiss()
                        } else {
                            ToastUtil.showShort(it.msg)
                            remindDialog?.dismiss()
                        }
                    }, {
                        GeetestUtil.showFailedDialog()
                        ToastUtil.showNetError()
                    }, {}, { sureClick(it) })
        }
        remindDialog?.setContentView(view)
        val lp = remindDialog?.window?.attributes
        if (screenWidth == 0) {
            mContext?.let {
                screenWidth = it.resources.displayMetrics.widthPixels
            }
        }
        lp?.let {
            it.width = screenWidth * 4 / 5 // 设置宽度
            remindDialog?.window?.attributes = lp
            remindDialog?.setCanceledOnTouchOutside(false)
            remindDialog?.setCancelable(false)
            remindDialog?.show()
        }
    }
}