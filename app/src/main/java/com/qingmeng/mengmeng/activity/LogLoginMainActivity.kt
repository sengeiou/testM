package com.qingmeng.mengmeng.activity

import android.os.Build
import android.support.annotation.RequiresApi
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.constant.ImageCodeHandler
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.GeetestUtil
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_log_login.*
import kotlinx.android.synthetic.main.activity_log_main_login.*
import kotlinx.android.synthetic.main.activity_log_password_login.*

import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import org.json.JSONObject
import java.util.regex.Pattern

/**
 * Created by mingyue
 * Date: 2019/1/11
 * mail: 153705849@qq.com
 * describe: 登录页面首页
 */
class LogLoginMainActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        //设置标题
        setHeadName(getString(R.string.login))
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
    btn_login_pw_main.setOnClickListener {     startActivity<LogLoginpwActivity>()  }
        //短信验证登录
        btn_login_sms_main.setOnClickListener { startActivity<LogLogincodeActivity>()  }
        //立即注册
        tv_login_sign_up_now_main.setOnClickListener {  startActivity<RegisterActivity>()  }
        //微信登录
        tv_login_other_login_wechat_main.setOnClickListener {  }
        //qq登录
        tv_login_other_login_qq_main.setOnClickListener {  }

    }

}