package com.mogujie.tt.utils.imageload;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.util.Util;
import com.mogujie.tt.utils.Logger;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Description :图片加载
 * Author :fengjing
 * Email :164303256@qq.com
 * Date :2016/10/28
 */
public class ImageLoader {
    private static Logger logger = Logger.getLogger(ImageLoader.class);
    private static final String TAG = "ImageLoader";

    /**
     * activity中加载图片
     *
     * @param activity
     * @param img
     * @param url
     */
    public static void loadImage(Activity activity, ImageView img, String url, ImageLoaderInterpolator interpolator) {
        if (Util.isOnBackgroundThread() || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            loadImage(activity.getApplicationContext(), img, url, interpolator);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
                logger.w("loadImage: You cannot start a load for a destroyed activity");
            } else {
                loadImage(Glide.with(activity), img, url, interpolator);
            }
        }
    }

    /**
     * fragment中加载图片
     *
     * @param fragment
     * @param img
     * @param url
     */
    public static void loadImage(Fragment fragment, ImageView img, String url, ImageLoaderInterpolator interpolator) {
        if (Util.isOnBackgroundThread() || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            loadImage(fragment.getActivity().getApplicationContext(), img, url, interpolator);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && fragment.getActivity().isDestroyed()) {
                logger.w("loadImage: You cannot start a load for a destroyed activity");
            } else {
                loadImage(Glide.with(fragment), img, url, interpolator);
            }
        }

    }


    /**
     * applicationContext对象加载图片
     *
     * @param context
     * @param img
     * @param url
     */
    public static void loadImage(Context context, ImageView img, String url, ImageLoaderInterpolator interpolator) {
        if (context instanceof Activity) {
            if (Util.isOnBackgroundThread() || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                loadImage(context.getApplicationContext(), img, url, interpolator);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && ((Activity) context).isDestroyed()) {
                    logger.w("loadImage: You cannot start a load for a destroyed activity");
                } else {
                    loadImage(Glide.with((Activity) context), img, url, interpolator);
                }
            }
        } else {
            loadImage(Glide.with(context), img, url, interpolator);
        }

    }

    public static void loadImage(Activity activity, ImageView img, String url) {
        loadImage(activity, img, url, null);
    }

    public static void loadImage(Fragment fragment, ImageView img, String url) {
        loadImage(fragment, img, url, null);
    }

    public static void loadImage(Context context, ImageView img, String url) {
        loadImage(context, img, url, null);
    }

    public static void loadImage(RequestManager manager, ImageView img, String url, ImageLoaderInterpolator interpolator) {
        RequestOptions options = new RequestOptions();
        if (interpolator == null) {
            manager.load(url).into(img);
        } else {
            if (!TextUtils.isEmpty(interpolator.getOssProcess())) {
                url += interpolator.getOssProcess();
            }
            RequestBuilder<Drawable> request = manager.load(url);
            if (interpolator.isLoadGif()) {//是否加载动态图
                if (interpolator.isCenterCrop()) {//是否从中间裁剪
                    options.centerCrop();
                }
                //                if (interpolator.getTransform()!=null){gif图能做transform处理
                //                    typeRequest.transform(interpolator.getTransform());
                //                }
                if (interpolator.getErrorImgId() != 0) {//是否加载错误图片
                    options.error(interpolator.getErrorImgId());
                }
                if (interpolator.getPlaceholderImgId() != 0) {//是否加载占位图
                    options.placeholder(interpolator.getPlaceholderImgId());
                }

            } else {

                if (interpolator.isCenterCrop()) {
                    options.centerCrop();
                }
                if (interpolator.getTransform() != null) {
                    options.transform(interpolator.getTransform());
                }
                if (interpolator.getErrorImgId() != 0) {
                    options.error(interpolator.getErrorImgId());
                }
                if (interpolator.getPlaceholderImgId() != 0) {
                    options.placeholder(interpolator.getPlaceholderImgId());
                }

                if (interpolator.isCacheOriginal()) {
                    options.diskCacheStrategy(DiskCacheStrategy.ALL);
                }
                try {
                    request.into(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            request.apply(options);
            request.into(img);
        }
        logger.v(url);
    }


    //Glide保存图片
    public static void savePicture(Context context, final String filePath, String url) {

    }


    //往SD卡写入文件的方法
    public static void saveFileToSD(String filepath, byte[] bytes) throws Exception {
        File dir1 = new File(filepath);
        if (!dir1.exists()) {
            dir1.mkdirs();
        }
        FileOutputStream output = new FileOutputStream(filepath, false);//这里就不要用openFileOutput了,那个是往手机内存中写数据的
        output.write(bytes);//将bytes写入到输出流中
        output.close(); //关闭输出流
    }

}
