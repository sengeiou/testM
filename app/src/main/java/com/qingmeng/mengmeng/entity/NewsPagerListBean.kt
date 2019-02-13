package com.qingmeng.mengmeng.entity

/**
 * Created by fyf on 2019/1/18
 * 头报文章列表Bean
 */
data class NewsPagerListBean(
        val data: List<NewsPagerList>
)

data class NewsPagerList(
        val id: Int,
        val banner: String,
        val title: String,
        val xmUrl: String,
        val createTime: String,
        val content: String,
        val formatTime: String

)