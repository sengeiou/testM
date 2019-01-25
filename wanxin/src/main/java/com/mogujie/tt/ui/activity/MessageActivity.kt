package com.mogujie.tt.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.util.TypedValue
import android.view.*
import android.view.View.OnTouchListener
import android.widget.AbsListView
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams
import android.widget.Toast
import com.app.common.api.subscribeExtApi
import com.handmark.pulltorefresh.library.PullToRefreshBase
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2
import com.leimo.wanxin.R
import com.mogujie.tt.api.RequestManager
import com.mogujie.tt.api.composeDefault
import com.mogujie.tt.app.IMApplication
import com.mogujie.tt.config.*
import com.mogujie.tt.db.entity.GroupEntity
import com.mogujie.tt.db.entity.MessageEntity
import com.mogujie.tt.db.entity.PeerEntity
import com.mogujie.tt.db.sp.SystemConfigSp
import com.mogujie.tt.imservice.entity.*
import com.mogujie.tt.imservice.event.MessageEvent
import com.mogujie.tt.imservice.event.PriorityEvent
import com.mogujie.tt.imservice.event.SelectEvent
import com.mogujie.tt.imservice.event.UnreadEvent
import com.mogujie.tt.imservice.manager.IMStackManager
import com.mogujie.tt.imservice.manager.IMUnreadMsgManager
import com.mogujie.tt.imservice.support.IMServiceConnector
import com.mogujie.tt.protobuf.helper.EntityChangeEngine
import com.mogujie.tt.ui.adapter.MessageAdapter
import com.mogujie.tt.ui.adapter.album.AlbumHelper
import com.mogujie.tt.ui.adapter.album.ImageItem
import com.mogujie.tt.ui.helper.AudioPlayerHandler
import com.mogujie.tt.ui.helper.Emoparser
import com.mogujie.tt.ui.view.CustomDialog
import com.mogujie.tt.ui.widget.EmoGridView.OnEmoGridViewItemClick
import com.mogujie.tt.ui.widget.MGProgressbar
import com.mogujie.tt.ui.widget.YayaEmoGridView
import com.mogujie.tt.utils.Logger
import com.mogujie.tt.utils.SDPathUtil
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener
import de.greenrobot.event.EventBus
import kotlinx.android.synthetic.main.tt_activity_message.*
import java.io.IOException
import java.util.*

/**
 * @author Nana
 * @Description 主消息界面
 * @date 2014-7-15
 *
 *
 */
class MessageActivity : MessageBaseActivity(), OnRefreshListener2<ListView>, View.OnClickListener, OnTouchListener, TextWatcher {
    private val logger = Logger.getLogger(MessageActivity::class.java)
    internal var progressbar: MGProgressbar? = null
    private var historyTimes = 0
    private lateinit var myDialog: CustomDialog    //进度框


    private val imServiceConnector = object : IMServiceConnector() {
        override fun onIMServiceConnected() {
            IMServiceConnector.logger.d("message_activity#onIMServiceConnected")
            mImService = this.imService
            initMsgData()
        }

        override fun onServiceDisconnected() {}
    }


    override fun onBackPressed() {
        IMApplication.gifRunning = false
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        logger.d("message_activity#onCreate:%s", this)
        super.onCreate(savedInstanceState)
        val bundle = intent.extras
        currentSessionKey = bundle!!.getString(IntentConstant.KEY_SESSION_KEY)
        peerEntity = bundle.getSerializable(IntentConstant.KEY_PEERENTITY) as? PeerEntity
        //        userEntityList = (List<UserEntity>) bundle.getSerializable(IntentConstant.KEY_GROUP_MEMBERS);

        if (peerEntity != null) {
            logger.e("message_activity#chat#peerEntity=%s", peerEntity.toString())
        }
        logger.d("message_activity#currentSessionKey=%s", currentSessionKey)
        initEmo()
        initMsgView()
        imServiceConnector.connect(this)
        EventBus.getDefault().register(this)
        logger.d("message_activity#register im service and eventBus")
    }

