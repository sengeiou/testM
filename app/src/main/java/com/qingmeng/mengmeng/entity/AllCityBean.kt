package com.qingmeng.mengmeng.entity

/**
 *  Description :所有城市bean

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/15
 */

data class AllCityBean(
        val city: List<AllCity>
)

data class AllCity(
        val id: String,
        val name: String,
        val fatherId: Int,
        val level: Int
)