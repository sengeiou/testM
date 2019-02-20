package com.lemo.emojcenter.manage

import android.text.TextUtils
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.lemo.emojcenter.FaceConfigInfo
import com.lemo.emojcenter.FaceInitData
import com.lemo.emojcenter.api.FaceNetRequestApi
import com.lemo.emojcenter.bean.*
import com.lemo.emojcenter.constant.FaceEmojOpeType
import com.lemo.emojcenter.constant.FaceLocalConstant
import com.lemo.emojcenter.utils.GsonReturnCallBack
import com.lemo.emojcenter.utils.PathUtils
import com.lemo.emojcenter.utils.SharedPreferencesUtils
import com.lemo.emojcenter.utils.ULogToDevice
import com.lemo.emojcenter.utils.decompression.FaceDecompressionUtil
import com.lemo.emojcenter.utils.download.FaceDownLoadObserver
import com.lemo.emojcenter.utils.download.FaceDownloadInfo
import com.lemo.emojcenter.utils.download.FaceDownloadManager
import com.zhy.http.okhttp.callback.StringCallback
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.Call
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ExecutionException


/**
 * Description:表情包下载
 *
 *
 * Author:wxw
 * Date:2018/2/6.
 */
object FaceDownloadFaceManage {
    private val TAG = "DownloadFaceManage"

    /**
     * 表情包详情信息
     *
     * @param isDown 是否是下载表情时取表情详情
     * @param faceID 表情包id
     */
    fun getDetail(username: String, faceID: String, isDown: Boolean) {
        val emojInfoBean = FaceEmojManage.instance.getEmojInfoByFaceId(faceID)
        //取到表情详情才通知下载成功
        if (emojInfoBean.isNotNull!!) {
            noticeAndupdate(emojInfoBean)
            return
        }
        FaceNetRequestApi.getEmojInfo(faceID, object : GsonReturnCallBack<EmojInfoBean>() {
            override fun onError(call: Call, e: Exception, id: Int) {
                if (isDown) {
                    FaceStatuManage.loadEmojFail(faceID)
                }
            }

            override fun onResponse(response: EmojInfoBean?, id: Int) {
                if (response != null) {
                    SharedPreferencesUtils.getinstance().setStringValue(faceID, Gson().toJson(response))
                }
                if (isDown) {
                    if (response != null) {
                        response.faceId = Integer.parseInt(faceID)
                        noticeAndupdate(response)
                    } else {
                        FaceStatuManage.loadEmojFail(faceID)
                    }
                }
            }
        })
    }


    /**
     * 用户已下载过的表情和收藏的表情
     *
     * @param userID 用户id
     */
    fun downAndSaveHostFace(userID: String) {
        FaceNetRequestApi.getMyFaceAndCollect(userID, object : GsonReturnCallBack<HostFaceBean>() {
            override fun onError(call: Call, e: Exception, id: Int) {
                super.onError(call, e, id)
                Log.d(TAG, "onError: 用户已下载过的表情和收藏的表情")
            }

            override fun onResponse(response: HostFaceBean?, id: Int) {
                super.onResponse(response, id)
                if (response != null) {
                    SharedPreferencesUtils.getinstance().setStringValue(FaceLocalConstant.USER_ID_FACE, userID)
                    FaceInitData.isIsRequestFaceSuc = true
                    //保存表情
                    FaceEmojManage.instance.init(response)
                    FaceEmojManage.instance.saveFace(response)

                    for (i in 0 until response.keys!!.size) {
                        val faceId = response.keys!![i].faceId
                        if (!FaceDownloadFaceManage.isEmojExit(faceId.toString())) {
                            fileDownGetUrl(response.keys!![i].resource, faceId.toString())
                        }
                    }
                    for (i in 0 until response.collects!!.size) {
                        val url = response.collects!![i].master
                        url?.let { downCollectImage(it) }
                    }
                }
            }
        })

    }

