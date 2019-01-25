package com.leimo.imgcompress.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * http://blog.csdn.net/jason_996/article/details/51605614
 * The type of files directory to return. May be null for the root of the files directory or one of the
 * following Environment constants for a subdirectory: {@link Environment#DIRECTORY_MUSIC},
 * {@link Environment#DIRECTORY_PODCASTS}, {@link Environment#DIRECTORY_RINGTONES},
 * {@link Environment#DIRECTORY_ALARMS}, {@link Environment#DIRECTORY_NOTIFICATIONS},
 * {@link Environment#DIRECTORY_PICTURES}, or {@link Environment#DIRECTORY_MOVIES}.
 * The path of the directory holding application files on external storage. Returns null if external storage is
 * not currently mounted so it could not ensure the path exists; you will need to call this method again when it
 * is available.
 * Environment#getExternalStoragePublicDirectory
 */
public class SDPathUtil {
    /**
     * 获取SD卡公有目录的路径,SD卡卸载就取系统目录
     *
     * @param type Environment
     */
    public static String getSDCardPublicDirByType(String type) {
        if (isSDCardMounted()) {
            return Environment.getExternalStoragePublicDirectory(type).toString();
        } else {
            ///data
            return Environment.getDataDirectory().getAbsolutePath();
        }
    }

    /**
     * SD 卡目录
     * 直接写SD下的目录
     *
     * @param filedir 目录名 eg: wx/log
     * @return
     */
    public static String getSDCardPublicDir(String filedir) {
        String path;
        if (isSDCardMounted()) {
            path = Environment.getExternalStorageDirectory().getPath();
        } else {
            path = Environment.getDataDirectory().getAbsolutePath();
        }
        if (!TextUtils.isEmpty(filedir)) {
            path += ("/" + filedir);
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return path;
    }

    /**
     * 获取SD卡私有Cache目录的路径,SD卡卸载就取系统目录
     *
     * @param context
     * @return
     */
    public static String getSDCardPrivateCacheDir(Context context) {
        if (isSDCardMounted()) {
            // /mnt/sdcard/Android/data/com.my.app/cache
            return context.getExternalCacheDir().getAbsolutePath();
        } else {
            // /data/data/com.my.app/cache
            return context.getCacheDir().getAbsolutePath();
        }
    }

    /**
     * 获取SD卡私有Cache目录的路径,SD卡卸载就取系统目录
     *
     * @param context
     * @param filedir 目录名 eg: wx/log
     * @return
     */
    public static String getSDCardPrivateCacheDir(Context context, String filedir) {
        String path;
        if (isSDCardMounted()) {
            // /mnt/sdcard/Android/data/com.my.app/cache
            path = Environment.getExternalStorageDirectory().getPath();
        } else {
            // /data/data/com.my.app/cache
            path = Environment.getDataDirectory().getAbsolutePath();
        }

        if (TextUtils.isEmpty(filedir)) {
            path = path + "/" + filedir;
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return path;
    }

    // 获取SD卡私有Files目录的路径,SD卡卸载就取系统目录
    public static String getSDCardPrivateCacheDirByType(Context context, String type) {
        if (isSDCardMounted()) {
            // SDCard/Android/data/你的应用的包名/files/typename
            return context.getExternalFilesDir(type).getAbsolutePath();
        } else {
            // /data/data/com.my.app/files/typename
            String path = "";
            if (!TextUtils.isEmpty(type)) {
                path = "/" + type;
            }
            return context.getFilesDir().getAbsolutePath() + path;
        }
    }

    /**
     * 从文件路径中获取文件名带后缀 photo.png
     *
     * @param path
     * @return
     */
    public static String getFileNameWithTypeForPath(String path) {
        if (path.contains("/")) {
            int start = path.lastIndexOf("/");
            path = path.substring(start + 1, path.length());
        }
        return path;
    }

    /**
     * 从文件路径中获取文件名不带后缀 photo
     *
     * @param path
     * @return
     */
    public static String getFileNameForPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        int start = path.lastIndexOf("/");
        int end = path.lastIndexOf(".");
        if (start != -1 && end > 1) {
            path = path.substring(start + 1, end);
        }
        return path;
    }

    /**
     * 获取文件后缀 带点
     *
     * @param path
     * @return
     */
    public static String getFileSuffix(String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        int start = path.lastIndexOf(".");
        int end = path.length();
        if (start != -1 && end > 1) {
            path = path.substring(start, end);
        }
        return path;
    }


    // 判断SD卡是否被挂载
    public static boolean isSDCardMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    // 获取SD卡的根目录
    public static String getSDCardBaseDir() {
        if (isSDCardMounted()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return null;
    }

    // 获取SD卡的完整空间大小，返回MB
    @SuppressLint("NewApi")
    public static long getSDCardSize() {
        if (isSDCardMounted()) {
            StatFs fs = new StatFs(getSDCardBaseDir());
            long count = fs.getBlockCountLong();
            long size = fs.getBlockSizeLong();
            return count * size / 1024 / 1024;
        }
        return 0;
    }

    // 获取SD卡的剩余空间大小
    @SuppressLint("NewApi")
    public static long getSDCardFreeSize() {
        if (isSDCardMounted()) {
            StatFs fs = new StatFs(getSDCardBaseDir());
            long count = fs.getFreeBlocksLong();
            long size = fs.getBlockSizeLong();
            return count * size / 1024 / 1024;
        }
        return 0;
    }

    // 获取SD卡的可用空间大小
    @SuppressLint("NewApi")
    public static long getSDCardAvailableSize() {
        if (isSDCardMounted()) {
            StatFs fs = new StatFs(getSDCardBaseDir());
            long count = fs.getAvailableBlocksLong();
            long size = fs.getBlockSizeLong();
            return count * size / 1024 / 1024;
        }
        return 0;
    }

    // 往SD卡的公有目录下保存文件
    public static boolean saveFileToSDCardPublicDir(byte[] data, String type, String fileName) {
        BufferedOutputStream bos = null;
        if (isSDCardMounted()) {
            File file = Environment.getExternalStoragePublicDirectory(type);
            try {
                bos = new BufferedOutputStream(new FileOutputStream(new File(file, fileName)));
                bos.write(data);
                bos.flush();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    bos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    // 往SD卡的自定义目录下保存文件
    public static boolean saveFileToSDCardCustomDir(byte[] data, String dir, String fileName) {
        BufferedOutputStream bos = null;
        if (isSDCardMounted()) {
            File file = new File(getSDCardBaseDir() + File.separator + dir);
            if (!file.exists()) {
                file.mkdirs();// 创建自定义目录
            }
            try {
                bos = new BufferedOutputStream(new FileOutputStream(new File(file, fileName)));
                bos.write(data);
                bos.flush();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    // 往SD卡的私有Files目录下保存文件
    public static boolean saveFileToSDCardPrivateFilesDir(byte[] data, String type, String fileName, Context context) {
        BufferedOutputStream bos = null;
        if (isSDCardMounted()) {
            File file = context.getExternalFilesDir(type);
            try {
                bos = new BufferedOutputStream(new FileOutputStream(new File(file, fileName)));
                bos.write(data);
                bos.flush();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    // 往SD卡的私有Cache目录下保存文件
    public static boolean saveFileToSDCardPrivateCacheDir(byte[] data, String fileName, Context context) {
        BufferedOutputStream bos = null;
        if (isSDCardMounted()) {
            File file = context.getExternalCacheDir();
            try {
                bos = new BufferedOutputStream(new FileOutputStream(new File(file, fileName)));
                bos.write(data);
                bos.flush();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    // 保存bitmap图片到SDCard的私有Cache目录
    public static boolean saveBitmapToSDCardPrivateCacheDir(Bitmap bitmap, String fileName, Context context) {
        if (isSDCardMounted()) {
            BufferedOutputStream bos = null;
            // 获取私有的Cache缓存目录
            File file = context.getExternalCacheDir();

            try {
                bos = new BufferedOutputStream(new FileOutputStream(new File(file, fileName)));
                if ((fileName.contains(".png") || fileName.contains(".PNG"))) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                } else {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                }
                bos.flush();
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
            return true;
        } else {
            return false;
        }
    }

    // 从SD卡获取文件
    public static byte[] loadFileFromSDCard(String fileDir) {
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bis = new BufferedInputStream(new FileInputStream(new File(fileDir)));
            byte[] buffer = new byte[8 * 1024];
            int c;
            while ((c = bis.read(buffer)) != -1) {
                baos.write(buffer, 0, c);
                baos.flush();
            }
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // 从SDCard中寻找指定目录下的文件，返回Bitmap
    public static Bitmap loadBitmapFromSDCard(String filePath) {
        byte[] data = loadFileFromSDCard(filePath);
        if (data != null) {
            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (bm != null) {
                return bm;
            }
        }
        return null;
    }

    // 从sdcard中删除文件
    public static boolean removeFileFromSDCard(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                file.delete();
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
