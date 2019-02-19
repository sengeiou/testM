package com.lemo.emojcenter.manage

import android.util.Log
import com.lemo.emojcenter.bean.*
import com.lemo.emojcenter.constant.FaceEmojOpeType
import com.lemo.emojcenter.constant.FaceLocalConstant
import org.greenrobot.eventbus.EventBus

/**
 * 表情包状态
 * Created by wangru
 * Date: 2018/4/19  13:19
 * mail: 1902065822@qq.com
 * describe:
 */

object FaceStatuManage {

    private val TAG = FaceStatuManage::class.java.simpleName

    //表情包下载成功
    fun loadEmojSuc(emojInfoBean: EmojInfoBean) {
        //表情详情是否完整
        if (emojInfoBean.isNotNull!!) {
            FaceEmojManage.instance.addEmojAndSave(emojInfoBean)
            val emojOpe = EmojOpeBean(emojInfoBean.faceId.toString())
            emojOpe.emojOpeType = FaceEmojOpeType.EmojOver
            EventBus.getDefault().postSticky(emojOpe)
            Log.d(TAG, "表情包:通知后台下载成功" + emojInfoBean.faceId)
        }
    }

    //表情包下载失败
    fun loadEmojFail(faceID: String) {
        val emojOpe = EmojOpeBean(faceID)
        emojOpe.emojOpeType = FaceEmojOpeType.EmojDowning
        emojOpe.downloadStatus = DownloadStatus.download_error
        EventBus.getDefault().postSticky(emojOpe)
    }


    /**
     * 添加收藏图片成功
     */
    fun collectAddSuc(myAddEmojBean: MyAddEmojBean) {
        val collectsBean = CollectsBean(myAddEmojBean)
        FaceEmojManage.instance.addCollectsAndSave(collectsBean)
        val emojOpe = EmojOpeBean(FaceLocalConstant.IMGTYPE_COLLECT.toString())
        emojOpe.emojOpeType = FaceEmojOpeType.CollectsAdd
        EventBus.getDefault().postSticky(emojOpe)
    }

    /**
     * 删除收藏表情成功
     *
     * @param ids
     */
    fun collectDeleteSuc(ids: List<Int>) {
        FaceEmojManage.instance.removeCollectsListByIdList(ids)
        val emojOpe = EmojOpeBean(FaceLocalConstant.IMGTYPE_COLLECT.toString())
        emojOpe.emojOpeType = FaceEmojOpeType.CollectsDelete
        EventBus.getDefault().postSticky(emojOpe)
    }

    fun collectSort(mDatas: List<MyAddEmojBean>) {
        FaceEmojManage.instance.clearCollectsList()
        FaceEmojManage.instance.addHostFaceCollectsByMyAddEmojList(mDatas)
        val emojOpe = EmojOpeBean(FaceLocalConstant.IMGTYPE_COLLECT.toString())
        emojOpe.emojOpeType = FaceEmojOpeType.CollectsSort
        EventBus.getDefault().postSticky(emojOpe)
    }
}
