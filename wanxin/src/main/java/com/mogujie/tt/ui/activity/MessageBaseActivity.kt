package com.mogujie.tt.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.provider.MediaStore
import android.provider.Settings
import android.text.Selection
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.Toast
import com.app.common.base.AppBaseActivity
import com.leimo.wanxin.R
import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.config.IntentConstant
import com.mogujie.tt.config.SysConstant
import com.mogujie.tt.db.entity.MessageEntity
import com.mogujie.tt.db.entity.PeerEntity
import com.mogujie.tt.db.entity.UserEntity
import com.mogujie.tt.db.sp.SystemConfigSp
import com.mogujie.tt.imservice.entity.TextMessage
import com.mogujie.tt.imservice.service.IMService
import com.mogujie.tt.ui.adapter.MessageAdapter
import com.mogujie.tt.ui.adapter.album.AlbumHelper
import com.mogujie.tt.ui.adapter.album.ImageBucket
import com.mogujie.tt.ui.helper.AudioPlayerHandler
import com.mogujie.tt.ui.helper.Emoparser
import com.mogujie.tt.ui.widget.EmoGridView
import com.mogujie.tt.ui.widget.YayaEmoGridView
import com.mogujie.tt.utils.CommonUtil
import com.mogujie.tt.utils.Logger
import com.mogujie.tt.utils.path.UriUtil
import kotlinx.android.synthetic.main.tt_activity_message.*
import java.io.File

/**
 * Created by wr
 * Date: 2019/1/15  14:54
 * mail: 1902065822@qq.com
 * describe:
 */
open class MessageBaseActivity : AppBaseActivity(), View.OnClickListener, SensorEventListener {
    private val logger = Logger.getLogger(MessageBaseActivity::class.java)
    //
    protected var takePhotoSavePath: String? = ""
    protected var audioSavePath: String? = null
    //
    private val receiver = SwitchInputMethodReceiver()
    protected var currentInputMethod: String? = null
    protected var inputManager: InputMethodManager? = null
    internal var keyboardHeight = 0
    //
    protected var y1 = 0f
    protected var y2 = 0f

    //键盘布局相关参数
    internal var rootBottom = Integer.MIN_VALUE
    //
    protected lateinit var adapter: MessageAdapter

    protected var mImService: IMService? = null
    // 当前的session
    protected var currentSessionKey: String? = null
    protected var loginUser: UserEntity? = null
    protected var peerEntity: PeerEntity? = null

    var mToast: Toast? = null
    //
    //private boolean audioReday = false; 语音先关的
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null

    //
    private var albumHelper: AlbumHelper? = null
    protected var albumList: MutableList<ImageBucket>? = null

    override fun bindLayout(): Int = R.layout.tt_activity_message

    override fun initView() {
        super.initView()
        initSoftInputMethod()
        initAudioSensor()
        initAlbumHelper()
    }

    /**
     * @Description 初始化数据（相册,表情,数据库相关）
     */
    private fun initAlbumHelper() {
        albumHelper = AlbumHelper.getHelper(this@MessageBaseActivity)
        albumList = albumHelper!!.getImagesBucketList(false)
    }

