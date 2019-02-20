package com.lemo.emojcenter.activity


import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import com.lemo.emojcenter.FaceInitData
import com.lemo.emojcenter.R
import com.lemo.emojcenter.adapter.FaceDownloadAdapter
import com.lemo.emojcenter.api.FaceNetRequestApi
import com.lemo.emojcenter.base.FaceBaseActivity
import com.lemo.emojcenter.bean.DownloadBean
import com.lemo.emojcenter.bean.DownloadStatus
import com.lemo.emojcenter.bean.EmojOpeBean
import com.lemo.emojcenter.constant.FaceEmojOpeType
import com.lemo.emojcenter.constant.FaceIConstants
import com.lemo.emojcenter.constant.FaceRefreshStatuEnum
import com.lemo.emojcenter.manage.FaceDownloadFaceManage
import com.lemo.emojcenter.utils.GsonReturnCallBack
import com.lemo.emojcenter.utils.download.FaceDownlodThreadExecutor
import kotlinx.android.synthetic.main.face_activity_download_record.*
import okhttp3.Call
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Exception
import java.util.*
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

/**
 * Description:下载记录
 * Author:wxw
 * Date:2018/1/24.
 */
class FaceDownloadRecordActivity : FaceBaseActivity() {
    private lateinit var mList: MutableList<DownloadBean>

    private lateinit var mDownloadAdapter: FaceDownloadAdapter
    private var mDownloadExecutor: FaceDownlodThreadExecutor? = null

    private var mPage = -1
    private var mRefreshStatu = FaceRefreshStatuEnum.DEFAULT

    override fun getLayoutId(): Int {
        return R.layout.face_activity_download_record
    }

    override fun initView() {
        super.initView()
        EventBus.getDefault().register(this)
        mList = ArrayList()
        //设置toolbar右操作按钮显示隐藏
        top_view.setRightOption()
        //初始化线程池
        mDownloadExecutor = FaceDownlodThreadExecutor(FaceIConstants.CORE_POOL_SIZE, FaceIConstants.MAX_POOL_SIZE,
                FaceIConstants.KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
                LinkedBlockingDeque())
        initAdapter()

        //去掉动画
        val animator = downloade_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    override fun initData() {
        loadFirst()
    }

    override fun initListener() {
        super.initListener()
        //下载按钮的点击事件
        mDownloadAdapter.setOnItemChildClickListener { adapter, view, position ->
            val emojOpe = EmojOpeBean(mList[position].faceId.toString() + "")
            emojOpe.emojOpeType = FaceEmojOpeType.EmojDowning
            emojOpe.downloadStatus = DownloadStatus.download_start
            EventBus.getDefault().postSticky(emojOpe)
            //获取1.resource资源
            val mFileResourse = mList[position].resource
            mList[position].downloadStatus = DownloadStatus.download_start
            //获取下载路径
            FaceDownloadFaceManage.fileDownGetUrl(mFileResourse, mList[position].faceId.toString() + "")
        }

        //点击下拉刷新
        download_refresh.setOnRefreshListener { refresh() }
    }

    private fun initAdapter() {
        (downloade_view.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        val linearLayoutManager = LinearLayoutManager(this)
        downloade_view.layoutManager = linearLayoutManager
        mDownloadAdapter = FaceDownloadAdapter(R.layout.face_adapter_download, mList)
        downloade_view.adapter = mDownloadAdapter
        mDownloadAdapter.setOnLoadMoreListener(
                { downloade_view.postDelayed({ loadMore() }, 500) }, downloade_view)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onEvent(emojOpe: EmojOpeBean) {
        if (emojOpe.emojOpeType?.isEmojChange == true) {
            mList.forEachIndexed { index, downloadBean ->
                if (emojOpe.faceId == downloadBean.faceId.toString()) {
                    when (emojOpe.emojOpeType) {
                        FaceEmojOpeType.EmojOver -> {
                            downloadBean.downloadStatus = DownloadStatus.download_finish
                            mDownloadAdapter.notifyItemChanged(index)
                        }
                        FaceEmojOpeType.EmojDelete -> {
                            downloadBean.isDelete = 1
                            downloadBean.downloadStatus = DownloadStatus.download_init
                            mDownloadAdapter.notifyItemChanged(index)
                        }
                        FaceEmojOpeType.EmojDowning -> {
                            downloadBean.downloadStatus = emojOpe.downloadStatus
                            downloadBean.progress = emojOpe.downProgress
                            mDownloadAdapter.notifyItemChanged(index)
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }


    private fun getItemInfo(page: Int) {
        val size = 10
        FaceNetRequestApi.getMyEmojDownRecord(FaceInitData.userId, page, size, object : GsonReturnCallBack<List<DownloadBean>>() {
            override fun onResponse(response: List<DownloadBean>?, id: Int) {
                super.onResponse(response, id)
                mRefreshStatu = mRefreshStatu.setStatuSuc()
                mDownloadAdapter.setEnableLoadMore(true)
                download_refresh.isRefreshing = false
                if (mRefreshStatu.isRefresh) {
                    mPage = 0
                }
                if (mRefreshStatu.isFirst) {
                    dismissLoadingDialog()
                    mPage = 0
                }
                if (mRefreshStatu.isLoad) {
                    mPage++
                }
                if (mPage == 0) {
                    mList.clear()
                    mDownloadAdapter.notifyDataSetChanged()
                }
                if (response?.size ?: 0 > 0) {
                    mList.addAll(response!!)
                    mDownloadAdapter.notifyDataSetChanged()
                }
                if (mList.size == 0) {
                    download_refresh.visibility = View.GONE
                    no_data_layout.visibility = View.VISIBLE
                }
                if (response?.isNotEmpty() == true) {
                    mDownloadAdapter.loadMoreComplete()
                }
                if (response?.size == 0) {
                    mDownloadAdapter.loadMoreEnd()
                }
            }

            override fun onError(call: Call, e: Exception, id: Int) {
                super.onError(call, e, id)
                mRefreshStatu = mRefreshStatu.setStatuFail()
                mDownloadAdapter.setEnableLoadMore(true)
                download_refresh.isRefreshing = false
                if (mRefreshStatu.isFirst) {
                    dismissLoadingDialog()
                }
                if (mList.size == 0) {
                    download_refresh.visibility = View.GONE
                    no_data_layout.visibility = View.VISIBLE
                }
            }

        })
    }


    private fun refresh() {
        if (!mRefreshStatu.isDoing) {
            mRefreshStatu = FaceRefreshStatuEnum.REFRESH
            getItemInfo(0)
            mDownloadAdapter.setEnableLoadMore(false)
        } else {
            download_refresh.isRefreshing = false
        }
    }

    private fun loadFirst() {
        if (!mRefreshStatu.isDoing) {
            showLoadingDialog()
            mRefreshStatu = FaceRefreshStatuEnum.FIRST
            getItemInfo(0)
            if (mDownloadAdapter != null) {
                mDownloadAdapter.setEnableLoadMore(false)
            }
        }
    }


    private fun loadMore() {
        if (!mRefreshStatu.isDoing) {
            mRefreshStatu = FaceRefreshStatuEnum.LOAD
            getItemInfo(mPage + 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
