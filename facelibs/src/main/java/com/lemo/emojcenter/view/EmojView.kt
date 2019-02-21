package com.lemo.emojcenter.view

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.google.gson.Gson
import com.lemo.emojcenter.FaceInitData
import com.lemo.emojcenter.R
import com.lemo.emojcenter.activity.FaceEmojStoreActivity
import com.lemo.emojcenter.activity.FaceMyAddEmojActivity
import com.lemo.emojcenter.activity.FaceMyEmojActivity
import com.lemo.emojcenter.api.FaceNetRequestApi
import com.lemo.emojcenter.bean.*
import com.lemo.emojcenter.constant.FaceEmojOpeType
import com.lemo.emojcenter.constant.FaceEmojType
import com.lemo.emojcenter.constant.FaceLocalConstant
import com.lemo.emojcenter.manage.FaceEmojManage
import com.lemo.emojcenter.utils.GsonReturnCallBack
import com.lemo.emojcenter.utils.ImageUtils
import com.lemo.emojcenter.utils.SharedPreferencesUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.Call
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * Description:表情键盘
 * Author:wxw
 * Date:2018/1/30.
 */
class EmojView : LinearLayout {
    protected var listener: EmojMenuListener? = null
    private var CurrentPosition = 0
    private lateinit var mViewPager: EmojPagerView
    private lateinit var mIndicator: IndicatorView
    private lateinit var mEmojTabBarScroll: EmojTabBarScroll
    private var mEmojGroupBeanList: MutableList<EmojGroupBean>? = ArrayList()
    private lateinit var mAddMore: View
    private lateinit var mSettingImage: View
    private var mDelete = false
    private var itemCurrentId: String? = null
    private var clicked = false
    private var isFirstSetDatas = true
    private var isFirstInit = true
    private var clickCallBack: ((emotionFace: EmotionFaceSendInfo) -> Unit)? = null

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.face_fragment_main_emotion1, this)
        initView()
        EventBus.getDefault().register(this)
        initValue()
        initDatas()
        initListener()
        setData()
    }

    private fun initValue() {
        SharedPreferencesUtils.init(context)
    }

    /**
     * 初始化view控件
     */
    fun initView() {
        mViewPager = findViewById(R.id.vp_emotionview_layout)
        mAddMore = findViewById(R.id.add_more_image)
        mSettingImage = findViewById(R.id.setting_image)
        mIndicator = findViewById(R.id.emoj_point_group)
        mEmojTabBarScroll = findViewById(R.id.tab_bar_emoj)
    }

    private fun setData() {
        Observable.create(ObservableOnSubscribe<List<EmojGroupBean>> { e ->
            mEmojGroupBeanList = FaceEmojManage.emojDataAll
            e.onNext(mEmojGroupBeanList!!)
            e.onComplete()
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(object : Observer<List<EmojGroupBean>> {
            override fun onSubscribe(d: Disposable) {}

            override fun onNext(emojGroupBeans: List<EmojGroupBean>) {
                //        mViewPager.setOffscreenPageLimit(mEmojGroupBeanList.size()-1);
                mViewPager.setEmojPagerViewListener(EmojiconPagerViewListener())
                mViewPager.init(mEmojGroupBeanList, 7, 4)

                mEmojTabBarScroll.setData(mEmojGroupBeanList)
                if (isFirstSetDatas) {
                    if (mEmojGroupBeanList!!.size >= 0) {
                        switchTab(0)
                        val pageSize = mViewPager.getPageSize(mEmojGroupBeanList!![0])
                        mIndicator.updateIndicator(pageSize)
                        mIndicator.selectTo(0)
                    }
                    isFirstSetDatas = false
                } else {
                    setCurrentTab()
                }
            }


            override fun onError(e: Throwable) {}

            override fun onComplete() {
                //首次进来没请求成功才重新请求
                if (!FaceInitData.isIsRequestFaceSuc) {
                    getHostFace()
                }
            }
        })
    }


    private fun getHostFace() {
        FaceNetRequestApi.getMyFaceAndCollect(FaceInitData.userId, object : GsonReturnCallBack<HostFaceBean>() {
            override fun onError(call: Call, e: Exception, id: Int) {
                super.onError(call, e, id)
            }

            override fun onResponse(response: HostFaceBean?, id: Int) {
                super.onResponse(response, id)
                if (response != null) {
                    FaceInitData.isIsRequestFaceSuc = true
                    FaceEmojManage.instance.saveFace(response)
                    FaceEmojManage.instance.init()
                    setData()
                }
            }
        })
    }

    /**
     * 初始化监听器
     */
    fun initListener() {

        mAddMore.setOnClickListener { context.startActivity(Intent(context, FaceEmojStoreActivity::class.java)) }
        mSettingImage.setOnClickListener {
            context.startActivity(Intent(context, FaceMyEmojActivity::class.java))
            mDelete = true
        }
        mViewPager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                switchTab(position)
            }

            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })
        mEmojTabBarScroll.setTabBarItemClickListener { position ->
            clicked = true
            mViewPager.setGroupPosition(position)
        }
    }

    private fun switchTab(position: Int) {
        if (position < mEmojGroupBeanList!!.size) {
            itemCurrentId = mEmojGroupBeanList!![position].faceId
        }
        mEmojTabBarScroll.selectedTo(position, clicked)
        clicked = false
    }

    private fun switchFragment(position: Int) {
        mViewPager.setGroupPosition(position)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onEvent(emojOpe: EmojOpeBean) {
        synchronized(EmojView::class.java) {
            if (emojOpe.emojOpeType!!.isFaceChange) {
                if (emojOpe.emojOpeType == FaceEmojOpeType.EmojDelete) {
                    var position = -1
                    //移除底部icon
                    if (mEmojGroupBeanList != null) {
                        for (i in mEmojGroupBeanList!!.indices) {
                            val faceIdTemp = mEmojGroupBeanList!![i].faceId
                            //是当前页面
                            if (!TextUtils.isEmpty(faceIdTemp) && faceIdTemp == emojOpe.faceId && faceIdTemp == itemCurrentId) {
                                // Log.d(TAG, "faceId: Remove faceId:" + faceIdTemp + "#mEmojGroupBeanList:" + new Gson().toJson(mEmojGroupBeanList).toString());
                                position = i - 1
                                if (position >= 0) {
                                    itemCurrentId = mEmojGroupBeanList!![position].faceId
                                }
                                break
                            }
                        }
                    }
                }
                setData()
            }
        }
    }


    private fun setCurrentTab() {
        for (i in mEmojGroupBeanList!!.indices) {
            if (TextUtils.equals(itemCurrentId, mEmojGroupBeanList!![i].faceId)) {
                switchTab(i)
                switchFragment(i)
                val pageSize = mViewPager.getPageSize(mEmojGroupBeanList!![i])
                mIndicator.updateIndicator(pageSize)
                Log.d(TAG, "setCurrentTab: " + i)
                break
            }
        }
    }

    /**
     * 数据操作
     */
    fun initDatas() {
        //记录底部默认选中第一个
        CurrentPosition = 0
        SharedPreferencesUtils.getinstance().setIntValue(CURRENT_POSITION_FLAG, CurrentPosition)
    }

    fun onDestoryEmojView() {
        EventBus.getDefault().unregister(this)
    }

    fun setEmojiconMenuListener(listener: EmojMenuListener) {
        this.listener = listener
    }

    interface EmojMenuListener {
        fun onExpressionClicked(emojicon: EmojBean)

        fun onDeleteImageClicked()
    }

    private inner class EmojiconPagerViewListener : EmojPagerView.EmojPagerViewListener {
//        override fun onExpressionClicked(emojicon: EmojBean?) {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        }

        override fun onPagerViewInited(groupMaxPageSize: Int, firstGroupPageSize: Int) {
            if (isFirstInit) {
                mIndicator.updateIndicator(groupMaxPageSize)
                mEmojTabBarScroll.selectedTo(0, true)
                isFirstInit = false
            }
        }

        override fun onGroupPositionChanged(groupPosition: Int, pagerSizeOfGroup: Int) {
            mIndicator.updateIndicator(pagerSizeOfGroup)
            switchTab(groupPosition)
        }

        override fun onGroupInnerPagePostionChanged(oldPosition: Int, newPosition: Int) {
            mIndicator.selectTo(oldPosition, newPosition)
        }

        override fun onGroupPagePostionChangedTo(position: Int) {
            mIndicator.selectTo(position)
        }

        override fun onGroupMaxPageSizeChanged(maxCount: Int) {
            mIndicator.updateIndicator(maxCount)
        }

        override fun onDeleteImageClicked() {
            if (listener != null) {
                listener!!.onDeleteImageClicked()
            }
            val emotionEmoj = EmotionFaceSendInfo()
            emotionEmoj.type = FaceLocalConstant.IMGTYPE_EMOJ_DELETE
            postInfo(emotionEmoj)
        }

        override fun onExpressionClicked(emojicon: EmojBean?) {
            if (listener != null) {
                emojicon?.let { listener!!.onExpressionClicked(it) }
            }
            Log.e(TAG, "onExpressionClicked: EmojBean=" + Gson().toJson(emojicon).toString())
            when (emojicon?.emojType) {
                FaceEmojType.NORMAL -> {
                    val emotionName = emojicon.emojiText
                    val emotionEmoj = EmotionFaceSendInfo()
                    emotionEmoj.type = FaceLocalConstant.IMGTYPE_EMOJ
                    emotionEmoj.name = emotionName
                    postInfo(emotionEmoj)
                }
                FaceEmojType.COLLECTION -> if (emojicon.emojType != FaceEmojType.COLLECTION_ADD) {
                    val emotionFaceSendInfo = EmotionFaceSendInfo()
                    emotionFaceSendInfo.pathLocal = emojicon.pathLocal
                    emotionFaceSendInfo.url = emojicon.url
                    emotionFaceSendInfo.type = FaceLocalConstant.IMGTYPE_COLLECT
                    emotionFaceSendInfo.name = emojicon.name
                    var width = emojicon.width
                    var height = emojicon.height
                    if (width == 0 || height == 0) {
                        emojicon.pathLocal?.let {
                            ImageUtils.getImageSize(it)
                        }?.let {
                            width = it.width
                            height = it.height
                        }
                    }
                    emotionFaceSendInfo.width = width
                    emotionFaceSendInfo.height = height
                    Log.d(TAG, "表情点击收藏:" + Gson().toJson(emotionFaceSendInfo).toString())
                    postInfo(emotionFaceSendInfo)
                }
                FaceEmojType.EXPRESSION -> {
                    val emotionFace = EmotionFaceSendInfo()
                    emotionFace.pathLocal = emojicon.pathLocal
                    emotionFace.url = emojicon.url
                    emotionFace.type = FaceLocalConstant.IMGTYPE_FACE
                    emotionFace.name = emojicon.name
                    val imageSize = emojicon.pathLocal?.let { ImageUtils.getImageSize(it) }
                    if (imageSize != null) {
                        emotionFace.width = imageSize.width
                    }
                    if (imageSize != null) {
                        emotionFace.height = imageSize.height
                    }
                    Log.d(TAG, "表情点击收藏:" + Gson().toJson(emotionFace).toString())
                    postInfo(emotionFace)
                }

                FaceEmojType.COLLECTION_ADD -> context.startActivity(Intent(context, FaceMyAddEmojActivity::class.java))
                else -> {
                }
            }
        }
    }

    //发送事件和回调
    private fun postInfo(emotionFaceSendInfo: EmotionFaceSendInfo) {
        EventBus.getDefault().post(emotionFaceSendInfo)
        Handler(Looper.getMainLooper()).post {
            clickCallBack?.invoke(emotionFaceSendInfo)
        }
    }

    fun setClickCallBack(callback: (emotionFace: EmotionFaceSendInfo) -> Unit) {
        clickCallBack = callback
    }

    companion object {
        //当前被选中底部tab
        private val CURRENT_POSITION_FLAG = "CURRENT_POSITION_FLAG"
        private val TAG = "EmotionMainFragment"
    }

}


