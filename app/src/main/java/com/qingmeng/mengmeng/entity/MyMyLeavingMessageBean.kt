package com.qingmeng.mengmeng.entity

/**
 *  Description :我的留言bean

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/17
 */

data class MyMyLeavingMessageBean(
        val data: List<MyLeavingMessage>
)
data class MyLeavingMessage(
        val id: Int,
        val message: String,
        val status: Int,
        val createTime: String,
        val brandName: String,
        val logo: String,
        val capitalName: String,
        val storesNum: String
)