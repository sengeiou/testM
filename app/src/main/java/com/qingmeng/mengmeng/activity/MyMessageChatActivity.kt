package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.widget.*
import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.config.IntentConstant
import com.mogujie.tt.config.MessageConstant
import com.mogujie.tt.config.MessageExtConst
import com.mogujie.tt.db.DBInterface
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
import com.mogujie.tt.ui.adapter.album.ImageItem
import com.mogujie.tt.ui.helper.AudioPlayerHandler
import com.mogujie.tt.ui.helper.Emoparser
import com.mogujie.tt.ui.widget.SpeekerToast
import com.mogujie.tt.ui.widget.message.MessageOperatePopup
import com.mogujie.tt.ui.widget.message.RenderType
import com.mogujie.tt.utils.CommonUtil
import com.mogujie.tt.utils.DateUtil
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.ChatAdapter
import com.qingmeng.mengmeng.adapter.MultiItemTypeAdapter
import com.qingmeng.mengmeng.adapter.MyFragmentPagerAdapter
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.fragment.MyMessageChatExpressionTabLayoutFragment
import com.qingmeng.mengmeng.utils.KeyboardUtil
import com.qingmeng.mengmeng.utils.PermissionUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.audio.AudioManager
import com.qingmeng.mengmeng.utils.audio.MediaManager
import com.qingmeng.mengmeng.utils.getLoacalBitmap
import com.qingmeng.mengmeng.utils.imageLoader.CacheType
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import com.qingmeng.mengmeng.utils.photo.PhotoConfig
import com.qingmeng.mengmeng.utils.photo.SimplePhotoUtil
import de.greenrobot.event.EventBus
import kotlinx.android.synthetic.main.activity_my_message_chat.*
import kotlinx.android.synthetic.main.layout_head.*
import kotlinx.android.synthetic.main.view_dialog_sound_volume.*
import org.jetbrains.anko.ctx
import java.io.File
import java.io.IOException
import java.util.*


