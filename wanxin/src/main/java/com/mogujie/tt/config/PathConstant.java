package com.mogujie.tt.config;

import android.content.Context;

import com.mogujie.tt.utils.SDPathUtil;

/**
 * Created by wangru
 * Date: 2017/12/16  12:50
 * mail: 1902065822@qq.com
 * describe:
 */

public class PathConstant {
    public static final String PATH_MAIN_NAME = "mengmeng";

    public interface Log{
        String SendMsg="sendMsg";
    }
    public static String getVideoThumbnailDir(Context context) {
        return SDPathUtil.getSDCardPrivateCacheDir(context, PATH_MAIN_NAME + "/thumbnail");
    }
}
