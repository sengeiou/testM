package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.lemo.emojcenter.constant.FaceLocalConstant
import com.lemo.emojcenter.utils.EmotionUtils
import com.lemo.emojcenter.utils.SpanStringUtils
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.mogujie.tt.app.IMApplication
import com.mogujie.tt.config.*
import com.mogujie.tt.db.entity.GroupEntity
import com.mogujie.tt.db.entity.MessageEntity
import com.mogujie.tt.db.entity.PeerEntity
import com.mogujie.tt.db.entity.UserEntity
import com.mogujie.tt.imservice.entity.*
import com.mogujie.tt.imservice.event.MessageEvent
import com.mogujie.tt.imservice.event.PriorityEvent
import com.mogujie.tt.imservice.event.SelectEvent
import com.mogujie.tt.imservice.event.UnreadEvent
import com.mogujie.tt.imservice.manager.IMUnreadMsgManager
import com.mogujie.tt.imservice.service.IMService
import com.mogujie.tt.imservice.support.IMServiceConnector
import com.mogujie.tt.protobuf.helper.EntityChangeEngine
import com.mogujie.tt.ui.adapter.album.ImageItem
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.ChatAdapterTwo
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.constant.IConstants.MESSAGE_TO_CHAT
import com.qingmeng.mengmeng.utils.*
import com.qingmeng.mengmeng.utils.audio.AudioRecordManager
import com.qingmeng.mengmeng.utils.audio.MediaManager
import com.qingmeng.mengmeng.view.dialog.DialogCommon
import com.qingmeng.mengmeng.view.dialog.PopChatImg
import de.greenrobot.event.EventBus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_my_message_chat.*
import kotlinx.android.synthetic.main.layout_head.*
import kotlinx.android.synthetic.main.view_dialog_sound_volume.*
import kotlinx.android.synthetic.main.view_tips.*
import org.jetbrains.anko.startActivity
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


/**
 *  Description :设置 - 消息 - 聊天界面

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
@SuppressLint("CheckResult")
class MyMessageChatActivity : BaseActivity() {
    private lateinit var mKeyBoardUtil: KeyBoardUtil            //系统键盘工具
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: ChatAdapterTwo
    //    private lateinit var mPageAdapter: PagerAdapter
    private lateinit var mAudioRecordManager: AudioRecordManager //语音工具类
    private var mMediaManager = MediaManager
    private lateinit var mSoundVolumeDialog: Dialog             //语音弹出框
    private lateinit var mSoundVolumeImg: ImageView
    private lateinit var mSoundVolumeLayout: LinearLayout
    private var mCanSendAudio = false                           //是否可以发送语音
    private var mIsAutomaticSendAudio = false                   //是否自动发送的语音
    private lateinit var mImgSeePopChat: PopChatImg             //最近图片pop
    private var y1 = 0                                          //手指坐标
    private var y2 = 0
    private var mExpressionOrFunction = 0                       //变量 0默认（都不受理） 1表情点击 2工具+点击
    //    private var mFragmentList = ArrayList<Fragment>()           //表情fragment
//    private val mTabTitles = arrayOf("", "")                    //tabLayout头部 初始化两个
    private var mRecyclerViewIsBottom = true                    //RecyclerView在底部
    //    private var albumHelper: AlbumHelper? = null                //相册数据
//    private var albumList: MutableList<ImageBucket>? = null
    private var mBundle: Bundle? = null                         //品牌详情内容
    private var mIsSystemMotification = false                   //是否是系统通知
    private val mTextMessage = TextMessage()                    //假文字消息
    private val mBrandMessage = BrandMessage()                  //假详情
    private var mImageView: ImageView? = null                   //当前正在播放的语音动画

    companion object {
        var mAvatar = ""                                        //默认发送者头像
        var instance: MyMessageChatActivity? = null             //当前aty
    }

    /**
     * 消息用到的
     */
    private var currentSessionKey: String? = null
    private var mImService: IMService? = null
    private var loginUser: UserEntity? = null
    private var peerEntity: PeerEntity? = null
    private var msgObjectList = ArrayList<Any>()
    private var historyTimes = 0
    private val imServiceConnector = object : IMServiceConnector() {
        override fun onIMServiceConnected() {
            mImService = this.imService
            initMsgData()
            peerEntity?.let {
                mImService?.contactManager?.reqGetDetaillUsers(listOf(it.peerId))
            }
        }

        override fun onServiceDisconnected() {}
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_my_message_chat
    }

    override fun initObject() {
        super.initObject()

        instance = this
        val title = intent.getStringExtra("title") ?: ""
        setHeadName(title)
        tvViewTips.text = getString(R.string.my_message_notification_null_tips)
        mAudioRecordManager = AudioRecordManager.getInstance(this)
        //系统通知隐藏聊天功能
        if (title == getString(R.string.systemNotification)) {
            mIsSystemMotification = true
            llMyMessageChatAll.visibility = View.GONE
        }
        mBundle = intent.getBundleExtra("bundle")
        if (mBundle != null) {
            mAvatar = mBundle!!.getString("avatar") ?: ""
        } else {
            mAvatar = intent.getStringExtra("avatar") ?: ""
        }
        //实例化键盘工具
        mKeyBoardUtil = KeyBoardUtil(this, etMyMessageChatContent)
//        //表情里添加fragment
//        mTabTitles.forEachIndexed { index, _ ->
//            mFragmentList.add(MyMessageChatExpressionTabLayoutFragment())
//            (mFragmentList[index] as MyMessageChatExpressionTabLayoutFragment).setContent("$index")
//        }
        //初始化音量对话框
        initSoundVolumeDlg()
//        //初始化相册
//        initAlbumHelper()
        initAdapter()
        httpLoad()

        /**
         * 消息用到的
         */
        val bundle = intent.extras
        currentSessionKey = bundle!!.getString(IntentConstant.KEY_SESSION_KEY)
        peerEntity = bundle.getSerializable(IntentConstant.KEY_PEERENTITY) as? PeerEntity
        imServiceConnector.connect(this)
        EventBus.getDefault().register(this)
    }

    /**
     * 消息相关信息
     */
    private fun initMsgData() {
        historyTimes = 0
        loginUser = mImService?.loginManager?.loginInfo
        mAdapter.setImService(mImService, loginUser)
        if (peerEntity == null) {
            peerEntity = mImService!!.sessionManager.findPeerEntity(currentSessionKey)
        }
        //头像、历史消息加载、取消通知
//        setTitleByUser()
        reqHistoryMsg()
        mImService?.unReadMsgManager?.readUnreadSession(currentSessionKey)
        mImService?.notificationManager?.cancelSessionNotifications(currentSessionKey)
    }

