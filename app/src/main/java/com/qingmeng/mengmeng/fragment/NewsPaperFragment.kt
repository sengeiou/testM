package com.qingmeng.mengmeng.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.aspsine.swipetoloadlayout.OnLoadMoreListener
import com.aspsine.swipetoloadlayout.OnRefreshListener
import com.qingmeng.mengmeng.BaseFragment
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.HeadDetailsActivity
import com.qingmeng.mengmeng.activity.ShopDetailActivity
import com.qingmeng.mengmeng.activity.WebViewActivity
import com.qingmeng.mengmeng.adapter.NewsPaperAdapter
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.entity.Banner
import com.qingmeng.mengmeng.entity.NewsPagerBean
import com.qingmeng.mengmeng.entity.NewsPagerList
import com.qingmeng.mengmeng.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_head_newspaper.*
import kotlinx.android.synthetic.main.layout_red_news_head.*
import org.jetbrains.anko.support.v4.startActivity

@SuppressLint("CheckResult")
class NewsPaperFragment : BaseFragment(), OnLoadMoreListener, OnRefreshListener {
    private lateinit var mLauyoutManger: LinearLayoutManager
    private lateinit var mNewsPagerAdapter: NewsPaperAdapter
    private var mCanHttpLoad = true                          //是否请求接口
    private var mHasNextPage = true                          //是否请求下一页
    private var mPageNum: Int = 1                            //接口请求页数
    private var isLoadMore = false                              //加载更多
    private var isRefeshing = false                             //下拉刷新
    private var mImgList = ArrayList<Banner>()
    private var mNewsList = ArrayList<NewsPagerList>()       //接口请求数据
    private var REQUEST_NEWS = 123
    override fun getLayoutId(): Int = R.layout.fragment_head_newspaper

    override fun initObject() {
        super.initObject()
        mRedNewsTitle.setText(R.string.tab_name_head_newspaper)
        // 获得状态栏高度
        val statusBarHeight = getBarHeight(context!!)
        //给布局的高度重新设置一下 加上状态栏高度
        mRedNewsHead.layoutParams.height = mRedNewsHead.layoutParams.height + getBarHeight(context!!)
        mRedNewsTitle.setMarginExt(top = statusBarHeight + context!!.dp2px(60))
        mRedNewsBack.visibility = View.GONE
        initAdapter()
    }

