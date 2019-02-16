package com.qingmeng.mengmeng.activity

import android.support.v7.widget.LinearLayoutManager
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.config.IntentConstant
import com.mogujie.tt.db.entity.GroupEntity
import com.mogujie.tt.imservice.entity.RecentInfo
import com.mogujie.tt.imservice.event.*
import com.mogujie.tt.imservice.service.IMService
import com.mogujie.tt.imservice.support.IMServiceConnector
import com.mogujie.tt.utils.DateUtil
import com.mogujie.tt.utils.IMUIHelper
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import com.qingmeng.mengmeng.view.SwipeMenuLayout
import com.qingmeng.mengmeng.view.dot.UnreadMsgUtils
import de.greenrobot.event.EventBus
import kotlinx.android.synthetic.main.activity_my_message.*
import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.startActivity
import java.util.*

/**
 *  Description :设置 - 消息

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MyMessageActivity : BaseActivity() {
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: CommonAdapter<RecentInfo>
    private var mRecentSessionList = ArrayList<RecentInfo>()             //消息内容

    /**
     * 消息用到的
     */
    private var mImService: IMService? = null
    private val imServiceConnector = object : IMServiceConnector() {
        override fun onServiceDisconnected() {
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this)
            }
        }

        override fun onIMServiceConnected() {
            IMServiceConnector.logger.d("chatfragment#recent#onIMServiceConnected")
            mImService = this.imService
            if (mImService == null) {
                //why ,some reason
                return
            }
            //依赖联系人回话、未读消息、用户的信息三者的状态
            onRecentContactDataReady()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_my_message
    }

    override fun initObject() {
        super.initObject()

        setHeadName(R.string.message)

        initAdapter()

        /**
         * 消息用到的
         */
        imServiceConnector.connect(this)
        EventBus.getDefault().register(this)
    }

    override fun initListener() {
        super.initListener()

        //下拉刷新
        srlMyMessage.setOnRefreshListener {
            onRecentContactDataReady()
        }

        //返回
        mBack.setOnClickListener {
            onBackPressed()
        }

        rvMyMessage.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                //SwipeMenuLayout关闭view
                SwipeMenuLayout.viewCache?.smoothClose()
            }
            false
        }
    }

    private fun initAdapter() {
        mLayoutManager = LinearLayoutManager(this)
        rvMyMessage.layoutManager = mLayoutManager
        mAdapter = CommonAdapter(this, R.layout.activity_my_message_item, mRecentSessionList, holderConvert = { holder, t, position, payloads ->
            holder.apply {
                //头像
                t.avatar?.let {
                    GlideLoader.load(this@MyMessageActivity, it[0], getView(R.id.ivMyMessageRvLogo), placeholder = R.drawable.default_img_icon)
                }
                //未读消息
                UnreadMsgUtils.show(getView(R.id.viewMyMessageRvTipsNum), t.unReadCnt)
                //姓名
                setText(R.id.tvMyMessageRvTitle, t.name)
                //最后消息
                setText(R.id.tvMyMessageContent, t.info)
                //最后时间
                setText(R.id.tvMyMessageRvTime, DateUtil.getSessionTime(t.updateTime))
                //消息点击
                getView<LinearLayout>(R.id.llMyMessageRv).setOnClickListener {
//                    startActivity(Intent(this@MyMessageActivity, MessageActivity::class.java).apply {
//                        putExtra(IntentConstant.KEY_SESSION_KEY, t.sessionKey)
//                    })
                    startActivity<MyMessageChatActivity>(IntentConstant.KEY_SESSION_KEY to t.sessionKey)
                }
                //删除
                getView<TextView>(R.id.tvMyMessageRvDelete).setOnClickListener {
//                    startActivity<MyMessageChatActivity>(IntentConstant.KEY_SESSION_KEY to t.sessionKey)
                    //关闭view
                    getView<SwipeMenuLayout>(R.id.smlMyMessageRv).smoothClose()
                    mImService?.sessionManager?.reqRemoveSession(mRecentSessionList[position])
                }
            }
        }, onItemClick = { view, holder, position ->

        })
        rvMyMessage.adapter = mAdapter
    }

    /**
     * -------------------------------------------------------------start-------------------------------------------------------------
     */

    /**
     * 更新单个RecentInfo 屏蔽群组信息
     */
    private fun updateRecentInfoByShield(entity: GroupEntity) {
        val sessionKey = entity.sessionKey
        for (recentInfo in mRecentSessionList) {
            if (recentInfo.sessionKey == sessionKey) {
                val status = entity.status
                val isFor = status == DBConstant.GROUP_STATUS_SHIELD
                recentInfo.isForbidden = isFor
                mAdapter.notifyDataSetChanged()
                break
            }
        }
    }

    /**
     * EventBus
     */
    fun onEventMainThread(sessionEvent: SessionEvent) {
        when (sessionEvent) {
            SessionEvent.RECENT_SESSION_LIST_UPDATE -> {
            }
            SessionEvent.RECENT_SESSION_LIST_SUCCESS -> {
            }
            SessionEvent.SET_SESSION_TOP -> onRecentContactDataReady()
        }
    }

    fun onEventMainThread(event: GroupEvent) {
        when (event.event) {
            GroupEvent.Event.GROUP_INFO_OK -> {
            }
            GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS -> {
                onRecentContactDataReady()
                searchDataReady()
            }
            GroupEvent.Event.GROUP_INFO_UPDATED -> {
                onRecentContactDataReady()
                searchDataReady()
            }
            GroupEvent.Event.SHIELD_GROUP_OK -> {
                //更新最下栏的未读计数、更新session
                onShieldSuccess(event.groupEntity)
            }
            GroupEvent.Event.SHIELD_GROUP_FAIL -> {
            }
            GroupEvent.Event.SHIELD_GROUP_TIMEOUT -> onShieldFail()
        }
    }

    fun onEventMainThread(event: UnreadEvent) {
        when (event.event) {
            UnreadEvent.Event.UNREAD_MSG_RECEIVED -> {
            }
            UnreadEvent.Event.UNREAD_MSG_LIST_OK -> {
            }
            UnreadEvent.Event.SESSION_READED_UNREAD_MSG -> onRecentContactDataReady()
        }
    }

    fun onEventMainThread(event: UserInfoEvent) {
        when (event) {
            UserInfoEvent.USER_INFO_UPDATE -> {
            }
            UserInfoEvent.USER_INFO_OK -> {
                onRecentContactDataReady()
                searchDataReady()
            }
        }
    }

    fun onEventMainThread(loginEvent: LoginEvent) {
        when (loginEvent) {
            LoginEvent.LOCAL_LOGIN_SUCCESS -> {
            }
            LoginEvent.LOGINING -> {
//                if (reconnectingProgressBar != null) {
//                    reconnectingProgressBar.setVisibility(View.VISIBLE)
//                }
                ToastUtil.showShort("ProgressBar显示")
            }
            LoginEvent.LOCAL_LOGIN_MSG_SERVICE -> {
            }
            LoginEvent.LOGIN_OK -> {
//                isManualMConnect = false
//                noNetworkView.setVisibility(View.GONE)
                ToastUtil.showShort("view隐藏")
            }
            LoginEvent.LOGIN_AUTH_FAILED -> {
            }
            LoginEvent.LOGIN_INNER_FAILED -> {
                onLoginFailure(loginEvent)
            }
            LoginEvent.PC_OFFLINE -> {
            }
            LoginEvent.KICK_PC_SUCCESS -> onPCLoginStatusNotify(false)
            LoginEvent.KICK_PC_FAILED -> ToastUtil.showShort(getString(R.string.kick_pc_failed))
            LoginEvent.PC_ONLINE -> onPCLoginStatusNotify(true)
            else -> {
//                reconnectingProgressBar.setVisibility(View.GONE)
                ToastUtil.showShort("ProgressBar隐藏")
            }
        }
    }


    fun onEventMainThread(socketEvent: SocketEvent) {
        when (socketEvent) {
            SocketEvent.MSG_SERVER_DISCONNECTED -> handleServerDisconnected()

            SocketEvent.CONNECT_MSG_SERVER_FAILED -> {
            }
            SocketEvent.REQ_MSG_SERVER_ADDRS_FAILED -> {
                handleServerDisconnected()
                onSocketFailure(socketEvent)
            }
        }
    }

    fun onEventMainThread(reconnectEvent: ReconnectEvent) {
        when (reconnectEvent) {
            ReconnectEvent.DISABLE -> handleServerDisconnected()
        }
    }

    /**
     * 登录失败
     */
    private fun onLoginFailure(event: LoginEvent) {
//        if (!isManualMConnect) {
//            return
//        }
//        isManualMConnect = false
        val errorTip = getString(IMUIHelper.getLoginErrorTip(event))
//        reconnectingProgressBar.setVisibility(View.GONE)
        ToastUtil.showShort(errorTip)
    }

    private fun onSocketFailure(event: SocketEvent) {
//        if (!isManualMConnect) {
//            return
//        }
//        isManualMConnect = false
        val errorTip = getString(IMUIHelper.getSocketErrorTip(event))
//        reconnectingProgressBar.setVisibility(View.GONE)
        ToastUtil.showShort(errorTip)
    }

    /**
     * 更新页面以及 下面的未读总计数
     */
    private fun onShieldSuccess(entity: GroupEntity?) {
        if (entity == null) {
            return
        }
        // 更新某个sessionId
        updateRecentInfoByShield(entity)
//        val unreadMsgManager = mImService?.unReadMsgManager
//        val totalUnreadMsgCnt = unreadMsgManager?.totalUnreadCount
//        (getActivity() as MainActivity).setUnreadMessageCnt(totalUnreadMsgCnt)
    }

    /**
     * 提示
     */
    private fun onShieldFail() {
        ToastUtil.showShort(getString(R.string.req_msg_failed))
    }

    /**
     * 搜索数据OK
     * 群组数据与 user数据都已经完毕
     */
    private fun searchDataReady() {
        if (mImService?.contactManager?.isUserDataReady!! && mImService?.groupManager?.isGroupReady!!) {
//            showSearchFrameLayout()
            ToastUtil.showShort("searchDataReady()\n搜索数据OK")
        }
    }

    /**
     * 多端，PC端在线状态通知
     */
    fun onPCLoginStatusNotify(isOnline: Boolean) {
        if (isOnline) {
//            reconnectingProgressBar.setVisibility(View.GONE)
//            noNetworkView.setVisibility(View.VISIBLE)
//            notifyImage.setImageResource(com.leimo.wanxin.R.drawable.pc_notify)
//            displayView.setText(com.leimo.wanxin.R.string.pc_status_notify)
            /**添加踢出事件 */
//            noNetworkView.setOnClickListener(View.OnClickListener {
//                reconnectingProgressBar.setVisibility(View.VISIBLE)
//                imService.getLoginManager().reqKickPCClient()
//            })
            ToastUtil.showShort("onPCLoginStatusNotify()\n多端，PC端在线状态通知")
        } else {
//            noNetworkView.setVisibility(View.GONE)
        }
    }

    /**
     * 下线提示
     */
    private fun handleServerDisconnected() {
//        if (reconnectingProgressBar != null) {
//            reconnectingProgressBar.setVisibility(View.GONE)
//        }

//        if (noNetworkView != null) {
//            notifyImage.setImageResource(com.leimo.wanxin.R.drawable.warning)
//            noNetworkView.setVisibility(View.VISIBLE)
//            if (imService != null) {
//                if (imService.getLoginManager().isKickout()) {
//                    displayView.setText(com.leimo.wanxin.R.string.disconnect_kickout)
//                } else {
//                    displayView.setText(com.leimo.wanxin.R.string.no_network)
//                }
//            }
//            /**重连【断线、被其他移动端挤掉】 */
//            noNetworkView.setOnClickListener(View.OnClickListener {
//                TTBaseFragment.logger.d("chatFragment#noNetworkView clicked")
//                val manager = imService.getReconnectManager()
//                if (NetworkUtil.isNetWorkAvalible(getActivity())) {
//                    isManualMConnect = true
//                    IMLoginManager.instance().relogin()
//                } else {
//                    Toast.makeText(getActivity(), com.leimo.wanxin.R.string.no_network_toast, Toast.LENGTH_SHORT).show()
//                    return@OnClickListener
//                }
//                reconnectingProgressBar.setVisibility(View.VISIBLE)
//            })
//        }
        ToastUtil.showShort("handleServerDisconnected()\n下线提示")
    }

    /**
     * 这个处理有点过于粗暴 消息列表展示
     */
    private fun onRecentContactDataReady() {
        val isUserData = mImService?.contactManager?.isUserDataReady
        val isSessionData = mImService?.sessionManager?.isSessionListReady
        val isGroupData = mImService?.groupManager?.isGroupReady

        if (!(isUserData!! && isSessionData!! && isGroupData!!)) {
            srlMyMessage.isRefreshing = false
            return
        }

//        val unreadMsgManager = mImService?.unReadMsgManager
//        val totalUnreadMsgCnt = unreadMsgManager?.totalUnreadCount
//        ((MainActivity) getActivity()).setUnreadMessageCnt(totalUnreadMsgCnt);
        //todo 设置未读消息
        val recentSessionList = mImService?.sessionManager?.recentListInfo
        mRecentSessionList.clear()
        mRecentSessionList.addAll(recentSessionList!!)
        setNoChatView(recentSessionList)
        mAdapter.notifyDataSetChanged()
        srlMyMessage.isRefreshing = false
    }

    /**
     * 无消息提示
     */
    private fun setNoChatView(recentSessionList: List<RecentInfo>) {
        if (recentSessionList.isEmpty()) {
//            noChatView.setVisibility(View.VISIBLE)
            ToastUtil.showShort("暂无消息")
        } else {
//            noChatView.setVisibility(View.GONE)
        }
    }

    /**
     * -------------------------------------------------------------end-------------------------------------------------------------
     */

    override fun onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        imServiceConnector.disconnect(this)

        super.onDestroy()
    }
}