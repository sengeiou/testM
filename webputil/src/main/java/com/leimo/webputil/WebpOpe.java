package com.leimo.webputil;

import android.graphics.Bitmap;
import com.google.webp.libwebp;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;

/**
 * Created by wangru
 * Date: 2018/6/6  14:27
 * mail: 1902065822@qq.com
 * describe:
 */

public class WebpOpe {
    static {
        System.loadLibrary("webp");
    }

    public static byte[] loadFileAsByteArray(String filePath) {
        File file = new File(filePath);
        byte[] data = new byte[(int) file.length()];
        try {
            FileInputStream inputStream;
            inputStream = new FileInputStream(file);
            inputStream.read(data);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static Bitmap webpToBitmap(byte[] encoded) {
        try {
            int[] width = new int[] {0};
            int[] height = new int[] {0};
            byte[] decoded = libwebp.WebPDecodeARGB(encoded, encoded.length, width, height);
            if (decoded.length == 0) {
                return null;
            }
            int[] pixels = new int[decoded.length / 4];
            ByteBuffer.wrap(decoded).asIntBuffer().get(pixels);
            return Bitmap.createBitmap(pixels, width[0], height[0], Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
