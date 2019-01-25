package com.mogujie.tt.utils.imageload;

import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.leimo.wanxin.R;

/**
 * Description :
 * Author :fengjing
 * Email :164303256@qq.com
 * Date :2016/10/28
 */
public class ImageLoaderInterpolator {
    /**
     * 圆角圆形处理等
     */
    private BitmapTransformation transform;
    /**
     * 加载错误时显示的图片id
     */
    private int errorImgId;
    /**
     * 加载时的占位图id
     */
    private int placeholderImgId;
    /**
     * 是否需要从中间裁剪
     */
    private boolean centerCrop;
    /**
     * 是否加载gif图 <默认不加载></>
     */
    private boolean loadGif;
    /**
     * 图片加载监听 只有加载bitmap图片时有效 gif图片无效
     */

    /**
     * 是否缓存原图
     */
    private boolean isCacheOriginal;

    private static final int IMAGE_LOAD_DEFAULT_RES = R.drawable.tt_default_image;
    /**
     * oss处理
     */
    private String ossProcess;

    public BitmapTransformation getTransform() {
        return transform;
    }

    public ImageLoaderInterpolator setTransform(BitmapTransformation transform) {
        this.transform = transform;
        return this;
    }

    public int getErrorImgId() {
        return errorImgId;
    }

    public ImageLoaderInterpolator setErrorImgId(int errorImgId) {
        this.errorImgId = errorImgId;
        return this;
    }

    public int getPlaceholderImgId() {
        if (placeholderImgId == 0) {
            placeholderImgId = IMAGE_LOAD_DEFAULT_RES;
        }
        return placeholderImgId;
    }

    public ImageLoaderInterpolator setPlaceholderImgId(int placeholderImgId) {
        this.placeholderImgId = placeholderImgId;
        return this;
    }

    public boolean isCenterCrop() {
        return centerCrop;
    }

    public ImageLoaderInterpolator setCenterCrop(boolean centerCrop) {
        this.centerCrop = centerCrop;
        return this;
    }

    public boolean isLoadGif() {
        return loadGif;
    }

    public ImageLoaderInterpolator setLoadGif(boolean loadGif) {
        this.loadGif = loadGif;
        return this;
    }

    public String getOssProcess() {
        return ossProcess;
    }

    public ImageLoaderInterpolator setOssProcess(String ossProcess) {
        this.ossProcess = ossProcess;
        return this;
    }

    public ImageLoaderInterpolator setCacheOriginal(boolean isCacheOriginal) {
        this.isCacheOriginal = isCacheOriginal;
        return this;
    }

    public boolean isCacheOriginal() {
        return isCacheOriginal;
    }
}
