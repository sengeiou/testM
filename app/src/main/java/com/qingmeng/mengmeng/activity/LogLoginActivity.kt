package com.qingmeng.mengmeng.activity

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_log_login.*

import kotlinx.android.synthetic.main.layout_head.*
import org.json.JSONObject
import java.util.regex.Pattern

/**
 * Created by mingyue
 * Date: 2019/1/9
 * mail: 153705849@qq.com
 * describe: 登录页面
 */
class LogLoginActivity : BaseActivity() {
    //登录模式,密码登录1,短信验证2
    var loginmode: Int = 1

    override fun getLayoutId(): Int {

        //设置标题
        setHeadName(getString(R.string.login))
        //获取验证隐藏
        tv_login_get_code.visibility = View.GONE
        //默认隐藏返回按钮
        var mboolean = false
        setShowBack(mboolean)
        return R.layout.activity_log_login
    }

    //初始化Object
    override fun initObject() {
        super.initObject()
    }
    //初始化Listener
    override fun initListener() {
        super.initListener()
        //密码文本框内容监听
        setEditTextContentListener(edt_login_pwd)
        //密码焦点监听
        setOnFocusChangeListener(edt_login_pwd)
        //返回
        mBack.setOnClickListener {
            this.finish()
        }

        //使用短信验证登录
        tv_login_sms_login.setOnClickListener {
            loginmode = 2
            tv_login_get_code.visibility = View.VISIBLE
        }
        //忘记密码
        tv_login_forget_password.setOnClickListener {

        }
        //获取验证码，默认隐藏
        tv_login_get_code.setOnClickListener {

        }
        //登录
        btn_login.setOnClickListener {
            var username = edt_login_account.text.toString()
            var password = edt_login_pwd.text.toString()
            when {
                TextUtils.isEmpty(username) -> ToastUtil.showShort(getString(R.string.user_name_empty))
                TextUtils.isEmpty(password) -> ToastUtil.showShort(getString(R.string.password_empty))
            //密码登录/短信登录
                loginmode == 1 -> accountlogin(username, password)
                loginmode == 2 -> msmlogin(username, password)
            }

        }
        //QQ登录
        tv_login_other_login_QQ.setOnClickListener {

        }
        //微信登录
        tv_login_other_login_wechat.setOnClickListener {

        }
        //立即注册
        tv_login_sign_up_now.setOnClickListener {

        }
    }


    /**
     *账号登录
     * username:用户名
     * password：密码
     */
    private fun accountlogin(username: String, password: String) {
        ApiUtils.getApi()
                .accountlogin(username, password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    val readText = it.toString()
                    val jsonObject = JSONObject(readText)
                    val code = jsonObject.optInt("code")
                    val msg = jsonObject.optString("msg")
                    when (code) {
                    //登录成功
                        12000 -> {
                            ToastUtil.showShort(getString(R.string.login_success))
                            this.finish()
                        }
                    //错误次数
                        15001 -> {
                            val count = jsonObject.optInt("data")
                            ToastUtil.showShort("$msg,还有${count}次机会")
//                            if (count == 0) {
//                                ToastUtil.showShort(getString(R.string.login_fail_recover_password))
//
//                            }
                        }
                        //密码错误三次以上
                        25094 -> {
                            ToastUtil.showShort(msg)
                            //找回密码弹窗
                        }
                    //参数有误
                        13000 -> {
                            ToastUtil.showShort(msg)
                        }
                    //用户名不存在
                        25092 -> {
                            ToastUtil.showShort(msg)
                        }
                    //手机号不存在
                        25091 -> {
                            //提示“该手机号尚未注册，是否前去注册？” “注册”和“取消”两个按钮
                            this.finish()
                        }
                    }
                })
    }

    /**
     *短信登录
     * phone:手机号
     * msmCode：短信验证码
     */
    private fun msmlogin(phone: String, msmCode: String) {
        ApiUtils.getApi()
                .accountlogin(phone, msmCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    val readText = it.toString()
                    val jsonObject = JSONObject(readText)
                    val code = jsonObject.optInt("code")
                    val msg = jsonObject.optString("msg")
                    when (code) {
                    //手机号没有注册
                        25088 -> {
                            ToastUtil.showShort(getString(R.string.phone_not_registered))
                            this.finish()
                        }
                    //登录成功
                        12000 -> {
                            ToastUtil.showShort(getString(R.string.login_success))
                            this.finish()
                        }
                    //参数有误
                        13000 -> {
                            ToastUtil.showShort(msg)
                        }
                    //验证码不正确
                        10000 -> {
                            ToastUtil.showShort(msg)
                        }
                    //验证码不正确
                        15002 -> {
                            ToastUtil.showShort(msg)
                        }
                    }
                })
    }

    /**
     *文本框内容监听
     */
    private fun setEditTextContentListener(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var temp: CharSequence? = null
            private var editStart: Int = 0
            private var editEnd: Int = 0

            // 输入文本之前的状态
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                temp = s
            }

            // 输入文字中的状态，count是一次性输入字符数
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            // 输入文字后的状态
            override fun afterTextChanged(s: Editable) {
                editStart = editText.selectionStart
                editEnd = editText.selectionEnd
                // 限制最大输入字数
                if (temp!!.length > 12) {
                    s.delete(editStart - 1, editEnd)
                    val tempSelection = editStart
                    editText.text = s
                    editText.setSelection(tempSelection)
                    ToastUtil.showShort(getString(R.string.passMax_tips))
                }
            }
        })
    }

    /**
     *文本框焦点监听
     */
    private fun setOnFocusChangeListener(editText: EditText) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {

                //失去焦点
            } else {
                if (editText.text.toString().trim().isNotBlank()) {
                    if (checkPass(editText.text.toString().trim())) {

                    } else {
                        ToastUtil.showShort(getString(R.string.passFormat_tips))
                    }
                }
            }
        }
    }

    /**
     * 6到12位区分大小写密码
     */
    private fun checkPass(pass: String): Boolean {
        val pattern = Pattern.compile("^[a-zA-Z0-9]{6,12}$")
        val matcher = pattern.matcher(pass)
        return matcher.matches()
    }
}