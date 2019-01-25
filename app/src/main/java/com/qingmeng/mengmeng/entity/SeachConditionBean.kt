package com.qingmeng.mengmeng.entity

data class SeachConditionBean(
        //投资金额
        val capitalList: List<ConditionBean>,
        //加盟模式
        val joinModes: List<ConditionBean>
)

data class ConditionBean(
        val id: Int,
        val name: String
)