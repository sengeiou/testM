package com.lemo.emojcenter.activity

import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.lemo.emojcenter.FaceConfigInfo
import com.lemo.emojcenter.FaceInitData
import com.lemo.emojcenter.R
import com.lemo.emojcenter.adapter.FaceEmojDetailAdapter
import com.lemo.emojcenter.adapter.layoutrecycle.FaceFullyGridLayoutManager
import com.lemo.emojcenter.base.FaceBaseActivity
import com.lemo.emojcenter.bean.DownloadStatus
import com.lemo.emojcenter.bean.EmojDetailBean
import com.lemo.emojcenter.bean.EmojOpeBean
import com.lemo.emojcenter.constant.*
import com.lemo.emojcenter.manage.FaceDownloadFaceManage
import com.lemo.emojcenter.utils.GsonReturnCallBack
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.utils.ResponseExcepiton
import kotlinx.android.synthetic.main.face_activity_emoj_newdetaill.*
import okhttp3.Call
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * Description :表情包详情页
 * Email:634804858@qq.com
 * Date: 2018/1/24
 */
class FaceEmojDetailActivity : FaceBaseActivity() {
    private lateinit var mDatas: ArrayList<EmojDetailBean.DetailsBean>
    private var mBannerEmojId: Int = 0
    private var typeFrom = FaceLocalConstant.Value.EMOJDETAIL_TYPE_FACE
    private lateinit var mEmojDetailAdapter: FaceEmojDetailAdapter
    private var mResource: String? = null
    private var headData: EmojDetailBean? = null

    override fun getLayoutId(): Int {
        return R.layout.face_activity_emoj_newdetaill
    }


    override fun initView() {
        super.initView()
        EventBus.getDefault().register(this)
        mDatas = ArrayList()
        //禁止recycleview滑动
        rclv_emoj_detail.isNestedScrollingEnabled = false

        //获取从表情商城banner图页面传递过来的数据
        val bundle = intent.extras
        mBannerEmojId = bundle!!.getInt(FaceLocalConstant.Key.EMOJDETAIL_ID, 0)
        if (bundle.containsKey(FaceLocalConstant.Key.EMOJDETAIL_TYPE)) {
            typeFrom = bundle.getInt(FaceLocalConstant.Key.EMOJDETAIL_TYPE, FaceLocalConstant.Value.EMOJDETAIL_TYPE_FACE)
        }
        top_view.setRightOption()//让右边操作按钮消失
        initAdapter(mDatas)
        if (typeFrom == FaceLocalConstant.Value.EMOJDETAIL_TYPE_CHAT) {
            top_view.setTextBack("返回")
        }
    }

    override fun initData() {
        super.initData()
        view_detail_content!!.visibility = View.GONE
        no_data_layout.visibility = View.GONE
        showLoadingDialog()
        getDataByNet()
    }

    private fun initAdapter(datas: List<EmojDetailBean.DetailsBean>) {
        val gridLayoutManager = FaceFullyGridLayoutManager(this, 4)
        gridLayoutManager.orientation = LinearLayoutManager.VERTICAL
        gridLayoutManager.setScrollEnabled(false)
        rclv_emoj_detail.layoutManager = gridLayoutManager

        mEmojDetailAdapter = FaceEmojDetailAdapter(R.layout.face_item_emoj_detail, datas)
        rclv_emoj_detail.adapter = mEmojDetailAdapter
    }

    override fun initListener() {
        super.initListener()
        //底部下载按钮点击事件
        tv_emoj_detail_download.setOnClickListener { FaceDownloadFaceManage.fileDownGetUrl(mResource, mBannerEmojId.toString()) }
        swipeRefresh_detail.setOnRefreshListener { getDataByNet() }
    }

    private fun updateDownloadStatus() {
        when (headData?.downloadStatus) {
            DownloadStatus.download_init -> {
                tv_emoj_detail_download.visibility = View.VISIBLE
                pb_emoj_detail.visibility = View.GONE
                tv_emoj_detail_download_complete.visibility = View.GONE
            }
            DownloadStatus.download_error -> {
                tv_emoj_detail_download.text = "重新下载"
                tv_emoj_detail_download.setTextColor(ContextCompat.getColor(applicationContext, R.color.down_error))
                tv_emoj_detail_download.setBackgroundResource(R.drawable.face_emoj_down_error_bg_shape)
                tv_emoj_detail_download.visibility = View.VISIBLE
                pb_emoj_detail.visibility = View.GONE
                tv_emoj_detail_download_complete.visibility = View.GONE
            }
            DownloadStatus.download_finish -> {
                tv_emoj_detail_download.visibility = View.GONE
                pb_emoj_detail.visibility = View.GONE
                tv_emoj_detail_download_complete.visibility = View.VISIBLE
            }
            else -> {
                tv_emoj_detail_download.visibility = View.GONE
                pb_emoj_detail.visibility = View.VISIBLE
                tv_emoj_detail_download_complete.visibility = View.GONE
            }
        }
    }


