package com.qingmeng.mengmeng.entity

/**
 *  Description :弹框bean

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/5
 */
class SelectDialogBean(
        val industry: List<SelectBean>,
        val capitalList: List<SelectBean>
)

data class SelectBean(
        var name: String,                     //菜单名
        val id: Int = 0,
        var checkState: Boolean = false      //是否点击
)