package com.qingmeng.mengmeng.entity

/**
 *  Description :我的 - 我的关注和我的足迹bean

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/7
 */

data class MyMyFollowBean(
        val pageNum: Int,
        val pageSize: Int,
        val size: Int,
        val startRow: Int,
        val endRow: Int,
        val total: Int,
        val pages: Int,
        val list: List<MyFollow>
)

data class MyFollow(
        val name: String,
        val logo: String,
        val foodName: String,
        val capitalName: String
)