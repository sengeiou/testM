package com.leimo.imgcompress;

import android.text.TextUtils;
import android.util.Log;
import com.leimo.imgcompress.utils.FileType;

import java.io.File;

/**
 * Created by wangru
 * Date: 2018/6/5  16:20
 * mail: 1902065822@qq.com
 * describe:
 */

public class CompressImage {
    private static final String TAG = "CompressImage";
    public String path;
    public String pathCompress;
    public int widthMax;
    public int heightMax;
    public int lengthMax;
    public int quality;
    //FileType 图片格式
    public FileType format;

    /**
     * 压缩图片
     */
    public String compress() {
        if (TextUtils.isEmpty(getPath()) || !new File(getPath()).exists()) {
            Log.d(TAG, "compress: 压缩文件不存在");
            return null;
        }
       return ImageCompressUtil.compressImageByPath(this);
    }

    private CompressImage(Builder builder) {
        setPath(builder.path);
        setPathCompress(builder.pathCompress);
        setWidthMax(builder.widthMax);
        setHeightMax(builder.heightMax);
        setLengthMax(builder.lengthMax);
        setQuality(builder.quality);
        setFormat(builder.format);
    }

    public int getWidthMax() {
        return widthMax;
    }

    public void setWidthMax(int widthMax) {
        this.widthMax = widthMax;
    }

    public int getHeightMax() {
        return heightMax;
    }

    public void setHeightMax(int heightMax) {
        this.heightMax = heightMax;
    }

    public int getLengthMax() {
        return lengthMax;
    }

    public void setLengthMax(int lengthMax) {
        this.lengthMax = lengthMax;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPathCompress() {
        return pathCompress;
    }

    public void setPathCompress(String pathCompress) {
        this.pathCompress = pathCompress;
    }

    public FileType getFormat() {
        return format;
    }

    public void setFormat(FileType format) {
        this.format = format;
    }

    public static final class Builder {
        private int widthMax;
        private int heightMax;
        private int lengthMax;
        private int quality;
        private FileType format;
        private String path;
        private String pathCompress;

        public Builder() {
        }

        public Builder widthMax(int widthMax) {
            this.widthMax = widthMax;
            return this;
        }

        public Builder heightMax(int heightMax) {
            this.heightMax = heightMax;
            return this;
        }

        public Builder lengthMax(int lengthMax) {
            this.lengthMax = lengthMax;
            return this;
        }

        public Builder quality(int quality) {
            this.quality = quality;
            return this;
        }

        public Builder format(FileType format) {
            this.format = format;
            return this;
        }

        public CompressImage build() {
            return new CompressImage(this);
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder pathCompress(String pathCompress) {
            this.pathCompress = pathCompress;
            return this;
        }
    }
}
