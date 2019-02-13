package com.mogujie.tt.config

/**
 * @author : yingmu on 15-1-5.
 * @email : yingmu@mogujie.com.
 */
object DBConstant {

    /**
     * 性别
     * 1. 男性 2.女性
     */
    const val SEX_MAILE = 1
    const val SEX_FEMALE = 2

    /**
     * msgType
     */
    const val MSG_TYPE_SINGLE_TEXT = 0x01
    const val MSG_TYPE_SINGLE_AUDIO = 0x02
    const val MSG_TYPE_GROUP_TEXT = 0x11
    const val MSG_TYPE_GROUP_AUDIO = 0x12

    /**
     * msgDisplayType
     * 保存在DB中，与服务端一致，图文混排也是一条
     * 1. 最基础的文本信息
     * 2. 纯图片信息
     * 3. 语音
     * 4. 图文混排
     * 5. 视频
     */
    //    public final int SHOW_ORIGIN_TEXT_TYPE = 1;
    //    public final int  SHOW_IMAGE_TYPE = 2;
    //    public final int  SHOW_AUDIO_TYPE = 3;
    //    public final int  SHOW_MIX_TEXT = 4;
    //    public final int  SHOW_GIF_TYPE = 5;

    const val SHOW_ORIGIN_TEXT_TYPE = 1//文本
    const val SHOW_IMAGE_TYPE = 2//图片
    const val SHOW_AUDIO_TYPE = 3//语音
    const val SHOW_GIF_TYPE = 4//表情
    const val SHOW_VIDEO_TYPE = 5//视频
    const val SHOW_FILE_TYPE = 6//文件
    const val SHOW_REVOKE_TYPE = 8 //撤回

    const val SHOW_MIX_TEXT = 10


    const val DISPLAY_FOR_IMAGE = "[图片]"
    const val DISPLAY_FOR_MIX = "[图文消息]"
    const val DISPLAY_FOR_AUDIO = "[语音]"
    const val DISPLAY_FOR_VIDEO = "[视频]"
    const val DISPLAY_FOR_ERROR = "[未知消息]"


    /**
     * sessionType
     */
    const val SESSION_TYPE_SINGLE = 1
    const val SESSION_TYPE_GROUP = 2
    const val SESSION_TYPE_ERROR = 3

    /**
     * user status
     * 1. 试用期 2. 正式 3. 离职 4.实习
     */
    const val USER_STATUS_PROBATION = 1
    const val USER_STATUS_OFFICIAL = 2
    const val USER_STATUS_LEAVE = 3
    const val USER_STATUS_INTERNSHIP = 4

    /**
     * group type
     */
    const val GROUP_TYPE_NORMAL = 1
    const val GROUP_TYPE_TEMP = 2

    /**
     * group status
     * 1: shield  0: not shield
     */

    const val GROUP_STATUS_ONLINE = 0
    const val GROUP_STATUS_SHIELD = 1

    /**
     * group change Type
     */
    const val GROUP_MODIFY_TYPE_ADD = 0
    const val GROUP_MODIFY_TYPE_DEL = 1

    /**
     * depart status Type
     */
    const val DEPT_STATUS_OK = 0
    const val DEPT_STATUS_DELETE = 1

}
