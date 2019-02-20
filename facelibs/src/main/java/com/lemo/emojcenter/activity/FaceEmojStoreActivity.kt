package com.lemo.emojcenter.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import com.bigkoo.convenientbanner.ConvenientBanner
import com.chad.library.adapter.base.BaseQuickAdapter
import com.lemo.emojcenter.FaceConfigInfo
import com.lemo.emojcenter.FaceInitData
import com.lemo.emojcenter.R
import com.lemo.emojcenter.adapter.FaceEmojStoreAdapter
import com.lemo.emojcenter.base.FaceBaseActivity
import com.lemo.emojcenter.bean.DownloadStatus
import com.lemo.emojcenter.bean.EmojOpeBean
import com.lemo.emojcenter.bean.EmojStoreBean
import com.lemo.emojcenter.bean.StoreBannerBean
import com.lemo.emojcenter.constant.FaceEmojOpeType
import com.lemo.emojcenter.constant.FaceIConstants
import com.lemo.emojcenter.constant.FaceLocalConstant
import com.lemo.emojcenter.constant.FaceRefreshStatuEnum
import com.lemo.emojcenter.manage.FaceDownloadFaceManage
import com.lemo.emojcenter.utils.GsonReturnCallBack
import com.lemo.emojcenter.utils.MyLogUtils
import com.lemo.emojcenter.utils.NetworkUtils
import com.lemo.emojcenter.utils.SimpleToast
import com.lemo.emojcenter.view.NetworkImageHolderView
import com.zhy.http.okhttp.OkHttpUtils
import kotlinx.android.synthetic.main.face_activity_emojstore.*
import okhttp3.Call
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * Description :表情商城
 * Author:fangxu
 * Email:634804858@qq.com
 * Date: 2018/1/23
 */

class FaceEmojStoreActivity : FaceBaseActivity() {
    //关于线程池的一些配置
    //模拟第一次加载效果
    private val WHAT_FIRSTLOAD = 0
    //下载刷新条目
    private val DOWNLOAD_FRESH = 100
    lateinit var mConvenientBanner: ConvenientBanner<String>
    private lateinit var mEmojStoreAdapter: FaceEmojStoreAdapter
    private var banner_arr: Array<String?>? = null

    private lateinit var mDatas: ArrayList<EmojStoreBean>
    private lateinit var mBannerList: ArrayList<StoreBannerBean>
    private var mPage = -1
    private var mRefreshStatu = FaceRefreshStatuEnum.DEFAULT
    private var mRefreshStatuBanner = FaceRefreshStatuEnum.DEFAULT

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                WHAT_FIRSTLOAD -> {
                    getBanner()
                    //网络加载例子
                    setBannerOption()
                }
                LOADD_BANNER -> setBannerOption()
                DOWNLOAD_FRESH -> {
                }
                else -> {
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.face_activity_emojstore
    }

    public override fun onResume() {
        super.onResume()
        //开始自动翻页
        mConvenientBanner.startTurning(3000)
    }

    override fun initView() {
        super.initView()
        //注册eventbus
        EventBus.getDefault().register(this)
        mDatas = ArrayList()
        mBannerList = ArrayList()
        if (top_view != null) {
            top_view.setBackStytletoText()
        }
        initAdapter(mDatas)
        initHead()
        val animator = rclv_emojstore.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    override fun initData() {
        super.initData()
        loadFirst()
    }

    override fun initListener() {
        //topview右上角设置按钮
        top_view.setSettingCallBack { startActivity(Intent(this@FaceEmojStoreActivity, FaceMyEmojActivity::class.java)) }

        //点击下载按钮
        mEmojStoreAdapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.item_tv_emojstore_download) {
                mDatas[position].downloadStatus = DownloadStatus.download_start
                val mFileResourse = mDatas[position].resource
                FaceDownloadFaceManage.fileDownGetUrl(mFileResourse, mDatas[position].id.toString())

            } else if (view.id == R.id.item_ll_emoj_store) {
                val faceId = mDatas[position].id
                val intent = Intent()
                intent.putExtra(FaceLocalConstant.Key.EMOJDETAIL_ID, faceId)
                intent.putExtra(FaceLocalConstant.Key.EMOJDETAIL_TYPE, FaceLocalConstant.Value.EMOJDETAIL_TYPE_FACE)
                intent.setClass(this@FaceEmojStoreActivity, FaceEmojDetailActivity::class.java)
                startActivity(intent)
            }
        }

