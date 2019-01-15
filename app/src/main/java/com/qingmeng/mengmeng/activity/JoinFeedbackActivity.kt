package com.qingmeng.mengmeng.activity

import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.entity.SelectDialogBean
import com.qingmeng.mengmeng.view.dialog.SelectDialog
import kotlinx.android.synthetic.main.activity_join_feedback.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 * Created by leimo on 2019/1/14.
 */
class JoinFeedbackActivity: BaseActivity() {
    private lateinit var mBottomDialog: SelectDialog
    override fun getLayoutId(): Int {

        return R.layout.activity_join_feedback
    }

    override fun initData() {
        //设置标题
        setHeadName(getString(R.string.join_feedback))
        //标题栏提交
        mMenu.setText(getString(R.string.submit))
    }

    //初始化Listener
    override fun initListener() {
        super.initListener()

        btn_join_feedback.setOnClickListener {
            //菜单内容
            val menuList = ArrayList<SelectDialogBean>()
            menuList.add(SelectDialogBean(getString(R.string.join_feedback_type1)))
            menuList.add(SelectDialogBean(getString(R.string.join_feedback_type2)))
            menuList.add(SelectDialogBean(getString(R.string.join_feedback_type3)))
            menuList.add(SelectDialogBean(getString(R.string.join_feedback_type4)))
            mBottomDialog = SelectDialog(this, menuList, onItemClick = {
                //******************
            })
            mBottomDialog.show()
        }
    }
}