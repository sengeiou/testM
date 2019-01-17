package com.qingmeng.mengmeng.entity

/**
 *  Description :我的板块用户信息bean

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/14
 */

data class MyInformation(
        val id: Int,
        val avatar: String,
        val state: Int,
        val phone: String,
        val userName: String,
        val userType: Int,
        val myAttention: Int,
        val myFootprint: Int,
        val myComment: Int
)