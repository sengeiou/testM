package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.lemo.emojcenter.FaceInitData
import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.config.IntentConstant
import com.mogujie.tt.db.entity.GroupEntity
import com.mogujie.tt.imservice.entity.RecentInfo
import com.mogujie.tt.imservice.event.GroupEvent
import com.mogujie.tt.imservice.event.SessionEvent
import com.mogujie.tt.imservice.event.UnreadEvent
import com.mogujie.tt.imservice.event.UserInfoEvent
import com.mogujie.tt.imservice.service.IMService
import com.mogujie.tt.imservice.support.IMServiceConnector
import com.mogujie.tt.utils.DateUtil
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.constant.IConstants.MESSAGE_TO_CHAT
import com.qingmeng.mengmeng.constant.IConstants.MYFRAGMENT_TO_MESSAGE
import com.qingmeng.mengmeng.constant.IConstants.MY_TO_MESSAGE
import com.qingmeng.mengmeng.constant.IConstants.TO_MESSAGE
import com.qingmeng.mengmeng.entity.MyMessage
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import com.qingmeng.mengmeng.view.SwipeMenuLayout
import com.qingmeng.mengmeng.view.dot.UnreadMsgUtils
import de.greenrobot.event.EventBus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_my_message.*
import kotlinx.android.synthetic.main.activity_shop_detail.*
import kotlinx.android.synthetic.main.layout_head.*
import kotlinx.android.synthetic.main.view_tips.*
import org.jetbrains.anko.startActivityForResult
import java.util.*

