package com.qingmeng.mengmeng.entity

data class ShareBean(
        val WeChat: List<WeChat>,
        val WeChatCircle: List<WeChatCircle>,
        val microBlog: List<MicroBlog>,
        val qq: List<Qq>
)

data class WeChat(
        val content: String,
        val icon: String,
        val title: String,
        val url: String
)

data class MicroBlog(
        val content: String,
        val icon: String,
        val title: String,
        val url: String
)

data class WeChatCircle(
        val content: String,
        val icon: String,
        val title: String,
        val url: String
)

data class Qq(
        val content: String,
        val icon: String,
        val title: String,
        val url: String
)