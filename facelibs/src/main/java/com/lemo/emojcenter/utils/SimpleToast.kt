package com.lemo.emojcenter.utils

import android.content.Context
import android.support.annotation.StringRes
import android.text.TextUtils
import android.view.Gravity
import android.widget.Toast
import com.lemo.emojcenter.FaceConfigInfo
import com.lemo.emojcenter.FaceInitData
import com.lemo.emojcenter.R


object SimpleToast {
    fun show(@StringRes strRes: Int) {
        show(FaceInitData.context, FaceInitData.context.getString(strRes))
    }

    fun show(content: String) {
        show(FaceInitData.context, content)
    }

    fun show(context: Context, @StringRes strRes: Int) {
        show(context, context.resources.getString(strRes))
    }

    @JvmOverloads
    fun show(context: Context, content: String, gravity: Int = Gravity.BOTTOM, duration: Int = Toast.LENGTH_SHORT) {
        ToastUtil.showToast(context, content, duration)
    }

    /**
     * toast网络异常
     *
     * @param e
     */
    fun showNetError(e: String) {
        if ("Canceled" != e) {
            if (FaceConfigInfo.isDebug) {
                show(e)
            } else {
                show(R.string.face_response_failure)
            }
        }
    }

    fun showNetError(e: Exception?) {
        if (e != null && !TextUtils.isEmpty(e.message)) {
            e.message?.let { showNetError(it) }
        }
    }

}
