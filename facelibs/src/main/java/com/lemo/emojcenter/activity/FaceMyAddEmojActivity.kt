package com.lemo.emojcenter.activity

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.lemo.emojcenter.FaceConfigInfo
import com.lemo.emojcenter.FaceInitData
import com.lemo.emojcenter.R
import com.lemo.emojcenter.adapter.FaceMyAddEmojAdapter
import com.lemo.emojcenter.api.FaceNetRequestApi
import com.lemo.emojcenter.base.FaceBaseActivity
import com.lemo.emojcenter.bean.MyAddEmojBean
import com.lemo.emojcenter.bean.OssDataBean
import com.lemo.emojcenter.bean.SortBean
import com.lemo.emojcenter.bean.UpLoadBean
import com.lemo.emojcenter.constant.FaceConstants
import com.lemo.emojcenter.constant.FaceIConstants
import com.lemo.emojcenter.constant.FaceLocalConstant
import com.lemo.emojcenter.constant.FaceNetConstant
import com.lemo.emojcenter.manage.FaceDownloadFaceManage
import com.lemo.emojcenter.manage.FaceStatuManage
import com.lemo.emojcenter.utils.*
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.callback.StringCallback
import kotlinx.android.synthetic.main.face_activity_my_add_emoj.*
import okhttp3.Call
import okhttp3.MediaType
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * Description :添加表情
 * Author:fangxu
 * Email:634804858@qq.com
 * Date: 2018/1/25
 */

class FaceMyAddEmojActivity : FaceBaseActivity() {
    private lateinit var mMyAddEmojAdapter: FaceMyAddEmojAdapter
    private var mDatas: ArrayList<MyAddEmojBean> = ArrayList()
    private var mIsEdit: Boolean = false
    private var mIdDatas: ArrayList<SortBean> = ArrayList()
    private var statuEdit = FaceLocalConstant.CollectEditStatu.INIT

    override fun getLayoutId(): Int {
        return R.layout.face_activity_my_add_emoj
    }

    override fun initData() {
        super.initData()
        //获取我添加的表情数据
        showLoadingDialog()
        getData()
    }

    override fun initView() {
        super.initView()
        setUploadPicture()
        initAdapter(mDatas)
    }

