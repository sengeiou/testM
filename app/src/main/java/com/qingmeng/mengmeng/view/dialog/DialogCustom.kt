package com.qingmeng.mengmeng.view.dialog

import android.app.Dialog
import android.content.Context

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
}