package com.mogujie.tt.config

/**
 * Created by wr
 * Date: 2019/1/10  9:36
 * mail: 1902065822@qq.com
 * describe:
 * MessageEntity content里面 extInfo扩展字段
 */
object MessageExtConst {
    //本地扩展
    const val LOADSTATUS = "loadStatus"
    const val READSTATUS = "readStatus"
    //发送扩展
    const val MSGID = "wxMsgId"
    //图片
    const val IMG_URL = "imgUrl"
    const val IMG_PATH = "imgPath"
    const val IMG_WIDTH = "imgWidth"
    const val IMG_HEIGHT = "imgHeight"

    //视频
    const val VIDEO_URL = "videoUrl"
    const val VIDEO_PATH = "videoPath"
    const val VIDEO_DURATION = "videoDuration"
    const val VIDEO_THUMB_WIDTH = "thumbWidth"//int
    const val VIDEO_THUMB_HEIGHT = "thumbHeight"//int
    const val VIDEO_THUMB_PATH = "thumbPath"
    const val VIDEO_THUMB_URL = "thumbUrl"

    //语音
    const val AUDIO_URL = "audioUrl"
    const val AUDIO_PATH = "audioPath"
    const val AUDIO_DURATION = "audioDuration"

    //撤回消息
    const val CMD_TIME = "wxTimer"
}