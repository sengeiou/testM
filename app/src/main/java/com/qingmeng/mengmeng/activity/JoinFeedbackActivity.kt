package com.qingmeng.mengmeng.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import android.widget.TextView
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.GridImageAdapter
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.entity.SelectBean
import com.qingmeng.mengmeng.entity.SelectDialogBean
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.view.FullyGridLayoutManager
import com.qingmeng.mengmeng.view.dialog.SelectDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_join_feedback.*
import kotlinx.android.synthetic.main.activity_login_change_password.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 * Created by mingyue
 * Date: 2019/1/14
 * mail: 153705849@qq.com
 * describe: 我的反馈
 */
class JoinFeedbackActivity: BaseActivity() {
    private val maxSelectNum = 4  //最大图片数
    private lateinit var mBottomDialog: SelectDialog
    private val selectList = java.util.ArrayList<LocalMedia>()
    private val murlList = ArrayList<String>()
    private lateinit var adapter: GridImageAdapter
    private var pop: PopupWindow? = null
    lateinit var token:String
 //*****************
    //品牌ID
    var brandId =10
    var type =0
    lateinit var content:String
    override fun getLayoutId(): Int {

        return R.layout.activity_join_feedback
    }

    override fun initData() {
        //设置标题
        setHeadName(getString(R.string.join_feedback))
        //标题栏提交
        mMenu.setText(getString(R.string.submit))
        initWidget()
    }

