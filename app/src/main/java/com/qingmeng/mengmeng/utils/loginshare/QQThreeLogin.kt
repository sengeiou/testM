package com.qingmeng.mengmeng.utils.loginshare

import android.app.Activity
import android.content.Intent
import com.app.common.json.GsonUtil
import com.app.common.logger.Logger
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.loginshare.bean.QQDataBean
import com.qingmeng.mengmeng.utils.loginshare.bean.QQUserInfoBean
import com.tencent.connect.UserInfo
import com.tencent.connect.common.Constants
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError


/**
 * Created by wr
 * Date: 2018/12/6  17:19
 * describe:
 */
class QQThreeLogin {
    private lateinit var mActivity: Activity
    private var APP_ID_QQ = ""
    private var mCallbackToken: ((isSuc: Boolean, qqDataBean: QQDataBean?) -> Unit)? = null
    private var mCallback: ((isSuc: Boolean, qqDataBean: QQDataBean?, qqUserInfoBean: QQUserInfoBean?) -> Unit)? = null

    private lateinit var mTencent: Tencent
    private val mLoginQQListener = BaseUiListener()
    private var mIsServerSideLogin = false
    private var mInfo: UserInfo? = null

    fun login(activity: Activity, appId: String, callback: (isSuc: Boolean, qqDataBean: QQDataBean?, qqUserInfoBean: QQUserInfoBean?) -> Unit) {
        this.APP_ID_QQ = appId
        this.mActivity = activity
        this.mCallback = callback
        login()
    }

    fun login(activity: Activity, appId: String, callback: (isSuc: Boolean, qqDataBean: QQDataBean?) -> Unit) {
        this.APP_ID_QQ = appId
        this.mActivity = activity
        this.mCallbackToken = callback
        login()
    }

    private fun login() {
        mTencent = Tencent.createInstance(APP_ID_QQ, mActivity)
        mTencent.login(mActivity, "all", mLoginQQListener)
        //        if (!mTencent.isSessionValid()) {
//            mTencent.login(mActivity, "all", mLoginQQListener)
//            mIsServerSideLogin = false
//        } else {
//            if (mIsServerSideLogin) { // Server-Side 模式的登陆, 先退出，再进行SSO登陆
//                mTencent.logout(mActivity)
//                mTencent.login(mActivity, "all", mLoginQQListener)
//                mIsServerSideLogin = false
//                Log.d("SDKQQAgentPref", "FirstLaunch_SDK:" + SystemClock.elapsedRealtime())
//                return
//            }
//            mTencent.logout(mActivity)
//        }
    }

    private fun getQQUserInfo(qqDataBean: QQDataBean) {
        mTencent.setAccessToken(qqDataBean.accessToken, qqDataBean.expiresIn.toString())
        mTencent.openId = qqDataBean.openid

        mInfo = UserInfo(mActivity, mTencent.qqToken)
        mInfo?.getUserInfo(object : IUiListener {
            override fun onComplete(data: Any?) {
                val qqUserBean = GsonUtil().fromJson(data?.toString(), QQUserInfoBean::class.java)
                Logger.d("onComplete#获取信息")
                mCallback?.invoke(true, qqDataBean, qqUserBean)
            }

            override fun onCancel() {
                mCallback?.invoke(false, qqDataBean, null)
            }

            override fun onError(e: UiError?) {
                mCallback?.invoke(false, qqDataBean, null)
            }
        })
    }


    private inner class BaseUiListener : IUiListener {
        override fun onComplete(data: Any?) {
            val qqDataBean = GsonUtil().fromJson(data?.toString(), QQDataBean::class.java)
            mCallback?.let {
                getQQUserInfo(qqDataBean)
            }
            mCallbackToken?.invoke(true, qqDataBean)
        }

        override fun onError(e: UiError?) {
            mCallback?.invoke(false, null, null)
            mCallbackToken?.invoke(false, null)
            Logger.e("onError#${e?.errorMessage}")
            ToastUtil.showShort("QQ登录异常")
        }

        override fun onCancel() {
            mCallback?.invoke(false, null, null)
            mCallbackToken?.invoke(false, null)
            ToastUtil.showShort("取消")
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_LOGIN || requestCode == Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode, resultCode, data, mLoginQQListener)
        }
    }
}