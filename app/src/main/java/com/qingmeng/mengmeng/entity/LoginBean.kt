package com.qingmeng.mengmeng.entity
/**
 *  Description :登录接口数据

 *  Author:mingyue

 *  Email:153705849@qq.com

 *  Date: 2019/1/10
 */

data class Data(
        val userInfo: UserInfo
)


data class UserInfo(
        val avatar: String,
        val id: Int,
        val lastLoginTime: Long,
        val nickname: String,
        val phone: String,
        val qqOpenid: String,
        val qqToken: String,
        val token: String,
        val type: Int,
        val userName: String,
        val weChatOpenid: String,
        val weChatToken: String,
        val weChatUnionid: String,
        val wxName: String
)