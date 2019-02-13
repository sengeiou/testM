/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bither.util;

import android.graphics.*;
import android.media.ExifInterface;
import com.leimo.imgcompress.CompressImage;

import java.io.*;

/**
 * 压缩图片
 * 只支持cpu 为arm架构的
 * net.bither.util 目录名字不能变
 * https://github.com/yuyunhai/weixinyasuo
 */
public class NativeUtil {
    public static final String TAG = NativeUtil.class.getSimpleName();
    private static final int DEFAULT_QUALITY = 90;
    private static final int DEFAULT_LENGTH_MAX = 300;
    private static final int IMAGE_HEIGHT_MAX = 1280;
    private static final int IMAGE_WIDTH_MAX = 1280;
    private int mQuality = 90;
    private int mLengthMax = 300;
    private int mImageHeightMax = 1280;
    private int mImageWidthMax = 1280;
    private CompressImage mCompressImage;

    public NativeUtil(CompressImage compressImage) {
        this.mCompressImage = mCompressImage;
        if (compressImage.getQuality() > 0) {
            mQuality = compressImage.getQuality();
        }
        if (compressImage.getLengthMax() > 0) {
            mLengthMax = compressImage.getLengthMax();
        }
        if (compressImage.getWidthMax() > 0) {
            mImageWidthMax = compressImage.getWidthMax();
        }
        if (compressImage.getHeightMax() > 0) {
            mImageHeightMax = compressImage.getHeightMax();
        }
    }

    /**
     * 加载lib下两个so文件
     */
    static {
        try {
            System.loadLibrary("jpegbither");
            System.loadLibrary("bitherjni");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用底层 bitherlibjni.c中的方法
     *
     * @param bit
     * @param w
     * @param h
     * @param quality
     * @param fileNameBytes
     * @param optimize
     * @return
     * @Description:函数描述
     */
    public static native String compressBitmap(Bitmap bit, int w, int h, int quality, byte[] fileNameBytes,
                                               boolean optimize);

    /**
     * @param bit      bitmap对象
     * @param fileName 指定保存目录名
     * @param optimize 是否采用哈弗曼表数据计算 品质相差5-10倍
     * @Description: JNI基本压缩
     */
    public void compressBitmap(Bitmap bit, String fileName, boolean optimize) {
        saveBitmap(bit, mQuality, fileName, optimize);
    }


    public void compressBitmap(Bitmap bit, int quality, String fileName,
                               boolean optimize) {
        if (bit.getConfig() != Bitmap.Config.ARGB_8888) {
            Bitmap result = null;
            result = Bitmap.createBitmap(bit.getWidth(), bit.getHeight(),
            Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            Rect rect = new Rect(0, 0, bit.getWidth(), bit.getHeight());
            canvas.drawBitmap(bit, null, rect, null);
            saveBitmap(result, quality, fileName, optimize);
            result.recycle();
        } else {
            saveBitmap(bit, quality, fileName, optimize);
        }
    }


    /**
     * 调用native方法
     *
     * @param bit
     * @param quality
     * @param fileName
     * @param optimize
     * @Description:函数描述
     */
    public void saveBitmap(Bitmap bit, int quality, String fileName, boolean optimize) {
        compressBitmap(bit, bit.getWidth(), bit.getHeight(), quality, fileName.getBytes(), optimize);
    }


    /**
     * 计算缩放比
     *
     * @param bitWidth  当前图片宽度
     * @param bitHeight 当前图片高度
     * @return int 缩放比
     */
    public int getRatioSize(int bitWidth, int bitHeight) {

        // 缩放比
        int ratio = 1;
        // 缩放比,由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        if (bitWidth > bitHeight && bitWidth > mImageWidthMax) {
            // 如果图片宽度比高度大,以宽度为基准
            ratio = bitWidth / mImageWidthMax;
        } else if (bitWidth < bitHeight && bitHeight > mImageHeightMax) {
            // 如果图片高度比宽度大，以高度为基准
            ratio = bitHeight / mImageHeightMax;
        }
        // 最小比率为1
        if (ratio <= 0) {
            ratio = 1;
        }
        return ratio;
    }


    /**
     * @param bitmap   bitmap对象
     * @param filePath 要保存的指定目录
     * @Description: 通过JNI图片压缩把Bitmap保存到指定目录
     */
    public void compressBitmap(Bitmap bitmap, String filePath) {
        // 获取尺寸压缩倍数
        int ratio = getRatioSize(bitmap.getWidth(), bitmap.getHeight());
        // 压缩Bitmap到对应尺寸
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth() / ratio, bitmap.getHeight() / ratio, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Rect rect = new Rect(0, 0, bitmap.getWidth() / ratio, bitmap.getHeight() / ratio);
        canvas.drawBitmap(bitmap, null, rect, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        result.compress(Bitmap.CompressFormat.JPEG, options, baos);
        // 循环判断如果压缩后图片是否大于200kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > mLengthMax) {
            // 重置baos即清空baos
            baos.reset();
            // 每次都减少10
            options -= 10;
            // 这里压缩options%，把压缩后的数据存放到baos中
            result.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        // JNI保存图片到SD卡 这个关键
        saveBitmap(result, options, filePath, true);
        // 释放Bitmap
        if (!result.isRecycled()) {
            result.recycle();
        }
    }

    /**
     * @param curFilePath    当前图片文件地址
     * @param targetFilePath 要保存的图片文件地址
     * @Description: 通过JNI图片压缩把Bitmap保存到指定目录
     */
    public void compressBitmap(String curFilePath, String targetFilePath) {
        //根据地址获取bitmap
        Bitmap result = getBitmapFromFile(curFilePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int quality = 100;
        result.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
        int sub = 10;
        while (baos.toByteArray().length / 1024 > mLengthMax) {
            // 重置baos即清空baos
            baos.reset();
            if (sub > 6) {
                sub--;
            }
            quality -= sub;
            // 这里压缩quality，把压缩后的数据存放到baos中
            result.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }
        // JNI保存图片到SD卡 这个关键
        saveBitmap(result, quality, targetFilePath, true);
        // 释放Bitmap
        if (!result.isRecycled()) {
            result.recycle();
        }
    }

    /**
     * 通过文件路径读获取Bitmap防止OOM以及解决图片旋转问题
     */
    public Bitmap getBitmapFromFile(String filePath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;//只读边,不读内容
        BitmapFactory.decodeFile(filePath, newOpts);
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 获取尺寸压缩倍数
        newOpts.inSampleSize = getRatioSize(w, h);
        newOpts.inJustDecodeBounds = false;//读取所有内容
        newOpts.inDither = false;
        newOpts.inPurgeable = true;
        newOpts.inInputShareable = true;
        newOpts.inTempStorage = new byte[32 * 1024];
        Bitmap bitmap = null;
        File file = new File(filePath);
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (fs != null) {
                bitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, newOpts);
                //旋转图片
                int photoDegree = readPictureDegree(filePath);
                if (photoDegree != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(photoDegree);
                    // 创建新的图片
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */

    public int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }


}
