package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.entity.ThreeBindingBean
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.loginshare.QQThreeLogin
import com.qingmeng.mengmeng.utils.loginshare.bean.WxBean
import com.qingmeng.mengmeng.utils.loginshare.bean.WxInfoBean
import com.qingmeng.mengmeng.utils.loginshare.bean.WxTokenBean
import com.qingmeng.mengmeng.view.dialog.DialogCommon
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import de.greenrobot.event.EventBus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_my_threebinding.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 *  Description :设置 - 第三方绑定

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
@SuppressLint("CheckResult")
class MyThreeBindingActivity : BaseActivity() {
    private lateinit var mDialog: DialogCommon   //弹框
    private var mQQThreeLogin = QQThreeLogin()
    private var mCanClickAgain = true            //是否可以再次点击
    private var mWeChatApi: IWXAPI? = null       //微信api

    override fun getLayoutId(): Int {
        return R.layout.activity_my_threebinding
    }

    override fun initObject() {
        super.initObject()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        setHeadName(R.string.my_threeBinding)
        swipe_target.visibility = View.GONE

//        srlMyThreeBinding.isRefreshing = true
        myDialog.showLoadingDialog()
        httpLoad()
    }

    override fun initListener() {
        super.initListener()

        //下拉刷新
        srlMyThreeBinding.setOnRefreshListener {
            httpLoad()
        }

        //返回
        mBack.setOnClickListener {
            onBackPressed()
        }

        //QQ
        llMyThreeBindingQQ.setOnClickListener {
            //未绑定
            if (tvMyThreeBindingQQ.text.toString() == getString(R.string.my_threeBinding_yes)) {
                mDialog = DialogCommon(this, getString(R.string.tips), getString(R.string.my_threeBinding_qqTips), rightText = getString(R.string.my_threeBinding_untying), onRightClick = {
                    //请求接口
                    httpUnThreeBinding(1)
                })
                mDialog.show()
            } else {  //跳转QQ绑定
                if (mCanClickAgain) {
                    bindingQQ()
                }
            }
        }

        //微信
        llMyThreeBindingWechat.setOnClickListener {
            //未绑定
            if (tvMyThreeBindingWechat.text.toString() == getString(R.string.my_threeBinding_yes)) {
                mDialog = DialogCommon(this, getString(R.string.tips), getString(R.string.my_threeBinding_wechatTips), rightText = getString(R.string.my_threeBinding_untying), onRightClick = {
                    //请求接口
                    httpUnThreeBinding(2)
                })
                mDialog.show()
            } else {  //跳转微信绑定
                if (mCanClickAgain) {
                    bindingWeChat()
                }
            }
        }
    }