    // 触发条件,imservice链接成功，或者newIntent
    private fun initMsgData() {
        historyTimes = 0
        adapter.clearItem()
        ImageMessage.clearImageMessageList()
        loginUser = mImService?.loginManager?.loginInfo
        if (peerEntity == null) {
            peerEntity = mImService!!.sessionManager.findPeerEntity(currentSessionKey)
        }
        // 头像、历史消息加载、取消通知
        setTitleByUser()
        reqHistoryMsg()
        adapter.setImService(mImService, loginUser)
        mImService?.unReadMsgManager?.readUnreadSession(currentSessionKey)
        mImService?.notificationManager?.cancelSessionNotifications(currentSessionKey)
        //        logger.e("message_activity#chat#ToId=%s", peerEntity.getPeerId());
    }

    private fun printGroupInfo() {
        val groupEntityMap = mImService!!.groupManager.groupMap
        val entries = groupEntityMap.entries.iterator()
        while (entries.hasNext()) {
            val entry = entries.next()
            println("Key = " + entry.key + ", Value = " + (entry.value as GroupEntity).toString())
        }
        val sessionInfo = EntityChangeEngine.spiltSessionKey(currentSessionKey)
        val peerType = Integer.parseInt(sessionInfo[0])
        val peerId = Integer.parseInt(sessionInfo[1])
        val groupEntity = mImService?.sessionManager?.findPeerEntity(currentSessionKey) as? GroupEntity
        if (groupEntity != null) {
            val userIds = groupEntity.userList
            logger.e("message_activity#printGroupInfo#userIds=%s", userIds)
        } else {
            logger.e("message_activity#printGroupInfo#groupEntity is null")
        }
    }


    /**
     * 本身位于Message页面，点击通知栏其他session的消息
     */
    override fun onNewIntent(intent: Intent?) {
        logger.d("message_activity#onNewIntent:%s", this)
        super.onNewIntent(intent)
        setIntent(intent)
        historyTimes = 0
        if (intent == null) {
            return
        }
        val newSessionKey = getIntent().getStringExtra(IntentConstant.KEY_SESSION_KEY) ?: return
        logger.d("chat#newSessionInfo:%s", newSessionKey)
        if (newSessionKey != currentSessionKey) {
            currentSessionKey = newSessionKey
            initMsgData()
        }
    }

    override fun onResume() {
        logger.d("message_activity#onresume:%s", this)
        super.onResume()
        IMApplication.gifRunning = true
        historyTimes = 0
        // not the first time
        if (mImService != null) {
            // 处理session的未读信息
            handleUnreadMsgs()
        }
    }

