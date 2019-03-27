package com.mogujie.tt.api.request;

/**
 * Created by wangru
 * Date: 2017/12/13  11:01
 * mail: 1902065822@qq.com
 * describe:
 */

public class ApiUrl {
//    public static final String IP_SERVICE = "http://www.wxjishu.com:9999/";
    public static final String IP_SERVICE_DEBUG = "http://47.99.139.155:11000/";
    public static final String IP_SERVICE_RELEASE = "";

    /**
     * 上传文件
     */
//    public static final String UPLOAD_FILE = IP_SERVICE + "file/upload";
    public static final String UPLOAD_FILE = IP_SERVICE_DEBUG + "api/file/upload_voice";

    /**
     * 上传图片
     */
    public static final String UPLOAD_IMAGE = IP_SERVICE_DEBUG + "file/uploadimg";

    /**
     * 上传语音
     */
    public static final String UPLOAD_AUDIO = IP_SERVICE_DEBUG + "file/uploadvoice";
}