/**
 *  Description :设置 - 消息 - 聊天界面

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MyMessageChatActivity : BaseActivity() {
    private lateinit var mKeyboardUtil: KeyboardUtil            //系统键盘工具
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: MultiItemTypeAdapter<Any>
    private lateinit var mPageAdapter: PagerAdapter
    private var mAudioManager = AudioManager.getInstance(IConstants.DIR_AUDIO_STR)    //语音工具类
    private lateinit var mSoundVolumeDialog: Dialog             //语音弹出框
    private lateinit var mSoundVolumeImg: ImageView
    private lateinit var mSoundVolumeLayout: LinearLayout
    private var y1 = 0                                          //手指坐标
    private var y2 = 0
    private var mExpressionOrFunction = 0                       //变量 0默认（都不受理） 1表情点击 2工具+点击
    private var mFragmentList = ArrayList<Fragment>()           //表情fragment
    private val mTabTitles = arrayOf("", "", "")                //tabLayout头部 先加3个试试
    private var mLocalVideoPath = ""                            //语音本地路径

    /**
     * 消息用到的
     */
    private var currentSessionKey: String? = null
    private var mImService: IMService? = null
    private var loginUser: UserEntity? = null
    private var peerEntity: PeerEntity? = null
    private var msgObjectList = ArrayList<Any>()
    private var currentPop: MessageOperatePopup? = null    //弹出气泡
    private val mDefaultTimeDifference = 120               //默认时间差值
    private var takePhotoSavePath: String? = ""
    private var historyTimes = 0
    private val imServiceConnector = object : IMServiceConnector() {
        override fun onIMServiceConnected() {
            mImService = this.imService
            initMsgData()
        }

        override fun onServiceDisconnected() {}
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_my_message_chat
    }

    override fun initObject() {
        super.initObject()

//        setHeadName(intent.getStringExtra("title"))

        //实例化键盘工具
        mKeyboardUtil = KeyboardUtil(this, etMyMessageChatContent)
        //表情里添加fragment
        mTabTitles.forEachIndexed { index, _ ->
            mFragmentList.add(MyMessageChatExpressionTabLayoutFragment())
            (mFragmentList[index] as MyMessageChatExpressionTabLayoutFragment).setContent("$index")
        }
        //初始化音量对话框
        initSoundVolumeDlg()
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
        ChatAdapter.setData(mImService!!, loginUser!!)
        if (peerEntity == null) {
            peerEntity = mImService!!.sessionManager.findPeerEntity(currentSessionKey)
        }
        // 头像、历史消息加载、取消通知
        setTitleByUser()
        reqHistoryMsg()
        mImService?.unReadMsgManager?.readUnreadSession(currentSessionKey)
        mImService?.notificationManager?.cancelSessionNotifications(currentSessionKey)
    }

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
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //滑到顶部了
                if (!recyclerView.canScrollVertically(-1)) {
                    onPullDownToRefresh()
                }
            }
        })

        //音频
        ivMyMessageChatAudio.setOnClickListener {
            //判断是否有权限
            PermissionUtils.audio(this, {
                PermissionUtils.readAndWrite(this, {
                    //表情和工具布局隐藏 关闭软键盘
                    hiddenViewAndInputKeyboard()
                    //按住说话不显示就显示 反之隐藏
                    tvMyMessageChatClickSay.visibility = if (tvMyMessageChatClickSay.visibility == View.GONE) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                })
            })
        }

        //按住说话
        tvMyMessageChatClickSay.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {    //按下
                    y1 = event.y.toInt()
                    //设置点击背景
                    v.setBackgroundResource(R.drawable.ripple_bg_drawable_graydark_radius18)
                    tvMyMessageChatClickSay.text = getString(R.string.release_to_over)
                    mSoundVolumeImg.visibility = View.VISIBLE
                    mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_01)
                    mSoundVolumeLayout.setBackgroundResource(R.drawable.view_dialog_sound_volume_default_bg)
                    //录音
                    mAudioManager.readyAudio({
                        onReceiveMaxVolume(it)
                    })
                    //显示弹框
                    mSoundVolumeDialog.show()
                    true
                }
                MotionEvent.ACTION_MOVE -> {    //移动
                    y2 = event.y.toInt()
                    //向上移动180就改变提示
                    if (y1 - y2 > 180) {
                        tvMyMessageChatClickSay.text = getString(R.string.cancel_to_send)
                        mSoundVolumeImg.visibility = View.GONE
                        mSoundVolumeLayout.setBackgroundResource(R.drawable.view_dialog_sound_volume_cancel_bg)
                    } else {
                        tvMyMessageChatClickSay.text = getString(R.string.release_to_over)
                        mSoundVolumeImg.visibility = View.VISIBLE
                        mSoundVolumeLayout.setBackgroundResource(R.drawable.view_dialog_sound_volume_default_bg)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {  //松开
                    y2 = event.y.toInt()
                    v.setBackgroundResource(R.drawable.ripple_bg_drawable_gray_radius18)
                    tvMyMessageChatClickSay.text = getString(R.string.hold_to_talk)
                    //发送语音
                    if (y1 - y2 <= 180) {
                        //如果语音时间够的话就发送语音
                        if (false) { //真.发送

                            mSoundVolumeDialog.dismiss()
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
                        }
                    } else {  //取消发送
                        mSoundVolumeDialog.dismiss()
                    }
                    //释放录音
                    mAudioManager.releaseAudio({
                        ToastUtil.showShort(it)
                        //录制路径赋值
                        mLocalVideoPath = it
                    })
                    false
                }
                else -> true
            }
        }

        //表情
        ivMyMessageChatExpression.setOnClickListener {
            //如果键盘没有显示 就直接打开布局
            if (!mKeyboardUtil.isShowInputKeyboard()) {
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
                mKeyboardUtil.hideInputKeyboard()
            }
            scrollToBottomListItem()
        }

        //系统工具 +
        ivMyMessageChatFunction.setOnClickListener {
            //如果键盘没有显示 就直接打开布局
            if (!mKeyboardUtil.isShowInputKeyboard()) {
                llMyMessageChatFunction.visibility = View.VISIBLE
                //表情布局隐藏
                rlMyMessageChatExpression.visibility = View.GONE
                //按住说话隐藏
                tvMyMessageChatClickSay.visibility = View.GONE
            } else {
                //改变输入框上面的布局高度
                changeMiddleLayoutHeight()
                mExpressionOrFunction = 2
                //关闭软键盘
                mKeyboardUtil.hideInputKeyboard()
            }
            scrollToBottomListItem()
        }

        //输入框点击
        etMyMessageChatContent.setOnClickListener {
            //改变输入框上面的布局高度
            changeMiddleLayoutHeight()
            //表情和工具布局隐藏
            llMyMessageChatFunction.visibility = View.GONE
            rlMyMessageChatExpression.visibility = View.GONE
            //打开软键盘
            mKeyboardUtil.showInputKeyboard()
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
        mKeyboardUtil.setListener(object : KeyboardUtil.OnKeyboardListener {
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
            //发送消息
            pushList(textMessage)
            scrollToBottomListItem()
            etMyMessageChatContent.isFocusable = true
        }

        //拍照
        llMyMessageChatFunctionCamera.setOnClickListener {
            PermissionUtils.camera(this, {
                //打开相机
                openCamera()
            })
        }

        //照片
        llMyMessageChatFunctionPhoto.setOnClickListener {
            PermissionUtils.readAndWrite(this, {
                //打开相册
                openAlbum()
            })
        }

        //视频
        llMyMessageChatFunctionVideo.setOnClickListener {
            PermissionUtils.readAndWrite(this, {
                //打开视频
                openVideo()
            })
        }

        //语音听筒扬声器切换
        ivMyMessageChatSwitchAudio.setOnClickListener {
            if (MediaManager.mIsCall) {
                MediaManager.switchPlay(false)
                ToastUtil.showShort(getString(R.string.play_audio_music))
            } else {
                MediaManager.switchPlay(true)
                ToastUtil.showShort(getString(R.string.play_audio_call))
            }
        }
    }

    private fun initAdapter() {
        //消息适配器
        mLayoutManager = LinearLayoutManager(this)
        rvMyMessageChat.layoutManager = mLayoutManager
        mAdapter = MultiItemTypeAdapter(this, msgObjectList, holderConvert = { _, _, _, _ -> }, holderConvertP = { holder, t, position, _, parent ->
            holder.apply {
                when (RenderType.values()[getItemViewType(position)]) {
                    RenderType.MESSAGE_TYPE_TIME_TITLE, RenderType.MESSAGE_TYPE_MINE_REVOKE, RenderType.MESSAGE_TYPE_OTHER_REVOKE -> {    //时间(撤回)

                    }
                    RenderType.MESSAGE_TYPE_INVALID -> {    //品牌详情(失败类型)

                    }
                    RenderType.MESSAGE_TYPE_MINE_TEXT, RenderType.MESSAGE_TYPE_MINE_GIF -> {    //自己文字(表情)
                        getView<TextView>(R.id.tvMyMessageChatRvMineTextText).apply {
                            //长按事件
                            setOnLongClickListener {
                                showPopWindow(position, parent, this)
                                true
                            }
                        }
                    }
                    RenderType.MESSAGE_TYPE_OTHER_TEXT, RenderType.MESSAGE_TYPE_OTHER_GIF -> {    //别人文字(表情)
                        getView<TextView>(R.id.tvMyMessageChatRvOtherTextText).apply {
                            //长按事件
                            setOnLongClickListener {
                                showPopWindow(position, parent, this)
                                true
                            }
                        }
                    }
                    RenderType.MESSAGE_TYPE_MINE_IMAGE, RenderType.MESSAGE_TYPE_MINE_GIF_IMAGE -> {    //自己图片
                        getView<ImageView>(R.id.ivMyMessageChatRvMineImageImage).setOnClickListener {
                            val imageMessage = msgObjectList[position] as ImageMessage
                            val imagePath = imageMessage.url
                            ToastUtil.showShort("自己图片(gif)")
                        }
                        getView<ImageView>(R.id.ivMyMessageChatRvMineImageImage).apply {
                            //长按事件
                            setOnLongClickListener {
                                showPopWindow(position, parent, this)
                                true
                            }
                        }
                    }
                    RenderType.MESSAGE_TYPE_OTHER_IMAGE, RenderType.MESSAGE_TYPE_OTHER_GIF_IMAGE -> {    //别人图片
                        getView<ImageView>(R.id.ivMyMessageChatRvOtherImageImage).setOnClickListener {
                            val imageMessage = msgObjectList[position] as ImageMessage
                            val imagePath = imageMessage.url
                            ToastUtil.showShort("别人图片(gif)")
                        }
                        getView<ImageView>(R.id.ivMyMessageChatRvOtherImageImage).apply {
                            //长按事件
                            setOnLongClickListener {
                                showPopWindow(position, parent, this)
                                true
                            }
                        }
                    }
                    RenderType.MESSAGE_TYPE_MINE_AUDIO -> {    //自己语音
                        getView<LinearLayout>(R.id.llMyMessageChatRvMineAudio).setOnClickListener {
                            val audioMessage = msgObjectList[position] as AudioMessage
                            val audioPath = audioMessage.audioPath
                            ToastUtil.showShort("自己语音")
                        }
                        getView<LinearLayout>(R.id.llMyMessageChatRvMineAudio).apply {
                            //长按事件
                            setOnLongClickListener {
                                showPopWindow(position, parent, this)
                                true
                            }
                        }
                    }
                    RenderType.MESSAGE_TYPE_OTHER_AUDIO -> {    //别人语音
                        getView<LinearLayout>(R.id.llMyMessageChatRvOtherAudio).setOnClickListener {
                            val audioMessage = msgObjectList[position] as AudioMessage
                            val audioPath = audioMessage.audioPath
                            ToastUtil.showShort("别人语音")
                        }
                        getView<LinearLayout>(R.id.llMyMessageChatRvOtherAudio).apply {
                            //长按事件
                            setOnLongClickListener {
                                showPopWindow(position, parent, this)
                                true
                            }
                        }
                    }
                    RenderType.MESSAGE_TYPE_MINE_VIDEO -> {    //自己视频
                        getView<RelativeLayout>(R.id.rlMyMessageChatRvMineVideo).setOnClickListener {
                            val videoMessage = msgObjectList[position] as VideoMessage
                            val path = videoMessage.path
                            var url = videoMessage.url
                            if (!TextUtils.isEmpty(path) && File(path).exists()) {
                                url = path
                            }
                            val uri = Uri.parse(url)
                            //调用系统自带的播放器
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(uri, "video/mp4")
                            ctx.startActivity(intent)
                        }
                        getView<RelativeLayout>(R.id.rlMyMessageChatRvMineVideo).apply {
                            //长按事件
                            setOnLongClickListener {
                                showPopWindow(position, parent, this)
                                true
                            }
                        }
                    }
                    RenderType.MESSAGE_TYPE_OTHER_VIDEO -> {    //别人视频
                        getView<RelativeLayout>(R.id.rlMyMessageChatRvOtherVideo).setOnClickListener {
                            val videoMessage = msgObjectList[position] as VideoMessage
                            val path = videoMessage.path
                            var url = videoMessage.url
                            if (!TextUtils.isEmpty(path) && File(path).exists()) {
                                url = path
                            }
                            val uri = Uri.parse(url)
                            //调用系统自带的播放器
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(uri, "video/mp4")
                            ctx.startActivity(intent)
                        }
                        getView<RelativeLayout>(R.id.rlMyMessageChatRvOtherVideo).apply {
                            //长按事件
                            setOnLongClickListener {
                                showPopWindow(position, parent, this)
                                true
                            }
                        }
                    }
                }
            }
        }, itemClick = { _, _, _ ->
            hiddenViewAndInputKeyboard()
        })
        mAdapter.addItemViewDelegate(ChatAdapter.DefaultLayout())
        mAdapter.addItemViewDelegate(ChatAdapter.TimeLayout())
        mAdapter.addItemViewDelegate(ChatAdapter.BrandLayout())
        mAdapter.addItemViewDelegate(ChatAdapter.OtherVideoLayout())
        mAdapter.addItemViewDelegate(ChatAdapter.MineVideoLayout())
        mAdapter.addItemViewDelegate(ChatAdapter.OtherAudioLayout())
        mAdapter.addItemViewDelegate(ChatAdapter.MineAudioLayout())
        mAdapter.addItemViewDelegate(ChatAdapter.OtherImageLayout())
        mAdapter.addItemViewDelegate(ChatAdapter.MineImageLayout())
        mAdapter.addItemViewDelegate(ChatAdapter.OtherTextLayout())
        mAdapter.addItemViewDelegate(ChatAdapter.MineTextLayout())
        rvMyMessageChat.adapter = mAdapter

        //表情viewPager适配器
        mPageAdapter = MyFragmentPagerAdapter(supportFragmentManager, mFragmentList, mTabTitles)
        vpMyMessageChat.adapter = mPageAdapter
        //设置ViewPager缓存为5
        vpMyMessageChat.offscreenPageLimit = 5
        //将ViewPager和TabLayout绑定
        tlMyMessageChat.setupWithViewPager(vpMyMessageChat)

        mTabTitles.forEachIndexed { index, _ ->
            //tabLayout里添加view
            val tabView = View.inflate(this, R.layout.view_tab_layout, null)
            GlideLoader.load(this, "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3236989755,3566217273&fm=26&gp=0.jpg", tabView.findViewById(R.id.ivChatExpressionIcon), cacheType = CacheType.All)
            tlMyMessageChat.getTabAt(index)?.customView = tabView
        }
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
        mKeyboardUtil.hideInputKeyboard()
    }

    //列表移到最后一个
    private fun scrollToBottomListItem() {
        rvMyMessageChat.scrollToPosition(msgObjectList.lastIndex)
    }

    /**
     * -------------------------------------------------------------start-------------------------------------------------------------
     */

    /**
     * 添加历史消息
     */
    private fun addItem(msg: MessageEntity) {
        val nextTime = msg.created
        if (msgObjectList.size > 0) {
            val objectMessage = msgObjectList[msgObjectList.lastIndex]
            if (objectMessage is MessageEntity) {
                val preTime = (objectMessage as MessageEntity).created
                val needTime = DateUtil.needDisplayTime(preTime, nextTime)
                if (needTime) {
                    msgObjectList.add(nextTime)
                }
            }
        } else {
            val message = msg.created
            msgObjectList.add(message)
        }
        /**消息的判断 */
        if (msg.displayType == DBConstant.SHOW_MIX_TEXT) {
            val mixMessage = msg as MixMessage
            msgObjectList.addAll(mixMessage.getMsgList())
        } else {
            msgObjectList.add(msg)
        }
        if (msg is ImageMessage) {
            ImageMessage.addToImageMessageList(msg)
        }
        ChatAdapter.msgObjectList = msgObjectList
        mAdapter.notifyDataSetChanged()
    }

    /**
     * 删除一条消息
     */
    fun removeMsg(messageEntity: MessageEntity) {
        //根据消息Id删除数据库内容
        DBInterface.instance().deleteMessageByMsgId(messageEntity.msgId)
        msgObjectList.remove(messageEntity)
        mAdapter.notifyDataSetChanged()
    }

    /**
     * 撤回一条消息
     */
    fun revokeMsg(entity: MessageEntity) {
        entity.displayType = DBConstant.SHOW_REVOKE_TYPE
        //根据消息修改数据库内容
        DBInterface.instance().insertOrUpdateMessage(entity)
        mAdapter.notifyDataSetChanged()
    }

    /**
     * 修改新收到的消息
     */
    private fun updateRevokeMsg(entity: MessageEntity) {
        val txtMsg = entity as TextMessage
        for (objectList in msgObjectList) {
            if (objectList is MessageEntity) {
                val messageEntity = objectList as MessageEntity
                if (messageEntity.msgId == txtMsg.getAttributeInt(MessageExtConst.MSGID)) {
                    (objectList as MessageEntity).displayType = DBConstant.SHOW_REVOKE_TYPE
                    //根据消息修改数据库内容
                    DBInterface.instance().insertOrUpdateMessage(messageEntity)
                    mAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    /**
     * 是否是gif图
     */
    private fun isMsgGif(msg: MessageEntity): Boolean {
        val content = msg.info
        // @YM 临时处理  牙牙表情与消息混合出现的消息丢失
        return if (TextUtils.isEmpty(content) || !(content.startsWith("[") && content.endsWith("]"))) {
            false
        } else Emoparser.getInstance(this.ctx)!!.isMessageGif(msg.info)
    }

    /**
     * 获取第一个Message类
     */
    fun getTopMsgEntity(): MessageEntity? {
        if (msgObjectList.size <= 0) {
            return null
        }
        for (result in msgObjectList) {
            if (result is MessageEntity) {
                return result
            }
        }
        return null
    }

    /**
     * 时间比较
     */
    private class MessageTimeComparator : Comparator<MessageEntity> {
        override fun compare(lhs: MessageEntity, rhs: MessageEntity): Int {
            return if (lhs.created == rhs.created) {
                lhs.msgId - rhs.msgId
            } else lhs.created - rhs.created
        }
    }

    /**
     * 下拉载入历史消息,从最上面开始添加
     */
    private fun loadHistoryList(historyList: List<MessageEntity>?) {
        if (null == historyList || historyList.isEmpty()) {
            return
        }
        Collections.sort(historyList, MessageTimeComparator())
        val chatList = ArrayList<Any>()
        var preTime = 0
        var nextTime = 0
        val idset = HashSet<Int>()
        for (msg in historyList) {
            idset.add(msg.fromId)
            if (msg.displayType == DBConstant.MSG_TYPE_SINGLE_TEXT) {
                if (isMsgGif(msg)) {
                    msg.isGIfEmo = true
                }
            }
            nextTime = msg.created
            val needTimeBubble = DateUtil.needDisplayTime(preTime, nextTime)
            if (needTimeBubble) {
                val `in` = nextTime
                chatList.add(`in`)
            }
            preTime = nextTime
            if (msg.displayType == DBConstant.SHOW_MIX_TEXT) {
                val mixMessage = msg as MixMessage
                chatList.addAll(mixMessage.getMsgList())
            } else {
                chatList.add(msg)
            }
        }
        // 如果是历史消息，从头开始加
        msgObjectList.addAll(0, chatList)
        getImageList()
        ChatAdapter.msgObjectList = msgObjectList
        mAdapter.notifyDataSetChanged()
        mLayoutManager.scrollToPositionWithOffset(chatList.lastIndex + 1, 0)
    }

    /**
     * 获取图片消息列表
     */
    private fun getImageList() {
        for (i in msgObjectList.indices.reversed()) {
            val item = msgObjectList[i]
            if (item is ImageMessage) {
                ImageMessage.addToImageMessageList(item)
            }
        }
    }

    /**
     * 临时处理，一定要干掉
     */
    private fun hidePopup() {
        if (currentPop != null) {
            currentPop?.hidePopup()
        }
    }

    /**
     * msgId 是消息ID
     * localId是本地的ID
     * position 是list 的位置
     *
     * 只更新item的状态
     * 刷新单条记录
     */
    fun updateItemState(position: Int, messageEntity: MessageEntity) {
        //更新DB
        //更新单条记录
        mImService?.dbInterface?.insertOrUpdateMessage(messageEntity)
        mAdapter.notifyDataSetChanged()
    }

    /**
     * 对于混合消息的特殊处理
     */
    private fun updateItemState(messageEntity: MessageEntity) {
        val dbId = messageEntity.id!!
        val msgId = messageEntity.msgId
        val len = msgObjectList.size
        for (index in len - 1 downTo 1) {
            val objectMessage = msgObjectList[index]
            if (objectMessage is MessageEntity) {
                if (objectMessage is ImageMessage) {
                    ImageMessage.addToImageMessageList(objectMessage)
                }
                if (objectMessage.id == dbId && objectMessage.msgId == msgId) {
                    msgObjectList[index] = messageEntity
                    break
                }
            }
        }
        mAdapter.notifyDataSetChanged()
    }

    /**
     * 获取消息类型
     */
    private fun getItemViewType(position: Int): Int {
        /**默认是失败类型 */
        var type = RenderType.MESSAGE_TYPE_INVALID
        val obj = msgObjectList[position]
        if (obj is Int) {
            type = RenderType.MESSAGE_TYPE_TIME_TITLE
        } else if (obj is MessageEntity) {
            val isMine = obj.fromId == loginUser?.peerId
            when (obj.displayType) {
                DBConstant.SHOW_AUDIO_TYPE -> {
                    type = if (isMine) {
                        RenderType.MESSAGE_TYPE_MINE_AUDIO
                    } else {
                        RenderType.MESSAGE_TYPE_OTHER_AUDIO
                    }
                }
                DBConstant.SHOW_IMAGE_TYPE -> {
                    val imageMessage = obj as ImageMessage
                    if (CommonUtil.gifCheck(imageMessage.url)) {
                        type = if (isMine)
                            RenderType.MESSAGE_TYPE_MINE_GIF_IMAGE
                        else
                            RenderType.MESSAGE_TYPE_OTHER_GIF_IMAGE
                    } else {
                        type = if (isMine)
                            RenderType.MESSAGE_TYPE_MINE_IMAGE
                        else
                            RenderType.MESSAGE_TYPE_OTHER_IMAGE
                    }
                }
                DBConstant.SHOW_ORIGIN_TEXT_TYPE -> {
                    val txtMsg = obj as TextMessage
                    if (obj.isGIfEmo) {
                        type = if (isMine)
                            RenderType.MESSAGE_TYPE_MINE_GIF
                        else
                            RenderType.MESSAGE_TYPE_OTHER_GIF
                    } else if (txtMsg.contentEntity.infoType == DBConstant.SHOW_REVOKE_TYPE) {
                        type = if (isMine)
                            RenderType.MESSAGE_TYPE_MINE_REVOKE
                        else
                            RenderType.MESSAGE_TYPE_OTHER_REVOKE
                    } else {
                        type = if (isMine)
                            RenderType.MESSAGE_TYPE_MINE_TEXT
                        else
                            RenderType.MESSAGE_TYPE_OTHER_TEXT
                    }
                }
                DBConstant.SHOW_MIX_TEXT -> {
                }
                DBConstant.SHOW_VIDEO_TYPE -> {
                    type = if (isMine) {
                        RenderType.MESSAGE_TYPE_MINE_VIDEO
                    } else {
                        RenderType.MESSAGE_TYPE_OTHER_VIDEO
                    }
                }
                DBConstant.SHOW_REVOKE_TYPE -> {    //撤回
                    type = if (isMine) {
                        RenderType.MESSAGE_TYPE_MINE_REVOKE
                    } else {
                        RenderType.MESSAGE_TYPE_OTHER_REVOKE
                    }
                }
                else -> {
                }
            }
        }
        return type.ordinal
    }

    /**
     * pop点击事件的定义
     */
    private fun getPopMenu(parent: ViewGroup, listener: MessageOperatePopup.OnItemClickListener): MessageOperatePopup {
        val popupView = MessageOperatePopup.instance(ctx, parent)
        currentPop = popupView
        popupView.setOnItemClickListener(listener)
        return popupView
    }

    private inner class OperateItemClickListener(private val mMsgInfo: MessageEntity, private val mPosition: Int) : MessageOperatePopup.OnItemClickListener {
        private val mType: Int = mMsgInfo.displayType
        @SuppressLint("NewApi")
        override fun onCopyClick() {
            try {
                val manager = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                    val data = ClipData.newPlainText("data", mMsgInfo.content)
                    manager.primaryClip = data
                } else {
                    manager.text = mMsgInfo.content
                }
            } catch (e: Exception) {

            }
        }

        override fun onResendClick() {
            try {
                if (mType == DBConstant.SHOW_AUDIO_TYPE || mType == DBConstant.SHOW_ORIGIN_TEXT_TYPE) {
                    if (mMsgInfo.displayType == DBConstant.SHOW_AUDIO_TYPE) {
                        if (mMsgInfo.sendContent.size < 4) {
                            return
                        }
                    }
                } else if (mType == DBConstant.SHOW_IMAGE_TYPE) {
                    // 之前的状态是什么 上传没有成功继续上传
                    // 上传成功，发送消息
                    val imageMessage = mMsgInfo as ImageMessage
                    if (TextUtils.isEmpty(imageMessage.path)) {
                        Toast.makeText(ctx, ctx.getString(com.leimo.wanxin.R.string.image_path_unavaluable), Toast.LENGTH_LONG).show()
                        return
                    }
                }
                mMsgInfo.status = MessageConstant.MSG_SENDING
                msgObjectList.removeAt(mPosition)
                addItem(mMsgInfo)
                if (mImService != null) {
                    mImService?.messageManager?.resendMessage(mMsgInfo)
                }

            } catch (e: Exception) {

            }
        }

        override fun onSpeakerClick() {
            val audioPlayerHandler = AudioPlayerHandler.getInstance()
            if (audioPlayerHandler.getAudioMode(ctx) == android.media.AudioManager.MODE_NORMAL) {
                audioPlayerHandler.setAudioMode(android.media.AudioManager.MODE_IN_CALL, ctx)
                SpeekerToast.show(ctx, ctx.getText(com.leimo.wanxin.R.string.audio_in_call), Toast.LENGTH_SHORT)
            } else {
                audioPlayerHandler.setAudioMode(android.media.AudioManager.MODE_NORMAL, ctx)
                SpeekerToast.show(ctx, ctx.getText(com.leimo.wanxin.R.string.audio_in_speeker), Toast.LENGTH_SHORT)
            }
        }

        //撤回
        override fun onRevokeClick(position: Int) {

        }

        //删除
        override fun onDeleteClick(position: Int) {

        }
    }

    /**
     * 添加单个消息
     */
    private fun pushList(msg: MessageEntity?) {
        //撤回的消息
        if (msg?.msgType == DBConstant.SHOW_REVOKE_TYPE) {
            updateRevokeMsg(msg)
        } else {
            addItem(msg!!)
        }
    }

    /**
     * 添加list消息
     */
    private fun pushList(entityList: List<MessageEntity>?) {
        loadHistoryList(entityList)
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
                pushList(it)
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

            MessageEvent.Event.HANDLER_IMAGE_UPLOAD_SUCCESS, MessageEvent.Event.HANDLER_IMAGE_UPLOAD_FAILD -> {
                val imageMessage = event.messageEntity as ImageMessage
                updateItemState(imageMessage)
            }

            MessageEvent.Event.HANDLER_VIDEO_UPLOAD_SUCCESS, MessageEvent.Event.HANDLER_VIDEO_UPLOAD_FAILD -> {
                val videoMessage = event.messageEntity as VideoMessage
                updateItemState(videoMessage)
            }
            MessageEvent.Event.HISTORY_MSG_OBTAIN -> {
                if (historyTimes == 1) {
                    msgObjectList.clear()
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
                msgObjectList.clear()
                reqHistoryMsg()
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
        updateItemState(messageEntity)
    }

    /**
     * 肯定是在当前的session内
     */
    private fun onMsgRecv(entity: MessageEntity) {
        mImService?.unReadMsgManager?.ackReadMsg(entity)
        pushList(entity)
        scrollToBottomListItem()
    }

    /**
     * 修改内容状态
     */
    private fun onMsgUnAckTimeoutOrFailure(messageEntity: MessageEntity?) {
        // msgId 应该还是为0
        updateItemState(messageEntity!!)
    }

    /**
     * 1.初始化请求历史消息
     * 2.本地消息不全，也会触发
     */
    private fun reqHistoryMsg() {
        if (peerEntity != null) {
            historyTimes++
            val msgList = mImService?.messageManager?.loadHistoryMsg(historyTimes, currentSessionKey, peerEntity)
            pushList(msgList)
            scrollToBottomListItem()
        }
    }

    /**
     * 处理拍照后的数据
     */
    private fun handleTakePhotoData(data: Intent) {
        if (takePhotoSavePath != null && loginUser != null && peerEntity != null) {
            val imageMessage = ImageMessage.buildForSend(takePhotoSavePath!!, loginUser!!, peerEntity!!)
            val sendList = ArrayList<ImageMessage>(1)
            sendList.add(imageMessage)
            mImService?.messageManager?.sendImages(sendList)
            // 格式有些问题
            pushList(imageMessage)
            etMyMessageChatContent.clearFocus()//消除焦点
        }
    }

    /**
     * 选择视频后的数据处理
     */
    private fun handleTakeVideoData(data: Intent) {
        val selectedVideo = data.data
        val filePathColumn = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(selectedVideo!!, filePathColumn, null, null, null)
        cursor!!.moveToFirst()
        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
        val videoPath = cursor.getString(columnIndex)
        cursor.close()
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
            // 格式有些问题
            pushList(it)
            etMyMessageChatContent?.clearFocus()//消除焦点
        }
    }

    /**
     * 录音结束后处理录音数据
     */
    private fun onRecordVoiceEnd(audioSavePath: String, audioLen: Float) {
        val audioMessage = AudioMessage.buildForSend(audioLen, audioSavePath, loginUser, peerEntity)
        mImService?.messageManager?.sendVoice(audioMessage)
        pushList(audioMessage)
        mAdapter.notifyDataSetChanged()
    }

    /**
     * 上滑加载
     */
    fun onPullDownToRefresh() {
        // 获取消息
        val messageEntity = getTopMsgEntity()
        if (messageEntity != null) {
            val historyMsgInfo = mImService?.messageManager?.loadHistoryMsg(messageEntity, historyTimes)
            if (historyMsgInfo?.size ?: 0 > 0) {
                historyTimes++
                loadHistoryList(historyMsgInfo)
            }
        }
    }

    /**
     * 显示气泡
    */
    private fun showPopWindow(position: Int, parent: ViewGroup, view: View) {
        val message = (msgObjectList[position] as MessageEntity)
        // 创建一个pop对象，然后 分支判断状态，然后显示需要的内容
        val isMine = message.fromId == loginUser?.peerId
        val popup = getPopMenu(parent, OperateItemClickListener(message, position))
        val bResend = message.status == MessageConstant.MSG_FAILURE
        //消息是否在2分钟之内创建的
        val bRevoke = (System.currentTimeMillis() / 1000) - message.created < mDefaultTimeDifference
        popup.show(view, message.displayType, bResend, isMine, bRevoke, position)
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
        SimplePhotoUtil.instance.setConfig(PhotoConfig(this, true, onPathCallback = { path ->
            //用ImageView显示出来
            val bitmap = getLoacalBitmap(path)
            ToastUtil.showShort(path)
        }))
    }

    //打开相册读取文件返回路径
    private fun openAlbum() {
        SimplePhotoUtil.instance.setConfig(PhotoConfig(this, false, onPathCallback = { path ->
            val bitmap = getLoacalBitmap(path)
            ToastUtil.showShort(path)
        }))
    }

    //打开视频文件读取返回路径
    private fun openVideo() {
        SimplePhotoUtil.instance.setConfig(this, { path ->
            ToastUtil.showShort(path)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //选择照片（图库，拍照）
        SimplePhotoUtil.instance.onPhotoResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        //如果表情或工具界面存在 返回就关闭他们
        if (llMyMessageChatFunction.visibility == View.VISIBLE || rlMyMessageChatExpression.visibility == View.VISIBLE) {
            llMyMessageChatFunction.visibility = View.GONE
            rlMyMessageChatExpression.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        historyTimes = 0

        super.onResume()
    }

    override fun onStop() {
        hidePopup()

        super.onStop()
    }

    override fun onDestroy() {
        historyTimes = 0
        MediaManager.release()
        imServiceConnector.disconnect(this)
        EventBus.getDefault().unregister(this)

        super.onDestroy()
    }
}