//    /**
//     * 初始化数据（相册,表情,数据库相关）
//     */
//    private fun initAlbumHelper() {
//        albumHelper = AlbumHelper.getHelper(this)
//        albumList = albumHelper!!.getImagesBucketList(false)
//    }


    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            onBackPressed()
        }

        //输入框上部分点击事件
        rvMyMessageChat.setOnTouchListener { _, _ ->
            hiddenViewAndInputKeyboard()
            false
        }

        //RecyclerView滑动监听
        rvMyMessageChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            internal var lastVisibleItemPosition: Int = 0

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //滑到顶部了
                if (!recyclerView.canScrollVertically(-1)) {
                    onPullDownToRefresh()
                }
                //没有滑动&&在最后一个item
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItemPosition + 1 == mAdapter.itemCount) {
                    mRecyclerViewIsBottom = true
                    tvMyMessageChatTips.visibility = View.GONE
                } else {
                    mRecyclerViewIsBottom = false
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
            }
        })

        //音频
        ivMyMessageChatAudio.setOnClickListener {
            //判断是否有权限
            PermissionUtils.audio(this) {
                PermissionUtils.readAndWrite(this) {
                    //表情和工具布局隐藏 关闭软键盘
                    hiddenViewAndInputKeyboard()
                    //按住说话不显示就显示 反之隐藏
                    tvMyMessageChatClickSay.visibility = if (tvMyMessageChatClickSay.visibility == View.GONE) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
            }
        }

        //按住说话
        tvMyMessageChatClickSay.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {    //按下
                    y1 = event.y.toInt()
                    mIsAutomaticSendAudio = false
                    //设置点击背景
                    v.setBackgroundResource(R.drawable.ripple_bg_drawable_graydark_radius18)
                    (v as TextView).text = getString(R.string.release_to_over)
                    mSoundVolumeImg.visibility = View.VISIBLE
                    mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_01)
                    mSoundVolumeLayout.setBackgroundResource(R.drawable.view_dialog_sound_volume_default_bg)
                    //录音
                    mAudioRecordManager.readyAudio(IConstants.DIR_AUDIO_STR, {
                        onReceiveMaxVolume(it)
                    }, { path, recordTime ->
                        //超出60秒的回调
                        //自动发送语音 设置松开手后就不做处理
                        mIsAutomaticSendAudio = true
                        v.setBackgroundResource(R.drawable.ripple_bg_drawable_gray_radius18)
                        v.text = getString(R.string.hold_to_talk)
                        mSoundVolumeDialog.dismiss()
                        onRecordVoiceEnd(path, recordTime)
                    })
                    //显示弹框
                    mSoundVolumeDialog.show()
                    true
                }
                MotionEvent.ACTION_MOVE -> {    //移动
                    y2 = event.y.toInt()
                    //这里没有自动发送语音再设置相关值
                    if (!mIsAutomaticSendAudio) {
                        //向上移动180就改变提示
                        if (y1 - y2 > 180) {
                            mCanSendAudio = false
                            (v as TextView).text = getString(R.string.cancel_to_send)
                            mSoundVolumeImg.visibility = View.GONE
                            mSoundVolumeLayout.setBackgroundResource(R.drawable.view_dialog_sound_volume_cancel_bg)
                        } else {
                            mCanSendAudio = true
                            (v as TextView).text = getString(R.string.release_to_over)
                            mSoundVolumeImg.visibility = View.VISIBLE
                            mSoundVolumeLayout.setBackgroundResource(R.drawable.view_dialog_sound_volume_default_bg)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {  //松开
                    v.setBackgroundResource(R.drawable.ripple_bg_drawable_gray_radius18)
                    (v as TextView).text = getString(R.string.hold_to_talk)
                    //这里没有自动发送语音松开手再发送
                    if (!mIsAutomaticSendAudio) {
                        //发送语音
                        if (mCanSendAudio) {
                            //如果语音时间够的话就发送语音
                            if (mAudioRecordManager.getRecordTime() > 1) { //真.发送
                                mSoundVolumeDialog.dismiss()
                                //释放录音
                                mAudioRecordManager.releaseAudio { path, recordTime ->
                                    if (mAudioRecordManager.canSendAudio) {
                                        //发送语音
                                        onRecordVoiceEnd(path, recordTime)
                                    }
                                }
                            } else {  //发送条件未满足
                                mSoundVolumeImg.visibility = View.GONE
                                mSoundVolumeLayout.setBackgroundResource(R.drawable.view_dialog_sound_volume_short_tip_bg)
                                //延时0.7秒再关闭弹框
                                val timer = Timer()
                                timer.schedule(object : TimerTask() {
                                    override fun run() {
                                        mSoundVolumeDialog.dismiss()
                                        this.cancel()
                                    }
                                }, 700)
                                mAudioRecordManager.cancelAudio()
                            }
                        } else {  //取消发送
                            mSoundVolumeDialog.dismiss()
                            //删除文件
                            mAudioRecordManager.cancelAudio()
                        }
                    }
                    false
                }
                else -> true
            }
        }

        //表情
        ivMyMessageChatExpression.setOnClickListener {
            //如果键盘没有显示 就直接打开布局
            if (!mKeyBoardUtil.isShowInputKeyboard()) {
                rlMyMessageChatExpression.visibility = View.VISIBLE
                //工具布局隐藏
                llMyMessageChatFunction.visibility = View.GONE
                //按住说话隐藏
                tvMyMessageChatClickSay.visibility = View.GONE
            } else {
                //改变输入框上面的布局高度
                changeMiddleLayoutHeight()
                mExpressionOrFunction = 1
                //关闭软键盘
                mKeyBoardUtil.hideInputKeyboard()
            }
            scrollToBottomListItem()
        }

        //系统工具 +
        ivMyMessageChatFunction.setOnClickListener {
            PermissionUtils.readAndWrite(this) {
                //如果键盘没有显示 就直接打开布局
                if (!mKeyBoardUtil.isShowInputKeyboard()) {
                    llMyMessageChatFunction.visibility = View.VISIBLE
                    //表情布局隐藏
                    rlMyMessageChatExpression.visibility = View.GONE
                    //按住说话隐藏
                    tvMyMessageChatClickSay.visibility = View.GONE
                    //显示最近60秒内的截图或拍照
                    showLastImg()
                } else {
                    //改变输入框上面的布局高度
                    changeMiddleLayoutHeight()
                    mExpressionOrFunction = 2
                    //关闭软键盘
                    mKeyBoardUtil.hideInputKeyboard()
                }
                scrollToBottomListItem()
            }
        }

        //输入框点击
        etMyMessageChatContent.setOnClickListener {
            //改变输入框上面的布局高度
            changeMiddleLayoutHeight()
            //表情和工具布局隐藏
            llMyMessageChatFunction.visibility = View.GONE
            rlMyMessageChatExpression.visibility = View.GONE
            //打开软键盘
            mKeyBoardUtil.showInputKeyboard()
        }

        //输入框内容改变监听
        etMyMessageChatContent.addTextChangedListener(object : TextWatcher {

            //输入文本之前的状态
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            //输入文字中的状态，count是一次性输入字符数
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //内容不为空就显示发送按钮 隐藏工具按钮
                if (s.isNotBlank()) {
                    ivMyMessageChatFunction.visibility = View.GONE
                    tvMyMessageChatSend.visibility = View.VISIBLE
                } else {
                    ivMyMessageChatFunction.visibility = View.VISIBLE
                    tvMyMessageChatSend.visibility = View.GONE
                }
            }

            //输入文字后的状态
            override fun afterTextChanged(s: Editable) {

            }
        })

        //系统键盘监听
        mKeyBoardUtil.setListener(object : KeyBoardUtil.OnKeyboardListener {
            override fun onKeyboardShow(i: Int) {
                //恢复输入框上面的布局高度
                recoveryMiddleLayoutHeight()
                //恢复默认值 其他都不受理
                mExpressionOrFunction = 0
                scrollToBottomListItem()
            }

            override fun onKeyboardHide(i: Int) {
                //恢复输入框上面的布局高度
                recoveryMiddleLayoutHeight()
                when (mExpressionOrFunction) {
                    1 -> {
                        rlMyMessageChatExpression.visibility = View.VISIBLE
                        //工具布局隐藏
                        llMyMessageChatFunction.visibility = View.GONE
                        //按住说话隐藏
                        tvMyMessageChatClickSay.visibility = View.GONE
                    }
                    2 -> {
                        llMyMessageChatFunction.visibility = View.VISIBLE
                        //表情布局隐藏
                        rlMyMessageChatExpression.visibility = View.GONE
                        //按住说话隐藏
                        tvMyMessageChatClickSay.visibility = View.GONE
                        //显示最近60秒内的截图或拍照
                        showLastImg()
                    }
                }
                //恢复默认值 其他都不受理
                mExpressionOrFunction = 0
                scrollToBottomListItem()
            }
        })

        //发送消息
        tvMyMessageChatSend.setOnClickListener {
            val textMessage = TextMessage.buildForSend(etMyMessageChatContent.text.toString().trim(), loginUser, currentSessionKey)
            mImService!!.messageManager.sendText(textMessage)
            //输入框置空
            etMyMessageChatContent.setText("")
            pushList(textMessage, true)
            etMyMessageChatContent.isFocusable = true
        }

        //拍照
        ivMyMessageChatFunctionCamera.setOnClickListener {
            PermissionUtils.camera(this) {
                //打开相机
                openCamera()
            }
        }

        //照片
        ivMyMessageChatFunctionPhoto.setOnClickListener {
            PermissionUtils.readAndWrite(this) {
                //打开相册
                openAlbum()
            }
        }

        //视频
        ivMyMessageChatFunctionVideo.setOnClickListener {
            PermissionUtils.readAndWrite(this) {
                //打开视频
                openVideo()
            }
        }

        //语音听筒扬声器切换
        cdMyMessageChatSwitchAudio.setOnClickListener {
            if (mMediaManager.mIsCall) {
                mMediaManager.switchPlay(false)
                ToastUtil.showShort(getString(R.string.play_audio_music_tips))
                ivMyMessageChatSwitchAudio.setImageResource(R.mipmap.chat_speaker)
                tvMyMessageChatSwitchAudio.text = getString(R.string.play_audio_music)
            } else {
                mMediaManager.switchPlay(true)
                ToastUtil.showShort(getString(R.string.play_audio_call_tips))
                ivMyMessageChatSwitchAudio.setImageResource(R.mipmap.chat_receiver)
                tvMyMessageChatSwitchAudio.text = getString(R.string.play_audio_call)
            }
        }

        //新消息
        tvMyMessageChatTips.setOnClickListener {
            scrollToBottomListItem(true)
        }

        //撤回删除事件
        mAdapter.setCallBack(object : ChatAdapterTwo.CallBack {
            //撤回
            override fun onRevokeClick(position: Int) {
                myDialog.showLoadingDialog()
                val messageEntity = mAdapter.msgObjectList[position] as MessageEntity
                //网络请求
                ApiUtils.getApi()
                        .msgRevokeDelete(messageEntity.fromId, messageEntity.toId, messageEntity.msgId, 1)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            myDialog.dismissLoadingDialog()
                            it.apply {
                                if (code == 12000) {
                                    val newMsg = CmdMessage.buildForSend("撤回了一条消息", loginUser, currentSessionKey)?.apply {
                                        displayType = DBConstant.SHOW_REVOKE_TYPE
                                        setAttribute(MessageExtConst.MSGID, messageEntity.msgId)
                                        setAttribute(MessageExtConst.CMD_TIME, System.currentTimeMillis())
                                        mImService?.messageManager?.sendCMD(this)
                                    }
                                    //调用adapter的撤回方法
                                    mAdapter.revokeMsg(messageEntity, newMsg!!, position)
                                } else {
                                    ToastUtil.showShort(msg)
                                }
                            }
                        }, {
                            myDialog.dismissLoadingDialog()
                            ToastUtil.showNetError()
                        }, {}, { addSubscription(it) })

//                RequestManager.instanceApi
//                        .msgRevoke(messageEntity.fromId, messageEntity.toId, messageEntity.msgId)
//                        .compose(composeDefault())
//                        .subscribeExtApi({
//                        },{
//                            myDialog.dismissLoadingDialog()
//                            ToastUtil.showNetError()
//                        },{},{ addSubscription(it) })
            }

            //删除
            override fun onDeleteClick(position: Int) {
                val messageEntity = mAdapter.msgObjectList[position] as MessageEntity
                //发送中和发送失败就直接删就行了
                if (messageEntity.status == MessageConstant.MSG_SENDING || messageEntity.status == MessageConstant.MSG_FAILURE) {
                    mAdapter.removeMsg(messageEntity)
                } else {
                    myDialog.showLoadingDialog()
                    //网络请求
                    ApiUtils.getApi()
                            .msgRevokeDelete(messageEntity.fromId, messageEntity.toId, messageEntity.msgId, 2)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({
                                myDialog.dismissLoadingDialog()
                                it.apply {
                                    if (code == 12000) {
                                        //调用adapter的删除方法
                                        mAdapter.removeMsg(messageEntity)
                                    } else {
                                        ToastUtil.showShort(msg)
                                    }
                                }
                            }, {
                                myDialog.dismissLoadingDialog()
                                ToastUtil.showNetError()
                            }, {}, { addSubscription(it) })

//                    RequestManager.instanceApi
//                            .msgDelete(messageEntity.fromId, messageEntity.toId, messageEntity.msgId)
//                            .compose(composeDefault())
//                            .subscribeExtApi({
//                            },{
//                                myDialog.dismissLoadingDialog()
//                                ToastUtil.showNetError()
//                            },{},{ addSubscription(it) })
                }
            }

            //品牌点击 跳转详情
            override fun onBrandClick(position: Int) {
                val id = (mAdapter.msgObjectList[position] as BrandMessage).brandId
                startActivity<ShopDetailActivity>(IConstants.BRANDID to id)
                MESSAGE_TO_CHAT = false
            }

            //发送品牌
            override fun onSendBrandClick(position: Int) {
                val id = (mAdapter.msgObjectList[position] as BrandMessage).brandId
                val avatar = (mAdapter.msgObjectList[position] as BrandMessage).brandLogo
                val name = (mAdapter.msgObjectList[position] as BrandMessage).brandName
                val capitalName = (mAdapter.msgObjectList[position] as BrandMessage).brandValue
                val brandMessage = BrandMessage.buildForSend(id, avatar, name, capitalName, loginUser, currentSessionKey)
                mImService?.messageManager?.sendBrand(brandMessage)
                mAdapter.msgObjectList.remove(mAdapter.msgObjectList[position])
                pushList(brandMessage, true)
            }
        })

        //表情商城点击事件
        evMyMessageChatExpression.setClickCallBack {
            val localPath = it.pathLocal
            val url = it.url
            when (it.type) {
                FaceLocalConstant.IMGTYPE_FACE -> { //其他表情

                }
                FaceLocalConstant.IMGTYPE_COLLECT -> {  //收藏表情

                }
                FaceLocalConstant.IMGTYPE_EMOJ -> { //emoji表情
                    val msg = it.name
                    val curPosition = etMyMessageChatContent.selectionStart
                    val sb = StringBuilder(etMyMessageChatContent.text.toString())
                    sb.insert(curPosition, msg)
                    // 特殊文字处理,将表情等转换一下
                    etMyMessageChatContent.setText(SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE, this@MyMessageChatActivity, sb.toString(), etMyMessageChatContent))
                    // 将光标设置到新增完表情的右侧
                    etMyMessageChatContent.setSelection(curPosition + msg!!.length)
                }
                FaceLocalConstant.IMGTYPE_EMOJ_DELETE -> etMyMessageChatContent.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                else -> {

                }
            }
        }
    }

    private fun initAdapter() {
        //消息适配器
        mLayoutManager = LinearLayoutManager(this)
        rvMyMessageChat.layoutManager = mLayoutManager
        mAdapter = ChatAdapterTwo(this, msgObjectList, mIsSystemMotification) { audioMessage, imageView ->
            PermissionUtils.readAndWrite(this) {
                //音频点击事件
                if (mMediaManager.isPlaying()) {    //正在播放点击就结束播放
                    cdMyMessageChatSwitchAudio.visibility = View.GONE
                    mMediaManager.release()
                    if (mImageView == imageView) {
                        stopAudioAnimation(imageView)
                    } else {
                        stopAudioAnimation(mImageView!!)
                        mMediaManager.play(this, audioMessage.audioPath, audioMessage.audioUrl, {
                            mImageView = imageView
                            cdMyMessageChatSwitchAudio.visibility = View.VISIBLE
                            startAudioAnimation(imageView)
                        }, {
                            cdMyMessageChatSwitchAudio.visibility = View.GONE
                            stopAudioAnimation(imageView)
                        })
                    }
                } else {
                    mMediaManager.play(this, audioMessage.audioPath, audioMessage.audioUrl, {
                        //刚开始播放
                        mImageView = imageView
                        cdMyMessageChatSwitchAudio.visibility = View.VISIBLE
                        startAudioAnimation(imageView)
                    }, {
                        //播放结束
                        cdMyMessageChatSwitchAudio.visibility = View.GONE
                        stopAudioAnimation(imageView)
                    })
                }
            }
        }
        rvMyMessageChat.adapter = mAdapter

//        //表情viewPager适配器
//        mPageAdapter = MyFragmentPagerAdapter(supportFragmentManager, mFragmentList, mTabTitles)
//        vpMyMessageChat.adapter = mPageAdapter
//        //设置ViewPager缓存为5
//        vpMyMessageChat.offscreenPageLimit = 5
//        //将ViewPager和TabLayout绑定
//        tlMyMessageChat.setupWithViewPager(vpMyMessageChat)

//        mTabTitles.forEachIndexed { index, _ ->
//            //tabLayout里添加view
//            val tabView = View.inflate(this, R.layout.view_tab_layout, null)
//            GlideLoader.load(this, "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3236989755,3566217273&fm=26&gp=0.jpg", tabView.findViewById(R.id.ivChatExpressionIcon), cacheType = CacheType.All)
//            tlMyMessageChat.getTabAt(index)?.customView = tabView
//        }
    }

    //消息接口请求
    private fun httpLoad() {

    }

    //初始化音量对话框
    private fun initSoundVolumeDlg() {
        mSoundVolumeDialog = Dialog(this, R.style.SoundVolumeStyle)
        mSoundVolumeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mSoundVolumeDialog.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        mSoundVolumeDialog.setContentView(R.layout.view_dialog_sound_volume)
        mSoundVolumeDialog.setCanceledOnTouchOutside(true)
        mSoundVolumeImg = mSoundVolumeDialog.ivViewDialogSoundVolume
        mSoundVolumeLayout = mSoundVolumeDialog.llViewDialogSoundVolume
    }

    //根据分贝值设置录音时的音量动画
    private fun onReceiveMaxVolume(voiceValue: Int) {
        if (voiceValue < 500.0) {   //200.0
            mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_01)
        } else if (voiceValue > 500.0 && voiceValue < 1000.0) { //200.0 600.0
            mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_02)
        } else if (voiceValue > 1000.0 && voiceValue < 1500.0) {    //600 1200.0
            mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_03)
        } else if (voiceValue > 1500.0 && voiceValue < 2000.0) {    //1200.0 2400.0
            mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_04)
        } else if (voiceValue > 2000.0 && voiceValue < 3000.0) {    //2400.0 10000.0
            mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_05)
        } else if (voiceValue > 3000.0 && voiceValue < 5000.0) {    //10000.0 28000.0
            mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_06)
        } else if (voiceValue > 5000.0) {   //28000.0
            mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_07)
        }
    }

    //播放语音动画
    private fun startAudioAnimation(imageView: ImageView) {
        val animationDrawable = imageView.background as AnimationDrawable
        animationDrawable.start()
    }

    //结束语音动画
    private fun stopAudioAnimation(imageView: ImageView) {
        val animationDrawable = imageView.background as AnimationDrawable
        if (animationDrawable.isRunning) {
            animationDrawable.stop()
            animationDrawable.selectDrawable(0)
        }
    }

    //改变输入框上面布局(中间布局)的高度，临时改变下（为了引起视觉误差 让用户感觉上面的布局在等键盘弹起，会感觉很流畅）
    private fun changeMiddleLayoutHeight() {
        val fragmentParams = flMyMessageChat.layoutParams as LinearLayout.LayoutParams
        fragmentParams.weight = 0f
        //让布局的高度等于当前recyclerView的高度,形成短暂的等待视差
        fragmentParams.height = rvMyMessageChat.height
    }

    //恢复输入框上面布局的高度（键盘弹起后和关闭后恢复）
    private fun recoveryMiddleLayoutHeight() {
        val fragmentParams = flMyMessageChat.layoutParams as LinearLayout.LayoutParams
        fragmentParams.weight = 1f
        fragmentParams.height = 0
    }

    //表情和工具布局隐藏 关闭软键盘
    private fun hiddenViewAndInputKeyboard() {
        //表情和工具布局隐藏
        llMyMessageChatFunction.visibility = View.GONE
        rlMyMessageChatExpression.visibility = View.GONE
        //关闭软键盘
        mKeyBoardUtil.hideInputKeyboard()
    }

    //显示最近60秒内的截图或拍照
    private fun showLastImg() {
        val imgBean = GetImgUtils.getLatestPhoto(this)
        if (imgBean.isNotEmpty()) {
            if (System.currentTimeMillis() / 1000 - imgBean[0].mTime <= 60) {
                mImgSeePopChat = PopChatImg(this, imgBean[0].imgUrl, true) {
                    //打开一个发送图片的pop
                    PopChatImg(this, it, false) {
                        handleTakePhotoData(it)
                    }.showAtLocation(llMyMessageRoot, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
                }
                if (!mImgSeePopChat.isShowing) {
                    mImgSeePopChat.showAsDropDown(ivMyMessageChatFunction, -(this.dp2px(85) - ivMyMessageChatFunction.width), -(this.dp2px(125) + ivMyMessageChatFunction.height))
                }
            }
        }
    }

    //列表移到最后一个
    private fun scrollToBottomListItem(isAnimation: Boolean = false) {
        if (isAnimation) {
//            SmoothScrollLayoutManager(this).smoothScrollToPosition(rvMyMessageChat,1,mAdapter.msgObjectList.lastIndex)
            rvMyMessageChat.smoothScrollToPosition(mAdapter.msgObjectList.lastIndex)
            mRecyclerViewIsBottom = true
        } else {
            rvMyMessageChat.scrollToPosition(mAdapter.msgObjectList.lastIndex)
        }
        tvMyMessageChatTips.visibility = View.GONE
    }

    /**
     * -------------------------------------------------------------start-------------------------------------------------------------
     */

    /**
     * 假文字消息
     */
    private fun localTextMessage(): TextMessage {
        //内容是空的就初始添加&&和必须不是系统通知
        if ((TextUtils.isEmpty(mTextMessage.info) || mTextMessage.info == "null") && !mIsSystemMotification) {
            mTextMessage.displayType = DBConstant.SHOW_ORIGIN_TEXT_TYPE
            mTextMessage.info = getString(R.string.firstToChat_tips)
//            mTextMessage.content = "{\"extInfo\":{\"time\":${System.currentTimeMillis()}},\"info\":\"${getString(R.string.firstToChat_tips)}\",\"infoType\":1,\"nickname\":\"${intent.getStringExtra("title")}\",\"special\":false}"
            //里面时根据时间排序的 为了拆分开来
            mTextMessage.created = (System.currentTimeMillis() / 1000).toInt() + 1
        }
        return mTextMessage
    }

    /**
     * 假品牌消息
     */
    private fun localBrandMessage(): BrandMessage {
        if (TextUtils.isEmpty(mBrandMessage.brandName) || mBrandMessage.brandName == "null") {
            mBrandMessage.brandId = mBundle?.getInt("id") ?: 0
            mBrandMessage.brandLogo = mBundle?.getString("logo") ?: ""
            mBrandMessage.brandName = mBundle?.getString("name") ?: ""
            mBrandMessage.brandValue = mBundle?.getString("capitalName") ?: ""
            mBrandMessage.displayType = DBConstant.SHOW_BRAND_TYPE
            mBrandMessage.created = (System.currentTimeMillis() / 1000).toInt()
        }
        return mBrandMessage
    }

    /**
     * 添加单个消息
     */
    private fun pushList(msg: MessageEntity?, isMinePhone: Boolean) {
        msg?.let {
            if (isMinePhone) {
                mAdapter.addItem(msg, mLayoutManager)
            } else {
                mAdapter.addItem(msg)
            }
        }
    }

    /**
     * 添加list消息
     */
    private fun pushList(entityList: MutableList<MessageEntity>?) {
        //添加假消息给用户
        val textMessage = localTextMessage()
        val list: MutableList<MessageEntity>? = if (entityList?.size ?: 0 > 0) {
            entityList?.add(textMessage)
            entityList
        } else {
            mutableListOf(textMessage)
        }
        //为空是状态栏跳进来的 就不发送假消息了
        if (TextUtils.isEmpty(mAvatar)) {
            list!!.remove(textMessage)
        } else {
            //无系统通知提示
            if (list!![0].fromId == 0 && mIsSystemMotification) {
                llMyMessageChatTips.visibility = View.VISIBLE
            } else {
                llMyMessageChatTips.visibility = View.GONE
            }
        }
        //添加品牌
        if (mBundle != null) {
            val brandMessage = localBrandMessage()
            list?.add(brandMessage)
        }
        mAdapter.msgObjectList.clear()
        mAdapter.loadHistoryList(list)
    }

    /**
     * 设定聊天名称
     * 1. 如果是user类型， 点击触发UserProfile
     * 2. 如果是群组，检测自己是不是还在群中
     */
    private fun setTitleByUser() {
        if (peerEntity == null) {
            return
        }
        title = peerEntity?.mainName
        val peerType = peerEntity?.type
        when (peerType) {
            DBConstant.SESSION_TYPE_GROUP -> {
                val group = peerEntity as GroupEntity?
                val memberLists = group!!.getlistGroupMemberIds()
                if (loginUser != null) {
                    if (!memberLists.contains(loginUser!!.peerId)) {
                        //                        Toast.makeText(MessageActivity2.this, R.string.no_group_member, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            DBConstant.SESSION_TYPE_SINGLE -> {
//                topTitleTxt.setOnClickListener {
//                    IMUIHelper.openUserProfileActivity(this@MessageActivity, peerEntity?.peerId
//                            ?: 0)
//                }
            }
        }
    }

    /**
     * 添加聊天消息
     */
    private fun handleImagePickData(list: List<ImageItem>) {
        val listMsg = ArrayList<ImageMessage>()
        val itemList = list as ArrayList<ImageItem>
        for (item in itemList) {
            ImageMessage.buildForSend(item, loginUser, peerEntity)?.let {
                listMsg.add(it)
                pushList(it, true)
            }
        }
        mImService?.messageManager?.sendImages(listMsg)
    }

    /**
     * EventBus
     */
    fun onEventMainThread(event: SelectEvent) {
        val itemList = event.list
        if (itemList != null || itemList?.size ?: 0 > 0) {
            handleImagePickData(itemList)
        }
    }

    /**
     * 背景: 1.EventBus的cancelEventDelivery的只能在postThread中运行，而且没有办法绕过这一点
     * 2. onEvent(A a)  onEventMainThread(A a) 这个两个是没有办法共存的
     * 解决: 抽离出那些需要优先级的event，在onEvent通过handler调用主线程，
     * 然后cancelEventDelivery
     */
    fun onEvent(event: PriorityEvent) {
        when (event.event) {
            PriorityEvent.Event.MSG_RECEIVED_MESSAGE -> {
                val entity = event.`object` as MessageEntity
                /**正式当前的会话 */
                if (currentSessionKey == entity.sessionKey) {
                    onMsgRecv(entity)
                    EventBus.getDefault().cancelEventDelivery(event)
                }
            }
        }
    }

    /**
     * EventBus
     */
    fun onEventMainThread(event: MessageEvent) {
        val type = event.event
        val entity = event.messageEntity
        when (type) {
            MessageEvent.Event.ACK_SEND_MESSAGE_OK -> {
                onMsgAck(event.messageEntity)
            }

            MessageEvent.Event.ACK_SEND_MESSAGE_FAILURE -> {
                // 失败情况下新添提醒
                ToastUtil.showShort(getString(R.string.message_send_failed))
                run { onMsgUnAckTimeoutOrFailure(event.messageEntity) }
            }
            MessageEvent.Event.ACK_SEND_MESSAGE_TIME_OUT -> {
                onMsgUnAckTimeoutOrFailure(event.messageEntity)
            }

            MessageEvent.Event.HANDLER_IMAGE_UPLOAD_SUCCESS, MessageEvent.Event.HANDLER_IMAGE_UPLOAD_FAILD -> { //图片成功和失败
                val imageMessage = event.messageEntity as ImageMessage
                mAdapter.updateItemState(imageMessage)
            }

            MessageEvent.Event.HANDLER_AUDIO_UPLOAD_SUCCESS, MessageEvent.Event.HANDLER_AUDIO_UPLOAD_FAILD -> { //语音成功和失败
                val imageMessage = event.messageEntity as AudioMessage
                mAdapter.updateItemState(imageMessage)
            }

            MessageEvent.Event.HANDLER_VIDEO_UPLOAD_SUCCESS, MessageEvent.Event.HANDLER_VIDEO_UPLOAD_FAILD -> { //视频成功与失败
                val videoMessage = event.messageEntity as VideoMessage
                mAdapter.updateItemState(videoMessage)
            }
            MessageEvent.Event.HISTORY_MSG_OBTAIN -> {
                if (historyTimes == 1) {
                    reqHistoryMsg()
                }
            }
        }
    }

    /**
     * EventBus
     */
    fun onEventMainThread(event: UnreadEvent) {
        when (event.event) {
            UnreadEvent.Event.UNREAD_MSG_RECEIVED, UnreadEvent.Event.UNREAD_MSG_LIST_OK, UnreadEvent.Event.SESSION_READED_UNREAD_MSG -> if (IMUnreadMsgManager.instance().totalUnreadCount > 0) {
                historyTimes = 0
                //收到新消息了 RecyclerView在底部
                if (mRecyclerViewIsBottom) {
                    tvMyMessageChatTips.visibility = View.GONE
                    mAdapter.notifyDataSetChanged()
                    mLayoutManager.scrollToPosition(mAdapter.msgObjectList.lastIndex)
                } else {
                    tvMyMessageChatTips.visibility = View.VISIBLE
                    Handler().postDelayed({ tvMyMessageChatTips?.visibility = View.GONE }, 3000)
                    mAdapter.notifyDataSetChanged()
                }
//                reqHistoryMsg()
            }
        }
    }

    /**
     * [备注] DB保存，与session的更新manager已经做了
     */
    private fun onMsgAck(messageEntity: MessageEntity?) {
        val msgId = messageEntity!!.msgId

        /**到底采用哪种ID呐?? */
        val localId = messageEntity.id!!
        mAdapter.updateItemState(messageEntity)
    }

    /**
     * 肯定是在当前的session内
     */
    private fun onMsgRecv(entity: MessageEntity) {
        mImService?.unReadMsgManager?.ackReadMsg(entity)
        pushList(entity, false)
    }

    /**
     * 修改内容状态
     */
    private fun onMsgUnAckTimeoutOrFailure(messageEntity: MessageEntity?) {
        // msgId 应该还是为0
        mAdapter.updateItemState(messageEntity!!)
    }

    /**
     * 1.初始化请求历史消息
     * 2.本地消息不全，也会触发
     */
    private fun reqHistoryMsg() {
        val msgList = if (peerEntity == null) {
            if (currentSessionKey == null) {
                return
            }
            //将currentSessionKey拆分
            val sessionInfo = EntityChangeEngine.spiltSessionKey(currentSessionKey)
            mImService?.messageManager?.loadHistoryMsg(historyTimes, currentSessionKey, sessionInfo[0].toInt(), sessionInfo[1].toInt())
        } else {
            mImService?.messageManager?.loadHistoryMsg(historyTimes, currentSessionKey, peerEntity)
        }
        historyTimes++
        pushList(msgList)
        scrollToBottomListItem()
    }

    /**
     * 处理拍照后的数据
     */
    private fun handleTakePhotoData(takePhotoSavePath: String) {
        if (takePhotoSavePath != "" && loginUser != null) {
            val imageMessage = if (peerEntity == null) {
                if (currentSessionKey == null) {
                    return
                }
                val sessionInfo = EntityChangeEngine.spiltSessionKey(currentSessionKey)
                ImageMessage.buildForSend(takePhotoSavePath, loginUser!!, sessionInfo[0].toInt(), sessionInfo[1].toInt())
            } else {
                ImageMessage.buildForSend(takePhotoSavePath, loginUser!!, peerEntity!!)
            }
            val sendList = ArrayList<ImageMessage>(1)
            sendList.add(imageMessage)
            mImService?.messageManager?.sendImages(sendList)
            // 格式有些问题
            pushList(imageMessage, true)
            //消除焦点
            etMyMessageChatContent.clearFocus()
        }
    }

    /**
     * 选择视频后的数据处理
     */
    private fun handleTakeVideoData(videoPath: String) {
//        val selectedVideo = data.data
//        val filePathColumn = arrayOf(MediaStore.Video.Media.DATA)
//        val cursor = contentResolver.query(selectedVideo!!, filePathColumn, null, null, null)
//        cursor?.moveToFirst()
//        val columnIndex = cursor?.getColumnIndex(filePathColumn[0]) ?: 0
//        val videoPath = cursor?.getString(columnIndex)
//        cursor?.close()
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(videoPath)
            mediaPlayer.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var videolength = -1
        if (mediaPlayer.duration > -1) {
            videolength = mediaPlayer.duration / 1000
        }
        VideoMessage.buildForSend(videolength.toFloat(), videoPath, loginUser, currentSessionKey)?.let {
            val sendList = ArrayList<VideoMessage>(1)
            sendList.add(it)
            mImService?.messageManager?.sendVideos(sendList)
            //格式有些问题
            pushList(it, true)
            //消除焦点
            etMyMessageChatContent.clearFocus()
        }
    }

    /**
     * 录音结束后处理录音数据
     */
    private fun onRecordVoiceEnd(audioSavePath: String, audioLen: Float) {
        if (audioSavePath != "" && loginUser != null) {
//            val audioMessage = if (peerEntity == null) {
//                if (currentSessionKey == null) {
//                    return
//                }
//                val sessionInfo = EntityChangeEngine.spiltSessionKey(currentSessionKey)
//                AudioMessage.buildForSend(audioLen, audioSavePath, loginUser!!, sessionInfo[0].toInt(), sessionInfo[1].toInt())
//            } else {
//            }
            val audioMessage = AudioMessage.buildForSend(audioLen, audioSavePath, loginUser!!, currentSessionKey)

            mImService?.messageManager?.sendAudio(audioMessage)
            pushList(audioMessage, true)
        }
    }

    /**
     * 上滑加载
     */
    fun onPullDownToRefresh() {
        // 获取消息
        val messageEntity = mAdapter.getTopMsgEntity()
        if (messageEntity != null) {
            val historyMsgInfo = mImService?.messageManager?.loadHistoryMsg(messageEntity, historyTimes)
            if (historyMsgInfo?.size ?: 0 > 0) {
                historyTimes++
                mAdapter.loadHistoryList(historyMsgInfo, mLayoutManager)
            }
        }
    }

    /**
     * -------------------------------------------------------------end-------------------------------------------------------------
     */

    //权限申请结果
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //打开相机拍照返回路径
    private fun openCamera() {
//        takePhotoSavePath = CommonUtil.getImageSavePath("img_" + System.currentTimeMillis().toString() + ".jpg")
//        startActivityForResult(Intent("android.media.action.IMAGE_CAPTURE").apply {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                //添加这一句表示对目标应用临时授权该Uri所代表的文件
//                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            }
//            putExtra(MediaStore.EXTRA_OUTPUT, UriUtil.getUri(applicationContext, File(takePhotoSavePath!!)))
//        }, SysConstant.CAMERA_WITH_DATA)

//        SimplePhotoUtil.instance.setConfig(PhotoConfig(this, true, onPathCallback = { path ->
//            handleTakePhotoData(path)
//        }))

        startActivityForResult(Intent(this, CameraActivity::class.java), SysConstant.CAMERA_WITH_DATA)
        this.overridePendingTransition(com.leimo.wanxin.R.anim.tt_album_enter, com.leimo.wanxin.R.anim.tt_stay)
    }

    //打开相册读取文件返回路径
    private fun openAlbum() {
        //用PictureSelector工具
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofAll())   //视频和相册都显示
                .selectionMode(PictureConfig.SINGLE)    //单选，也可选多选模式MULTIPLE
                .forResult(SysConstant.ALBUM_BACK_DATA)

//        if (albumList != null && albumList!!.size < 1) {
//            ToastUtil.showShort(resources.getString(com.leimo.wanxin.R.string.not_found_album))
//            return
//        }
//        startActivityForResult(Intent(this, PickPhotoActivity::class.java).apply {
//            putExtra(IntentConstant.KEY_SESSION_KEY, currentSessionKey)
//        }, SysConstant.ALBUM_BACK_DATA)
//        this.overridePendingTransition(com.leimo.wanxin.R.anim.tt_album_enter, com.leimo.wanxin.R.anim.tt_stay)

//        SimplePhotoUtil.instance.setConfig(PhotoConfig(this, false, onPathCallback = { path ->
//            handleTakePhotoData(path)
//        }))
    }

    //打开视频文件读取返回路径
    private fun openVideo() {
        startActivityForResult(Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI), SysConstant.VIDEO_WITH_DATA)

//        SimplePhotoUtil.instance.setConfig(this, { path, data ->
//            handleTakeVideoData(data)
//        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        //选择照片（图库，拍照）
//        SimplePhotoUtil.instance.onPhotoResult(requestCode, resultCode, data)
        if (Activity.RESULT_OK != resultCode) {
            return
        }
        when (requestCode) {
            SysConstant.CAMERA_WITH_DATA -> {   //拍照返回
                val noPermission = data?.getBooleanExtra("noPermission", false)
                val type = data?.getIntExtra("type", -1)
                val mImagePath = data?.getStringExtra("mImagePath")
                val mVideoPath = data?.getStringExtra("mVideoPath")
                if (noPermission!!) {
                    //提示用户去设置里设置相应权限
                    DialogCommon(this, "提示", "使用该功能需要相机权限，请前往系统设置开启权限", rightText = "去设置",
                            onRightClick = {
                                toSelfSetting(this)
                            }).show()
                    return
                }
                when (type) {
                    1 -> {  //照片
                        mImagePath?.let {
                            handleTakePhotoData(it)
                        }
                    }
                    2 -> {  //视频
                        mVideoPath?.let {
                            handleTakeVideoData(it)
                        }
                    }
                    else -> {

                    }
                }
//            SDPathUtil.updateImageSysStatu(applicationContext, takePhotoSavePath)
            }
            SysConstant.ALBUM_BACK_DATA -> {    //相册
                val selectList = PictureSelector.obtainMultipleResult(data)
                selectList[0].apply {
                    when {
                        pictureType.contains("image") -> {   //图片
                            handleTakePhotoData(path)
                        }
                        pictureType.contains("video") -> {    //视频
                            handleTakeVideoData(path)
                        }
                    }
                }

//                intent = data
            }
//            SysConstant.VIDEO_WITH_DATA -> {    //视频
//                handleTakeVideoData(data!!)
//            }
        }
    }

    override fun onBackPressed() {
        //如果表情或工具界面存在 返回就关闭他们
        if (llMyMessageChatFunction.visibility == View.VISIBLE || rlMyMessageChatExpression.visibility == View.VISIBLE) {
            llMyMessageChatFunction.visibility = View.GONE
            rlMyMessageChatExpression.visibility = View.GONE
        } else {
            mImService?.unReadMsgManager?.readUnreadSession(currentSessionKey)
            setResult(Activity.RESULT_OK)
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        IMApplication.gifRunning = true
        historyTimes = 0
    }

    override fun onStop() {
        mAdapter.hidePopup()

        super.onStop()
    }

    override fun onDestroy() {
        historyTimes = 0
        mMediaManager.release()
        imServiceConnector.disconnect(this)
        EventBus.getDefault().unregister(this)

//        if (albumList != null) {
//            albumList?.clear()
//        }
        super.onDestroy()
    }

    //RecyclerView平滑滚动到指定位置
    inner class SmoothScrollLayoutManager(context: Context) : LinearLayoutManager(context) {

        override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State, position: Int) {
            val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {
                // 返回：滑过1px时经历的时间(ms)。
                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                    return 150f / displayMetrics.densityDpi
                }
            }
            smoothScroller.targetPosition = position
            startSmoothScroll(smoothScroller)
        }
    }
}