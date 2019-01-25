package com.qingmeng.mengmeng.activity

import android.app.Activity
import android.content.Intent
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.GlideCacheUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.getLoacalBitmap
import com.qingmeng.mengmeng.utils.imageLoader.CacheType
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import com.qingmeng.mengmeng.view.dialog.DialogCommon
import kotlinx.android.synthetic.main.activity_my_settings.*
import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

/**
 *  Description :我的 - 设置

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MySettingsActivity : BaseActivity() {
    private lateinit var mDialog: DialogCommon   //弹框
    private val REQUEST_MY_SETTINGS = 9264       //下一页返回数据的requestCode
    private var mPhone = ""                      //上个页面传过来的手机号
    private var mIsUpdatePass = false            //上个页面传过来的是否是修改密码
    private var mPhoneChange = false             //上个页面传过来的手机号是否改变过

    override fun getLayoutId(): Int {
        return R.layout.activity_my_settings
    }

    override fun initObject() {
        super.initObject()

        //设置标题
        setHeadName(getString(R.string.setting))
        mPhone = intent.getStringExtra("phone")
        mIsUpdatePass = intent.getBooleanExtra("isUpdatePass", false)
        //设置头像
        GlideLoader.load(this, intent.getStringExtra("avatar"), ivMySettingsHead, cacheType = CacheType.All)
        //设置用户名
        tvMySettingsUserName.text = intent.getStringExtra("userName")

        //修改密码
        if (mIsUpdatePass) {
            tvMySettingsNewOrOldPassword.text = getString(R.string.my_settings_updatePassword)
        } else {    //设置密码
            tvMySettingsNewOrOldPassword.text = getString(R.string.my_settings_setPassword)
        }

        //设置缓存大小
        tvMySettingsCache.text = GlideCacheUtils.getCacheSize(this)
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            onBackPressed()
        }

        //用户
        llMySettingsUserInformation.setOnClickListener {
            startActivityForResult<MySettingsUserActivity>(REQUEST_MY_SETTINGS)
        }

        //设置或修改密码
        llMySettingsUpdatePassword.setOnClickListener {
            startActivityForResult<MySettingsSetOrUpdatePasswordActivity>(REQUEST_MY_SETTINGS, "isUpdatePass" to mIsUpdatePass)
        }

        //换绑手机
        llMySettingsUpdatePhone.setOnClickListener {
            startActivityForResult<MySettingsUpdatePhoneActivity>(REQUEST_MY_SETTINGS, "phone" to mPhone)
        }

        //清理缓存
        llMySettingsClearCache.setOnClickListener {
            mDialog = DialogCommon(this, getString(R.string.tips), getString(R.string.clearCache_tips), onRightClick = {
                GlideCacheUtils.clearImageAllCache(this)
                tvMySettingsCache.text = getString(R.string.clearCache_defaultSize)
                ToastUtil.showShort("清除成功")
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MY_SETTINGS && resultCode == Activity.RESULT_OK) {
            val phone = data?.getStringExtra("phone")
            val localPath = data?.getStringExtra("mLocalPath")
            val isSetPass = data?.getBooleanExtra("isSetPass", false) ?: false
            if (!phone.isNullOrBlank() || !localPath.isNullOrBlank() || isSetPass) {
                if (!phone.isNullOrBlank()) {
                    mPhone = phone!!
                }
                if (!localPath.isNullOrBlank()) {
                    val bitmap = getLoacalBitmap(localPath!!)
                    ivMySettingsHead.setImageBitmap(bitmap)
                }
                if (isSetPass) {
                    mIsUpdatePass = true
                }
                //设置返回时告诉上一个页面刷新
                mPhoneChange = true
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("mPhoneChange", mPhoneChange)
        })
        super.onBackPressed()
    }
}