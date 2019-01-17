package com.qingmeng.mengmeng.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.constant.ImageCodeHandler
import com.qingmeng.mengmeng.fragment.JoinFragment
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.GeetestUtil
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login_change_password.*
import kotlinx.android.synthetic.main.layout_head.*
import org.json.JSONObject
import java.util.regex.Pattern


/**
 * Created by mingyue
 * Date: 2019/1/11
 * mail: 153705849@qq.com
 * describe: 忘记密码
 */
class LoginChangePswActivity : BaseActivity() {
    override fun getLayoutId(): Int {

        return R.layout.activity_login_change_password
    }

    //初始化Object
    override fun initObject() {
        super.initObject()
        imgHandler = ImageCodeHandler(this, tv_get_code_change_psw)
        GeetestUtil.init(this)
    }


    //初始化Listener
    @RequiresApi(Build.VERSION_CODES.O)
    override fun initListener() {
        //设置标题
        setHeadName(getString(R.string.retrieve_password))
        super.initListener()
        //返回
        mBack.setOnClickListener {
            this.finish()
        }
        //点击页面其他地方取消EditText的焦点并且隐藏软键盘
        mlogchangepsw.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (null != this@LoginChangePswActivity.getCurrentFocus()) {
                    //点击取消EditText的焦点
                    mlogchangepsw.setFocusable(true);
                    mlogchangepsw.setFocusableInTouchMode(true);
                    mlogchangepsw.requestFocus();
                    /** * 点击空白位置 隐藏软键盘  */
                    val mInputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    return mInputMethodManager!!.hideSoftInputFromWindow(this@LoginChangePswActivity.getCurrentFocus()!!.getWindowToken(), 0)
                }
                return false
            }
        })
        edt_input_phone_change_psw.setText(sharedSingleton.getString(IConstants.LOGIN_PHONE))
        //密码焦点监听
        setOnFocusChangeListener(edt_input_new_psw_change_psw)
        setOnFocusChangeListener(edt_input_sure_new_psw_change_psw)
        //密码框输入监听
        //当输入确认密码后确定按钮改变
        edt_input_sure_new_psw_change_psw.addTextChangedListener(object : TextWatcher {
            // 输入文本之前的状态
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            // 输入文字中的状态，count是一次性输入字符数
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (edt_input_phone_change_psw.text.toString().trim().isNotBlank() && edt_input_code_change_psw.text.toString().trim().isNotBlank() && edt_input_new_psw_change_psw.text.toString().trim().isNotBlank() && edt_input_sure_new_psw_change_psw.text.toString().trim().isNotBlank()) {
                    btn_sure_change_psw.setBackgroundColor(Color.parseColor("#5ab1e1"))
                } else {
                    btn_sure_change_psw.setBackgroundColor(Color.parseColor("#dcdcdc"))
                }
            }

            // 输入文字后的状态
            override fun afterTextChanged(s: Editable) {
            }
        })
        //验证码输入监听
        //当输入验证码后确定按钮改变
        edt_input_code_change_psw.addTextChangedListener(object : TextWatcher {
            // 输入文本之前的状态
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            // 输入文字中的状态，count是一次性输入字符数
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (edt_input_phone_change_psw.text.toString().trim().isNotBlank() && edt_input_code_change_psw.text.toString().trim().isNotBlank() && edt_input_new_psw_change_psw.text.toString().trim().isNotBlank() && edt_input_sure_new_psw_change_psw.text.toString().trim().isNotBlank()) {
                    btn_sure_change_psw.setBackgroundColor(Color.parseColor("#5ab1e1"))
                } else {
                    btn_sure_change_psw.setBackgroundColor(Color.parseColor("#dcdcdc"))
                }
            }

            // 输入文字后的状态
            override fun afterTextChanged(s: Editable) {
            }
        })
        //获取验证码
        tv_get_code_change_psw.setOnClickListener {
            hasRegistered(edt_input_phone_change_psw.text.toString())
        }
        //确定
        btn_sure_change_psw.setOnClickListener {
            forgetpsw(edt_input_phone_change_psw.text.toString(), edt_input_code_change_psw.text.toString(), edt_input_new_psw_change_psw.text.toString(), edt_input_sure_new_psw_change_psw.text.toString())
        }


    }

    //验证手机号是否注册
    private fun hasRegistered(phone: String) {

        ApiUtils.getApi().hasRegistered(phone, 2)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    //已注册
                    if (bean.code == 25089) {
                        GeetestUtil.customVerity({ checkCodeType() }, { sendSmsCode(it) })
                    } else {
                        ToastUtil.showShort(bean.msg)

                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    //校验极验是否可用
    private fun checkCodeType() {
        ApiUtils.getApi().checkCodeType()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    when {
                        bean.code == 12000 -> {
                            bean.data!!.new_captcha = true
                            GeetestUtil.showGeetest(bean.data!!.toJson())
                        }
                        bean.code == 25080 -> {
                            GeetestUtil.dismissGeetestDialog()
                            showImgCode()
                        }
                        else -> {
                            GeetestUtil.dismissGeetestDialog()
                            ToastUtil.showShort(bean.msg)
                        }
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    //展示图片验证码
    private fun showImgCode() {
        myDialog.showImageCodeDialog(edt_input_phone_change_psw.text.toString(), 2,
                { addSubscription(it) }, { imgHandler.sendEmptyMessage(timing) })
    }

    /**
     * 发送短信验证码
     * @param type 1极验验证  0图片验证码
     **/
    private fun sendSmsCode(result: String) {
        val phone = edt_input_phone_change_psw.text.toString()
        val params = JSONObject(result)
        ApiUtils.getApi().sendSms(phone, 3, geetest_challenge = params.optString("geetest_challenge"),
                geetest_validate = params.optString("geetest_validate"), geetest_seccode = params.optString("geetest_seccode"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.code == 12000) {
                        imgHandler.sendEmptyMessage(timing)
                        GeetestUtil.showSuccessDialog()
                    } else {
                        GeetestUtil.showFailedDialog()
                        ToastUtil.showShort(it.msg)
                    }
                }, {
                    GeetestUtil.showFailedDialog()
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    /**
     *忘记密码
     * phone:手机号
     * msmCode：短信验证码
     * password:新密码
     * surepassword：确认密码
     */
    private fun forgetpsw(phone: String, smsCode: String, password: String, surepassword: String) {
        ApiUtils.getApi()
                .forgetpassword(phone, smsCode, password, surepassword)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    when (bean.code) {
                    //手机号没有注册
                        25088 -> {
                            ToastUtil.showShort(getString(R.string.phone_not_registered))
                        }
                    //登录成功
                        12000 -> {
                            bean.data?.let {
                                MainApplication.instance.user = it
                                MainApplication.instance.TOKEN = it.token
                                it.upDate()
                            }
                            sharedSingleton.setString(IConstants.LOGIN_PHONE, phone)
                            sharedSingleton.setString(IConstants.LOGIN_PSW, password)
                            ToastUtil.showShort(getString(R.string.login_success))
                         //   this.finish()
                            //登录成功跳转首页
//                            val i = Intent()
//                            i.setClass(this@LoginChangePswActivity, JoinFragment::class.java!!)
//                            startActivity(i)


                        }
                    //参数有误
                        13000 -> {
                            ToastUtil.showShort("错误")
                            //  ToastUtil.showShort(bean.msg)
                        }
                    //验证码不正确
                        10000 -> {
                            //   ToastUtil.showShort("错误2")
                            ToastUtil.showShort(bean.msg)
                        }
                    //验证码不正确
                        15002 -> {
                            //      ToastUtil.showShort("错误3")
                            ToastUtil.showShort(bean.msg)
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

    override fun onDestroy() {
        super.onDestroy()
        GeetestUtil.destroy()
    }
}