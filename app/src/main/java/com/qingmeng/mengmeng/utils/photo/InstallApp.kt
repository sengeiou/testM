package com.qingmeng.mengmeng.utils.photo

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.content.FileProvider
import com.qingmeng.mengmeng.R
import java.io.File


/**
 * Created by wangru
 * Date: 2018/7/24  14:51
 * mail: 1902065822@qq.com
 * describe:
 */
/**
 * 8.0以上系统设置安装未知来源权限
 */
object InstallApp {
    val INSTALL_PERMISS_CODE = 101
    var mApkFile: File? = null
    fun installProcess(activity: Activity, apk: File) {
        mApkFile = apk
        val haveInstallPermission: Boolean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //先判断是否有安装未知来源应用的权限
            haveInstallPermission = activity.packageManager.canRequestPackageInstalls()
            if (!haveInstallPermission) {
                //弹框提示用户手动打开
                showAlert(activity, "安装权限", "需要打开允许来自此来源，请去设置中开启此权限", DialogInterface.OnClickListener { dialog, which ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        //此方法需要API>=26才能使用
                        toInstallPermissionSettingIntent(activity)
                    }
                })
                return
            }
        }
        installApk(activity)
    }

    /**
     * 开启安装未知来源权限
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun toInstallPermissionSettingIntent(activity: Activity) {
        val packageURI = Uri.parse("package:" + activity.getPackageName())
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI)
        activity.startActivityForResult(intent, INSTALL_PERMISS_CODE)
    }

    /**
     * alert 消息提示框显示
     * @param context   上下文
     * @param title     标题
     * @param message   消息
     * @param listener  监听器
     */
    private fun showAlert(context: Context, title: String, message: String, listener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("确定", listener)
        builder.setCancelable(false)
        builder.setIcon(R.mipmap.ic_launcher)
        val dialog = builder.create()
        dialog.show()
    }

    //安装应用
    fun installApk(activity: Activity) {
        mApkFile?.let {
            val intent = Intent(Intent.ACTION_VIEW)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.setDataAndType(Uri.fromFile(it), "application/vnd.android.package-archive")
            } else {//Android7.0之后获取uri要用contentProvider
                val uri = activity.getUriFromFile(it)
                intent.setDataAndType(uri, "application/vnd.android.package-archive")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        }
    }

    private fun Context.getUriFromFile(file: File): Uri {
        val imageUri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        } else {
            imageUri = Uri.fromFile(file)
        }
        return imageUri
    }
}