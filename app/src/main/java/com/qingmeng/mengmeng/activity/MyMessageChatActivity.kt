package com.qingmeng.mengmeng.activity

import android.app.Dialog
import android.support.v4.app.Fragment
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.ChatAdapter
import com.qingmeng.mengmeng.adapter.ChatType
import com.qingmeng.mengmeng.adapter.MultiItemTypeAdapter
import com.qingmeng.mengmeng.adapter.MyFragmentPagerAdapter
import com.qingmeng.mengmeng.fragment.MyMessageChatExpressionTabLayoutFragment
import com.qingmeng.mengmeng.utils.KeyboardUtil
import com.qingmeng.mengmeng.utils.imageLoader.CacheType
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import kotlinx.android.synthetic.main.activity_my_message_chat.*
import kotlinx.android.synthetic.main.layout_head.*
import kotlinx.android.synthetic.main.view_dialog_sound_volume.*
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
    private lateinit var mAdapter: MultiItemTypeAdapter<Int>
    private lateinit var mPageAdapter: PagerAdapter
    private lateinit var mSoundVolumeDialog: Dialog             //语音弹出框
    private lateinit var mSoundVolumeImg: ImageView
    private lateinit var mSoundVolumeLayout: LinearLayout
    private var y1 = 0                                          //手指坐标
    private var y2 = 0
    private var mExpressionOrFunction = 0                       //变量 0默认（都不受理） 1表情点击 2工具+点击
    private var mFragmentList = ArrayList<Fragment>()
    private val mTabTitles = arrayOf("", "", "")                //tabLayout头部 先加3个试试
    private var mList = ArrayList<Int>()

    override fun getLayoutId(): Int {
        return R.layout.activity_my_message_chat
    }

    override fun initObject() {
        super.initObject()

        setHeadName(intent.getStringExtra("title"))

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
    }


    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            this.finish()
        }

        //输入框上部分点击事件
        rvMyMessageChat.setOnTouchListener { _, _ ->
            //表情和工具布局隐藏
            llMyMessageChatFunction.visibility = View.GONE
            rlMyMessageChatExpression.visibility = View.GONE
            //关闭软键盘
            mKeyboardUtil.hideInputKeyboard()
            false
        }

        //音频
        ivMyMessageChatAudio.setOnClickListener {
            //关闭软键盘
            mKeyboardUtil.hideInputKeyboard()
            //表情和工具布局隐藏
            llMyMessageChatFunction.visibility = View.GONE
            rlMyMessageChatExpression.visibility = View.GONE
            //按住说话不显示就显示 反之隐藏
            tvMyMessageChatClickSay.visibility = if (tvMyMessageChatClickSay.visibility == View.GONE) {
                View.VISIBLE
            } else {
                View.GONE
            }
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
                mExpressionOrFunction = 1
                //关闭软键盘
                mKeyboardUtil.hideInputKeyboard()
            }
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
                mExpressionOrFunction = 2
                //关闭软键盘
                mKeyboardUtil.hideInputKeyboard()
            }
        }

        //输入框点击
        etMyMessageChatContent.setOnTouchListener { _, _ ->
            //表情和工具布局隐藏
            llMyMessageChatFunction.visibility = View.GONE
            rlMyMessageChatExpression.visibility = View.GONE
            //打开软键盘
            mKeyboardUtil.showInputKeyboard()
            false
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
                //恢复默认值 其他都不受理
                mExpressionOrFunction = 0
            }

            override fun onKeyboardHide(i: Int) {
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
            }
        })

        //发送消息
        tvMyMessageChatSend.setOnClickListener {

        }
    }

    private fun initAdapter() {
        //消息适配器
        mLayoutManager = LinearLayoutManager(this)
        rvMyMessageChat.layoutManager = mLayoutManager
        mAdapter = MultiItemTypeAdapter(this, mList, itemClick = { view, holder, position ->
            //表情和工具布局隐藏
            llMyMessageChatFunction.visibility = View.GONE
            rlMyMessageChatExpression.visibility = View.GONE
            //关闭软键盘
            mKeyboardUtil.hideInputKeyboard()
        })
        mAdapter.addItemViewDelegate(ChatAdapter.TimeLayout())
        mAdapter.addItemViewDelegate(ChatAdapter.BrandLayout())
        mAdapter.addItemViewDelegate(ChatAdapter.MineLayout())
        mAdapter.addItemViewDelegate(ChatAdapter.OtherLayout())
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
        setData()
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
        if (voiceValue < 200.0) {
            mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_01)
        } else if (voiceValue > 200.0 && voiceValue < 600) {
            mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_02)
        } else if (voiceValue > 600.0 && voiceValue < 1200) {
            mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_03)
        } else if (voiceValue > 1200.0 && voiceValue < 2400) {
            mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_04)
        } else if (voiceValue > 2400.0 && voiceValue < 10000) {
            mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_05)
        } else if (voiceValue > 10000.0 && voiceValue < 28000.0) {
            mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_06)
        } else if (voiceValue > 28000.0) {
            mSoundVolumeImg.setBackgroundResource(R.drawable.view_dialog_sound_volume_07)
        }
    }

    private fun setData() {
        mList.clear()
        mList.add(ChatType.CHAT_TYPE_TIME)
        mList.add(ChatType.CHAT_TYPE_BRAND)
        for (i in 0..10) {
            mList.add(ChatType.CHAT_TYPE_OTHER)
            mList.add(ChatType.CHAT_TYPE_MINE)
        }
        mAdapter.notifyDataSetChanged()
        //滚到最后一个
        rvMyMessageChat.scrollToPosition(mList.size - 1)
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
}