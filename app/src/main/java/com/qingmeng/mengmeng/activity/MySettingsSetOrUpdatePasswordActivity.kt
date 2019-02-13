package com.qingmeng.mengmeng.activity

import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.InputCheckUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_my_settings_setorupdate.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 *  Description :设置 - 设置或修改密码

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MySettingsSetOrUpdatePasswordActivity : BaseActivity() {
    private var mIsSetPass = true  //是否是设置密码

    override fun getLayoutId(): Int {
        return R.layout.activity_my_settings_setorupdate
    }

    override fun initObject() {
        super.initObject()

        //修改密码
        if (intent.getBooleanExtra("isUpdatePass", false)) {
            setHeadName(R.string.my_settings_updatePassword)
            mIsSetPass = false
        } else {    //设置密码
            setHeadName(R.string.my_settings_setPassword)
            mIsSetPass = true
            etMySettingsSetOrUpdateOld.setHint(R.string.username)
            //设置输入类型 默认
            etMySettingsSetOrUpdateOld.inputType = InputType.TYPE_CLASS_TEXT
            ivMySettingsSetOrUpdateIcon.setImageResource(R.mipmap.my_settings_updatepass_user)
        }
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            onBackPressed()
        }

        //根据格式 设置不同输入监听
        if (mIsSetPass) {
            setEditTextContentListener(etMySettingsSetOrUpdateOld, false)
        } else {
            setEditTextContentListener(etMySettingsSetOrUpdateOld)
        }
        //内容输入监听
        setEditTextContentListener(etMySettingsSetOrUpdateNew)
        setEditTextContentListener(etMySettingsSetOrUpdateNewTwo)

        //根据格式 设置不同焦点监听
        if (mIsSetPass) {
            setOnFocusChangeListener(etMySettingsSetOrUpdateOld, false)
        } else {
            setOnFocusChangeListener(etMySettingsSetOrUpdateOld)
        }
        //焦点监听
        setOnFocusChangeListener(etMySettingsSetOrUpdateNew)
        setOnFocusChangeListener(etMySettingsSetOrUpdateNewTwo)

        //保存
        tvMySettingsSetOrUpdateSave.setOnClickListener {
            //设置密码就直接给旧密码随便赋些值
            val oldPass = etMySettingsSetOrUpdateOld.text.toString().trim()
            val newPass = etMySettingsSetOrUpdateNew.text.toString().trim()
            val newPassTwo = etMySettingsSetOrUpdateNewTwo.text.toString().trim()
            //设置密码判断
            if (mIsSetPass) {
                if (oldPass.isNotBlank() && newPass.isNotBlank() && newPassTwo.isNotBlank()) {
                    if (!InputCheckUtils.checkPass6_16(newPass) || !InputCheckUtils.checkPass6_16(newPassTwo)) {
                        ToastUtil.showShort(getString(R.string.passFormat_tips))
                    } else {
                        if (newPass == newPassTwo) {
                            //设置密码
                            setPassHttp(oldPass, newPassTwo)
                        } else {
                            ToastUtil.showShort(getString(R.string.passDissimilarity_tips))
                        }
                    }
                } else {
                    if (oldPass.isBlank()) {
                        ToastUtil.showShort(getString(R.string.please_input_user_name))
                    } else if (newPass.isBlank() || newPassTwo.isBlank()) {
                        ToastUtil.showShort(getString(R.string.passNew_tips))
                    }
                }
            } else {    //修改密码
                if (oldPass.isNotBlank() && newPass.isNotBlank() && newPassTwo.isNotBlank()) {
                    if (!InputCheckUtils.checkPass6_16(oldPass) || !InputCheckUtils.checkPass6_16(newPass) || !InputCheckUtils.checkPass6_16(newPassTwo)) {
                        ToastUtil.showShort(getString(R.string.passFormat_tips))
                    } else {
                        if (newPass == newPassTwo) {
                            //修改密码
                            updatePassHttp(oldPass, newPass, newPassTwo)
                        } else {
                            ToastUtil.showShort(getString(R.string.passDissimilarity_tips))
                        }
                    }
                } else {
                    if (oldPass.isBlank()) {
                        ToastUtil.showShort(getString(R.string.passSystemIn_tips))
                    } else if (newPass.isBlank() || newPassTwo.isBlank()) {
                        ToastUtil.showShort(getString(R.string.passNew_tips))
                    }
                }
            }
        }
    }

    //设置密码接口
    private fun setPassHttp(name: String, pass: String) {
        myDialog.showLoadingDialog()
        ApiUtils.getApi()
                .setPass(name, pass, MainApplication.instance.TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    myDialog.dismissLoadingDialog()
                    if (it.code == 12000) {
                        ToastUtil.showShort(getString(R.string.setPass_success))
                        setResult(Activity.RESULT_OK, Intent().apply {
                            putExtra("isSetPass", true)
                        })
                        onBackPressed()
                    } else {
                        ToastUtil.showShort(it.msg)
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                })
    }

    //修改密码接口
    private fun updatePassHttp(oldPass: String, newPass: String, newPassTwo: String) {
        myDialog.showLoadingDialog()
        ApiUtils.getApi()
                .updatePass(oldPass, newPass, newPassTwo, MainApplication.instance.TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    myDialog.dismissLoadingDialog()
                    if (it.code == 12000) {
                        ToastUtil.showShort(getString(R.string.updatePass_success))
                        onBackPressed()
                    } else {
                        ToastUtil.showShort(it.msg)
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                })
    }

    /**
     * 文本框内容监听
     */
    private fun setEditTextContentListener(editText: EditText, isPass: Boolean = true) {
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
                    if (isPass) {
                        ToastUtil.showShort(getString(R.string.passMax_tips))
                    } else {  //设置用户名限制提示
                        ToastUtil.showShort(getString(R.string.userNameMax_tips))
                    }
                }

            }
        })
    }

    /**
     * 文本框焦点监听
     */
    private fun setOnFocusChangeListener(editText: EditText, isPass: Boolean = true) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            //失去焦点
            if (!hasFocus) {
                if (editText.text.toString().trim().isNotBlank()) {
                    //密码提示
                    if (isPass) {
                        if (!InputCheckUtils.checkPass6_16(editText.text.toString().trim())) {
                            ToastUtil.showShort(getString(R.string.passFormat_tips))
                        }
                    } else {  //设置用户名限制提示
                        if (!InputCheckUtils.checkString4_6(editText.text.toString().trim())) {
                            ToastUtil.showShort(getString(R.string.userNameFormat_tips))
                        }
                    }
                }
            }
        }
    }
}