    //下载收藏的表情到指定位置
    fun downCollectImage(url: String) {
        val filename = url.substring(url.lastIndexOf("/") + 1, url.length)
        val filePath = FaceConfigInfo.dirCollect + File.separator + filename
        if (!File(filePath).exists()) {
            try {
                FaceThreadPoolManager.newFixThreadPoolDown().execute {
                    try {
                        if (FaceConfigInfo.isDebug) {
                            ULogToDevice.d("down", TAG, "下载url:$url ##到本地：$filePath")
                        }
                        val file = Glide.with(FaceInitData.context).load(url).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get()
                        val target = File(filePath)
                        PathUtils.copy(file, target)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } catch (e: ExecutionException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "downCollectImage: 异常")
                e.printStackTrace()
            }

        }
    }

    //下载表情包表情到指定位置
    fun downFaceImage(url: String, filePath: String) {
        downImage(url, filePath, RequestOptions(), null)
    }

    fun downImage(url: String, filePath: String, requestOptions: RequestOptions?, imageDown: ImageDownListener?) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(filePath)) {
            imageDown?.onFail()
            return
        }
        Log.d(TAG, "downFaceImage: #url:$url #filePath$filePath")
        try {
            FaceThreadPoolManager.newFixThreadPoolDown().execute {
                var isSuccess = false
                try {
                    if (FaceConfigInfo.isDebug) {
                        ULogToDevice.d("down", TAG, "下载表情包图片:$url ##到本地：$filePath")
                    }
                    //删除不完整图片
                    if (!PathUtils.isImageComplete(filePath)) {
                        File(filePath).delete()
                    }
                    val requestBuilder = Glide.with(FaceInitData.context).load(url)
                    if (requestOptions != null) {
                        requestBuilder.apply(requestOptions)
                    }
                    val file = requestBuilder.downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get()
                    val target = File(filePath)
                    PathUtils.copy(file, target)
                    isSuccess = true

                } catch (e: Exception) {
                    isSuccess = false
                    e.printStackTrace()
                }

                val finalIsSuccess = isSuccess
                Observable
                        .empty<Any>()
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete {
                            if (imageDown != null) {
                                if (finalIsSuccess) {
                                    imageDown.onSuccess()
                                } else {
                                    imageDown.onFail()
                                }
                            }
                        }
                        .subscribe()
            }
        } catch (e: Exception) {
            imageDown?.onFail()
            Log.e(TAG, "downFaceImage: 异常")
            e.printStackTrace()
        }

    }


    /**
     * 获取下载地址
     *
     * @param resourse
     * @param faceID
     */
    fun fileDownGetUrl(resourse: String?, faceID: String) {
        if (TextUtils.isEmpty(resourse)) {
            FaceStatuManage.loadEmojFail(faceID)
            return
        }
        val emojOpe = EmojOpeBean(faceID)
        emojOpe.emojOpeType = FaceEmojOpeType.EmojDowning
        emojOpe.downloadStatus = DownloadStatus.download_start
        EventBus.getDefault().postSticky(emojOpe)

        val runnable = {
            //下载:1.获取文件路径
            if (!Thread.currentThread().isInterrupted) {
                try {
                    requestDownUrl(resourse, faceID)
                } catch (e: Exception) {
                    FaceStatuManage.loadEmojFail(faceID)
                }

            }
        }
        FaceThreadPoolManager.newFixThreadPoolDownFile().execute(runnable)
    }

    private fun requestDownUrl(resoursePath: String?, faceID: String) {
        resoursePath?.let {
            FaceNetRequestApi.getDownEmojZipUrl(it, object : StringCallback() {
                override fun onError(call: Call, e: Exception, id: Int) {
                    FaceStatuManage.loadEmojFail(faceID)
                }

                override fun onResponse(response: String, id: Int) = try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.optInt("code") == 12000) {
                        val resultObject = jsonObject.optJSONObject("result")
                        val fileName = resoursePath.substring(resoursePath.lastIndexOf("/") + 1, resoursePath.length)
                        loadFile(resultObject.optString("url"), fileName, faceID)
                    } else {
                        FaceStatuManage.loadEmojFail(faceID)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            })
        }
    }

    /**
     * 下载文件
     *
     * @param fileUrl
     * @param fileName
     * @param faceID
     */
    fun loadFile(fileUrl: String, fileName: String, faceID: String) {
        val isExit = isEmojExit(faceID)
        //存在表情包不下载
        if (isExit) {
            Log.d(TAG, "loadFile: 存在表情包" + faceID)
            getDetail(FaceInitData.userId, faceID, true)
            return
        }
        if (FaceConfigInfo.isDebug) {
            ULogToDevice.d("down", TAG, "下载表情包压缩包faceId（" + faceID + "）网络地址:" + FaceConfigInfo.dirEmojZipRoot + File.separator + fileUrl + " ##到本地：" + fileName)
        }
        FaceDownloadManager.getInstance().download(fileUrl, object : FaceDownLoadObserver() {
            private var progressDown = -1

            override fun onNext(downloadInfo: FaceDownloadInfo) {
                super.onNext(downloadInfo)
                val progress = (downloadInfo.progress * 100 / downloadInfo.total).toInt()
                if (progress != progressDown) {
                    progressDown = progress
                    val emojOpe = EmojOpeBean(faceID)
                    emojOpe.emojOpeType = FaceEmojOpeType.EmojDowning
                    emojOpe.downloadStatus = DownloadStatus.download_run
                    emojOpe.downProgress = progressDown
                    EventBus.getDefault().postSticky(emojOpe)
                }
            }

            override fun onComplete() {
                if (downloadInfo.file != null) {
                    if (downloadInfo.file!!.exists()) {
                        //解压
                        FaceDecompressionUtil.Unzip(fileName, faceID)
                        //获取表情详情
                        getDetail(FaceInitData.userId, faceID, true)
                    } else {
                        FaceStatuManage.loadEmojFail(faceID)
                    }
                }
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                FaceStatuManage.loadEmojFail(faceID)
            }
        }, fileName)

    }

    //通知后台已下载
    private fun noticeAndupdate(emojInfoBean: EmojInfoBean?) {
        //下载成功,通知后台
        FaceNetRequestApi.downloadSuccess(FaceInitData.userId, emojInfoBean!!.faceId.toString(), object : GsonReturnCallBack<Any>() {
            override fun onError(call: Call, e: Exception, id: Int) {
                FaceStatuManage.loadEmojFail(emojInfoBean.faceId.toString())
            }

            override fun onResponse(`object`: Any?, id: Int) {
                FaceStatuManage.loadEmojSuc(emojInfoBean)
            }
        })

    }


    //解压全部失败压缩包
    fun unZipFileAll() {
        val zipFile = File(FaceConfigInfo.dirEmojZipRoot)
        val files = zipFile.listFiles()
        if (files != null) {
            for (i in files.indices) {
                val file = files[i]
                val filepath = file.path
                if (!TextUtils.isEmpty(filepath)) {
                    val idStart = filepath.lastIndexOf("_") + 1
                    var idEnd = filepath.length
                    val fileName = filepath.substring(filepath.lastIndexOf("/") + 1, filepath.length)
                    if (filepath.contains(".")) {
                        val endA = filepath.lastIndexOf(".")
                        if (endA > idStart) {
                            idEnd = endA
                        }
                    }
                    if (filepath.contains("(")) {
                        val endA = filepath.lastIndexOf("(")
                        if (endA > idStart) {
                            idEnd = endA
                        }
                    }
                    val faceId = filepath.substring(idStart, idEnd)
                    //解压表情包
                    val isExit = isEmojExit(faceId)

                    if (!isExit) {
                        FaceDecompressionUtil.Unzip(fileName, faceId)
                    } else {
                        Log.d(TAG, "unZipFileAll: 已存在 faceId:" + filepath)
                        FaceDecompressionUtil.deleteFile(filepath)
                    }
                }
            }
        }
    }

    //表情包是否存在
    fun isEmojExit(faceId: String): Boolean {
        val faceFile = File(FaceConfigInfo.getDirEmoj(faceId))
        //表情包文件不存在才解压
        var faceFiles: Array<File>? = null
        if (faceFile.exists()) {
            faceFiles = faceFile.listFiles()
        }
        val isExit = faceFiles != null && faceFile.listFiles().size >= 8
        if (isExit) {
            Log.d(TAG, "表情包" + faceId + "已存在")
        }
        return isExit
    }

    /**
     * 保存所有表情数据
     */
    fun getAllEmoj() {
        val version = SharedPreferencesUtils.getinstance().getIntValue(FaceLocalConstant.VERSION_EMOJ, 0)
        FaceNetRequestApi.getAllEmojInfo(version, object : GsonReturnCallBack<AllEmojBean>() {
            override fun onError(call: Call, e: Exception, id: Int) {}

            override fun onResponse(response: AllEmojBean?, id: Int) {
                if (response != null) {
                    SharedPreferencesUtils.getinstance().setIntValue(FaceLocalConstant.VERSION_EMOJ, response.version)
                    FaceEmojManage.instance.saveEmojInfoList(response.emojList)
                }
            }
        })
    }

    /**
     * 图片下载监听
     */
    interface ImageDownListener {
        fun onSuccess()

        fun onFail()
    }

}
