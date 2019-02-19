package com.lemo.emojcenter.utils

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.StringRes
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.lemo.emojcenter.FaceConfigInfo
import com.lemo.emojcenter.FaceInitData
import com.lemo.emojcenter.R

/**
 * @Function Toast工具类
 * @Auther Lee
 * @Date 16/12/15
 */
object ToastUtil {

    var isShow = true

    var mToast: Toast? = null
    private var currentToast: Toast? = null
    private var toastView: View? = null

    private fun createToast(context: Context, msg: CharSequence, duration: Int): Toast {
        val view = LayoutInflater.from(context).inflate(R.layout.face_layout_custom_toast, null)
        val toastMsg_tv = view.findViewById<TextView>(R.id.toastMsg_tv)
        toastMsg_tv.text = msg
        mToast = Toast(context)
        mToast!!.view = view
        mToast!!.duration = duration
        mToast!!.setGravity(Gravity.CENTER, 0, 0)
        return mToast as Toast
    }

    fun showToast(context: Context, message: CharSequence, showDate: Int) {
        if (isShow) {
            if (mToast != null) {
                cancel()
            }
            mToast = createToast(context, message, showDate)

            mToast!!.show()
        }
    }

    fun showShort(context: Context, @StringRes strRes: Int) {
        showShort(context, context.getString(strRes))
    }

    fun showShort(context: Context, message: CharSequence) {
        if (isShow) {
            if (mToast != null) {
                cancel()
            }
            mToast = createToast(context, message, Toast.LENGTH_SHORT)
            mToast!!.show()
        }
    }

    fun showShort(message: CharSequence) {
        if (isShow) {
            if (mToast != null) {
                cancel()
            }
            mToast = createToast(FaceInitData.context, message, Toast.LENGTH_SHORT)
            mToast!!.show()
        }
    }

    fun showShortDebug(message: CharSequence) {
        if (isShow && FaceConfigInfo.isDebug) {
            if (mToast != null) {
                cancel()
            }
            mToast = createToast(FaceInitData.context, message, Toast.LENGTH_SHORT)
            mToast!!.show()
        }
    }

    fun showLong(context: Context, message: CharSequence) {

        if (isShow) {
            if (mToast != null) {
                cancel()
            }
            mToast = createToast(context, message, Toast.LENGTH_LONG)
            mToast!!.show()
        }
    }

    fun showLong(message: CharSequence) {

        if (isShow) {
            if (mToast != null) {
                mToast!!.cancel()
            }
            mToast = createToast(FaceInitData.context, message, Toast.LENGTH_LONG)
            mToast!!.show()
        }
    }


    fun cancel() {
        if (mToast != null) {
            mToast!!.cancel()
        }
    }

    /**
     * 使用同1个toast,避免多toast重复问题
     */
    fun makeText(context: Context?, text: CharSequence, duration: Int): Toast? {
        if (currentToast == null && context != null) {
            currentToast = Toast.makeText(context, text, duration)
            toastView = currentToast!!.view
        } else {
            // try {
            // currentToast.cancel();
            // } catch (Exception e) {
            // }
            // if (mlastContext != context.getApplicationContext()) {
            // currentToast = Toast.makeText(context, text, duration);
            // }
        }
        if (toastView != null) {
            currentToast!!.view = toastView
            currentToast!!.setText(text)
            currentToast!!.duration = duration
        }
        return currentToast
    }

}