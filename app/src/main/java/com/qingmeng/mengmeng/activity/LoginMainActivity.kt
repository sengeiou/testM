package com.qingmeng.mengmeng.activity

import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_log_main_login.*
import org.jetbrains.anko.startActivity

/**
 * Created by mingyue
 * Date: 2019/1/11
 * mail: 153705849@qq.com
 * describe: 登录页面首页
 */
class LoginMainActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        //设置标题
        //     setHeadName(getString(R.string.login))
        return R.layout.activity_log_main_login
    }

    //初始化Object
    override fun initObject() {
        super.initObject()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //初始化Listener
    override fun initListener() {
        super.initListener()

        //账号密码登录
        btn_login_pw_main.setOnClickListener { startActivity<LoginpwActivity>() }
        //短信验证登录
        btn_login_sms_main.setOnClickListener { startActivity<LogincodeActivity>() }
        //立即注册
        tv_login_sign_up_now_main.setOnClickListener { startActivity<LoginRegisterActivity>() }
        //微信登录
        tv_login_other_login_wechat_main.setOnClickListener { }
        //qq登录
        tv_login_other_login_qq_main.setOnClickListener { }
        setBGABannerLogin()
    }

    //设置登录页面BGAbanner
    fun setBGABannerLogin() {
        ApiUtils.getApi().getbanner("1.0", 5)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->

                    //请求成功
                    if (bean.code == 12000) {
                        ToastUtil.showShort(bean.msg)
                    } else {
                        ToastUtil.showShort(bean.msg)

                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

}

