package com.mogujie.tt.imservice.service

import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import com.app.common.api.RequestFileManager
import com.leimo.imgcompress.CompressImage
import com.mogujie.tt.api.request.ApiUrl
import com.mogujie.tt.config.NetConstant
import com.mogujie.tt.config.PathConstant
import com.mogujie.tt.config.SysConstant
import com.mogujie.tt.imservice.entity.AudioMessage
import com.mogujie.tt.imservice.entity.ImageMessage
import com.mogujie.tt.imservice.entity.VideoMessage
import com.mogujie.tt.imservice.event.MessageEvent
import com.mogujie.tt.utils.Logger
import com.mogujie.tt.utils.SDPathUtil
import com.youke.yingba.base.api.upload.FileUpLoadObserver
import de.greenrobot.event.EventBus
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * @author : yingmu on 15-1-12.
 * @email : yingmu@mogujie.com.
 */
class LoadImageService : IntentService {

    constructor() : super("LoadImageService") {}

    constructor(name: String) : super(name) {}

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call [.stopSelf].
     *
     * @param intent The value passed to [               ][android.content.Context.startService].
     */
    override fun onHandleIntent(intent: Intent?) {
        val obj = intent!!.getSerializableExtra(SysConstant.UPLOAD_INTENT_PARAMS)
        if (obj is ImageMessage) {
            uploadImage(obj)
        }
        if(obj is AudioMessage){
            upLoadAudio(obj)
        }
        if (obj is VideoMessage) {
            uploadVedio(obj)
        }
    }