/**
 *  Description :设置 - 消息

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
@SuppressLint("CheckResult")
class MyMessageActivity : BaseActivity() {
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: CommonAdapter<RecentInfo>
    private var mAllList = ArrayList<RecentInfo>()
    private var mRecentSessionList = ArrayList<RecentInfo>()             //消息内容
    private var mSysRecentInfo = RecentInfo()
    private var mKefuRecentInfo = RecentInfo()
    private var mAvatar = ""                                             //默认发送者头像
    private var mIsMyFragmentEnter = false                               //是我的板块进来的

    companion object {
        var instance: MyMessageActivity? = null                          //当前aty
    }

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
            IMServiceConnector.logger.d("MyMessageActivity#recent#onIMServiceConnected")
            mImService = this.imService
            if (mImService == null) {
                //why ,some reason
                return
            }
            httpLoad()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_my_message
    }

    override fun initObject() {
        super.initObject()

        instance = this
        setHeadName(R.string.message)
        tvViewTips.text = getString(R.string.my_message_null_tips)
        mIsMyFragmentEnter = intent.getBooleanExtra(MY_TO_MESSAGE, false)
        if (mIsMyFragmentEnter) {
            MYFRAGMENT_TO_MESSAGE = true
        }

        initAdapter()
        myDialog.showLoadingDialog()

        /**
         * 消息用到的
         */
        imServiceConnector.connect(this)
        EventBus.getDefault().register(this)

        mSysRecentInfo.name = "系统消息"
        mSysRecentInfo.peerId = 60834
        mSysRecentInfo.sessionKey = "1_60834"


        mKefuRecentInfo.name = "盟盟客服"
        mKefuRecentInfo.peerId = 60831
        mKefuRecentInfo.sessionKey = "1_60831"
    }

    override fun initListener() {
        super.initListener()

        //下拉刷新
        srlMyMessage.setOnRefreshListener {
            httpLoad()
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
        //RecyclerView滑动监听
        rvMyMessage.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                srlMyMessage.isRefreshEnabled = !recyclerView.canScrollVertically(-1)
            }
        })
    }

    private fun initAdapter() {
        mLayoutManager = LinearLayoutManager(this)
        rvMyMessage.layoutManager = mLayoutManager
        mAdapter = CommonAdapter(this, R.layout.activity_my_message_item, mAllList, holderConvert = { holder, t, position, payloads ->
            holder.apply {
                //接口请求的不给他左滑
                getView<SwipeMenuLayout>(R.id.smlMyMessageRv).isSwipeEnable = t.sessionType != 0
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
                    FaceInitData.init(applicationContext)
                    FaceInitData.setAlias("${MainApplication.instance.user.wxUid}")
                    if (t.sessionKey != null && t.name != null) {
                        startActivityForResult<MyMessageChatActivity>(TO_MESSAGE, IntentConstant.KEY_SESSION_KEY to t.sessionKey, "title" to t.name, "avatar" to if (t.avatar!=null && t.avatar.size>0 && t.avatar[0] != null && t.avatar[0].isNotEmpty()) t.avatar[0] else mAvatar)
                        MESSAGE_TO_CHAT = true
                        //如果聊天页面存在了 就销毁它
                        finishAty(MyMessageChatActivity::class.java)
                    } else {
                        httpLoad()
                    }
                }
                //删除
                getView<TextView>(R.id.tvMyMessageRvDelete).setOnClickListener {
                    //                    startActivity<MyMessageChatActivity>(IntentConstant.KEY_SESSION_KEY to t.sessionKey)
                    //关闭view
                    getView<SwipeMenuLayout>(R.id.smlMyMessageRv).smoothClose()
                    mImService?.sessionManager?.reqRemoveSession(mAllList[position])
                }
            }
        }, onItemClick = { view, holder, position ->

        })
        rvMyMessage.adapter = mAdapter
    }

    private fun httpLoad() {
        ApiUtils.getApi()
                .getMyMessage()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    myDialog.dismissLoadingDialog()
                    srlMyMessage.isRefreshing = false
                    it.apply {
                        if (code == 12000) {
                            setData(data?.chatInfoList)
                        } else {
                            ToastUtil.showShort(msg)
                            //依赖联系人回话、未读消息、用户的信息三者的状态
                            onRecentContactDataReady()
                            setNoChatView()
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    srlMyMessage.isRefreshing = false
                    onRecentContactDataReady()
                    setNoChatView()
                }, {}, { addSubscription(it) })
    }

    private fun setData(chatInfoList: List<MyMessage>?) {
        chatInfoList?.forEachIndexed { index, myMessage ->
            val recentInfo = RecentInfo()
            recentInfo.avatar = listOf(myMessage.avatar)
            recentInfo.name = myMessage.name
            recentInfo.peerId = myMessage.wxUid
            recentInfo.sessionKey = "1_${myMessage.wxUid}"

            when (myMessage.name) {
                getString(R.string.systemNotification) -> {
                    if(mSysRecentInfo.avatar==null) {
                        mSysRecentInfo = recentInfo
                    }
                }
                "盟盟客服" -> {
                    if(mKefuRecentInfo.avatar==null) {
                        mKefuRecentInfo = recentInfo
                    }
                }
            }
        }
        onRecentContactDataReady()
        setNoChatView()
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
            SessionEvent.SET_SESSION_TOP -> {
                httpLoad()
            }
        }
    }

    fun onEventMainThread(event: GroupEvent) {
        when (event.event) {
            GroupEvent.Event.GROUP_INFO_OK -> {
            }
            GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS -> {
                httpLoad()
                searchDataReady()
            }
            GroupEvent.Event.GROUP_INFO_UPDATED -> {
                httpLoad()
                searchDataReady()
            }
            GroupEvent.Event.SHIELD_GROUP_OK -> {   //更新最下栏的未读计数、更新session
                onShieldSuccess(event.groupEntity)
            }
            GroupEvent.Event.SHIELD_GROUP_FAIL -> {
            }
            GroupEvent.Event.SHIELD_GROUP_TIMEOUT -> {
                onShieldFail()
            }
        }
    }

    fun onEventMainThread(event: UnreadEvent) {
        when (event.event) {
            UnreadEvent.Event.UNREAD_MSG_RECEIVED -> {  //新消息接收
                httpLoad()
            }
            UnreadEvent.Event.UNREAD_MSG_LIST_OK -> {
            }
            UnreadEvent.Event.SESSION_READED_UNREAD_MSG -> {
                httpLoad()
            }
        }
    }

    fun onEventMainThread(event: UserInfoEvent) {
        when (event) {
            UserInfoEvent.USER_INFO_UPDATE -> {
            }
            UserInfoEvent.USER_INFO_OK -> {
                httpLoad()
                searchDataReady()
            }
        }
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
//            ToastUtil.showShort("searchDataReady()\n搜索数据OK")
        }
    }

    /**
     * 这个处理有点过于粗暴 消息列表展示
     */
    private fun onRecentContactDataReady(): Boolean {
        val isUserData = mImService?.contactManager?.isUserDataReady
        val isSessionData = mImService?.sessionManager?.isSessionListReady
        val isGroupData = mImService?.groupManager?.isGroupReady

        if (!(isUserData!! && isSessionData!! && isGroupData!!)) {
            srlMyMessage.isRefreshing = false
            return false
        }

//        val unreadMsgManager = mImService?.unReadMsgManager
//        val totalUnreadMsgCnt = unreadMsgManager?.totalUnreadCount
//        ((MainActivity) getActivity()).setUnreadMessageCnt(totalUnreadMsgCnt);
        //todo 设置未读消息
        val recentSessionList = mImService?.sessionManager?.recentListInfo
        recentSessionList?.filterNot {
            when (it.peerId) {
                mSysRecentInfo.peerId -> {
                    mSysRecentInfo = it
                    true
                }
                mKefuRecentInfo.peerId -> {
                    mKefuRecentInfo = it
                    true
                }
                else -> false
            }

        }?.let {
            mRecentSessionList.clear()
            mRecentSessionList.addAll(it)
        }


        mAllList.clear()
        mAllList.add(mSysRecentInfo)
        mAllList.add(mKefuRecentInfo)
        mAllList.addAll(mRecentSessionList)
        mAdapter.notifyDataSetChanged()
        return true
    }

    /**
     * 无消息提示
     */
    private fun setNoChatView() {
        if (mAllList.isEmpty()) {
            llMyMessageTips.visibility = View.VISIBLE
        } else {
            llMyMessageTips.visibility = View.GONE
        }
    }


    //如果页面存在了 就销毁它
    private fun finishAty(activity: Class<*>) {
        when (activity) {
            MyMessageChatActivity::class.java -> {
                if (MyMessageChatActivity.instance != null) {
                    MyMessageChatActivity.instance!!.setResult(Activity.RESULT_OK)
                    MyMessageChatActivity.instance!!.finish()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let { mDetailVp.currentItem = it.getIntExtra(IConstants.POSITION, 0) }
        if (resultCode == Activity.RESULT_OK && requestCode == IConstants.TO_MESSAGE) {
            //必须是我的板块进的聊天列表，然后不是第一次进（聊天列表进聊天后就返回不finish,进了聊天后又进了另一个页面才finish）
            if (MYFRAGMENT_TO_MESSAGE && !mIsMyFragmentEnter && !MESSAGE_TO_CHAT) {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        //只有从我的板块进来返回时才为false
        if (MYFRAGMENT_TO_MESSAGE && mIsMyFragmentEnter) {
            MYFRAGMENT_TO_MESSAGE = false
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        imServiceConnector.disconnect(this)

        super.onDestroy()
    }
}