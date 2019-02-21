package com.lemo.emojcenter.manage

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.lemo.emojcenter.FaceConfigInfo
import com.lemo.emojcenter.FaceInitData
import com.lemo.emojcenter.R
import com.lemo.emojcenter.bean.*
import com.lemo.emojcenter.constant.FaceEmojType
import com.lemo.emojcenter.constant.FaceLocalConstant
import com.lemo.emojcenter.utils.EmotionUtils
import com.lemo.emojcenter.utils.SharedPreferencesUtils
import com.lemo.emojcenter.utils.decompression.FaceDecompressionUtil
import com.zhy.http.okhttp.utils.Convert
import java.util.*

class FaceEmojManage private constructor() {
    private var mEmojList: MutableList<EmojInfoBean>? = null
    private var mCollectList: MutableList<CollectsBean>? = null

    /**
     * 获取我的表情包
     *
     * @return
     */
    val emojList: MutableList<EmojInfoBean>?
        get() {
            if (mEmojList == null) {
                init()
            }
            return mEmojList
        }

    /**
     * 获取我的收藏
     *
     * @return
     */
    val collectsList: List<CollectsBean>?
        get() {
            if (mCollectList == null) {
                init()
            }
            return mCollectList
        }

    /**
     * 获取我的所有表情
     *
     * @return
     */
    val faceAllMy: HostFaceBean?
        get() {
            var response = HostFaceBean()
            val json = SharedPreferencesUtils.getinstance().getStringValue(FaceLocalConstant.FACE_SELF + FaceInitData.userId)
            if (!TextUtils.isEmpty(json)) {
                try {
                    response = Gson().fromJson(json, HostFaceBean::class.java)
                } catch (e: JsonSyntaxException) {
                    Log.d(TAG, "getFaceAllMy: " + FaceLocalConstant.FACE_SELF + FaceInitData.userId + " 数据类型不是HostFaceBean 格式")
                    e.printStackTrace()
                }

            }
            return response
        }

    init {
        init()
    }

    fun init(faceBean: HostFaceBean?) {
        if (faceBean != null) {
            mEmojList = faceBean.keys as MutableList<EmojInfoBean>?
            mCollectList = faceBean.collects as MutableList<CollectsBean>?
        }
    }

    fun init() {
        mEmojList = ArrayList()
        mCollectList = ArrayList()
        val faceBean = faceAllMy
        if (faceBean != null) {
            mEmojList = faceBean.keys as MutableList<EmojInfoBean>?
            mCollectList = faceBean.collects as MutableList<CollectsBean>?
        }
    }

    fun addCollectsAndSave(collectsBean: CollectsBean) {
        addCollects(collectsBean)
        saveToLocal()
    }

    @Synchronized
    fun addCollects(collectsBean: CollectsBean) {
        if (mCollectList == null) {
            init()
        }
        mCollectList!!.add(collectsBean)
    }

    @Synchronized
    fun addEmojAndSave(keysBean: EmojInfoBean) {
        if (addEmoj(keysBean)) {
            saveToLocal()
        }
    }

    @Synchronized
    fun addEmoj(keysBean: EmojInfoBean): Boolean {
        if (mEmojList == null) {
            init()
        }
        var isExit = false
        for (emojInfoBean in mEmojList!!) {
            if (emojInfoBean.faceId == keysBean.faceId) {
                isExit = true
            }
        }
        //不存在添加
        if (!isExit) {
            mEmojList!!.add(0, keysBean)
            return true
        }
        return false
    }

    fun addEmojList(emojInfoBeanList: List<EmojInfoBean>?) {
        if ((emojInfoBeanList != null) and (emojInfoBeanList!!.size > 0)) {
            for (emojInfoBean in emojInfoBeanList) {
                addEmoj(emojInfoBean)
            }
            saveToLocal()
        }
    }

    @Synchronized
    fun updateCollectsListAndSave(collectsBeanList: MutableList<CollectsBean>) {
        mCollectList = collectsBeanList
        saveToLocal()
    }

    fun addCollectsListAndSave(collectsBeanList: List<CollectsBean>?) {
        if (collectsBeanList != null && collectsBeanList.size > 0) {
            for (collectsBean in collectsBeanList) {
                addCollects(collectsBean)
            }
            saveToLocal()
        }
    }

    @Synchronized
    fun removeEmojByFaceId(context: Context, faceId: Int) {
        if (mEmojList == null) {
            init()
        }
        val iterator = mEmojList!!.iterator()
        while (iterator.hasNext()) {
            val emojInfoBean = iterator.next()
            if (faceId == emojInfoBean.faceId) {
                iterator.remove()
            }
        }
        onDeleteFile(context, faceId.toString())
        saveToLocal()
    }

    @Synchronized
    fun removeCollectsListByIdList(mList: List<Int>) {
        if (mCollectList == null) {
            init()
        }
        if (mCollectList != null) {
            val iterator = mCollectList!!.iterator()
            while (iterator.hasNext()) {
                val collectsBean = iterator.next()
                if (mList.contains(collectsBean.id)) {
                    iterator.remove()
                }
            }
        }
        saveToLocal()
    }

    fun addHostFaceCollectsByMyAddEmojList(addEmojBeanList: List<MyAddEmojBean>) {
        for (addEmojBean in addEmojBeanList) {
            val collectsBean = CollectsBean()
            if (addEmojBean != null && (addEmojBean.master != null || addEmojBean.master != null)) {
                collectsBean.id = addEmojBean.id
                collectsBean.appId = addEmojBean.appId
                collectsBean.cover = addEmojBean.cover
                collectsBean.master = addEmojBean.master
                collectsBean.sortBy = addEmojBean.sortBy
                collectsBean.userId = addEmojBean.userId
                addCollects(collectsBean)
            }
        }
    }

