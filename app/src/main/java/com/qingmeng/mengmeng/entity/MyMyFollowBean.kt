package com.qingmeng.mengmeng.entity

/**
 *  Description :我的 - 我的关注和我的足迹bean

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/7
 */

data class MyMyFollowBean(
        val data: List<MyFollow>
)

data class MyFollow(
        val id: Int,
        val brandId: Int,
        val name: String,
        val brandName: String,
        val logo: String,
        val foodName: String,
        val attentionId: Int,
        val capitalName: String
)