    override fun initListener() {
        super.initListener()
        //toobar右上角操作按钮:整理
        top_add_emoj.setSettingCallBack {
            mIsEdit = !mIsEdit
            top_add_emoj.setRightText(if (mIsEdit) "完成" else "整理")
            rl_my_add_emoj_bottom.visibility = if (mIsEdit) View.VISIBLE else View.GONE
            mMyAddEmojAdapter.setStatusCode(mIsEdit)
        }
        //条目点击事件

        mMyAddEmojAdapter.setImageClickListener(object : FaceMyAddEmojAdapter.ImageClickListener {
            override fun click(v: View, position: Int) {
                //初始化pictureselector
                initPictureSelector()
            }
        })

        mMyAddEmojAdapter.setCheckBoxSelectListener(object : FaceMyAddEmojAdapter.CheckBoxSelectListener {
            override fun selecet(positon: Int, isCheck: Boolean) {
                setCheckMyAddEmoj(positon, isCheck)
            }
        })

        //底部添加到前面点击监听
        tv_my_add_emoj_addtofront.setOnClickListener(View.OnClickListener {
            if (mIdDatas.size == 0) {
                Toast.makeText(applicationContext, "请选择表情", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            //调用排序接口
            sortEmoj()
        })
        //底部删除按钮
        tv_my_add_emoj_delete.setOnClickListener(View.OnClickListener {
            if (mIdDatas.size == 0) {
                Toast.makeText(applicationContext, "请选择表情", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            //调用删除接口
            deleteEmoj(mIdDatas)
        })
    }

    private fun setCheckMyAddEmoj(positon: Int, isCheck: Boolean) {
        if (mIsEdit && statuEdit == FaceLocalConstant.CollectEditStatu.INIT) {
            val id = mDatas[positon].id
            if (isCheck) {//选中状态
                mIdDatas.add(SortBean().apply { this.id = id })
                Log.d(TAG, "select: add id=$id  #pos=$positon")
            } else {//未选中状态
                val iterator = mIdDatas.iterator()
                while (iterator.hasNext()) {
                    val sortBean = iterator.next()
                    if (sortBean.id == id) {
                        iterator.remove()
                        Log.d(TAG, "selecet: remove id=$id  #pos=$positon")
                    }
                }
            }
            setEditSum()
        }
    }

    fun setUploadPicture() {
        rclv_my_add_emoj.visibility = View.VISIBLE
    }

    private fun initAdapter(datas: List<MyAddEmojBean>) {
        val gridLayoutManager = GridLayoutManager(this, 4)
        gridLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rclv_my_add_emoj.layoutManager = gridLayoutManager

        mMyAddEmojAdapter = FaceMyAddEmojAdapter(R.layout.face_item_add_emoj, datas)
        rclv_my_add_emoj.adapter = mMyAddEmojAdapter
    }


    /**
     * 上传图片请求获取oss权限
     */
    private fun getOssPermission(path: String) {
        showLoadingDialog()
        val clientUpload = ClientUpload(this@FaceMyAddEmojActivity)
        OkHttpUtils.get()
                .tag(this)
                .url(FaceIConstants.GET_OSS_PERMISSION)
                .addHeader(FaceIConstants.APP_KEY, FaceConfigInfo.appKeyValue)
                .build()
                .execute(object : GsonReturnCallBack<OssDataBean>() {
                    override fun onError(call: Call, e: Exception, id: Int) {
                        super.onError(call, e, id)
                        dismissLoadingDialog()
                        SimpleToast.showNetError("请求oss权限失败")
                    }

                    override fun onResponse(response: OssDataBean?, id: Int) {
                        super.onResponse(response, id)
                        //上传图片,oss回调获取url
                        if (response?.oss == null) {
                            dismissLoadingDialog()
                            return
                        }

                        Thread(Runnable {
                            clientUpload.updateFile(response.accessUrl, response.endpoint,
                                    response.buckName, response.folder,
                                    path, response.oss?.accessKeyId,
                                    response.oss?.accessKeySecret,
                                    response.oss?.securityToken,
                                    response.oss?.expiration, 1)
                        }).start()
                        clientUpload.setClientCallback(object : ClientUpload.ClientCallback {
                            override fun onSuccess(url: String) {
                                //返回的url实例http://pic.xiaozhuapp.com//diaryImg/2018-01-30/201801311501401.jpg
                                //调用请求上传图片接口
                                //开始上传图片,请求接口
                                upLoadData(path, url)
                            }

                            override fun onError(msg: String) {
                                dismissLoadingDialog()
                                SimpleToast.show(this@FaceMyAddEmojActivity, "图片上传失败")
                            }
                        })

                    }
                })
    }

    //上传图片
    fun upLoadData(path: String, url: String) {
        if (TextUtils.isEmpty(url)) {
            dismissLoadingDialog()
            return
        }
        val imageSize = ImageUtils.getImageSize(path)

        OkHttpUtils.post()
                .tag(this)
                .url(FaceIConstants.COLLECT_EMOJ_UP)
                .addHeader(FaceIConstants.APP_KEY, FaceConfigInfo.appKeyValue)
                .addParams("userId", FaceInitData.userId)
                .addParams("master", url)
                .addParams("cover", url)
                .addParams("imageWidth", imageSize.width.toString())
                .addParams("imageHeight", imageSize.height.toString())
                .build()
                .execute(object : GsonReturnCallBack<UpLoadBean>() {
                    override fun onResponse(response: UpLoadBean, id: Int) {
                        super.onResponse(response, id)
                        SimpleToast.show(this@FaceMyAddEmojActivity, "上传成功")
                        val myAddEmojBean = MyAddEmojBean()
                        myAddEmojBean.userId = response.userId
                        myAddEmojBean.id = response.id
                        myAddEmojBean.sortBy = response.sortBy
                        myAddEmojBean.cover = url
                        myAddEmojBean.master = url
                        myAddEmojBean.appId = response.appId
                        mDatas[mDatas.size - 1] = myAddEmojBean
                        FaceDownloadFaceManage.downCollectImage(url)
                        mDatas.add(MyAddEmojBean())
                        top_add_emoj.setTopNum("(" + (mDatas.size - 1) + ")")
                        mMyAddEmojAdapter.notifyDataSetChanged()
                        FaceStatuManage.collectAddSuc(myAddEmojBean)
                    }

                    override fun onError(call: Call, e: Exception, id: Int) {
                        super.onError(call, e, id)
                        SimpleToast.show(this@FaceMyAddEmojActivity, "图片上传失败")
                    }

                    override fun onAfter(id: Int) {
                        super.onAfter(id)
                        dismissLoadingDialog()
                    }
                })

    }


    //删除图片
    fun deleteEmoj(idDatas: List<SortBean>) {
        val ids = ArrayList<Int>()
        for (i in idDatas.indices) {
            ids.add(idDatas[i].id)
        }
        val map = HashMap<String, Any>(2)
        map["userId"] = FaceInitData.userId
        map["ids"] = ids

        val s = Gson().toJson(map)
        MyLogUtils.e(TAG, s)

        OkHttpUtils.postString()
                .tag(this)
                .url(FaceIConstants.COLLLECT_EMOJ_DELETE)
                .addHeader(FaceIConstants.APP_KEY, FaceConfigInfo.appKeyValue)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .content(s)
                .build()
                .execute(object : StringCallback() {
                    override fun onError(call: Call, e: Exception, id: Int) {
                        SimpleToast.showNetError(e)
                    }

                    override fun onResponse(response: String, id: Int) {
                        val jsonObject = JSONObject(response)
                        val code = jsonObject.optString("code")
                        if (TextUtils.equals(code, FaceConstants.CODE_SUCCESS)) {
                            deleteCollect(mIdDatas)
                            mIdDatas.clear()
                            mMyAddEmojAdapter.notifyDataSetChanged()
                            setEditSum()
                            top_add_emoj.setTopNum("(" + (mDatas.size - 1) + ")")
                            FaceStatuManage.collectDeleteSuc(ids)
                        } else {
                            SimpleToast.show(this@FaceMyAddEmojActivity, "删除失败")
                        }
                    }
                })
    }

    //排序
    fun sortEmoj() {
        val ids = ArrayList<Int>()
        Log.e(TAG, "sortEmoj: mIdDatas" + mIdDatas.size)
        for (i in mIdDatas.indices) {
            mIdDatas[i].sortBy = i + 1
            mIdDatas[i].userId = FaceInitData.userId
            Log.e(TAG, "sortEmoj: id" + mIdDatas[i].id)
        }
        //        ids.add(mIdDatas.get(i).getSortBy())
        MyLogUtils.e(TAG, Gson().toJson(mIdDatas).toString())
        val s = Gson().toJson(mIdDatas).toString()//传给服务器的参数
        OkHttpUtils.postString()
                .tag(this)
                .url(FaceIConstants.COLLLECT_EMOJ_SORT)
                .addHeader(FaceIConstants.APP_KEY, FaceConfigInfo.appKeyValue)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .content(s)
                .build()
                .execute(object : StringCallback() {
                    override fun onError(call: Call, e: Exception, id: Int) {
                        SimpleToast.showNetError(e)
                    }

                    override fun onResponse(response: String, id: Int) {
                        val jsonObject = JSONObject(response)
                        val code = jsonObject.optString("code")
                        if (TextUtils.equals(code, FaceConstants.CODE_SUCCESS)) {
                            getData()
                        } else {
                            SimpleToast.show(this@FaceMyAddEmojActivity, "排序失败")
                        }
                    }
                })
    }

    fun initPictureSelector() {
        PictureSelector.create(this@FaceMyAddEmojActivity)
                .openGallery(PictureMimeType.ofImage())//类型,图片
                .theme(R.style.PictureDefault_Single)
                .selectionMode(PictureConfig.SINGLE)//单张上传
                //        .enableCrop(true)//是否裁剪
                //            .freeStyleCropEnabled(true)//裁剪框是否可以拖拽
//                .withAspectRatio(1, 1)
                .previewImage(false)
                .showCropGrid(false)//是否显示矩形裁剪风格
                .compress(true)//是否压缩
                .minimumCompressSize(300)// 小于 100kb 的图片不压缩
                .forResult(PictureConfig.CHOOSE_REQUEST)//结果回调,resultCode
    }


    fun getData() {
        FaceNetRequestApi.getMyCollect(FaceInitData.userId, object : GsonReturnCallBack<List<MyAddEmojBean>>() {
            override fun onError(call: Call, e: Exception, id: Int) {
                super.onError(call, e, id)
                SimpleToast.showNetError(e)
                dismissLoadingDialog()
            }

            override fun onResponse(response: List<MyAddEmojBean>?, id: Int) {
                super.onResponse(response, id)
                dismissLoadingDialog()
                statuEdit = FaceLocalConstant.CollectEditStatu.DOING
                if (mIdDatas.size != 0) {
                    mIdDatas.clear()
                }
                mMyAddEmojAdapter.setCheckBoxState(false)
                mDatas.clear()
                if (response != null && response.size > 0) {
                    mDatas.addAll(response)
                    FaceStatuManage.collectSort(mDatas)
                    removeDefault()
                }
                mDatas.add(MyAddEmojBean())
                top_add_emoj.setTopNum("(" + (mDatas.size - 1) + ")")
                mMyAddEmojAdapter.notifyDataSetChanged()
                statuEdit = FaceLocalConstant.CollectEditStatu.INIT
            }
        })
    }

    //默认收藏不显示
    private fun removeDefault() {
        val iterator = mDatas.iterator()
        while (iterator.hasNext()) {
            val addEmojBean = iterator.next()
            if (addEmojBean.type == FaceNetConstant.CollectType.DEFAULT) {
                iterator.remove()
            }
        }
    }

    //删除添加的表情
    private fun deleteCollect(editCollect: ArrayList<SortBean>?) {
        val iterator = mDatas.iterator()
        while (iterator.hasNext()) {
            val addEmojBean = iterator.next()
            for (sortBean in editCollect!!) {
                if (addEmojBean.id == sortBean.id) {
                    iterator.remove()
                }
            }
        }
    }

    private fun setEditSum() {
        val sum = mIdDatas.size
        if (sum > 0) {
            tv_my_add_emoj_delete.setTextColor(resources.getColor(R.color.my_addemoj_select))
            tv_my_add_emoj_delete.text = "删除($sum)"
        } else {
            tv_my_add_emoj_delete.setTextColor(resources.getColor(R.color.my_addemoj_red))
            tv_my_add_emoj_delete.text = "删除"
        }
        Log.d(TAG, "selected: " + sum)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)//图片选择结果回调
        if (requestCode == PictureConfig.CHOOSE_REQUEST && resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    // 图片选择结果回调
                    val selectList = PictureSelector.obtainMultipleResult(data)
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                    if (selectList.size > 0) {
                        var path = selectList[0].path
                        //gif图片不压缩
                        if (!ImageUtils.isImageGif(path)) {
                            if (selectList[0].isCut) {
                                val pathCut = selectList[0].cutPath
                                if (!TextUtils.isEmpty(pathCut) && File(pathCut).exists()) {
                                    path = pathCut
                                }
                            }
                            if (selectList[0].isCompressed) {
                                val pathCompress = selectList[0].compressPath
                                if (!TextUtils.isEmpty(pathCompress) && File(pathCompress).exists()) {
                                    path = pathCompress
                                }
                            }
                        }
                        //请求上传到oss权限;上传到后台;刷新适配器
                        //请求获取oss权限
                        getOssPermission(path)
                    }
                }
                else -> {
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.getInstance().cancelTag(this)
    }

    companion object {
        private val TAG = "MyAddEmojActivity"
    }
}