    private fun getDataByNet() {
        OkHttpUtils.post()
                .tag(this)
                .url(FaceIConstants.EMOJ_DETAIL)
                .addHeader(FaceIConstants.APP_KEY, FaceConfigInfo.appKeyValue)
                .addParams("userId", FaceInitData.userId)
                .addParams("faceId", mBannerEmojId.toString() + "")
                .build()
                .execute(object : GsonReturnCallBack<EmojDetailBean>() {
                    override fun onError(call: Call, e: Exception, id: Int) {
                        super.onError(call, e, id)
                        dismissLoadingDialog()
                        tv_tip.setText(R.string.face_no_data_text_store)
                        if (e is ResponseExcepiton) {
                            val code = e.code
                            if (code == FaceConstants.CODE_ALREADYDOWN) {
                                tv_tip.setText(R.string.face_no_data_text_store_alreadyDown)
                            } else if (code == FaceConstants.CODE_FALI) {
                                tv_tip.setText(R.string.face_no_data_text_store)
                            } else {
                                //                        tvTip.setText(responseExcepiton.getMsg());
                            }
                        }
                        no_data_layout.visibility = View.VISIBLE
                        view_detail_content.visibility = View.GONE
                        swipeRefresh_detail.isRefreshing = false
                    }

                    override fun onResponse(response: EmojDetailBean?, id: Int) {
                        super.onResponse(response, id)
                        dismissLoadingDialog()
                        swipeRefresh_detail.isRefreshing = false

                        mDatas.clear()
                        if (response != null) {
                            //不为空才显示
                            if (response.banner != null || response.details != null || response.name != null || response.id > 0) {
                                view_detail_content.visibility = View.VISIBLE
                                no_data_layout.visibility = View.GONE
                            } else {
                                view_detail_content.visibility = View.GONE
                                no_data_layout.visibility = View.VISIBLE
                            }
                            headData = response
                            mResource = response.resource

                            val myOptions = RequestOptions()
                            myOptions.placeholder(R.mipmap.face_banner_error_dudu)
                            myOptions.error(R.mipmap.face_banner_error_dudu)
                            //更新头部UI
                            Glide.with(applicationContext)
                                    .load(response.banner)
                                    .apply(myOptions)
                                    .into(iv_emoj_detail_!!)
                            tv_emoj_detail_name.text = response.name
                            tv_emoj_detail_content.text = response.intro
                            tv_emoj_detail_is_animation.visibility = if (response.emotionType == 1) View.VISIBLE else View.GONE
                            top_view.setTopTitle(response.name)
                            //是否删除
                            if (response.isDelete == FaceNetConstant.IsDown.DOWN) {
                                tv_emoj_detail_download.visibility = View.GONE
                                tv_emoj_detail_download_complete.visibility = View.VISIBLE
                                response.downloadStatus = DownloadStatus.download_finish
                            }
                            //获取条目数据
                            response.details?.let {
                                mDatas.addAll(it)
                            }
                            mEmojDetailAdapter.notifyDataSetChanged()

                        } else {
                            view_detail_content.visibility = View.GONE
                            no_data_layout.visibility = View.VISIBLE
                        }
                    }
                })
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onEvent(emojOpe: EmojOpeBean) {
        if (emojOpe.emojOpeType?.isEmojChange == true) {
            if (emojOpe.faceId == mBannerEmojId.toString()) {
                if (headData != null) {
                    when (emojOpe.emojOpeType) {
                        FaceEmojOpeType.EmojOver -> headData?.downloadStatus = DownloadStatus.download_finish
                        FaceEmojOpeType.EmojDelete -> headData?.downloadStatus = DownloadStatus.download_init
                        FaceEmojOpeType.EmojDowning -> {
                            headData?.downloadStatus = emojOpe.downloadStatus
                            pb_emoj_detail.progress = emojOpe.downProgress
                        }
                        else -> {
                        }
                    }
                    updateDownloadStatus()
                }
            }
        }

    }

    companion object {
        private val TAG = "EmojDetailActivity"
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        OkHttpUtils.getInstance().cancelTag(this)
    }
}
