package com.lemo.emojcenter.utils;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.module.GlideModule;

/**
 * Created by wangru
 * Date: 2018/3/29  19:50
 * mail: 1902065822@qq.com
 * describe:
 */

public class FaceGlideModule implements GlideModule {
    public static final int SIZE_Glide_MAX = 200 * 1024 * 1024;

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
//        //设置图片的显示格式PREFER_ARGB_8888,如果怕图片太耗内存，可以改为DecodeFormat.PREFER_RGB_565
//        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
//        int maxMemory = (int) Runtime.getRuntime().maxMemory();
//        // 使用最大可用内存值的1/8作为缓存的大小。
//        int cacheSize = maxMemory / 8;
//        builder.setMemoryCache(new LruResourceCache(cacheSize));//设置内存缓存大小
//        //ExternalCacheDiskCacheFactory:/sdcard/Android/data/<application package>/cache
//        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, "face", SIZE_Glide_MAX));
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {

    }
}