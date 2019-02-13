package com.qingmeng.mengmeng.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

data class SeachConditionMoneyBean(
        //投资金额
        var capitalList: List<ConditionMoneyBean>
)

@Entity
data class ConditionMoneyBean(
        @Id
        var cacheId: Long = 0,
        val id: Int,
        val name: String,
        var checkState: Boolean = false
)