    override fun onDestroy() {
        logger.d("message_activity#onDestroy:%s", this)
        historyTimes = 0
        imServiceConnector.disconnect(this)
        EventBus.getDefault().unregister(this)
        if (adapter != null) {
            adapter.clearItem()
        }
        if (albumList != null) {
            albumList!!.clear()
        }

        ImageMessage.clearImageMessageList()
        super.onDestroy()
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
        setTitle(peerEntity?.mainName)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Activity.RESULT_OK != resultCode) {
            return
        }
        data?.let {
            when (requestCode) {
                SysConstant.CAMERA_WITH_DATA -> {
                    SDPathUtil.updateImageSysStatu(applicationContext, takePhotoSavePath)
                    handleTakePhotoData(data)
                }
                SysConstant.VIDEO_WITH_DATA -> handleTakeVideoData(data)
                SysConstant.ALBUM_BACK_DATA -> {
                    logger.d("pic#ALBUM_BACK_DATA")
                    intent = data
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

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
     *
     *
     * todo  need find good solution
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

    fun onEventMainThread(event: MessageEvent) {
        val type = event.event
        val entity = event.messageEntity
        when (type) {
            MessageEvent.Event.ACK_SEND_MESSAGE_OK -> {
                onMsgAck(event.messageEntity)
            }

            MessageEvent.Event.ACK_SEND_MESSAGE_FAILURE -> {
                // 失败情况下新添提醒
                showToast(R.string.message_send_failed)
                run { onMsgUnAckTimeoutOrFailure(event.messageEntity) }
            }
            MessageEvent.Event.ACK_SEND_MESSAGE_TIME_OUT -> {
                onMsgUnAckTimeoutOrFailure(event.messageEntity)
            }

            MessageEvent.Event.HANDLER_IMAGE_UPLOAD_SUCCESS, MessageEvent.Event.HANDLER_IMAGE_UPLOAD_FAILD -> {
                val imageMessage = event.messageEntity as ImageMessage
                adapter.updateItemState(imageMessage)
            }

            MessageEvent.Event.HANDLER_VIDEO_UPLOAD_SUCCESS, MessageEvent.Event.HANDLER_VIDEO_UPLOAD_FAILD -> {
                val videoMessage = event.messageEntity as VideoMessage
                adapter.updateItemState(videoMessage)
            }
            MessageEvent.Event.HISTORY_MSG_OBTAIN -> {
                if (historyTimes == 1) {
                    adapter.clearItem()
                    reqHistoryMsg()
                }
            }
        }
    }

    fun onEventMainThread(event: UnreadEvent) {
        when (event.event) {
            UnreadEvent.Event.UNREAD_MSG_RECEIVED, UnreadEvent.Event.UNREAD_MSG_LIST_OK, UnreadEvent.Event.SESSION_READED_UNREAD_MSG -> if (IMUnreadMsgManager.instance().totalUnreadCount > 0) {
                historyTimes = 0
                adapter.clearItem()
                reqHistoryMsg()
            }
        }
    }

    /**
     * [备注] DB保存，与session的更新manager已经做了
     *
     * @param messageEntity
     */
    private fun onMsgAck(messageEntity: MessageEntity?) {
        logger.d("message_activity#onMsgAck")
        val msgId = messageEntity!!.msgId
        logger.d("chat#onMsgAck, msgId:%d", msgId)

        /**到底采用哪种ID呐?? */
        val localId = messageEntity.id!!
        adapter.updateItemState(messageEntity)
    }


    private fun handleUnreadMsgs() {
        logger.d("messageacitivity#handleUnreadMsgs sessionId:%s", currentSessionKey)
        // 清除未读消息
        val unreadEntity = mImService?.unReadMsgManager?.findUnread(currentSessionKey) ?: return
        val unReadCnt = unreadEntity.unReadCnt
        if (unReadCnt > 0) {
            mImService!!.notificationManager.cancelSessionNotifications(currentSessionKey)
            adapter.notifyDataSetChanged()
            scrollToBottomListItem()
        }
    }


    // 肯定是在当前的session内
    private fun onMsgRecv(entity: MessageEntity) {
        logger.d("message_activity#onMsgRecv")

        mImService?.unReadMsgManager?.ackReadMsg(entity)
        logger.d("chat#start pushList")
        pushList(entity)
        val lv = pullToRefreshMessageList.refreshableView
        if (lv != null) {
            if (lv.lastVisiblePosition < adapter.count) {
                tvNewMsgTip?.visibility = View.VISIBLE
            } else {
                scrollToBottomListItem()
            }
        }
    }

    private fun onMsgUnAckTimeoutOrFailure(messageEntity: MessageEntity?) {
        logger.d("chat#onMsgUnAckTimeoutOrFailure, msgId:%s", messageEntity!!.msgId)
        // msgId 应该还是为0
        adapter.updateItemState(messageEntity)
    }

    private fun initEmo() {
        Emoparser.getInstance(this@MessageActivity)
        IMApplication.gifRunning = true
    }

    /**
     * @Description 初始化界面控件
     * 有点庞大 todo
     */
    private fun initMsgView() {
        myDialog = CustomDialog(this)

        pullToRefreshMessageList.refreshableView.addHeaderView(LayoutInflater.from(this).inflate(R.layout.tt_messagelist_header, pullToRefreshMessageList.refreshableView, false))
        val loadingDrawable = resources.getDrawable(R.drawable.pull_to_refresh_indicator)
        val indicatorWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 29f,
                resources.displayMetrics).toInt()
        loadingDrawable.bounds = Rect(0, indicatorWidth, 0, indicatorWidth)
        pullToRefreshMessageList.loadingLayoutProxy.setLoadingDrawable(loadingDrawable)
        pullToRefreshMessageList.refreshableView.cacheColorHint = Color.WHITE
        pullToRefreshMessageList.refreshableView.selector = ColorDrawable(Color.WHITE)
        pullToRefreshMessageList.refreshableView.setOnTouchListener(lvPTROnTouchListener)
        adapter = MessageAdapter(this)
        pullToRefreshMessageList.setAdapter(adapter)
        pullToRefreshMessageList.setOnRefreshListener(this)
        pullToRefreshMessageList.setOnScrollListener(object : PauseOnScrollListener(ImageLoader.getInstance(), true, true) {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                when (scrollState) {
                    AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> if (view.lastVisiblePosition == view.count - 1) {
                        tvNewMsgTip.visibility = View.GONE
                    }
                }
            }
        })


        val messageEdtParam = edtMessageTxt.layoutParams as LayoutParams
        messageEdtParam.addRule(RelativeLayout.LEFT_OF, R.id.btnEmo)
        messageEdtParam.addRule(RelativeLayout.RIGHT_OF, R.id.btnVoice)
        edtMessageTxt.onFocusChangeListener = msgEditOnFocusChangeListener
        initListener()


        //OTHER_PANEL_VIEW
        val params = layoutAddOtherPanel.layoutParams as LayoutParams
        if (keyboardHeight > 0) {
            params.height = keyboardHeight
            layoutAddOtherPanel.layoutParams = params
        }

        //EMO_LAYOUT
        val paramEmoLayout = layoutEmo.layoutParams as LayoutParams
        if (keyboardHeight > 0) {
            paramEmoLayout.height = keyboardHeight
            layoutEmo.layoutParams = paramEmoLayout
        }
        gridviewEmo.setOnEmoGridViewItemClick(onEmoGridViewItemClick)
        gridviewEmo.setAdapter()
        gridviewEmoYaya.setOnEmoGridViewItemClick(yayaOnEmoGridViewItemClick)
        gridviewEmoYaya.setAdapter()
        rgEmo.setOnCheckedChangeListener(emoOnCheckedChangeListener)


        //LOADING
        val view = LayoutInflater.from(this@MessageActivity)
                .inflate(R.layout.tt_progress_ly, null)
        progressbar = view.findViewById(R.id.tt_progress) as MGProgressbar
        val pgParms = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        pgParms.bottomMargin = 50
        addContentView(view, pgParms)

        //ROOT_LAYOUT_LISTENER
        baseRoot.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)

        //撤回删除事件
        adapter.setPopCallBack(object : MessageAdapter.PopCallBack {

            //撤回
            override fun onRevokeClick(position: Int) {
                myDialog.showLoadingDialog()
                val objectArrayList = adapter.msgObjectList
                val messageEntity = objectArrayList[position] as MessageEntity
                //网络请求
                RequestManager.instanceApi
                        .msgRevoke(messageEntity.fromId, messageEntity.toId, messageEntity.msgId)
                        .compose(composeDefault())
                        .subscribeExtApi({
                            myDialog.dismissLoadingDialog()
                            CmdMessage.buildForSend("撤回了一条消息", loginUser, currentSessionKey)?.apply {
                                displayType = DBConstant.SHOW_REVOKE_TYPE
                                setAttribute(MessageExtConst.MSGID, messageEntity.msgId)
                                setAttribute(MessageExtConst.CMD_TIME, System.currentTimeMillis())
                                mImService?.messageManager?.sendCMD(this)
                            }
                            //调用adapter的撤回方法
                            adapter.revokeMsg(messageEntity)
                        })
            }

            //删除
            override fun onDeleteClick(position: Int) {
                myDialog.showLoadingDialog()
                val objectArrayList = adapter.msgObjectList
                val messageEntity = objectArrayList[position] as MessageEntity

                //网络请求
                RequestManager.instanceApi
                        .msgDelete(messageEntity.fromId, messageEntity.toId, messageEntity.msgId)
                        .compose(composeDefault())
                        .subscribeExtApi({
                            myDialog.dismissLoadingDialog()
                            //调用adapter的删除方法
                            adapter.removeMsg(messageEntity)
                        })
            }
        })
    }

