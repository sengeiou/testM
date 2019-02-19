package com.lemo.emojcenter.utils

import android.content.Context
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.lemo.emojcenter.bean.OssDataBean
import com.lemo.emojcenter.bean.UploadFileBean
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ClientUpload(private val context: Context) {
    private var putFolder: String? = null

    var callback: ClientCallback? = null

    // 没有权限上传
    fun uploadFile(context: Context, url: String) {
        val endpoint = "http://oss-cn-hangzhou.aliyuncs.com"
        val accessKeyId = "Z7cuCHuxGu9JqU1h"
        val accessKeySecret = "wtM10DEAXdiMo4OMEoJdxAeL9hurDP"
        val credentialProvider = OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret)

        val oss = OSSClient(context, endpoint, credentialProvider)
        // PutObjectRequest put = new PutObjectRequest("<bucketName>",
        // "<objectKey>", "<uploadFilePath>");
        val put = PutObjectRequest("chuzhaoimage",
                "headPhoto/" + SimpleDateFormat("yyyyMMdd").format(Date()) + "/"
                        + SimpleDateFormat("yyyyMMddHHmmss").format(Date()) + ".jpg",
                url)

        val task = oss.asyncPutObject(put, object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
            override fun onSuccess(request: PutObjectRequest, result: PutObjectResult) {
                MyLogUtils.e("PutObject" + result.eTag)
            }

            override fun onFailure(request: PutObjectRequest, clientExcepion: ClientException?,
                                   serviceException: ServiceException?) {
                // 请求异常
                clientExcepion?.printStackTrace()
                if (serviceException != null) {
                    // 服务异常
                    MyLogUtils.e("ErrorCode" + serviceException.errorCode)
                    MyLogUtils.e("RequestId" + serviceException.requestId)
                    MyLogUtils.e("HostId" + serviceException.hostId)
                    MyLogUtils.e("RawMessage" + serviceException.rawMessage)
                }
            }
        })
    }

    // 需要权限的上传文件
    fun updateFile(domin: String?, endpoint: String?, buck: String?, folder: String?, url: String?, ak: String?,
                   sk: String?, token: String?, expiration: String?, i: Int) {

        val credetialProvider = object : OSSFederationCredentialProvider() {
            override fun getFederationToken(): OSSFederationToken {
                return OSSFederationToken(ak, sk, token, expiration)
            }
        }
        val oss = OSSClient(context, endpoint, credetialProvider)

        //http://zmei.oss-cn-hangzhou.aliyuncs.com/yimei/app/company/2016-08-16/20160816184725.jpg
        putFolder = SimpleDateFormat("yyyyMMddHHmmss").format(Date()) + i + ".jpg"
        val put = PutObjectRequest(buck, folder + "/" + putFolder, url)
        MyLogUtils.e("put", "buck==$buck'----$folder/$putFolder---url---$url")

        val task = oss.asyncPutObject(put,
                object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                    override fun onSuccess(request: PutObjectRequest, result: PutObjectResult) {
                        MyLogUtils.e("PutObject....." + result.eTag)
                        val realUrl = domin + request.objectKey
                        MyLogUtils.e("url--->" + realUrl)
                        if (callback != null) {
                            callback!!.onSuccess(realUrl)
                        }

                    }

                    override fun onFailure(request: PutObjectRequest, clientExcepion: ClientException?,
                                           serviceException: ServiceException?) {
                        // 请求异常
                        MyLogUtils.e("request" + request.bucketName + "..." + request.objectKey + "..."
                                + request.uploadFilePath)
                        MyLogUtils.e("request" + request.toString())

                        if (callback != null) {
                            callback!!.onError("文件上传失败")
                        }
                        clientExcepion?.printStackTrace()
                        if (serviceException != null) {
                            // 服务异常
                            MyLogUtils.e("ErrorCode" + serviceException.errorCode)
                            MyLogUtils.e("RequestId" + serviceException.requestId)
                            MyLogUtils.e("HostId" + serviceException.hostId)
                            MyLogUtils.e("RawMessage" + serviceException.rawMessage)
                        }
                    }
                })
    }

    fun setClientCallback(callback: ClientCallback) {
        this.callback = callback
    }

    interface ClientCallback {
        fun onSuccess(url: String)

        fun onError(msg: String)
    }

    companion object {
        private val TAG = "ClientUpload"

        /**
         * 上传文件
         *
         * @param context
         * @param ossDataBean
         * @param callback
         * @param delete      上传完成是否删除文件<删除log文件时使用>
        </删除log文件时使用> */
        @JvmOverloads
        fun updateFile(context: Context, ossDataBean: OssDataBean, fileBean: UploadFileBean?, callback: ClientCallback?,
                       delete: Boolean = false) {
            if (fileBean == null || fileBean.isIllegal) {
                callback?.onError("文件信息错误")
                return
            }
            val credetialProvider = object : OSSFederationCredentialProvider() {
                override fun getFederationToken(): OSSFederationToken {
                    return OSSFederationToken(ossDataBean.oss!!.accessKeyId,
                            ossDataBean.oss!!.accessKeySecret, ossDataBean.oss!!.securityToken,
                            ossDataBean.oss!!.expiration)
                }
            }
            val oss = OSSClient(context, ossDataBean.endpoint!!, credetialProvider)
            val objectKey = ossDataBean.folder + "/" + fileBean.uploadFileName
            val put = PutObjectRequest(ossDataBean.buckName,
                    objectKey, fileBean.localPath)
            oss.asyncPutObject(put,
                    object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                        override fun onSuccess(request: PutObjectRequest, result: PutObjectResult) {
                            callback!!.onSuccess(ossDataBean.accessUrl + "/" + ossDataBean.folder + "/" + fileBean.uploadFileName)
                            if (delete) {
                                val file = File(fileBean.localPath!!)
                                if (file.exists()) {
                                    file.delete()
                                }
                            }
                        }

                        override fun onFailure(request: PutObjectRequest, clientExcepion: ClientException?,
                                               serviceException: ServiceException?) {
                            if (serviceException != null) {
                                MyLogUtils.e(TAG, "onFailure: serviceException " + serviceException.message)
                                callback!!.onError("serviceException " + serviceException.message)
                            } else if (clientExcepion != null) {
                                MyLogUtils.e(TAG, "onFailure: clientExcepion " + clientExcepion.message)
                                callback!!.onError("clientExcepion " + clientExcepion.message)
                            }
                        }
                    })
        }
    }
}
