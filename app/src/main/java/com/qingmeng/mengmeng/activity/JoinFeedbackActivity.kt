package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.view.Gravity
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
import com.qingmeng.mengmeng.constant.IConstants.BRANDID
import com.qingmeng.mengmeng.entity.SelectBean
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.view.dialog.SelectDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_join_feedback.*
import kotlinx.android.synthetic.main.layout_head.*

/**
 * Created by mingyue
 * Date: 2019/1/14
 * mail: 153705849@qq.com
 * describe: 我的反馈
 */
@SuppressLint("CheckResult", "SetTextI18n")
class JoinFeedbackActivity : BaseActivity() {
    private val maxSelectNum = 3  //最大图片数
    private lateinit var mBottomDialog: SelectDialog
    private val selectList = ArrayList<LocalMedia>()
    private var callUrl = ArrayList<String>()
    private lateinit var adapter: GridImageAdapter
    private var pop: PopupWindow? = null
    lateinit var token: String
    //品牌ID
    private var brandId = 0
    private var type = 0
    lateinit var content: String
    override fun getLayoutId(): Int = R.layout.activity_join_feedback
    override fun initData() {
        brandId = intent.getIntExtra(BRANDID, 0)
        //设置标题
        setHeadName(R.string.join_feedback)
        //标题栏提交
        mMenu.text = getString(R.string.submit)
        initWidget()
        initBottomDialog()
    }

    private fun initBottomDialog() {
        //菜单内容
        val menuList = ArrayList<SelectBean>()
        menuList.add(SelectBean(getString(R.string.join_feedback_type1), 1))
        menuList.add(SelectBean(getString(R.string.join_feedback_type2), 2))
        menuList.add(SelectBean(getString(R.string.join_feedback_type3), 3))
        menuList.add(SelectBean(getString(R.string.join_feedback_type4), 4))
        mBottomDialog = SelectDialog(this, menuList, onItemClick = { id ->
            btn_join_feedback.text = menuList[id].name
            type = id
        })
    }

    //初始化Listener
    override fun initListener() {
        super.initListener()
        //点击页面其他地方取消EditText的焦点并且隐藏软键盘
        mjoinLinear.setOnTouchListener(View.OnTouchListener { _, _ ->
            this@JoinFeedbackActivity.currentFocus?.let {
                //点击取消EditText的焦点
                mjoinLinear.isFocusable = true
                mjoinLinear.isFocusableInTouchMode = true
                mjoinLinear.requestFocus()
                /** * 点击空白位置 隐藏软键盘  */
                /** * 点击空白位置 隐藏软键盘  */
                val mInputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                return@OnTouchListener mInputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            }
            false
        })
        //选择问题
        btn_join_feedback.setOnClickListener { mBottomDialog.show() }
        //提交
        mMenu.setOnClickListener { _ ->
            myDialog.showLoadingDialog()
            token = MainApplication.instance.TOKEN
            content = edt_join_feedback.text.toString()
            callUrl.clear()
            var failCount = 0
            var successCount = 0
            var url = ""
            if (selectList.isEmpty()) {
                setFeedback(token, brandId, type, content, url)
            } else {
                selectList.indices.forEach { i ->
                    val path = if (selectList[i].isCompressed) {
                        selectList[i].compressPath
                    } else {
                        selectList[i].path
                    }
                    ApiUtils.updateImg(this@JoinFeedbackActivity, path, callback = { newUrl, oldUrl ->
                        if (TextUtils.isEmpty(newUrl)) {
                            failCount++
                            (selectList.size - 1).downTo(0).forEach {
                                if (selectList[it].path == oldUrl || selectList[it].compressPath == oldUrl) {
                                    selectList.removeAt(it)
                                }
                            }
                        } else {
                            successCount++
                            callUrl.add(newUrl)
                        }
                        if (successCount == selectList.size) {
                            if (failCount != 0) {
                                myDialog.dismissLoadingDialog()
                                adapter.notifyDataSetChanged()
                                ToastUtil.showShort("图片上传完成，共成功${successCount}张，失败${failCount}张,请再次选择图片提交")
                            } else {
                                callUrl.forEach { url += "$it," }
                                setFeedback(token, brandId, type, content, url)
                            }
                        }
                    })
                }
            }
        }
    }

    /**
     * @param token  用户token
     * @param brandId    品牌id
     * @param type    反馈类型
     * @param content  反馈内容
     * @param urlList 反馈图片（多张逗号隔开）
     */
    private fun setFeedback(token: String, brandId: Int, type: Int, content: String, urlList: String) {
        ApiUtils.getApi().feedback(token, brandId, type, content, urlList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    myDialog.dismissLoadingDialog()
                    if (it.code == 12000) {
                        ToastUtil.showShort(R.string.join_feedback_success)
                        finish()
                    } else {
                        ToastUtil.showShort(it.msg)
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    private fun initWidget() {
        val manager = GridLayoutManager(this, 4)
        recy_join_feedback.layoutManager = manager
        adapter = GridImageAdapter(this, { showPop() }, {
            if (selectList.size > 0) {
                selectList[it].apply {
                    when (PictureMimeType.pictureToVideo(pictureType)) {
                        1 ->
                            // 预览图片 可自定长按保存路径
                            //PictureSelector.create(MainActivity.this).externalPicturePreview(position, "/custom_file", selectList);
                            PictureSelector.create(this@JoinFeedbackActivity).externalPicturePreview(position, selectList)
                        2 ->
                            // 预览视频
                            PictureSelector.create(this@JoinFeedbackActivity).externalPictureVideo(path)
                        3 ->
                            // 预览音频
                            PictureSelector.create(this@JoinFeedbackActivity).externalPictureAudio(path)
                    }
                }
            }
        })
        adapter.setList(selectList)
        adapter.setSelectMax(maxSelectNum)
        recy_join_feedback.adapter = adapter
    }

    private fun showPop() {
        val mInputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mInputMethodManager.hideSoftInputFromWindow(recy_join_feedback.windowToken, 0)
        val bottomView = View.inflate(this@JoinFeedbackActivity, R.layout.layout_feedback_bottom_dialog, null)
        val mAlbum = bottomView.findViewById<TextView>(R.id.tv_album)
        val mCamera = bottomView.findViewById<TextView>(R.id.tv_camera)
        val mCancel = bottomView.findViewById<TextView>(R.id.tv_cancel)

        pop = PopupWindow(bottomView, -1, -2).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isOutsideTouchable = true
            isFocusable = true
            val lp = window.attributes
            lp.alpha = 0.5f
            window.attributes = lp
            setOnDismissListener {
                val params = window.attributes
                params.alpha = 1f
                window.attributes = params
            }
            showAtLocation(window.decorView, Gravity.BOTTOM, 0, 0)
        }

        val clickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.tv_album ->
                    //相册
                    PictureSelector.create(this@JoinFeedbackActivity)
                            .openGallery(PictureMimeType.ofImage())
                            .maxSelectNum(maxSelectNum - selectList.size)
                            .minSelectNum(1)
                            .imageSpanCount(4)
                            .selectionMode(PictureConfig.MULTIPLE)
                            .forResult(PictureConfig.CHOOSE_REQUEST)
                R.id.tv_camera ->
                    //拍照
                    PictureSelector.create(this@JoinFeedbackActivity)
                            .openCamera(PictureMimeType.ofImage())
                            .forResult(PictureConfig.CHOOSE_REQUEST)
            }
            pop?.dismiss()
            pop = null
        }

        mAlbum.setOnClickListener(clickListener)
        mCamera.setOnClickListener(clickListener)
        mCancel.setOnClickListener(clickListener)
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