    fun clearCollectsList() {
        if (mCollectList != null) {
            mCollectList!!.clear()
        }
    }

    fun clearEmojList() {
        if (mEmojList != null) {
            mEmojList!!.clear()
        }
    }

    /**
     * 保存表情到本地
     *
     * @param faceBean
     */
    fun saveFace(faceBean: HostFaceBean) {
        SharedPreferencesUtils.getinstance().setStringValue(FaceLocalConstant.FACE_SELF + FaceInitData.userId, Gson().toJson(faceBean))
    }

    //保存我的表情到本地
    @Synchronized
    fun saveToLocal() {

        if (mEmojList != null || mCollectList != null) {
            val face = HostFaceBean()
            face.keys = mEmojList
            face.collects = mCollectList
            saveFace(face)
        }
    }

    //保存emoj详情信息
    fun saveEmojInfoList(emojInfoBeanList: List<EmojInfoBean>?) {
        if (emojInfoBeanList != null) {
            Thread(Runnable {
                for (emojInfoBean in emojInfoBeanList) {
                    SharedPreferencesUtils.getinstance().setStringValue(emojInfoBean.faceId.toString(), Gson().toJson(emojInfoBean).toString())
                }
            }).start()
        }
    }

    //获取保存表情详情
    fun getEmojInfoByFaceId(faceId: String): EmojInfoBean {
        var mEmojInfoBean = EmojInfoBean()
        mEmojInfoBean.faceId = Integer.parseInt(faceId)
        val data = SharedPreferencesUtils.getinstance().getStringValue(faceId)
        if (!TextUtils.isEmpty(data)) {
            mEmojInfoBean = Convert.fromJson(data, EmojInfoBean::class.java)
        }
        return mEmojInfoBean
    }

    companion object {
        private val TAG = "EmojManage"
        private var emojManage: FaceEmojManage? = null

        val instance: FaceEmojManage
            get() {
                if (emojManage == null) {
                    synchronized(FaceEmojManage::class.java) {
                        if (emojManage == null) {
                            emojManage = FaceEmojManage()
                        }
                    }
                }
                return this!!.emojManage!!
            }

        /**
         * 获取所有表情
         *
         * @return
         */
        //经典表情
        //收藏表情
        //表情商城
        //经典表情
        //收藏表情
        //表情商城
        val emojDataAll: MutableList<EmojGroupBean>
            get() {
                val emojGroupList = ArrayList<EmojGroupBean>()
                val emotionMap = EmotionUtils.getEmojiMap(EmotionUtils.EMOTION_CLASSIC_TYPE)
                val collectsBeanList = FaceEmojManage.instance.collectsList
                val keysBeanList = FaceEmojManage.instance.emojList
                val emojGroupEmotion = EmojGroupBean(FaceLocalConstant.FACE_ID_EMOJ, "", "")
                emojGroupEmotion.emojType = FaceEmojType.NORMAL
                if (emotionMap != null) {
                    val iter = emotionMap.entries.iterator()
                    while (iter.hasNext()) {
                        val entry = (iter.next()) as Map.Entry<*, *>
                        val content = entry.key.toString()
                        val resId = entry.value as Int
                        val emojBean = EmojBean(content, resId)
                        emojGroupEmotion.emojiconList.add(emojBean)
                    }
                }
                emojGroupList.add(emojGroupEmotion)
                val emojGroupCollect = EmojGroupBean(FaceLocalConstant.FACE_ID_COLLECT, "", "")
                emojGroupCollect.emojType = FaceEmojType.COLLECTION
                val emojBeanAdd = EmojBean()
                emojBeanAdd.emojType = FaceEmojType.COLLECTION_ADD
                emojBeanAdd.iconRes = R.mipmap.face_tianjia
                emojGroupCollect.emojiconList.add(emojBeanAdd)
                /**
                 * 收藏表情
                 */
                if (collectsBeanList != null) {
                    for (collectsBean in collectsBeanList) {
                        val emojBean = EmojBean(collectsBean)
                        emojBean.name = FaceLocalConstant.COLLECT_TYPE_DESC
                        emojBean.width = collectsBean.imageWidth
                        emojBean.height = collectsBean.imageHeight
                        emojGroupCollect.emojiconList.add(emojBean)
                    }
                }
//                emojGroupList.add(emojGroupCollect)
                if (keysBeanList != null) {
                    for (keysBean in keysBeanList) {
                        val faceIdStr = keysBean.faceId.toString()
                        val path = FaceConfigInfo.getPathFaceIcon(faceIdStr)
                        val url = keysBean.icon
                        val emojGroupExpression = EmojGroupBean(faceIdStr, path, url!!)
                        emojGroupExpression.emojType = FaceEmojType.EXPRESSION
                        val faceItemList = keysBean.itemList
                        if (faceItemList != null) {
                            for (faceItem in faceItemList) {
                                val emojBean = EmojBean(faceIdStr, faceItem)
                                emojGroupExpression.emojiconList.add(emojBean)
                            }
                        }
                        emojGroupList.add(emojGroupExpression)
                    }
                }
                return emojGroupList
            }
    }

    //删除表情文件
    fun onDeleteFile(context: Context, filePackName: String) {
        FaceDecompressionUtil.deleteDir(context, filePackName)
    }
}
