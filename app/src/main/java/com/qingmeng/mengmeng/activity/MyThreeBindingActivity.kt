package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.view.View
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.view.dialog.DialogCommon
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

    override fun getLayoutId(): Int {
        return R.layout.activity_my_threebinding
    }

    override fun initObject() {
        super.initObject()

        setHeadName(R.string.my_threeBinding)

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
                bindingQQ()
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
                bindingWeChat()
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
                            llMyThreeBindingTips.visibility = View.GONE
                            setData()
                        } else {
                            llMyThreeBindingTips.visibility = View.VISIBLE
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    srlMyThreeBinding.isRefreshing = false
                    llMyThreeBindingTips.visibility = View.VISIBLE
                }, {}, { addSubscription(it) })
    }

    //第三方绑定接口
    private fun httpThreeBinding(type: Int) {
        ApiUtils.getApi()
                .threeBinding(type, "", "", "", MainApplication.instance.TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    srlMyThreeBinding.isRefreshing = false
                    it.apply {
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
//        mQQLoginShare.login(this, { isSuc, qqDataBean ->
//            if (isSuc) {
//                httpThreeBinding(1)
//            } else {
//                ToastUtil.showShort(getString(R.string.my_threeBinding_qq_tips))
//            }
//        })
    }

    //绑定微信
    private fun bindingWeChat() {
//        val mWeChatApi = WXAPIFactory.createWXAPI(this, WX_APPID)
//        if (!mWeChatApi.isWXAppInstalled) {
//            // 提醒用户没有安装微信
//            ToastUtil.showShort(getString(R.string.my_threeBinding_no_wechat))
//            return
//        }
//
//        mWeChatApi.registerApp(WX_APPID)
//        val req = SendAuth.Req()
//        req.scope = "snsapi_userinfo"
//        req.state = "wechat_sdk_demo_test"
//        mWeChatApi.sendReq(req)
    }

    private fun setData() {

    }
}