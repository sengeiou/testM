package com.qingmeng.mengmeng.activity

import android.content.Context
import android.view.View
import com.lemo.emojcenter.FaceInitData
import com.mogujie.tt.config.IntentConstant
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.InputCheckUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_my_settings_aboutus.*
import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.startActivity

/**
 *  Description :设置 - 关于我们

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MySettingsAboutUsActivity : BaseActivity() {
    private var clickNum = 0
    private var CheckNum = false

    override fun getLayoutId(): Int {
        return R.layout.activity_my_settings_aboutus
    }

    override fun initObject() {
        super.initObject()

        setHeadName(R.string.my_settings_aboutUs)

        //设置版本号
        tvMySettingsAboutUsVersion.text = tvMySettingsAboutUsVersion.text.toString() + getLocalVersion(this)
        etChat.setText("${MainApplication.instance.user.wxUid}")
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            onBackPressed()
        }

        tvMySettingsAboutUsVersion.setOnClickListener {
            clickNum++
            if (clickNum >= 10) {
                etChat.visibility = View.VISIBLE
                btnChat.visibility = View.VISIBLE
            }
        }

        btnChat.setOnClickListener {
            if (InputCheckUtils.checkFiveOrSixNum(etChat.text.toString().trim())) {
                if (etChat.text.toString().trim() == "${MainApplication.instance.user.wxUid}") {
                    ToastUtil.showShort("这是你自己的id")
                } else {
                    FaceInitData.init(applicationContext)
                    FaceInitData.setAlias("${MainApplication.instance.user.wxUid}")
                    startActivity<MyMessageChatActivity>(IntentConstant.KEY_SESSION_KEY to "1_${etChat.text.toString().trim()}")
                }
            } else {
                if (CheckNum) {
                    ToastUtil.showShort("逗我呢")
                } else {
                    CheckNum = true
                    ToastUtil.showShort("5-6位数字啊老哥")
                }
            }
        }
    }

    //版本
    private fun getLocalVersion(context: Context): String {
        val packageInfo = context.applicationContext
                .packageManager
                .getPackageInfo(context.packageName, 0)
        return packageInfo.versionName
    }
}