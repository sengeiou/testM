package com.qingmeng.mengmeng.utils.loginshare

import AppManager
import android.app.Activity
import android.content.Intent
import com.app.common.by.Weak
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.loginshare.bean.SinaUserBean
import com.sina.weibo.sdk.auth.AccessTokenKeeper
import com.sina.weibo.sdk.auth.Oauth2AccessToken
import com.sina.weibo.sdk.auth.WbConnectErrorMessage
import com.sina.weibo.sdk.auth.sso.SsoHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/**
 * Created by wr
 * Date: 2018/12/10  15:09
 * describe:
 */
class SinaThreeLogin {
    private var mActivity: Activity? by Weak()
    private var mSsoHandler: SsoHandler? = null

    private var mCallback: ((isSuc: Boolean, errorInfo: String?, accessToken: Oauth2AccessToken?) -> Unit)? = null
    private var mCallbackUser: ((isSuc: Boolean, errorInfo: String?, accessToken: Oauth2AccessToken?, sinaUserBean: SinaUserBean?) -> Unit)? = null

    private var mAccessToken: Oauth2AccessToken? = null

    fun login(activity: Activity, callback: (isSuc: Boolean, errorInfo: String?, accessToken: Oauth2AccessToken?) -> Unit) {
        mActivity = activity
        mCallback = callback
        mSsoHandler = SsoHandler(mActivity)
        mSsoHandler?.authorize(SelfWbAuthListener())
    }

    fun login(activity: Activity, callbackUser: (isSuc: Boolean, errorInfo: String?, accessToken: Oauth2AccessToken?, sinaUserBean: SinaUserBean?) -> Unit) {
        mActivity = activity
        mCallbackUser = callbackUser
        mSsoHandler = SsoHandler(mActivity)
        mSsoHandler?.authorize(SelfWbAuthListener())
    }

    private inner class SelfWbAuthListener : com.sina.weibo.sdk.auth.WbAuthListener {
        override fun onSuccess(token: Oauth2AccessToken) {
            mActivity?.runOnUiThread(java.lang.Runnable {
                mAccessToken = token
                if (mAccessToken?.isSessionValid() ?: false) {
                    AccessTokenKeeper.writeAccessToken(AppManager.instance.currentActivity(), mAccessToken)
                    mCallbackUser?.let {
                        getUserInfo()
                    }
                    mCallback?.invoke(true, null, mAccessToken)
                } else {
                    mCallback?.invoke(false, "新浪微博授权失败", null)
                    mCallbackUser?.invoke(false, "新浪微博授权失败", null, null)
                }
            })
        }

        override fun cancel() {
            ToastUtil.showShort("新浪微博取消授权")
            mCallback?.invoke(false, "新浪微博取消授权", null)
            mCallbackUser?.invoke(false, "新浪微博取消授权", null, null)
        }

        override fun onFailure(errorMessage: WbConnectErrorMessage) {
            ToastUtil.showShort("新浪微博授权失败")
            mCallback?.invoke(false, errorMessage.errorMessage, null)
            mCallbackUser?.invoke(false, errorMessage.errorMessage, null, null)
        }
    }


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mSsoHandler?.authorizeCallBack(requestCode, resultCode, data)
    }

    /**
     * //获取新浪
    @GET("https://api.weibo.com/2/users/show.json")
    fun getSinaInfo(@Query("access_token") access_token: String, @Query("uid") uid: String): Observable<SinaUserBean>
     */
    private fun getUserInfo() {
        mAccessToken?.let { accessToken ->
            ApiUtils.getApi()
                    .getSinaInfo(accessToken.token, accessToken.uid)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        mCallbackUser?.invoke(true, null, accessToken, it)
                    }, {
                        mCallbackUser?.invoke(true, it.message, accessToken, null)
                    })
        }
    }
}