    //初始化Listener
    override fun initListener() {
        super.initListener()
        //点击页面其他地方取消EditText的焦点并且隐藏软键盘
        mjoinLinear.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (null != this@JoinFeedbackActivity.getCurrentFocus()) {
                    //点击取消EditText的焦点
                    mjoinLinear.setFocusable(true);
                    mjoinLinear.setFocusableInTouchMode(true);
                    mjoinLinear.requestFocus();
                    /** * 点击空白位置 隐藏软键盘  */
                    val mInputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    return mInputMethodManager!!.hideSoftInputFromWindow(this@JoinFeedbackActivity.getCurrentFocus()!!.getWindowToken(), 0)
                }
                return false
            }
        })
        //选择问题
        btn_join_feedback.setOnClickListener {
            //菜单内容
            val menuList = ArrayList<SelectBean>()
            menuList.add(SelectBean(name =getString(R.string.join_feedback_type1),id = 1))
            menuList.add(SelectBean(name =getString(R.string.join_feedback_type2),id = 2))
            menuList.add(SelectBean(name =getString(R.string.join_feedback_type3),id = 3))
            menuList.add(SelectBean(name =getString(R.string.join_feedback_type4),id = 4))
            mBottomDialog = SelectDialog(this, menuList, onItemClick = { id ->
                 run  {
                    btn_join_feedback.setText("" + menuList.get(id).name)
                    type=id
                }

            })
            mBottomDialog.show()
        }
        //提交
        mMenu.setOnClickListener {
            token=IConstants.TEST_ACCESS_TOKEN
            content=edt_join_feedback.text.toString()

            setfeedback(token,brandId,type,content,murlList)
        }
    }
    //反馈
    /*ACCESS-TOKEN  用户token
    *brandId    品牌id
    *type  	反馈类型
    * content  反馈内容
    * urlList 反馈图片（多张逗号隔开）
     */
    //
    private fun setfeedback(token :String,brandId: Int, type: Int, content: String, urlList: ArrayList<String>) {
        ApiUtils.getApi().join_feedback(token, brandId, type, content, urlList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        ToastUtil.showShort(bean.msg)
                            Log.e("aaaa","aaaa")

                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                })
    }
    private fun initWidget() {
        val manager = FullyGridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false)
        recy_join_feedback.setLayoutManager(manager)
        adapter = GridImageAdapter(this, onAddPicClickListener)
        adapter.setList(selectList)
        adapter.setSelectMax(maxSelectNum)
        recy_join_feedback.setAdapter(adapter)
        adapter.setOnItemClickListener(object : GridImageAdapter.OnItemClickListener {
         override   fun onItemClick(position: Int, v: View) {
                if (selectList.size > 0) {
                    val media = selectList.get(position)
                    val pictureType = media.getPictureType()
                    val mediaType = PictureMimeType.pictureToVideo(pictureType)
                    when (mediaType) {
                        1 ->
                            // 预览图片 可自定长按保存路径
                            //PictureSelector.create(MainActivity.this).externalPicturePreview(position, "/custom_file", selectList);
                            PictureSelector.create(this@JoinFeedbackActivity).externalPicturePreview(position, selectList)
                        2 ->
                            // 预览视频
                            PictureSelector.create(this@JoinFeedbackActivity).externalPictureVideo(media.getPath())
                        3 ->
                            // 预览音频
                            PictureSelector.create(this@JoinFeedbackActivity).externalPictureAudio(media.getPath())
                    }
                }
            }
        })
    }

    private val onAddPicClickListener = object : GridImageAdapter.onAddPicClickListener{

        override  fun onAddPicClick() {

            //第一种方式，弹出选择和拍照的dialog
            showPop()

            //第二种方式，直接进入相册，但是 是有拍照得按钮的
            //参数很多，根据需要添加

            //            PictureSelector.create(MainActivity.this)
            //                    .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
            //                    .maxSelectNum(maxSelectNum)// 最大图片选择数量
            //                    .minSelectNum(1)// 最小选择数量
            //                    .imageSpanCount(4)// 每行显示个数
            //                    .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选PictureConfig.MULTIPLE : PictureConfig.SINGLE
            //                    .previewImage(true)// 是否可预览图片
            //                    .compressGrade(Luban.THIRD_GEAR)// luban压缩档次，默认3档 Luban.FIRST_GEAR、Luban.CUSTOM_GEAR
            //                    .isCamera(true)// 是否显示拍照按钮
            //                    .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
            //                    //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
            //                    .enableCrop(true)// 是否裁剪
            //                    .compress(true)// 是否压缩
            //                    .compressMode(LUBAN_COMPRESS_MODE)//系统自带 or 鲁班压缩 PictureConfig.SYSTEM_COMPRESS_MODE or LUBAN_COMPRESS_MODE
            //                    //.sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
            //                    .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
            //                    .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
            //                    //.selectionMedia(selectList)// 是否传入已选图片
            //                    //.previewEggs(false)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
            //                    //.cropCompressQuality(90)// 裁剪压缩质量 默认100
            //                    //.compressMaxKB()//压缩最大值kb compressGrade()为Luban.CUSTOM_GEAR有效
            //                    //.compressWH() // 压缩宽高比 compressGrade()为Luban.CUSTOM_GEAR有效
            //                    //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
            //                    .rotateEnabled(false) // 裁剪是否可旋转图片
            //                    //.scaleEnabled()// 裁剪是否可放大缩小图片
            //                    //.recordVideoSecond()//录制视频秒数 默认60s
            //                    .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
        }
    }

    private fun showPop() {
        val bottomView = View.inflate(this@JoinFeedbackActivity, R.layout.layout_feedback_bottom_dialog, null)
        val mAlbum = bottomView.findViewById<TextView>(R.id.tv_album)
        val mCamera = bottomView.findViewById<TextView>(R.id.tv_camera)
        val mCancel = bottomView.findViewById<TextView>(R.id.tv_cancel)

        pop = PopupWindow(bottomView, -1, -2)
        pop?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        pop?.setOutsideTouchable(true)
        pop?.setFocusable(true)
        val lp = window.attributes
        lp.alpha = 0.5f
        window.attributes = lp
        pop?.setOnDismissListener(PopupWindow.OnDismissListener {
            val lp = window.attributes
            lp.alpha = 1f
            window.attributes = lp
        })
   //     pop.setAnimationStyle(R.style.main_menu_photo_anim)
        pop?.showAtLocation(window.decorView, Gravity.BOTTOM, 0, 0)

        val clickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.tv_album ->
                    //相册
                    PictureSelector.create(this@JoinFeedbackActivity)
                            .openGallery(PictureMimeType.ofImage())
                            .maxSelectNum(maxSelectNum)
                            .minSelectNum(1)
                            .imageSpanCount(4)
                            .selectionMode(PictureConfig.MULTIPLE)
                            .forResult(PictureConfig.CHOOSE_REQUEST)
                R.id.tv_camera ->
                    //拍照
                    PictureSelector.create(this@JoinFeedbackActivity)
                            .openCamera(PictureMimeType.ofImage())
                            .forResult(PictureConfig.CHOOSE_REQUEST)
                R.id.tv_cancel -> {
                }
            }//取消
            //closePopupWindow();
            closePopupWindow()
        }

        mAlbum.setOnClickListener(clickListener)
        mCamera.setOnClickListener(clickListener)
        mCancel.setOnClickListener(clickListener)
    }

    fun closePopupWindow() {
        if (pop != null && pop!!.isShowing()) {
            pop?.dismiss()
            pop = null
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val images: List<LocalMedia>
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    // 图片选择结果回调

                    images = PictureSelector.obtainMultipleResult(data)
                    selectList.addAll(images)

                    //                    selectList = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                    adapter.setList(selectList)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
}