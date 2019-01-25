package com.mogujie.tt.imutils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.utils.ImageLoaderUtil;

/**
 * Created by wangru
 * Date: 2017/8/30  13:46
 * mail: 1902065822@qq.com
 * describe:
 */
public class IMUtil {
    public static final String TAG = IMUtil.class.getSimpleName();

    public static void init(Context context) {
        Log.i(TAG, "start IMService");
        //开启服务
        Intent intent = new Intent();
        intent.setClass(context, IMService.class);
        context.startService(intent);
        //设置ImageLoader
        ImageLoaderUtil.initImageLoaderConfig(context);
    }
}