    @SuppressLint("CheckResult")
    private fun uploadImage(messageInfo: ImageMessage) {
        Log.d(TAG, "uploadImage: " + messageInfo.path)
        if (!File(messageInfo.path).exists()) {
            EventBus.getDefault().post(MessageEvent(MessageEvent.Event.IMAGE_UPLOAD_FAILD, messageInfo))
        } else {
            val pathPng = com.leimo.imgcompress.utils.SDPathUtil.getSDCardPublicDir("imageToPng") + File.separator + "webp_evme_" + System.currentTimeMillis() + ".png"
            Observable.create(ObservableOnSubscribe<String> { emitter ->
                val pathCompres = CompressImage.Builder().path(messageInfo.path).widthMax(1280).heightMax(1280).lengthMax(300).build().compress()
                emitter.onNext(pathCompres)
                emitter.onComplete()
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe {
                        messageInfo.path = it
                        upImage(messageInfo)
                    }
        }

    }

    private fun upImage(messageInfo: ImageMessage) {
        RequestFileManager.uploadFileByKey(ApiUrl.UPLOAD_FILE, "fileVoice", File(messageInfo.path), object : FileUpLoadObserver<String>() {
            override fun onUpLoadSuccess(t: String) {
                logger.d("上传图片完成：${t}")
                val jsonObj = JSONObject(t)
                val code = jsonObj.optInt("code")
                val imgUrl = jsonObj.optString("data")
                if (code == NetConstant.REQUEST_SUCCESS && !TextUtils.isEmpty(imgUrl)) {
                    messageInfo.url = imgUrl
                    EventBus.getDefault().post(MessageEvent(MessageEvent.Event.IMAGE_UPLOAD_SUCCESS, messageInfo))
                } else {
                    EventBus.getDefault().post(MessageEvent(MessageEvent.Event.IMAGE_UPLOAD_FAILD, messageInfo))
                }
            }

            override fun onUpLoadFail(throwable: Throwable) {
                Log.d(TAG, "onUpLoadFail: ", throwable)
                EventBus.getDefault().post(MessageEvent(MessageEvent.Event.IMAGE_UPLOAD_FAILD, messageInfo))
            }

            override fun onProgress(up: Long, total: Long) {
                Log.d(TAG, "onProgress: up=$up")
            }

            override fun onSubscribe(d: Disposable) {}
        })
    }

//    @SuppressLint("CheckResult")
//    private fun uploadAudio(messageInfo: AudioMessage) {
//        Log.d(TAG, "uploadImage: " + messageInfo.audioPath)
//        if (!File(messageInfo.audioPath).exists()) {
//            EventBus.getDefault().post(MessageEvent(MessageEvent.Event.AUDIO_UPLOAD_FAILD, messageInfo))
//        } else {
//            Observable.create(ObservableOnSubscribe<String> { emitter ->
//                val pathCompres = CompressImage.Builder().path(messageInfo.audioPath).widthMax(1280).heightMax(1280).lengthMax(300).build().compress()
//                emitter.onNext(pathCompres)
//                emitter.onComplete()
//            })
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(Schedulers.io())
//                    .subscribe {
//                        messageInfo.audioPath = it
//                        upAudio(messageInfo)
//                    }
//        }
//    }

    //上传语音流
    private fun upLoadAudio(messageInfo: AudioMessage) {
        RequestFileManager.uploadFileByKey(ApiUrl.UPLOAD_FILE, "fileVoice", File(messageInfo.audioPath), object : FileUpLoadObserver<String>() {
            override fun onUpLoadSuccess(t: String) {
                logger.d("上传语音完成：${t}")
                val jsonObj = JSONObject(t)
                val code = jsonObj.optInt("code")
                val audioUrl = jsonObj.optString("data")
                if (code == NetConstant.REQUEST_SUCCESS && !TextUtils.isEmpty(audioUrl)) {
                    messageInfo.audioUrl = audioUrl
                    EventBus.getDefault().post(MessageEvent(MessageEvent.Event.AUDIO_UPLOAD_SUCCESS, messageInfo))
                } else {
                    EventBus.getDefault().post(MessageEvent(MessageEvent.Event.AUDIO_UPLOAD_FAILD, messageInfo))
                }
            }

            override fun onUpLoadFail(throwable: Throwable) {
                Log.d(TAG, "onUpLoadFail: ", throwable)
                EventBus.getDefault().post(MessageEvent(MessageEvent.Event.AUDIO_UPLOAD_FAILD, messageInfo))
            }

            override fun onProgress(up: Long, total: Long) {
                Log.d(TAG, "onProgress: up=$up")
            }

            override fun onSubscribe(d: Disposable) {}
        })
    }

    private fun uploadVedio(messageInfo: VideoMessage) {
        Log.d(TAG, "uploadVedio: " + messageInfo.toString())
        RequestFileManager.uploadFileByKey(ApiUrl.UPLOAD_FILE, "fileVoice", File(messageInfo.path), object : FileUpLoadObserver<String>() {
            override fun onUpLoadSuccess(t: String) {
                logger.i("upload video return,result=$t")
                val jsonObj = JSONObject(t)
                val code = jsonObj.optInt("code")
                val url = jsonObj.optString("data")
                if (code == NetConstant.REQUEST_SUCCESS && !TextUtils.isEmpty(url)) {
                    logger.i("upload video succcess,videourl is %s", url)
                    messageInfo.url = url
                    upThumbnailImage(messageInfo)
                } else {
                    logger.i("upload video faild,cause by result is empty/null$t")
                    EventBus.getDefault().post(MessageEvent(MessageEvent.Event.VIDEO_UPLOAD_FAILD, messageInfo))
                }
            }

            override fun onUpLoadFail(throwable: Throwable) {
                Log.d(TAG, "onUpLoadFail: ", throwable)
                EventBus.getDefault().post(MessageEvent(MessageEvent.Event.VIDEO_UPLOAD_FAILD, messageInfo))
            }

            override fun onProgress(up: Long, total: Long) {
                Log.d(TAG, "onProgress: up=$up")
            }

            override fun onSubscribe(d: Disposable) {}
        })
    }

    //上传视频缩略图
    private fun upThumbnailImage(messageInfo: VideoMessage) {
        val bitmap = ThumbnailUtils.createVideoThumbnail(messageInfo.path, MediaStore.Video.Thumbnails.MICRO_KIND)//创建一个视频缩略图
        val fileName = SDPathUtil.getFileNameForPath(messageInfo.path)
        val thumbnailPath = PathConstant.getVideoThumbnailDir(applicationContext) + "/" + fileName + ".jpg"
        saveBitmapFile(bitmap, thumbnailPath)

        RequestFileManager.uploadFileByKey(ApiUrl.UPLOAD_FILE, "fileVoice", File(messageInfo.path), object : FileUpLoadObserver<String>() {
            override fun onUpLoadSuccess(t: String) {
               val root = JSONObject(t)
                val code = root.optInt("code")
                val imgUrl = root.optString("data")
                if (code == NetConstant.REQUEST_SUCCESS && !TextUtils.isEmpty(imgUrl)) {
                    messageInfo.thumbUrl = imgUrl
                    EventBus.getDefault().post(MessageEvent(MessageEvent.Event.VIDEO_UPLOAD_SUCCESS, messageInfo))
                }
            }

            override fun onUpLoadFail(throwable: Throwable) {
                EventBus.getDefault().post(MessageEvent(MessageEvent.Event.VIDEO_UPLOAD_SUCCESS, messageInfo))
            }

            override fun onProgress(up: Long, total: Long) {

            }

            override fun onSubscribe(d: Disposable) {

            }
        })
    }

    fun saveBitmapFile(bitmap: Bitmap?, path: String) {
        val file = File(path)//将要保存图片的路径
        try {
            if (bitmap != null) {
                val bos = BufferedOutputStream(FileOutputStream(file))
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                bos.flush()
                bos.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    companion object {
        private val TAG = "LoadImageService"
        private val logger = Logger.getLogger(LoadImageService::class.java)
    }
}
