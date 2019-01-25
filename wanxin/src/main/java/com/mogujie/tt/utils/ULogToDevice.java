package com.mogujie.tt.utils;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.mogujie.tt.app.IMApplication;
import com.mogujie.tt.config.PathConstant;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 打印日志到手机文件
 * Created by wangru
 * Date: 2017/9/12  19:47
 * mail: 1902065822@qq.com
 * describe:
 */
public class ULogToDevice {
    private static final long LOG_MAX_LENGTH = 200 * 1024;//200k 每个日志文件最大大小(B)
    private static final char VERBOSE = 'v';
    private static final char DEBUG = 'd';
    private static final char INFO = 'i';
    private static final char WARN = 'w';
    private static final char ERROR = 'e';
    private static String TAG = ULogToDevice.class.getSimpleName();
    private static String logDir = getSDCardPublicDir(IMApplication.Companion.getInstance(), PathConstant.PATH_MAIN_NAME+"/log");//log日志存放路径
    private static boolean IS_DEBUG = true;
    private static String filePath;
    private static String fileName;
    private static String adminName;


    public static void v(String tag, String msg) {
        v(null, tag, msg);
    }

    public static void d(String tag, String msg) {
        d(null, tag, msg);
    }

    public static void i(String tag, String msg) {
        i(null, tag, msg);
    }

    public static void w(String tag, String msg) {
        w(null, tag, msg);
    }

    public static void e(String tag, String msg) {
        e(null, tag, msg);
    }

    public static void v(String filename, String tag, String msg) {
        write(VERBOSE, filename, tag, msg);
    }

    public static void d(String filename, String tag, String msg) {
        write(DEBUG, filename, tag, msg);
    }

    public static void i(String filename, String tag, String msg) {
        write(INFO, filename, tag, msg);
    }

    public static void w(String filename, String tag, String msg) {
        write(WARN, filename, tag, msg);
    }

    public static void e(String filename, String tag, String msg) {
        write(ERROR, filename, tag, msg);
    }

    public static void write(char type, String filename, String tag, String msg) {
        if (IS_DEBUG) {
            fileName = filename;
            writeToFile(type, tag, msg);
        }
    }

    /**
     * 将log信息写入文件中
     *
     * @param type
     * @param tag
     * @param msg
     */
    private static void writeToFile(char type, String tag, String msg) {
        if (null == logDir) {
            return;
        }
        FileOutputStream fos = null;//FileOutputStream会自动调用底层的close()方法，不用关闭
        BufferedWriter bw = null;
        try {
            String log = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.US).format(new Date()) + " " + type + " " + tag + "\n" + msg + "\n\n\n";//log日志内容，可以自行定制
            filePath = getLogPath(null);
            fos = new FileOutputStream(filePath, true);//这里的第二个参数代表追加还是覆盖，true为追加，flase为覆盖
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(log);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();//关闭缓冲流
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //获取文件大小
    private static long getFileSize(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return 0;
        }
        File file = new File(filePath);
        if (file == null) {
            return 0;
        }
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                size = fis.available();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return size;
    }

    //得到文件名
    private static String getLogPath(String path) {
        if (TextUtils.isEmpty(path)) {
            filePath = appPath() + ".log";
        }
        File logFile = new File(filePath);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long logFileSize = getFileSize(filePath);
        if (logFileSize >= LOG_MAX_LENGTH) {
            int temp = 0;
            if (filePath.contains("(")) {
                temp = Integer.parseInt(filePath.substring(filePath.indexOf("(") + 1, filePath.lastIndexOf(")")));
            }
            filePath = appPath() + "(" + (++temp) + ")" + ".log";
            return getLogPath(filePath);
        } else {
            return filePath;
        }
    }

    @NonNull
    private static String appPath() {
        String time = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String dir = logDir + File.separator + time;
        if (!TextUtils.isEmpty(adminName)) {
            dir = dir + File.separator + adminName;
        }
        if (!new File(dir).exists()) {
            new File(dir).mkdirs();
        }
        return dir + "/log" + (TextUtils.isEmpty(fileName) ? "" : "_" + fileName);
    }

    public static void setAdminName(String adminName) {
        ULogToDevice.adminName = adminName;
    }

    public static String getSDCardPublicDir(Context context, String filedir) {
        String path;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
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
}
