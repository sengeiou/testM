package com.mogujie.tt.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import com.leimo.wanxin.BuildConfig;

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
    private static final String TAG = SDPathUtil.class.getSimpleName();

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

    public static String getSDCardPublicDir(Context context, String filedir) {
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

    // 获取SD卡私有Cache目录的路径,SD卡卸载就取系统目录
    public static String getSDCardPrivateCacheDir(Context context) {
        return getSDCardPrivateCacheDir(context, null);
    }

    /**
     * 获取私有缓存目录
     *
     * @param context Context
     * @param filedir 文件夹名字 eg : zuzu/img
     * @return
     */
    public static String getSDCardPrivateCacheDir(Context context, String filedir) {
        String path = "";
        if (isSDCardMounted()) {
            if (context.getExternalCacheDir() != null) {
                path = context.getExternalCacheDir().getAbsolutePath();//  /mnt/sdcard/Android/data/com.my.app/cache
            } else if (Environment.getExternalStorageDirectory() != null) {
                path = Environment.getExternalStorageDirectory().getPath();//  /mnt/sdcard
            }
        } else {
            if (context.getCacheDir() != null) {
                path = context.getCacheDir().getAbsolutePath();//  /data/data/com.my.app/cache
            } else {
                path = Environment.getDataDirectory().getAbsolutePath();//  /data
            }

        }
        path = pathAddFile(filedir, path);
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
     * 在目录添加文件
     *
     * @param filedir 某种根目录 eg:Environment.getDataDirectory()
     * @param path    文件夹 eg: zuzu/img
     * @return
     */
    private static String pathAddFile(String filedir, String path) {
        if (!TextUtils.isEmpty(filedir)) {
            path = path + "/" + filedir;
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return path;
    }

    /**
     * 从文件路径中获取文件名带后缀 photo.png
     *
     * @param path
     * @return
     */
    public static String getFileNameWithTypeForPath(String path) {
        if (!TextUtils.isEmpty(path) && path.contains("/")) {
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
        int start = path.lastIndexOf("/");
        int end = path.lastIndexOf(".");
        if (start != -1 && end != -1) {
            path = path.substring(start + 1, end);
        }
        return path;
    }

    public static boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.isFile();
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
    public static boolean saveFileToSDCardCustomDir(byte[] data, String path) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        if (isSDCardMounted()) {
            try {
                fos = new FileOutputStream(new File(path));
                bos = new BufferedOutputStream(fos);
                bos.write(data);
                bos.flush();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    bos.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    // 往SD卡的私有Files目录下保存文件
    public static boolean saveFileToSDCardPrivateFilesDir(byte[] data, String type, String fileName, Context context) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        if (isSDCardMounted()) {
            try {
                File file = context.getExternalFilesDir(type);
                fos = new FileOutputStream(new File(file, fileName));
                bos = new BufferedOutputStream(fos);
                bos.write(data);
                bos.flush();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    bos.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
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

    /**
     * 清空文件夹
     */
    public static void clearFolder(String path) {
        clearFolder(new File(path));
    }

    public static void clearFolder(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                clearFolder(f);
            }
            file.delete();
        }
    }

    public static long getFolderLength(String path) {
        return getFolderLength(new File(path));
    }

    public static long getFolderLength(File file) {
        long size = 0;
        try {
            if (file != null) {
                File[] fileList = file.listFiles();
                if (fileList != null && fileList.length > 0) {
                    for (int i = 0; i < fileList.length; i++) {
                        if (fileList[i].isDirectory()) {
                            size = size + getFolderLength(fileList[i]);
                        } else {
                            size = size + fileList[i].length();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    //保存文件通知系统更新，在图库显示图片
    public static void updateImageSysStatu(Context context, String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (context != null && file != null && file.exists()) {
//                // 把文件插入到系统图库
//                try {
//                    MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), "mengmeng", "盟盟");
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                updateFileStatu(context, file);
            }
        } else {
            Log.d(TAG, "updateFileStatu: path is null");
        }
    }

    //保存文件通知系统更新，在图库显示图片
    public static void updateFileStatu(Context context, File file) {
        if (context != null && file != null && file.exists()) {
            //通知图库更新
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri;
                if (Build.VERSION.SDK_INT >= 24) {
                    uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                } else {
                    uri = Uri.fromFile(file);
                }
                intent.setData(uri);
                context.sendBroadcast(intent);
            } else {
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
            }
        } else {
            Log.d(TAG, "updateFileStatu: file is not exist");
        }
    }

    public static Uri getUri(Context context, File cameraFile) {
        Uri cameraUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //7.0
            cameraUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", cameraFile);
            //            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            cameraUri = Uri.fromFile(cameraFile);
        }
        return cameraUri;
    }
}
