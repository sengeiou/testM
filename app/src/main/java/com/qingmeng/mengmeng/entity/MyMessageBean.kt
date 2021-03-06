package com.qingmeng.mengmeng.entity

import com.mogujie.tt.imservice.entity.RecentInfo
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/**
 *  Description :我的 - 消息bean

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/9
 */
class MyMessageBean : RecentInfo() {
    val chatInfoList: List<MyMessage> = listOf(MyMessage())
}

@Entity
class MyMessage {
    @Id
    var id: Long = 0
    val name: String = ""
    val avatar: String = ""
    val wxUid: Int = 0
}