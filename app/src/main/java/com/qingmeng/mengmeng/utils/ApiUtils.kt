package com.qingmeng.mengmeng.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.app.common.api.interceptor.LogInterceptor
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.BuildConfig
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.base.Api
import com.qingmeng.mengmeng.base.SealAccountInterceptor
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.entity.OssDataBean
import com.qingmeng.mengmeng.view.dialog.DialogCustom
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by zq on 2018/8/13
 */
@SuppressLint("StaticFieldLeak", "CheckResult", "SimpleDateFormat")
object ApiUtils {
    fun getApi(): Api {
        val okClientBuilder = OkHttpClient.Builder().apply {
            addInterceptor(SealAccountInterceptor())
            if(BuildConfig.DEBUG) {
                addInterceptor(LogInterceptor())
            }
            connectTimeout(15, TimeUnit.SECONDS)
            readTimeout(60, TimeUnit.SECONDS)
            writeTimeout(60, TimeUnit.SECONDS)
        }
        return Retrofit.Builder()
                .baseUrl(IConstants.BASE_URL)
                .client(okClientBuilder.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(Api::class.java)
    }

    //加盟
    fun join(id: Int, name: String, phone: String, message: String, type: Int, myDialog: DialogCustom, success: () -> Unit, addSubscription: (Disposable) -> Unit) {
        myDialog.showLoadingDialog()
        getApi().join(id, name, phone, message, type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    myDialog.dismissLoadingDialog()
                    if (bean.code == 12000) {
                        ToastUtil.showShort(R.string.submit_success)
                        success()
                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    /**
     * @param url 图片本地地址，PictureSelector选择的图片使用compressPath图片压缩后的地址
     * @param callback 当返回值为空字符串时上传失败，返回值为链接时上传成功
     */
    fun updateImg(activity: BaseActivity, url: String, callback: (String, String) -> Unit) {
        getApi().getOssToken()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let { updateFile(activity, url, it, callback) }
                    } else {
                        callback("", url)
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    callback("", url)
                }, {}, { activity.addSubscription(it) })
    }

    // 需要权限的上传文件
    private fun updateFile(context: Context, url: String, ossDataBean: OssDataBean, callback: (String, String) -> Unit) {
        val ossData = ossDataBean.oss

        val credentialProvider = object : OSSFederationCredentialProvider() {
            override fun getFederationToken(): OSSFederationToken {
                return OSSFederationToken(ossData.accessKeyId, ossData.accessKeySecret, ossData.securityToken, ossData.expiration)
            }
        }
        val oss = OSSClient(context, "http://${ossDataBean.endpoint}", credentialProvider)

        val putFolder = SimpleDateFormat("yyyyMMddHHmmss").format(Date()) + ".jpg"
        val put = PutObjectRequest(ossDataBean.buckName, "${ossDataBean.folder}/$putFolder", url)

        oss.asyncPutObject(put,
                object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                    override fun onSuccess(request: PutObjectRequest, result: PutObjectResult) {
                        val realUrl = "${ossDataBean.domain}/${request.objectKey}"
                        callback(realUrl, url)
                    }

                    override fun onFailure(request: PutObjectRequest, clientExcepion: ClientException?,
                                           serviceException: ServiceException?) {
                        // 请求异常
                        callback("", url)
                        clientExcepion?.printStackTrace()
                    }
                })
    }

    fun getFileUri(context: Context, dir: File): Uri {
        val uri: Uri
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context, "com.qingmeng.mengmeng.fileprovider", dir)
        } else {
            uri = Uri.fromFile(dir)
        }
        return uri
    }
}