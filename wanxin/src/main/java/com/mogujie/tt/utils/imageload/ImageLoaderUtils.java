package com.mogujie.tt.utils.imageload;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * @author fengjing:
 * @function
 * @date ：2015年10月15日 下午5:58:59
 * @mail 164303256@qq.com
 */
public class ImageLoaderUtils {

    public static final int COMPRESSSIZE = 500;

    private static Bitmap getBitmapFromFile(String filePath) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //先获取图片的宽高信息
        bitmap = BitmapFactory.decodeFile(filePath, options);
        //设置比例
        options.inSampleSize = getSampleSize(options, COMPRESSSIZE, COMPRESSSIZE);
        options.inJustDecodeBounds = false;
        //获取实际的bitmap对象
        bitmap = BitmapFactory.decodeFile(filePath, options);
        return bitmap;
    }

    private static int getSampleSize(BitmapFactory.Options options, int width, int height) {
        int sampleSize = 1;
        int widthRate = options.outWidth / width;
        int heightRate = options.outHeight / height;
        sampleSize = widthRate > heightRate ? widthRate : heightRate;
        if (sampleSize < 1) {
            sampleSize = 1;
        }
        return sampleSize;
    }

    public static byte[] fileToByte(String path) {
        Bitmap bitmap = getBitmapFromFile(path);
        if (bitmap == null) {
            return null;
        }
        byte[] bytes = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, 100, baos);
            bytes = baos.toByteArray();
            baos.flush();
            baos.close();
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            bitmap.recycle();
        }
        return bytes;
    }

    public static byte[] byteFromBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        byte[] bytes = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, 100, baos);
            bytes = baos.toByteArray();
            baos.flush();
            baos.close();
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            bitmap.recycle();
        }
        return bytes;
    }

}
