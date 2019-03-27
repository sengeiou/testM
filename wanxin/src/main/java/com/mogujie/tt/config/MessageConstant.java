package com.mogujie.tt.config;

/**
 * @author : yingmu on 15-1-11.
 * @email : yingmu@mogujie.com.
 */
public interface MessageConstant {

    /**
     * 基础消息状态，表示网络层收发成功
     */
    public final int MSG_SENDING = 1;
    public final int MSG_FAILURE = 2;
    public final int MSG_SUCCESS = 3;

    /**
     * 图片、语音、视频要上传阿里云os的消息(这些消息就根据这个状态发送判断)，未读 已读 上传成功 上传失败 上传中
     */
    public final int UP_OSS_UNREAD = 1;
    public final int UP_OSS_READED = 2;
    public final int UP_OSS_SUCCESS = 3;
    public final int UP_OSS_FAILURE = 4;
    public final int UP_OSS_LOADING = 5;

    /**
     * 图片消息的前后常量
     */
    public final String IMAGE_MSG_START = "&$#@~^@[{:";
    public final String IMAGE_MSG_END = ":}]&$~@#@";

}