    override fun initListener() {
        edtMessageTxt.setOnClickListener(this)
        edtMessageTxt.addTextChangedListener(this)
        btnAddPhoto.setOnClickListener(this)
        btnEmo.setOnClickListener(this)
        btnKeyboard.setOnClickListener(this)
        btnVoice.setOnClickListener(this)
        btnRecordVoice.setOnTouchListener(this)
        btnSendMessage.setOnClickListener(this)
        tvNewMsgTip.setOnClickListener(this)
        take_photo_btn.setOnClickListener(this)
        take_camera_btn.setOnClickListener(this)
        take_video_btn.setOnClickListener(this)
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
     * @param data
     * @Description 处理拍照后的数据
     * 应该是从某个 activity回来的
     */
    private fun handleTakePhotoData(data: Intent) {
        if (takePhotoSavePath != null && loginUser != null && peerEntity != null) {
            val imageMessage = ImageMessage.buildForSend(takePhotoSavePath!!, loginUser!!, peerEntity!!)
            val sendList = ArrayList<ImageMessage>(1)
            sendList.add(imageMessage)
            mImService?.messageManager?.sendImages(sendList)
            // 格式有些问题
            pushList(imageMessage)
            edtMessageTxt.clearFocus()//消除焦点
        }
    }

    //选择视频后的数据处理
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
            edtMessageTxt?.clearFocus()//消除焦点
        }
    }

    /**
     * @param audioLen
     * @Description 录音结束后处理录音数据
     */
    private fun onRecordVoiceEnd(audioSavePath: String, audioLen: Float) {
        logger.d("message_activity#chat#audio#onRecordVoiceEnd audioLen:%f", audioLen)
        val audioMessage = AudioMessage.buildForSend(audioLen, audioSavePath, loginUser, peerEntity)
        mImService?.messageManager?.sendVoice(audioMessage)
        pushList(audioMessage)
        adapter.notifyDataSetChanged()
    }

    override fun onPullUpToRefresh(refreshView: PullToRefreshBase<ListView>) {}

    override fun onPullDownToRefresh(
            refreshView: PullToRefreshBase<ListView>) {
        // 获取消息
        refreshView.postDelayed({
            val mlist = pullToRefreshMessageList.refreshableView
            val preSum = mlist.count
            val messageEntity = adapter.topMsgEntity
            if (messageEntity != null) {
                val historyMsgInfo = mImService?.messageManager?.loadHistoryMsg(messageEntity, historyTimes)
                if (historyMsgInfo?.size ?: 0 > 0) {
                    historyTimes++
                    adapter.loadHistoryList(historyMsgInfo)
                }
            }
            val afterSum = mlist.count
            mlist.setSelection(afterSum - preSum)
            refreshView.onRefreshComplete()
        }, 200)
    }


    // 主要是录制语音的
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val id = v.id
        scrollToBottomListItem()
        if (id == R.id.btnRecordVoice) {
            soundVolumeView.onPressToSpeakBtnTouch(v, event, { voiceFilePath, voiceTimeLength ->
                onRecordVoiceEnd(voiceFilePath, voiceTimeLength)
            })
        }
        return false
    }

    override fun onStop() {
        logger.d("message_activity#onStop:%s", this)
        adapter.hidePopup()
        AudioPlayerHandler.getInstance().clear()
        super.onStop()
    }


    override fun afterTextChanged(s: Editable) {}

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                   after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.length > 0) {
            btnSendMessage.visibility = View.VISIBLE
            val param = edtMessageTxt
                    .layoutParams as LayoutParams
            param.addRule(RelativeLayout.LEFT_OF, R.id.btnEmo)
            btnAddPhoto.visibility = View.GONE
        } else {
            btnAddPhoto.visibility = View.VISIBLE
            val param = edtMessageTxt
                    .layoutParams as LayoutParams
            param.addRule(RelativeLayout.LEFT_OF, R.id.btnEmo)
            btnSendMessage.visibility = View.GONE
        }
    }


    override fun onPause() {
        logger.d("message_activity#onPause:%s", this)
        super.onPause()
    }


    private fun actFinish() {
        inputManager?.hideSoftInputFromWindow(edtMessageTxt.windowToken, 0)
        IMStackManager.getStackManager().popTopActivitys(MainActivity::class.java)
        IMApplication.gifRunning = false
        this@MessageActivity.finish()
    }
}
