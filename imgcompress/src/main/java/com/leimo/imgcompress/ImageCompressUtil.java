package com.leimo.imgcompress;

import android.os.Build;
import android.text.TextUtils;

import android.util.Log;
import com.leimo.imgcompress.utils.BitmapUtil;
import com.leimo.imgcompress.utils.FileType;
import com.leimo.imgcompress.utils.GetTypeByHead;
import com.leimo.imgcompress.utils.SDPathUtil;
import net.bither.util.NativeUtil;

import java.io.File;

/**
 * 图片压缩
 * Created by Administrator on 2017/7/28.
 */

public class ImageCompressUtil {
    private static final String TAG = "ImageCompressUtil";
    /**
     * 压缩图片
     */
    public static String compressImageByPath(CompressImage compressImage) {
        String path = compressImage.getPath();
        String pathCompress = compressImage.getPathCompress();
        FileType imgFormat = GetTypeByHead.getFileType(path);
        compressImage.setFormat(imgFormat);
        if (TextUtils.isEmpty(pathCompress)) {
            pathCompress = SDPathUtil.getSDCardPublicDir("imageCompress") + File.separator + System.currentTimeMillis() + "."+imgFormat.getFormat();
        }
        Log.d(TAG, "getFileType: #header:"+GetTypeByHead.getFileHeader(path)+" #type:"+ GetTypeByHead.getFileType(path));
        if (isCanUserCpu()) {//支持 arm 架构的cpu
            new NativeUtil(compressImage).compressBitmap(path, pathCompress);
        } else {
            BitmapUtil.saveBitmap(pathCompress, BitmapUtil.compressByPx(path, compressImage.getWidthMax(), compressImage.getHeightMax()), imgFormat);
        }
        File file = new File(path);
        File fileCompress = new File(pathCompress);
        if (!fileCompress.exists() || fileCompress.length() > file.length()) {
            return path;
        }
        return pathCompress;
    }

    public static boolean isCanUserCpu() {
        String cpuABI = getCpuABI();
        if (!TextUtils.isEmpty(cpuABI) && (TextUtils.equals(cpuABI, "armeabi") || TextUtils.equals(cpuABI, "armeabi-v7a"))) {
            return true;
        }
        return false;
    }

    /**
     * cpu指令集 armeabi、armeabi-v7a、arm64-v8a、x86、x86_64 、mips
     */
    public static String getCpuABI() {
        return Build.CPU_ABI;
    }
}
