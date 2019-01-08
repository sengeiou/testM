package com.qingmeng.mengmeng.activity

import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.view.dialog.DialogCommon
import kotlinx.android.synthetic.main.activity_my_settings.*
import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.startActivity

/**
 *  Description :我的 - 设置

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MySettingsActivity : BaseActivity() {
    private lateinit var mDialog: DialogCommon   //弹框

    override fun getLayoutId(): Int {
        return R.layout.activity_my_settings
    }

    override fun initObject() {
        super.initObject()

        //设置标题
        setHeadName(getString(R.string.setting))
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            this.finish()
        }

        //用户
        llMySettingsUser.setOnClickListener {
            startActivity<MySettingsUserActivity>()
        }

        //设置或修改密码
        llMySettingsUpdatePassword.setOnClickListener {
            startActivity<MySettingsSetOrUpdatePasswordActivity>("title" to tvMySettingsNewOrOldPassword.text as String)
        }

        //换绑手机
        llMySettingsUpdatePhone.setOnClickListener {
            startActivity<MySettingsUpdatePhoneActivity>()
        }

        //清理缓存
        llMySettingsClearCache.setOnClickListener {
            mDialog = DialogCommon(this, getString(R.string.tips), getString(R.string.clearCache_tips), onRightClick = {
                ToastUtil.showShort("确定")
            })
            mDialog.show()
        }

        //关于我们
        llMySettingsAboutUs.setOnClickListener {
            startActivity<MySettingsAboutUsActivity>()
        }

        //退出账号
        tvMySettingsExitUser.setOnClickListener {
            mDialog = DialogCommon(this, getString(R.string.tips), getString(R.string.exitApp_tips), onRightClick = {
                ToastUtil.showShort("确定")
            })
            mDialog.show()
        }
    }
}