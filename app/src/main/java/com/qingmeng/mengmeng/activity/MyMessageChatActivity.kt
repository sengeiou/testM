package com.qingmeng.mengmeng.activity

import android.app.Dialog
import android.content.Intent
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
import android.widget.TextView
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.ChatAdapter
import com.qingmeng.mengmeng.adapter.ChatType
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
    private var mAudioManager = AudioManager.getInstance(IConstants.DIR_AUDIO_STR)    //语音工具类
    private lateinit var mSoundVolumeDialog: Dialog             //语音弹出框
    private lateinit var mSoundVolumeImg: ImageView
    private lateinit var mSoundVolumeLayout: LinearLayout
    private var y1 = 0                                          //手指坐标
    private var y2 = 0
    private var mExpressionOrFunction = 0                       //变量 0默认（都不受理） 1表情点击 2工具+点击
    private var mFragmentList = ArrayList<Fragment>()           //表情fragment
    private val mTabTitles = arrayOf("", "", "")                //tabLayout头部 先加3个试试
    private var mList = ArrayList<Int>()
    private var mLocalVideoPath = ""                            //语音本地路径

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
            onBackPressed()
        }

        //输入框上部分点击事件
        rvMyMessageChat.setOnTouchListener { _, _ ->
            hiddenViewAndInputKeyboard()
            false
        }

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
            //列表移到最后一个
            rvMyMessageChat.scrollToPosition(mList.lastIndex)
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
            //列表移到最后一个
            rvMyMessageChat.scrollToPosition(mList.lastIndex)
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
                //列表移到最后一个
                rvMyMessageChat.scrollToPosition(mList.lastIndex)
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
                //列表移到最后一个
                rvMyMessageChat.scrollToPosition(mList.lastIndex)
            }
        })

        //发送消息
        tvMyMessageChatSend.setOnClickListener {

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
        mAdapter = MultiItemTypeAdapter(this, mList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                when (mList[position]) {
                    ChatType.CHAT_TYPE_BRAND -> {   //品牌详情
                        //品牌详情
                        getView<LinearLayout>(R.id.llMyMessageChatRvBrand).setOnClickListener {
                            ToastUtil.showShort("品牌详情")
                        }
                        //发送品牌
                        getView<TextView>(R.id.tvMyMessageChatRvBrandSend).setOnClickListener {
                            ToastUtil.showShort("发送品牌")
                        }
                    }
                    ChatType.CHAT_TYPE_OTHER -> {   //别人消息
                        getView<ImageView>(R.id.ivMyMessageChatRvOtherHead).setOnClickListener {
                            ToastUtil.showShort("别人头像")
                        }
                    }
                    ChatType.CHAT_TYPE_MINE -> {    //自己消息
                        getView<LinearLayout>(R.id.llMyMessageChatRvMine).setOnClickListener {
                            hiddenViewAndInputKeyboard()
                        }
                        getView<LinearLayout>(R.id.llMyMessageChatRvMineAudio).setOnClickListener {
                            ToastUtil.showShort("自己语音")
                            //测试播放语音
                            MediaManager.play(this@MyMessageChatActivity, mLocalVideoPath, {
                                //语音开始播放了
                                ivMyMessageChatSwitchAudio.visibility = View.VISIBLE
                                ToastUtil.showShort(getString(R.string.play_audio_call))
                            }, {
                                //语音播放结束
                                MediaManager.release()
                                ivMyMessageChatSwitchAudio.visibility = View.GONE
                            })
                        }
                    }
                }
            }
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

    override fun onDestroy() {
        super.onDestroy()

        MediaManager.release()
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