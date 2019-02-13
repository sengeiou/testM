package com.leimo.imgcompress.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片工具
 * Created by as on 2017/3/13.
 */

public class BitmapUtil {
    private static final String TAG = "BitmapUtil";
    private static final int IMG_MAX_WIDTH = 1280;//尺寸压缩图片最大宽度(px)
    private static final int IMG_MAX_HEIGHT = 12080;//尺寸压缩图片最大高度px)
    private static final int IMG_MAX_SIZE = 500;//尺寸压缩图片最大高度(k)


    /**
     * 获取资源drawable的Bitmap
     *
     * @param context
     * @param vectorDrawableId
     * @return
     */
    private static Bitmap getBitmap(Context context, int vectorDrawableId) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
            if (vectorDrawable != null) {
                bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                vectorDrawable.draw(canvas);
            }
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap;
    }

    /**
     * 质量压缩
     */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > IMG_MAX_SIZE && options > 8) { // 循环判断如果压缩后图片是否大于200kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 8;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 获取Bitmap
     *
     * @param srcPath 图片路径
     * @return
     */
    public static Bitmap getBitmapByPath(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath);
        return bitmap;
    }

    /**
     * 按尺寸等比例压缩（根据路径获取图片并压缩）
     */
    public static Bitmap compressByPx(String srcPath) {
        return compressByPx(srcPath, IMG_MAX_WIDTH, IMG_MAX_HEIGHT);
    }

    public static Bitmap compressByPx(String srcPath, int maxWidth, int maxHeight) {
        if (maxWidth == 0) {
            maxWidth = IMG_MAX_WIDTH;
        }
        if (maxHeight == 0) {
            maxHeight = IMG_MAX_HEIGHT;
        }
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        newOpts.inSampleSize = getRatioSize(w, h, maxWidth, maxHeight);// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
    }

    /**
     * 按大小等比例压缩（根据Bitmap图片压缩）
     */
    public static Bitmap compressBySize(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int option = 100;
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int length = baos.toByteArray().length / 1024;//原图大小
        while (length > IMG_MAX_SIZE && option > 8) {// 判断如果图片大于IMG_MAX_SIZE,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();// 重置baos即清空baos
            option -= 8;
            image.compress(Bitmap.CompressFormat.JPEG, option, baos);// 压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        newOpts.inSampleSize = getRatioSize(w, h);// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
    }

    /**
     * drawable 转 Bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap uriToBitmap(Context context, Uri bitmapUri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(bitmapUri));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 计算缩放比
     *
     * @param bitWidth  当前图片宽度
     * @param bitHeight 当前图片高度
     * @return
     * @Description: 函数描述
     */
    public static int getRatioSize(int bitWidth, int bitHeight) {
        return getRatioSize(bitWidth, bitHeight, IMG_MAX_WIDTH, IMG_MAX_HEIGHT);
    }

    public static int getRatioSize(int bitWidth, int bitHeight, int maxWidth, int maxHeight) {
        // 缩放比
        int ratio = 1;
        // 缩放比,由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        if (bitWidth > bitHeight && bitWidth > maxWidth) {
            // 如果图片宽度比高度大,以宽度为基准
            ratio = bitWidth / maxWidth;
        } else if (bitWidth < bitHeight && bitHeight > maxHeight) {
            // 如果图片高度比宽度大，以高度为基准
            ratio = bitHeight / maxHeight;
        }
        // 最小比率为1
        if (ratio <= 0) {
            ratio = 1;
        }
        return ratio;
    }


    //保存图片
    public static void saveBitmap(String path, Bitmap bitmap, FileType imageType) {
        FileOutputStream bos = null;
        File file = new File(path);
        try {
            bos = new FileOutputStream(file);
            if (imageType == FileType.PNG) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            } else if (imageType == FileType.WEBP) {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 100, bos);
            }else {

            }
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * uri 获取图片路径
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getPhotoPath(Context context, Uri uri) {
        String filePath = "";
        if (uri != null) {
            Log.d(TAG, uri.toString());
            String scheme = uri.getScheme();
            if (TextUtils.equals("content", scheme)) {// android 4.4以上版本处理方式
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && DocumentsContract.isDocumentUri(context, uri)) {
                    String wholeID = DocumentsContract.getDocumentId(uri);
                    String id = wholeID.split(":")[1];
                    String[] column = {MediaStore.Images.Media.DATA};
                    String sel = MediaStore.Images.Media._ID + "=?";
                    Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[] {id}, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(column[0]);
                        filePath = cursor.getString(columnIndex);
                        cursor.close();
                    }
                } else {// android 4.4以下版本处理方式
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        filePath = cursor.getString(columnIndex);
                        Log.d(TAG, "filePath" + filePath);
                        cursor.close();
                    }
                }
            } else if (TextUtils.equals("file", scheme)) {// 小米云相册处理方式
                filePath = uri.getPath();
            }

        }
        return filePath;
    }
    // Bitmap、Drawable、InputStream、byte[] 之间转换

    /**********************************************************/
    // 1. Bitmap to InputStream
    public static InputStream bitmap2Input(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public static InputStream bitmap2Input(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    // 2. Bitmap to byte[]
    public static byte[] bitmap2ByteArray(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, baos);
        return baos.toByteArray();
    }

    public static byte[] bitmap2ByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    // 3. Drawable to byte[]
    public static byte[] drawable2ByteArray(Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        return out.toByteArray();
    }

    // 4. byte[] to Bitmap
    public static Bitmap byteArray2Bitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
