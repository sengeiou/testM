package com.qingmeng.mengmeng.adapter

/**
 *  Description :聊天type

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/9
 */
object ChatType {
    val CHAT_TYPE_TIME = 551         //消息时间
    val CHAT_TYPE_BRAND = 552        //第一次进入的品牌详情
    val CHAT_TYPE_OTHER = 553        //别人消息
    val CHAT_TYPE_MINE = 554         //自己消息
    /**
     * ==============自己的消息==============
     */
    val CHAT_TYPE_MINE_TETX = 5550    //文本
    val CHAT_TYPE_MINE_IMAGE = 5551   //图片
    val CHAT_TYPE_MINE_AUDIO = 5552   //语音
    val CHAT_TYPE_MINE_VIDEO = 5553   //视频
    val CHAT_TYPE_MINE_REVOKE = 5554  //撤回
    /**
     * ==============別人的消息==============
     */
    val CHAT_TYPE_OTHER_TEXT = 5555
    val CHAT_TYPE_OTHER_IMAGE = 5556
    val CHAT_TYPE_OTHER_AUDIO = 5557
    val CHAT_TYPE_OTHER_VIDEO = 5558
    val CHAT_TYPE_OTHER_REVOKE = 5559
}