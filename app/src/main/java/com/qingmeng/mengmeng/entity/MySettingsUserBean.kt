package com.qingmeng.mengmeng.entity

/**
 *  Description :设置用户信息bean

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/14
 */

data class MySettingsUserBean(
    val avatar: String,
    val name: String,
    val sex: Int,
    val phone: String,
    val telephone: String,
    val wx: String,
    val qq: String,
    val email: String,
    val fax: String,
    val provinceId: Int,
    val cityId: Int,
    val address: String,
    val capital: String,
    val industryOfInterest: String
)