    override fun initListener() {
        super.initListener()
        news_swipeLayout.setOnLoadMoreListener(this)
        news_swipeLayout.setOnRefreshListener(this)
        swipe_target.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            internal var lastVisibleItemPosition: Int = 0
            //滚动状态改变时
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //没有滑动时 在最下面
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItemPosition + 1 == mNewsPagerAdapter.itemCount) {
                    news_swipeLayout.isLoadMoreEnabled = true
                }
            }

            //滑动
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lastVisibleItemPosition = mLauyoutManger.findLastVisibleItemPosition()
            }
        })
    }

    private fun initAdapter() {
        mLauyoutManger = LinearLayoutManager(context)
        swipe_target.layoutManager = mLauyoutManger
        mNewsPagerAdapter = NewsPaperAdapter(context!!, mImgList, {
            startActivityForResult(Intent(context, HeadDetailsActivity::class.java).putExtra("URL", it.articleUrl), REQUEST_NEWS)
        }, {
            it.apply {
                when (skipType) {
                    2 -> startActivity<WebViewActivity>(IConstants.title to "详情", IConstants.detailUrl to url)
                    3 -> startActivity<HeadDetailsActivity>("URL" to url)
                    4 -> startActivity<ShopDetailActivity>(IConstants.BRANDID to interiorDetailsId)
                    5 -> {
                        try {
                            OpenMallApp.open(context!!, exteriorUrl)
                        } catch (e: OpenMallApp.NotInstalledException) {
                            startActivity<WebViewActivity>(IConstants.detailUrl to url)
                        }
                    }
                }
            }
        })
        swipe_target.adapter = mNewsPagerAdapter
    }

    override fun initData() {
        super.initData()
        getCacheData()
    }

    private fun getCacheData() {
        Observable.create<NewsPagerBean> {
            val newsList = BoxUtils.getNewsPager()
            val bannerList = BoxUtils.getBannersByType(3)
            if (!mImgList.isEmpty()) {
                BoxUtils.removeBanners(mImgList)
                mImgList.clear()
            }
            if (!mNewsList.isEmpty()) {
                BoxUtils.removeNewsPager(mNewsList)
                mNewsList.clear()
            }
            mImgList.addAll(bannerList)
            mNewsList.addAll(newsList)
            var mSeachCondition = NewsPagerBean(ArrayList())
            if (!mNewsList.isEmpty()) {
                mSeachCondition = NewsPagerBean.fromString(mNewsList[0].id)
            }
            if (!mImgList.isEmpty()) {
                mSeachCondition = NewsPagerBean.fromString(mImgList[0].id)
            }
            it.onNext(mSeachCondition)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    if (!mNewsList.isEmpty()) {
                        mNewsPagerAdapter.addItems(mNewsList)
                        mNewsPagerAdapter.notifyDataSetChanged()
                    }
                    if (!mImgList.isEmpty()) {
                        mNewsPagerAdapter.notifyDataSetChanged()
                    }
                    getNewData()
                }, {
                    getNewData()
                }, {}, { addSubscription(it) })
    }

    override fun onLoadMore() {
        //  isLoadMore = false
        news_swipeLayout.isRefreshing = false
        httpLoad(mPageNum)
    }

    override fun onRefresh() {
        getNewData()
    }

    private fun endLoadingMore() {
        if (news_swipeLayout.isRefreshing) {
            news_swipeLayout.endRefresh()
        }
        if (news_swipeLayout.isLoadingMore) {
            news_swipeLayout.endLoadMore()
        }
    }

    private fun getNewData() {
        isLoadMore = true
        isRefeshing = true
        httpLoad(1)
        httpBannerLoad(getVision(1))
    }

    private fun getVision(type: Int): String {
        return when {
            type == 1 && !mImgList.isEmpty() -> mImgList[0].version
            else -> ""
        }
    }

    //头报列表文章
    private fun httpLoad(pageNum: Int) {
        mCanHttpLoad = false

        ApiUtils.getApi().getNewsHeadList(pageNum)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    //加载状态关闭
                    endLoadingMore()
                    mCanHttpLoad = true
                    it.apply {
                        if (code == 12000) {
                            //请求后判断数据
                            if (data == null || data?.data!!.isEmpty()) {
                                mHasNextPage = false
                                if (pageNum == 1) {
                                    //拿数据库数据
                                    getCacheData()
                                }
                            } else {
                                mHasNextPage = true
                                data?.let {
                                    if (data !== null) {
                                        //如果页数是1 ，清空内容重新加载
                                        if (pageNum == 1) {
                                            //清空已选择集合
                                            BoxUtils.removeNewsPager(mNewsList)
                                            mNewsList.clear()
                                            mPageNum = 1
                                            if (!mNewsList.isEmpty()) {
                                                mNewsList.clear()
                                            }
                                            mNewsPagerAdapter.updateItems(mNewsList)
                                        }
                                        if (!mNewsList.isEmpty()) {
                                            BoxUtils.removeNewsPager(mNewsList)
                                            mNewsList.clear()
                                        }
                                        mNewsList.addAll(it.data)
                                        //数据库存入缓存
                                        mNewsPagerAdapter.addItems(mNewsList)
                                        BoxUtils.saveNewsPager(mNewsList)
                                        news_swipeLayout.isLoadMoreEnabled = false
                                        mPageNum++
                                    }
                                }
                            }
                            //适配器更新数据
                            mNewsPagerAdapter.notifyDataSetChanged()
                        }
                    }
                }, {
                    //上划加载 打开 未写
                    endLoadingMore()
                    mCanHttpLoad = true
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    private fun httpBannerLoad(version: String) {
        ApiUtils.getApi().getbanner(version, 3)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            if (!it.banners.isEmpty()) {
                                if (!mImgList.isEmpty()) {
                                    //清除缓存
                                    BoxUtils.removeBanners(mImgList)
                                    mImgList.clear()
                                }
                                it.setVersion()
                                mImgList.addAll(it.banners)
                                //存入缓存
                                BoxUtils.saveBanners(mImgList)
                                mNewsPagerAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, {
                    addSubscription(it)
                })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            getNewData()
        }
    }
}