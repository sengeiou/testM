package com.qingmeng.mengmeng.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

data class SeachConditionMoneyBean(
        //投资金额
        val capitalList: List<CapitalList>
)

@Entity
data class CapitalList(
        @Id
        var cacheId: Long = 0,
        var id: Int,
        var name: String,
        var checkState: Boolean = false
)