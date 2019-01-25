package me.everything.webp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.nio.ByteBuffer;

/**
 * github 地址：https://github.com/EverythingMe/webp-android
 */
public class WebPDecoder {
    private static WebPDecoder instance = null;

    private WebPDecoder() {
        System.loadLibrary("webp_evme");
    }

    public static WebPDecoder getInstance() {
        if (instance == null) {
            synchronized (WebPDecoder.class) {
                if (instance == null) {
                    instance = new WebPDecoder();
                }
            }
        }

        return instance;
    }

    public Bitmap decodeWebP(byte[] encoded) {
        return decodeWebP(encoded, 0, 0);
    }

    public Bitmap decodeWebP(byte[] encoded, int w, int h) {
        int[] width = new int[] {w};
        int[] height = new int[] {h};
        try {
            byte[] decoded = decodeRGBAnative(encoded, encoded.length, width, height);
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

    public static native byte[] decodeRGBAnative(byte[] encoded, long encodedLength, int[] width, int[] height);
}