    private fun initSoftInputMethod() {
        inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val filter = IntentFilter()
        filter.addAction("android.intent.action.INPUT_METHOD_CHANGED")
        registerReceiver(receiver, filter)
        SystemConfigSp.instance().init(this)
        currentInputMethod = Settings.Secure.getString(this@MessageBaseActivity.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        keyboardHeight = SystemConfigSp.instance().getIntConfig(currentInputMethod)
    }


    protected inner class SwitchInputMethodReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.intent.action.INPUT_METHOD_CHANGED") {
                currentInputMethod = Settings.Secure.getString(this@MessageBaseActivity.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
                SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.DEFAULTINPUTMETHOD, currentInputMethod)
                val height = SystemConfigSp.instance().getIntConfig(currentInputMethod)
                if (keyboardHeight != height) {
                    keyboardHeight = height
                    layoutAddOtherPanel.visibility = View.GONE
                    layoutEmo.visibility = View.GONE
                    this@MessageBaseActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

                    if (keyboardHeight != 0 && layoutAddOtherPanel.layoutParams.height != keyboardHeight) {
                        val params = layoutAddOtherPanel.layoutParams as RelativeLayout.LayoutParams
                        params.height = keyboardHeight
                    }
                    if (keyboardHeight != 0 && layoutEmo.layoutParams.height != keyboardHeight) {
                        val params = layoutEmo.layoutParams as RelativeLayout.LayoutParams
                        params.height = keyboardHeight
                    }
                } else {
                    layoutAddOtherPanel.visibility = View.VISIBLE
                    layoutEmo.visibility = View.VISIBLE
                    this@MessageBaseActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                }
                edtMessageTxt.requestFocus()
            }
        }
    }


    val emoOnCheckedChangeListener = RadioGroup.OnCheckedChangeListener { radioGroup, id ->
        if (id == R.id.tab2) {
            if (gridviewEmo.visibility != View.VISIBLE) {
                gridviewEmoYaya.visibility = View.GONE
                gridviewEmo.visibility = View.VISIBLE
            }


        } else if (id == R.id.tab1) {
            if (gridviewEmoYaya.visibility != View.VISIBLE) {
                gridviewEmo.visibility = View.GONE
                gridviewEmoYaya.visibility = View.VISIBLE
            }
        }
    }


    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.right_btn) {
            showGroupManageActivity()
        } else if (id == R.id.btnAddPhoto) {
            btnRecordVoice.visibility = View.GONE
            btnKeyboard.visibility = View.GONE
            edtMessageTxt.visibility = View.VISIBLE
            btnVoice.visibility = View.VISIBLE
            btnEmo.visibility = View.VISIBLE
            if (keyboardHeight != 0) {
                this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            }
            if (layoutAddOtherPanel.visibility == View.VISIBLE) {
                if (!edtMessageTxt.hasFocus()) {
                    edtMessageTxt.requestFocus()
                }
                inputManager?.toggleSoftInputFromWindow(edtMessageTxt.windowToken, 1, 0)
                if (keyboardHeight == 0) {
                    layoutAddOtherPanel.visibility = View.GONE
                }
            } else if (layoutAddOtherPanel.visibility == View.GONE) {
                layoutAddOtherPanel.visibility = View.VISIBLE
                inputManager?.hideSoftInputFromWindow(edtMessageTxt.windowToken, 0)
            }
            if (layoutEmo.visibility == View.VISIBLE) {
                layoutEmo.visibility = View.GONE
            }
            scrollToBottomListItem()

        } else if (id == R.id.take_photo_btn) { //选择相册
            if (albumList != null && albumList!!.size < 1) {
                Toast.makeText(this@MessageBaseActivity,
                        resources.getString(R.string.not_found_album), Toast.LENGTH_LONG)
                        .show()
                return
            }
            val intent = Intent(this@MessageBaseActivity, PickPhotoActivity::class.java)
            intent.putExtra(IntentConstant.KEY_SESSION_KEY, currentSessionKey)
            startActivityForResult(intent, SysConstant.ALBUM_BACK_DATA)
            this@MessageBaseActivity.overridePendingTransition(R.anim.tt_album_enter, R.anim.tt_stay)
            edtMessageTxt.clearFocus()//切记清除焦点scrollToBottomListItem();
        } else if (id == R.id.take_camera_btn) {    //选择拍照
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePhotoSavePath = CommonUtil.getImageSavePath(System
                    .currentTimeMillis().toString() + ".jpg")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, UriUtil.getUri(applicationContext, File(takePhotoSavePath!!)))
            startActivityForResult(intent, SysConstant.CAMERA_WITH_DATA)
            edtMessageTxt.clearFocus()//切记清除焦点scrollToBottomListItem();
        } else if (id == R.id.take_video_btn) { //选择视频
            val i = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(i, SysConstant.VIDEO_WITH_DATA)
            edtMessageTxt.clearFocus()//切记清除焦点scrollToBottomListItem();
        } else if (id == R.id.btnEmo) {
            btnRecordVoice.visibility = View.GONE
            btnKeyboard.visibility = View.GONE
            edtMessageTxt.visibility = View.VISIBLE
            btnVoice.visibility = View.VISIBLE
            btnEmo.visibility = View.VISIBLE
            if (keyboardHeight != 0) {
                this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            }
            if (layoutEmo.visibility == View.VISIBLE) {
                if (!edtMessageTxt.hasFocus()) {
                    edtMessageTxt.requestFocus()
                }
                inputManager?.toggleSoftInputFromWindow(edtMessageTxt.windowToken, 1, 0)
                if (keyboardHeight == 0) {
                    layoutEmo.visibility = View.GONE
                }
            } else if (layoutEmo.visibility == View.GONE) {
                layoutEmo.visibility = View.VISIBLE
                gridviewEmoYaya.visibility = View.VISIBLE
                rgEmo.check(R.id.tab1)
                gridviewEmo.visibility = View.GONE
                inputManager?.hideSoftInputFromWindow(edtMessageTxt.windowToken, 0)
            }
            if (layoutAddOtherPanel.visibility == View.VISIBLE) {
                layoutAddOtherPanel.visibility = View.GONE
            }

        } else if (id == R.id.btnSendMessage) {   //发送
            val content = edtMessageTxt.text.toString()
            logger.e("message_activity#chat content:%s", content)
            if (content.trim { it <= ' ' } == "") {
                Toast.makeText(this@MessageBaseActivity,
                        resources.getString(R.string.message_null), Toast.LENGTH_LONG).show()
                return
            }
            val textMessage = TextMessage.buildForSend(content, loginUser, currentSessionKey)
            //            TextMessage textMessage = TextMessage.buildForSend(content, loginUser, peerEntity);
            mImService!!.messageManager.sendText(textMessage)
            edtMessageTxt.setText("")
            pushList(textMessage)
            scrollToBottomListItem()
            logger.e("message_activity#chat loginUser:%s", loginUser!!.toString())
            //            logger.e("message_activity#chat peerEntity:%s", peerEntity.toString());

        } else if (id == R.id.btnVoice) {
            inputManager?.hideSoftInputFromWindow(edtMessageTxt.windowToken, 0)
            edtMessageTxt.visibility = View.GONE
            btnVoice.visibility = View.GONE
            btnRecordVoice.visibility = View.VISIBLE
            btnKeyboard.visibility = View.VISIBLE
            layoutEmo.visibility = View.GONE
            layoutAddOtherPanel.visibility = View.GONE
            edtMessageTxt.setText("")

        } else if (id == R.id.btnKeyboard) {
            btnRecordVoice.visibility = View.GONE
            btnKeyboard.visibility = View.GONE
            edtMessageTxt.visibility = View.VISIBLE
            btnVoice.visibility = View.VISIBLE
            btnEmo.visibility = View.VISIBLE

        } else if (id == R.id.message_text) {
        } else if (id == R.id.tvNewMsgTip) {
            scrollToBottomListItem()
            tvNewMsgTip.visibility = View.GONE

        }
    }

    val yayaOnEmoGridViewItemClick = YayaEmoGridView.OnEmoGridViewItemClick { facesPos, viewIndex ->
        val resId = Emoparser.getInstance(this@MessageBaseActivity).yayaResIdList[facesPos]
        logger.d("message_activity#yayaEmoGridView be clicked")

        val content = Emoparser.getInstance(this@MessageBaseActivity).yayaIdPhraseMap[resId]
        if (content == "") {
            Toast.makeText(this@MessageBaseActivity,
                    resources.getString(R.string.message_null), Toast.LENGTH_LONG).show()
            return@OnEmoGridViewItemClick
        }
        val textMessage = TextMessage.buildForSend(content, loginUser, currentSessionKey)
        //表情
        textMessage?.isGIfEmo = true
        mImService?.messageManager?.sendText(textMessage)
        pushList(textMessage)
        scrollToBottomListItem()
    }

    protected val onEmoGridViewItemClick = EmoGridView.OnEmoGridViewItemClick { facesPos, viewIndex ->
        var viewIndex = viewIndex
        var deleteId = ++viewIndex * (SysConstant.pageSize - 1)
        if (deleteId > Emoparser.getInstance(this@MessageBaseActivity)!!.resIdList.size) {
            deleteId = Emoparser.getInstance(this@MessageBaseActivity)!!.resIdList.size
        }
        if (deleteId == facesPos) {
            var msgContent = edtMessageTxt.text.toString()
            if (msgContent.isEmpty()) {
                return@OnEmoGridViewItemClick
            }
            if (msgContent.contains("[")) {
                msgContent = msgContent.substring(0, msgContent.lastIndexOf("["))
            }
            edtMessageTxt.setText(msgContent)
        } else {
            val resId = Emoparser.getInstance(this@MessageBaseActivity)!!.resIdList[facesPos]
            val pharse = Emoparser.getInstance(this@MessageBaseActivity)!!.idPhraseMap[resId]
            val startIndex = edtMessageTxt.selectionStart
            val edit = edtMessageTxt.editableText
            if (startIndex < 0 || startIndex >= edit.length) {
                if (null != pharse) {
                    edit.append(pharse)
                }
            } else {
                if (null != pharse) {
                    edit.insert(startIndex, pharse)
                }
            }
        }
        val edtable = edtMessageTxt.text
        val position = edtable.length
        Selection.setSelection(edtable, position)
    }

    val lvPTROnTouchListener = View.OnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            edtMessageTxt.clearFocus()
            if (layoutEmo.visibility == View.VISIBLE) {
                layoutEmo.visibility = View.GONE
            }

            if (layoutAddOtherPanel.visibility == View.VISIBLE) {
                layoutAddOtherPanel.visibility = View.GONE
            }
            inputManager?.hideSoftInputFromWindow(edtMessageTxt.windowToken, 0)
        }
        false
    }

    val msgEditOnFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
        if (hasFocus) {
            if (keyboardHeight == 0) {
                layoutAddOtherPanel.visibility = View.GONE
                layoutEmo.visibility = View.GONE
            } else {
                this@MessageBaseActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                if (layoutAddOtherPanel.visibility == View.GONE) {
                    layoutAddOtherPanel.visibility = View.VISIBLE
                }
            }
        }
    }

    val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val r = Rect()
        baseRoot.getGlobalVisibleRect(r)
        // 进入Activity时会布局，第一次调用onGlobalLayout，先记录开始软键盘没有弹出时底部的位置
        if (rootBottom == Integer.MIN_VALUE) {
            rootBottom = r.bottom
            return@OnGlobalLayoutListener
        }
        // adjustResize，软键盘弹出后高度会变小
        if (r.bottom < rootBottom) {
            //按照键盘高度设置表情框和按钮框的高度
            keyboardHeight = rootBottom - r.bottom
            SystemConfigSp.instance().init(this@MessageBaseActivity)
            SystemConfigSp.instance().setIntConfig(currentInputMethod, keyboardHeight)
            val params = layoutAddOtherPanel.layoutParams as RelativeLayout.LayoutParams
            params.height = keyboardHeight
            val params1 = layoutEmo.layoutParams as RelativeLayout.LayoutParams
            params1.height = keyboardHeight
        }
    }

    /**
     * @Description 初始化AudioManager，用于访问控制音量和钤声模式
     */
    private fun initAudioSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onAccuracyChanged(arg0: Sensor, arg1: Int) {}

    override fun onSensorChanged(arg0: SensorEvent) {
        try {
            if (!AudioPlayerHandler.getInstance().isPlaying) {
                return
            }
            val range = arg0.values[0]
            if (null != sensor && range == sensor!!.maximumRange) {
                // 屏幕恢复亮度
                AudioPlayerHandler.getInstance().setAudioMode(AudioManager.MODE_NORMAL, this)
            } else {
                // 屏幕变黑
                AudioPlayerHandler.getInstance().setAudioMode(AudioManager.MODE_IN_CALL, this)
            }
        } catch (e: Exception) {
            logger.error(e)
        }

    }


    fun showToast(resId: Int) {
        val text = resources.getString(resId)
        if (mToast == null) {
            mToast = Toast.makeText(this@MessageBaseActivity, text, Toast.LENGTH_SHORT)
        } else {
            mToast?.setText(text)
            mToast?.duration = Toast.LENGTH_SHORT
        }
        mToast?.setGravity(Gravity.CENTER, 0, 0)
        mToast?.show()
    }

    /**
     * @Description 滑动到列表底部
     */
    fun scrollToBottomListItem() {
        logger.d("message_activity#scrollToBottomListItem")
        // todo eric, why use the last one index + 2 can real scroll to the
        // bottom?
        pullToRefreshMessageList.refreshableView?.setSelection(adapter.count + 1)
        tvNewMsgTip.visibility = View.GONE
    }

    /**
     * @Description 显示联系人界面
     */
    private fun showGroupManageActivity() {
        val i = Intent(this, GroupManagermentActivity::class.java)
        i.putExtra(IntentConstant.KEY_SESSION_KEY, currentSessionKey)
        startActivity(i)
    }


    /**
     * @param msg
     */
    fun pushList(msg: MessageEntity?) {
        logger.d("chat#pushList msgInfo:%s", msg)
        //撤回的消息
        if (msg?.msgType == DBConstant.SHOW_REVOKE_TYPE) {
            adapter.updateRevokeMsg(msg)
        } else {
            adapter.addItem(msg)
        }
    }

    fun pushList(entityList: List<MessageEntity>?) {
        logger.d("chat#pushList list:%d", entityList?.size ?: 0)
        adapter.loadHistoryList(entityList)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        sensorManager?.unregisterListener(this, sensor)
    }
}