        //点击下拉刷新
        srl_emoj_store.setOnRefreshListener {
            if (NetworkUtils.isNetworkAvailable(this@FaceEmojStoreActivity)) {
                mEmojStoreAdapter.setEnableLoadMore(false)
                refresh()
            } else {
                srl_emoj_store.isRefreshing = false
            }
        }
    }

    fun setBannerOption() {
        mConvenientBanner.setPages({ NetworkImageHolderView() }, Arrays.asList<String>(*banner_arr!!))
                .setPageIndicator(
                        intArrayOf(R.mipmap.face_ic_page_indicator, R.mipmap.face_ic_page_indicator_focused))
                .setOnItemClickListener { position ->
                    val emojId = mBannerList[position].faceId
                    MyLogUtils.e(TAG, emojId.toString() + "")
                    val intent = Intent()
                    intent.putExtra(FaceLocalConstant.Key.EMOJDETAIL_ID, emojId)
                    intent.putExtra(FaceLocalConstant.Key.EMOJDETAIL_TYPE, FaceLocalConstant.Value.EMOJDETAIL_TYPE_FACE)
                    intent.setClass(this@FaceEmojStoreActivity, FaceEmojDetailActivity::class.java)
                    startActivity(intent)
                }
    }

    override fun hasToolbar(): Boolean {
        return true
    }

    private fun initAdapter(datas: List<EmojStoreBean>) {
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rclv_emojstore.layoutManager = linearLayoutManager
        mEmojStoreAdapter = FaceEmojStoreAdapter(R.layout.face_item_emoj_store, datas)
        rclv_emojstore.adapter = mEmojStoreAdapter
        mEmojStoreAdapter.setOnLoadMoreListener({ rclv_emojstore.postDelayed({ loadMore() }, 500) }, rclv_emojstore)
    }

    private fun initHead() {
        val headView = layoutInflater.inflate(R.layout.face_activity_head_emojstore, null)
        mConvenientBanner = headView.findViewById(R.id.convenientBanner)
        mEmojStoreAdapter.addHeaderView(headView)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onEvent(emojOpe: EmojOpeBean) {
        if (emojOpe.emojOpeType!!.isEmojChange) {
            for (i in mDatas.indices) {
                val item = i + mEmojStoreAdapter.headerLayoutCount
                val emojStoreBean = mDatas[i]
                if (emojOpe.faceId == emojStoreBean.id.toString()) {
                    when (emojOpe.emojOpeType) {
                        FaceEmojOpeType.EmojOver -> emojStoreBean.downloadStatus = DownloadStatus.download_finish
                        FaceEmojOpeType.EmojDelete -> {
                            emojStoreBean.isDelete = 1
                            emojStoreBean.downloadStatus = DownloadStatus.download_init
                        }
                        FaceEmojOpeType.EmojDowning -> {
                            emojStoreBean.downloadStatus = emojOpe.downloadStatus
                            emojStoreBean.progress = emojOpe.downProgress
                        }
                        else -> {
                        }
                    }
                    mEmojStoreAdapter.notifyItemChanged(item)
                }
            }
        }
    }

    fun getData(page: Int) {
        //获取推荐表情数据
        val size = 10//默认一页10个数据
        OkHttpUtils.post()
                .tag(this)
                .url(FaceIConstants.RECOMMEND_EMOJ)
                .addHeader(FaceIConstants.APP_KEY, FaceConfigInfo.appKeyValue)
                .addParams("page", page.toString() + "")
                .addParams("size", size.toString() + "")
                .addParams("userId", FaceInitData.userId)
                .build()
                .execute(object : GsonReturnCallBack<List<EmojStoreBean>>() {
                    override fun onError(call: Call, e: Exception, id: Int) {
                        super.onError(call, e, id)
                        mRefreshStatu = mRefreshStatu.setStatuFail()
                        mEmojStoreAdapter.setEnableLoadMore(true)
                        dismissLoadingDialog()
                        if (mRefreshStatu.isRefresh) {
                            srl_emoj_store.isRefreshing = false
                        }
                        SimpleToast.showNetError(e)
                        srl_emoj_store.isRefreshing = false
                        loadFail()
                    }

                    override fun onResponse(response: List<EmojStoreBean>?, id: Int) {
                        super.onResponse(response, id)
                        srl_emoj_store.isRefreshing = false
                        mRefreshStatu = mRefreshStatu.setStatuSuc()
                        mEmojStoreAdapter.setEnableLoadMore(true)

                        dismissLoadingDialog()
                        if (mRefreshStatu.isFirst) mPage = 0
                        if (mRefreshStatu.isRefresh) mPage = 0//下拉刷新重置页数
                        if (mRefreshStatu.isLoad) mPage++
                        if (mPage == 0) mDatas.clear()
                        if (response?.isNotEmpty() == true) {
                            mDatas.addAll(response)
                            mEmojStoreAdapter.notifyDataSetChanged()
                        }
                        //加载更多
                        loadMoreData(response)
                        mEmojStoreAdapter.setEnableLoadMore(mDatas.size == 0)
                        loadSuccess()
                    }
                })
    }

    fun loadMoreData(response: List<EmojStoreBean>?) {
        if (response?.isNotEmpty() == true) {
            mEmojStoreAdapter.loadMoreComplete()
        }
        if (response?.size == 0) {
            mEmojStoreAdapter.loadMoreEnd()
        }
    }


    fun getBanner() {
        mRefreshStatuBanner = FaceRefreshStatuEnum.REFRESH
        OkHttpUtils.post()
                .tag(this)
                .url(FaceIConstants.BANNER)
                .addHeader(FaceIConstants.APP_KEY, FaceConfigInfo.appKeyValue)
                .addParams("showType", 1.toString() + "")
                .build()
                .execute(object : GsonReturnCallBack<List<StoreBannerBean>>() {
                    override fun onError(call: Call, e: Exception, id: Int) {
                        super.onError(call, e, id)
                        SimpleToast.showNetError(e)
                        mRefreshStatu = mRefreshStatu.setStatuFail()
                        loadFail()
                    }

                    override fun onResponse(response: List<StoreBannerBean>?, id: Int) {
                        super.onResponse(response, id)
                        mRefreshStatuBanner = FaceRefreshStatuEnum.REFRESH_SUC
                        if (response != null) {
                            mBannerList.clear()
                            mBannerList.addAll(response)
                        }
                        banner_arr = arrayOfNulls(mBannerList.size)
                        for (i in mBannerList.indices) {
                            banner_arr!![i] = mBannerList[i].imgUrl
                        }
                        mHandler.sendEmptyMessage(LOADD_BANNER)
                        loadSuccess()
                    }
                })
    }

    private fun loadFirst() {
        if (!mRefreshStatu.isDoing) {
            mRefreshStatu = FaceRefreshStatuEnum.FIRST
            showLoadingDialog()
            getData(0)
            getBanner()
            no_data_layout!!.visibility = View.GONE
            mEmojStoreAdapter.setEnableLoadMore(false)
        }
    }

    private fun refresh() {
        if (!mRefreshStatu.isDoing) {
            mRefreshStatu = FaceRefreshStatuEnum.REFRESH
            getData(0)
            if (mBannerList.size == 0) {
                getBanner()
            }
            no_data_layout!!.visibility = View.GONE
            mEmojStoreAdapter.setEnableLoadMore(false)
        } else {
            srl_emoj_store.isRefreshing = false
        }
    }

    private fun loadMore() {
        if (!mRefreshStatu.isDoing) {
            mRefreshStatu = FaceRefreshStatuEnum.LOAD
            getData(mPage + 1)
        }
    }

    private fun loadFail() {
        if (!mRefreshStatu.isLoad && mDatas.size == 0 && !mRefreshStatu.isDoing && mBannerList.size == 0) {//无数据
            no_data_layout!!.visibility = View.VISIBLE
            rclv_emojstore.visibility = View.GONE
        }
    }

    private fun loadSuccess() {
        rclv_emojstore.visibility = View.VISIBLE
        no_data_layout!!.visibility = View.GONE
    }

    // 停止自动翻页
    public override fun onPause() {
        super.onPause()
        //停止翻页
        mConvenientBanner.stopTurning()
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.getInstance().cancelTag(this)
        EventBus.getDefault().unregister(this)
    }

    companion object {

        //加载轮播图
        val LOADD_BANNER = 200
        private val TAG = "EmojStoreActivity"
    }
}
