package com.qingmeng.mengmeng.activity

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_my_settings_setorupdate.*
import kotlinx.android.synthetic.main.layout_head.*
import java.util.regex.Pattern

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

        //设置密码标题
        if (intent.getStringExtra("title") == getString(R.string.my_settings_setPassword)) {
            setHeadName(getString(R.string.my_settings_setPassword))
            mIsSetPass = true
            etMySettingsSetOrUpdateOld.visibility = View.GONE
        } else {//修改密码
            setHeadName(getString(R.string.my_settings_updatePassword))
            mIsSetPass = false
        }
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            this.finish()
        }

        //内容输入监听
        setEditTextContentListener(etMySettingsSetOrUpdateOld)
        setEditTextContentListener(etMySettingsSetOrUpdateNew)
        setEditTextContentListener(etMySettingsSetOrUpdateNewTwo)
        //焦点监听
        setOnFocusChangeListener(etMySettingsSetOrUpdateOld)
        setOnFocusChangeListener(etMySettingsSetOrUpdateNew)
        setOnFocusChangeListener(etMySettingsSetOrUpdateNewTwo)

        //保存
        tvMySettingsSetOrUpdateSave.setOnClickListener {
            //设置密码就直接给旧密码随便赋些值
            val oldPass = if (mIsSetPass) {
                "1836111"
            } else {
                etMySettingsSetOrUpdateOld.text.toString().trim()
            }
            val newPass = etMySettingsSetOrUpdateNew.text.toString().trim()
            val newPassTwo = etMySettingsSetOrUpdateNewTwo.text.toString().trim()
            if (oldPass.isNotBlank() && newPass.isNotBlank() && newPassTwo.isNotBlank()) {
                if (!checkPass(oldPass) || !checkPass(newPass) || !checkPass(newPassTwo)) {
                    ToastUtil.showShort(getString(R.string.passMax_tips))
                } else {
                    if (newPass == newPassTwo) {
                        if (mIsSetPass) {
                            //设置密码
                            setPassHttp(newPass, newPassTwo)
                        } else {
                            //修改密码
                            updatePassHttp(oldPass, newPass, newPassTwo)
                        }
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

    //设置密码接口
    private fun setPassHttp(newPass: String, newPassTwo: String) {
        ApiUtils.getApi()
                .updatePass("", newPass, newPassTwo, "")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.code == 12000) {
                        ToastUtil.showShort(getString(R.string.updatePass_success))
                        this.finish()
                    } else if (it.code == 13000) {
                        ToastUtil.showShort(it.msg)
                    }
                }, {

                })
    }

    //修改密码接口
    private fun updatePassHttp(oldPass: String, newPass: String, newPassTwo: String) {
        ApiUtils.getApi()
                .updatePass(oldPass, newPass, newPassTwo, "")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.code == 12000) {
                        ToastUtil.showShort(getString(R.string.updatePass_success))
                        this.finish()
                    } else if (it.code == 13000) {
                        ToastUtil.showShort(it.msg)
                    }
                }, {

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
     * 6到16位区分大小写密码
     */
    private fun checkPass(pass: String): Boolean {
        val pattern = Pattern.compile("^[a-zA-Z0-9]{6,12}$")
        val matcher = pattern.matcher(pass)
        return matcher.matches()
    }
}