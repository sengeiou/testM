package com.qingmeng.mengmeng.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/**
 * Created by fyf on 2019/1/18
 * 头报文章列表Bean
 */
data class NewsPagerListBean(
        val data: List<NewsPagerList>
)

@Entity
data class NewsPagerList(
        @Id
        var cacheId: Long = 0,
        val id: Int,
        val banner: String,
        val title: String,
        val xmUrl: String,
        val createTime: String,
        val content: String,
        val formatTime: String,
        val articleUrl: String
)