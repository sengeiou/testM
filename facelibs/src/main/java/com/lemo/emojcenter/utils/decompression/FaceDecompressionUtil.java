package com.lemo.emojcenter.utils.decompression;


import android.content.Context;
import android.util.Log;

import com.lemo.emojcenter.FaceConfigInfo;
import com.lemo.emojcenter.manage.FaceThreadPoolManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Description:解压
 * Author:wxw
 * Date:2018/2/1.
 */
public class FaceDecompressionUtil {
    private static final String TAG = "DecompressionUtil";

    /**
     * 含子目录的文件压缩
     *
     * @throws Exception
     */
    // 第一个参数就是需要解压的文件，第二个就是解压的目录
    public static boolean upZipFile(String zipFile, String folderPath) {
        ZipFile zfile = null;
        try {
            // 转码为GBK格式，支持中文
            zfile = new ZipFile(zipFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        Enumeration zList = zfile.entries();
        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        while (zList.hasMoreElements()) {
            ze = (ZipEntry) zList.nextElement();
            // 列举的压缩文件里面的各个文件，判断是否为目录
            if (ze.isDirectory()) {
                String dirstr = folderPath + ze.getName();
                Log.d(TAG, "dirstr=" + dirstr);
                dirstr.trim();
                File f = new File(dirstr);
                f.mkdir();
                continue;
            }
            OutputStream os = null;
            FileOutputStream fos = null;
            // ze.getName()会返回 script/start.script这样的，是为了返回实体的File
            File realFile = getRealFileName(folderPath, ze.getName());
            try {
                fos = new FileOutputStream(realFile);
            } catch (FileNotFoundException e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
            os = new BufferedOutputStream(fos);
            InputStream is = null;
            try {
                is = new BufferedInputStream(zfile.getInputStream(ze));
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
            int readLen = 0;
            // 进行一些内容复制操作
            try {
                while ((readLen = is.read(buf, 0, 1024)) != -1) {
                    os.write(buf, 0, readLen);
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
        }
        try {
            zfile.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 给定根目录，返回一个相对路径所对应的实际文件名.
     *
     * @param baseDir     指定根目录
     * @param absFileName 相对路径名，来自于ZipEntry中的name
     * @return java.io.File 实际的文件
     */
    public static File getRealFileName(String baseDir, String absFileName) {
        Log.d(TAG, "baseDir=" + baseDir + "------absFileName="
        + absFileName);
        absFileName = absFileName.replace("\\", "/");
        Log.d(TAG, "absFileName=" + absFileName);
        String[] dirs = absFileName.split("/");
        Log.d(TAG, "dirs=" + dirs);
        File ret = new File(baseDir);
        String substr = null;
        if (dirs.length > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                substr = dirs[i];
                ret = new File(ret, substr);
            }

            if (!ret.exists()) {
                ret.mkdirs();
            }
            substr = dirs[dirs.length - 1];
            ret = new File(ret, substr);
            return ret;
        } else {
            ret = new File(ret, absFileName);
        }
        return ret;
    }


    /**
     * 解压表情包
     *
     * @param zipFile 压缩包文件名
     * @param dirname 存放解压文件的文件夹名称（用表情ID命名文件夹，便于表情键盘读取）
     */
    public static synchronized void Unzip(String zipFile, String dirname) {
        //解压
        FaceThreadPoolManager.INSTANCE.newFixThreadPoolSingle().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "解压表情开始:" + zipFile);
                zipFile(zipFile, dirname);
                Log.d(TAG, "解压表情完成:" + zipFile);
            }
        });
    }

    private static boolean zipFile(String zipFile, String dirname) {
        String filePath = FaceConfigInfo.INSTANCE.getDirEmojZipRoot() + File.separator + zipFile;
        String targetDirPath = FaceConfigInfo.INSTANCE.getDirEmojRoot() + File.separator + dirname + File.separator;
        if(!new File(filePath).exists()){
            Log.d(TAG, "Unzip: 解压目录 faceId：zipPath：" + filePath + " 不存在");
            return false;
        }
        Log.d(TAG, "Unzip: 解压目录 faceId：zipPath：" + filePath + " #unZipDir:" + targetDirPath);
        int BUFFER = 4096; //这里缓冲区我们使用4KB，
        boolean isZipSuc = true;
        try {
            BufferedOutputStream dest = null; //缓冲输出流
            FileInputStream fis = new FileInputStream(filePath);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry; //每个zip条目的实例
            while ((entry = zis.getNextEntry()) != null) {
                try {
                    Log.d(TAG, "Unzip 压缩包里文件 faceId:" + "压缩包：" + zipFile + " #解压：" + targetDirPath + entry.getName());
                    int count;
                    byte data[] = new byte[BUFFER];
                    String strEntry = entry.getName(); //保存每个zip的条目名称
                    File entryFile = new File(targetDirPath + strEntry + ".unzip");
                    File newFile = new File(targetDirPath + strEntry);
                    File entryDir = new File(entryFile.getParent());
                    if (!entryDir.exists()) {
                        entryDir.mkdirs();
                    }
                    if (entryFile.exists()) {
                        entryFile.delete();
                    }
                    if (newFile.exists()) {
                        newFile.delete();
                    }
                    FileOutputStream fos = new FileOutputStream(entryFile);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    entryFile.renameTo(newFile);
                    dest.flush();
                    dest.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    isZipSuc = false;
                    Log.d(TAG, "Unzip: 解压失败:" + targetDirPath + entry.getName());
                }
            }
            //解压成功才删除压缩包
            if (isZipSuc) {
                Log.d(TAG, "Unzip: 解压成功 " + filePath);
                deleteFile(filePath);
            }
            zis.close();
        } catch (Exception cwj) {
            cwj.printStackTrace();
            isZipSuc = false;
            //解压异常删除
//            deleteFile(filePath);
        }
        return isZipSuc;
    }

    /**
     * 删除压缩包
     *
     * @param filePath 压缩包文件路径
     */

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    private static String getDirName(String name) {
        String dirName;
        dirName = name.subSequence(0, name.indexOf(".")).toString();
        return dirName;
    }


    //删除文件夹和文件夹里面的文件
    public static void deleteDir(Context context, final String filePackName) {
        String path = FaceConfigInfo.INSTANCE.getDirEmojRoot() + File.separator + filePackName;
        File dir = new File(path);
        deleteDirWihtFile(context, dir);
    }

    public static void deleteDirWihtFile(Context context, File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete(); // 删除所有文件
            } else if (file.isDirectory()) {
                deleteDirWihtFile(context, file); // 递规的方式删除文件夹
            }
        }
        dir.delete();// 删除目录本身
//        Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 重命名文件
     *
     * @param oldPath 原来的文件地址
     * @param newPath 新的文件地址
     */
    public static void renameFile(String oldPath, String newPath) {
        File oleFile = new File(oldPath);
        File newFile = new File(newPath);
        //执行重命名
        oleFile.renameTo(newFile);
    }
}
