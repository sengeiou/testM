package com.qingmeng.mengmeng.entity

class ShareBean {
    val WeChat = ShareContent()
    val WeChatCircle = ShareContent()
    val microBlog = ShareContent()
    val qq = ShareContent()
}

class ShareContent {
    val content: String = ""
    val icon: String = ""
    val title: String = ""
    val url: String = ""
}
