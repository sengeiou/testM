package com.mogujie.tt.utils.path;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import com.leimo.wanxin.BuildConfig;

import java.io.File;

/**
 * Created by wangru
 * Date: 2018/5/22  16:15
 * mail: 1902065822@qq.com
 * describe:
 */

public class UriUtil {
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
