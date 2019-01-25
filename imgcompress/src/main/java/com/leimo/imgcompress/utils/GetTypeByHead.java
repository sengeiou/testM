package com.leimo.imgcompress.utils;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * 获取和判断文件头信息
 *
 * @author Sud
 *
 */
public class GetTypeByHead {
    private static final String TAG = "GetTypeByHead";

    /**
     * 根据文件路径获取文件头信息
     *
     * @param filePath
     *            文件路径
     * @return 文件头信息
     */
    public static FileType getFileType(String filePath) {
        String header=getFileHeader(filePath);
        Log.d(TAG, "getFileType: header="+header);
        return FileType.getFileTypeByHeader(header);
    }

    /**
     * 根据文件路径获取文件头信息
     *
     * @param filePath
     *            文件路径
     * @return 文件头信息
     */
    public static String getFileHeader(String filePath) {
        FileInputStream is = null;
        String value = null;
        try {
            is = new FileInputStream(filePath);
            byte[] b = new byte[4];
            /*
             * int read() 从此输入流中读取一个数据字节。 int read(byte[] b) 从此输入流中将最多 b.length
             * 个字节的数据读入一个 byte 数组中。 int read(byte[] b, int off, int len)
             * 从此输入流中将最多 len 个字节的数据读入一个 byte 数组中。
             */
            is.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception e) {
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return value;
    }

    /**
     * 将要读取文件头信息的文件的byte数组转换成string类型表示
     *
     * @param src
     *            要读取文件头信息的文件的byte数组
     * @return 文件头信息
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }
}