    //查询绑定状态
    private fun httpLoad() {
        ApiUtils.getApi()
                .threeBindingState(MainApplication.instance.TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    myDialog.dismissLoadingDialog()
                    srlMyThreeBinding.isRefreshing = false
                    it.apply {
                        if (code == 12000) {
                            swipe_target.visibility = View.VISIBLE
                            llMyThreeBindingTips.visibility = View.GONE
                            data?.let {
                                setData(it)
                            }
                        } else {
                            swipe_target.visibility = View.GONE
                            llMyThreeBindingTips.visibility = View.VISIBLE
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    srlMyThreeBinding.isRefreshing = false
                    swipe_target.visibility = View.VISIBLE
                    llMyThreeBindingTips.visibility = View.VISIBLE
                }, {}, { addSubscription(it) })
    }

    //第三方绑定接口
    private fun httpThreeBinding(type: Int, infoBean: WxInfoBean, token: String) {
        ApiUtils.getApi()
                .threeBinding(type, infoBean.openid, token, infoBean.unionid, MainApplication.instance.TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    srlMyThreeBinding.isRefreshing = false
                    it.apply {
                        myDialog.dismissLoadingDialog()
                        if (code == 12000) {
                            //1.QQ 2.微信
                            when (type) {
                                1 -> {
                                    tvMyThreeBindingQQ.text = getString(R.string.my_threeBinding_yes)
                                    tvMyThreeBindingQQ.setTextColor(resources.getColor(R.color.color_333333))
                                }
                                2 -> {
                                    tvMyThreeBindingWechat.text = getString(R.string.my_threeBinding_yes)
                                    tvMyThreeBindingWechat.setTextColor(resources.getColor(R.color.color_333333))
                                }
                            }
                        } else {
                            ToastUtil.showShort(msg)
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                }, {}, { addSubscription(it) })
    }

    //取消第三方绑定接口
    private fun httpUnThreeBinding(type: Int) {
        myDialog.showLoadingDialog()
        ApiUtils.getApi()
                .unThreeBinding(type, MainApplication.instance.TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    srlMyThreeBinding.isRefreshing = false
                    it.apply {
                        myDialog.dismissLoadingDialog()
                        if (code == 12000) {
                            //1.QQ 2.微信
                            when (type) {
                                1 -> {
                                    tvMyThreeBindingQQ.text = getString(R.string.my_threeBinding_not)
                                    tvMyThreeBindingQQ.setTextColor(resources.getColor(R.color.color_999999))
                                }
                                2 -> {
                                    tvMyThreeBindingWechat.text = getString(R.string.my_threeBinding_not)
                                    tvMyThreeBindingWechat.setTextColor(resources.getColor(R.color.color_999999))
                                }
                            }
                        } else {
                            ToastUtil.showShort(msg)
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                }, {}, { addSubscription(it) })
    }

    //qq绑定
    private fun bindingQQ() {
        mCanClickAgain = false
        myDialog.showLoadingDialog()
        mQQThreeLogin.login(this, IConstants.APP_ID_QQ) { isSuc, qqDataBean ->
            mCanClickAgain = true
            myDialog.dismissLoadingDialog()
            if (isSuc) {
                httpThreeBinding(1, WxInfoBean(openid = qqDataBean?.openid
                        ?: "", headimgurl = "", nickname = ""), qqDataBean?.accessToken ?: "")
            } else {
                ToastUtil.showShort("获取用户信息失败")
            }
        }
    }

    //绑定微信
    private fun bindingWeChat() {
        mCanClickAgain = false
        mWeChatApi = WXAPIFactory.createWXAPI(this, IConstants.APPID_WECHAT)
        if (!mWeChatApi!!.isWXAppInstalled) {
            mCanClickAgain = true
            // 提醒用户没有安装微信
            ToastUtil.showShort(getString(R.string.wechat_no_tips))
            return
        }

        mWeChatApi!!.registerApp(IConstants.APPID_WECHAT)
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo"
        req.state = "wechat_sdk_demo_test"
        mWeChatApi!!.sendReq(req)
        mCanClickAgain = true
    }

    //微信登录
    fun onEvent(wxBean: WxBean) {
        if (!TextUtils.isEmpty(wxBean.code)) {
            EventBus.getDefault().removeStickyEvent(wxBean)
            loginCode(wxBean.code)
        }
    }

    private fun loginCode(code: String) {
        myDialog.showLoadingDialog()
        ApiUtils.getApi()
                .getWeChatToken(IConstants.APPID_WECHAT, IConstants.SECRET_WECHAT, code)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it != null && !TextUtils.isEmpty(it.access_token)) {
                        getWxInfo(it)
                    } else {
                        myDialog.dismissLoadingDialog()
                        ToastUtil.showShort("获取用户信息失败")
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    ToastUtil.showShort("获取用户信息失败")
                }, {}, { addSubscription(it) })
    }

    private fun getWxInfo(tokenBean: WxTokenBean) {
        ApiUtils.getApi()
                .getWeChatInfo(tokenBean.access_token, tokenBean.openid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it != null && !TextUtils.isEmpty(it.openid)) {
                        httpThreeBinding(2, it, tokenBean.access_token)
                    } else {
                        myDialog.dismissLoadingDialog()
                        ToastUtil.showShort("获取用户信息失败")
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    ToastUtil.showShort("获取用户信息失败")
                }, {}, { addSubscription(it) })
    }

    private fun setData(threeBindingBean: ThreeBindingBean) {
        when (threeBindingBean.qqBinding) {
            0 -> {
                tvMyThreeBindingQQ.text = getString(R.string.my_threeBinding_yes)
                tvMyThreeBindingQQ.setTextColor(resources.getColor(R.color.color_333333))
            }
            1 -> {
                tvMyThreeBindingQQ.text = getString(R.string.my_threeBinding_not)
                tvMyThreeBindingQQ.setTextColor(resources.getColor(R.color.color_999999))
            }
        }
        when (threeBindingBean.wxBinding) {
            0 -> {
                tvMyThreeBindingWechat.text = getString(R.string.my_threeBinding_yes)
                tvMyThreeBindingWechat.setTextColor(resources.getColor(R.color.color_333333))
            }
            1 -> {
                tvMyThreeBindingWechat.text = getString(R.string.my_threeBinding_not)
                tvMyThreeBindingWechat.setTextColor(resources.getColor(R.color.color_999999))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mQQThreeLogin.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        //解除第三方微信的引用，彻底关闭此aty
        mWeChatApi?.detach()
    }
}