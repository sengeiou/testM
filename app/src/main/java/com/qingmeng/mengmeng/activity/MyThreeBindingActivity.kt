package com.qingmeng.mengmeng.activity

import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.view.dialog.DialogCommon
import kotlinx.android.synthetic.main.activity_my_threebinding.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 *  Description :设置 - 第三方绑定

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MyThreeBindingActivity : BaseActivity() {
    private lateinit var mDialog: DialogCommon   //弹框

    override fun getLayoutId(): Int {
        return R.layout.activity_my_threebinding
    }

    override fun initObject() {
        super.initObject()

        setHeadName(getString(R.string.my_threeBinding))
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            this.finish()
        }

        //QQ
        llMyThreeBindingQQ.setOnClickListener {
            //未绑定
            if (tvMyThreeBindingQQ.text.toString() == getString(R.string.my_threeBinding_yes)) {
                mDialog = DialogCommon(this, getString(R.string.tips), getString(R.string.my_threeBinding_qqTips), rightText = getString(R.string.my_threeBinding_untying), onRightClick = {
                    tvMyThreeBindingQQ.text = getString(R.string.my_threeBinding_not)
                    tvMyThreeBindingQQ.setTextColor(resources.getColor(R.color.color_999999))
                })
                mDialog.show()
            } else {  //跳转QQ绑定

            }
        }

        //微信
        llMyThreeBindingWechat.setOnClickListener {
            //未绑定
            if (tvMyThreeBindingWechat.text.toString() == getString(R.string.my_threeBinding_yes)) {
                mDialog = DialogCommon(this, getString(R.string.tips), getString(R.string.my_threeBinding_wechatTips), rightText = getString(R.string.my_threeBinding_untying), onRightClick = {
                    tvMyThreeBindingWechat.text = getString(R.string.my_threeBinding_not)
                    tvMyThreeBindingWechat.setTextColor(resources.getColor(R.color.color_999999))
                })
                mDialog.show()
            } else {  //跳转微信绑定

            }
        }
    }
}