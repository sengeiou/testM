package com.qingmeng.mengmeng.fragment

import android.annotation.SuppressLint
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.app.common.logger.Logger
import com.qingmeng.mengmeng.BaseFragment
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.HeadDetailsActivity
import com.qingmeng.mengmeng.activity.ShopDetailActivity
import com.qingmeng.mengmeng.activity.WebViewActivity
import com.qingmeng.mengmeng.adapter.LoadMoreWrapper
import com.qingmeng.mengmeng.adapter.NewsPaperAdapter
import com.qingmeng.mengmeng.adapter.util.FooterView
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
class NewsPaperFragment : BaseFragment() {
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: NewsPaperAdapter
    private lateinit var mLoadMoreAdapter: LoadMoreWrapper<Any>
    private lateinit var mFootView: FooterView
    private var mCanHttpLoad = true                          //是否请求接口
    private var mHasNextPage = true                          //是否请求下一页
    private var mPageNum: Int = 1                            //接口请求页数
    private var mImgList = ArrayList<Banner>()
    private var imgList = ArrayList<Banner>()                //用来删除本地数据用
    private var mNewsList = ArrayList<NewsPagerList>()       //接口请求数据
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
        mFootView = FooterView(context!!)
        setFooterStatus(mFootView, 3)
        initAdapter()
        getCacheData()
    }

    override fun initListener() {
        super.initListener()

        //下拉刷新
        news_swipeLayout.setOnRefreshListener {
            Logger.d("下拉刷新")
            getNewData()
        }

        //上滑加载
        news_swipeLayout.setOnLoadMoreListener {
            httpLoad(mPageNum)
        }

        swipe_target.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            //滚动状态改变时
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //滑到顶部了
                if (!recyclerView.canScrollVertically(-1)) {
                    if (!news_swipeLayout.isLoadingMore) {
                        news_swipeLayout.isRefreshEnabled = true
                        news_swipeLayout.isLoadMoreEnabled = false
                    }
                    //没有滑动时 在最下面一个item
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE && mLayoutManager.findLastVisibleItemPosition() + 1 == mLoadMoreAdapter.itemCount) {
                    //如果下拉刷新没有刷新的话
                    if (!news_swipeLayout.isRefreshing) {
                        if (mNewsList.isNotEmpty()) {
                            //是否有下一页
                            if (mHasNextPage) {
                                //是否可以请求接口
                                if (mCanHttpLoad) {
                                    news_swipeLayout.isRefreshEnabled = false
//                                    news_swipeLayout.isLoadMoreEnabled = true
                                    httpLoad(mPageNum)
                                }
                            }
                        }
                    }
                } else {
//                    news_swipeLayout.isRefreshEnabled = false
                    news_swipeLayout.isLoadMoreEnabled = false
                }
            }
        })
    }

    private fun initAdapter() {
        mLayoutManager = LinearLayoutManager(context)
        swipe_target.layoutManager = mLayoutManager
        mAdapter = NewsPaperAdapter(context!!, mImgList, {
            startActivity<HeadDetailsActivity>("URL" to it.articleUrl, IConstants.articleId to it.id)
        }, {
            it.apply {
                when (skipType) {
                    2 -> startActivity<WebViewActivity>(IConstants.title to "详情", IConstants.detailUrl to url)
                    3 -> startActivity<HeadDetailsActivity>("URL" to url, IConstants.articleId to id)
                    4 -> startActivity<ShopDetailActivity>(IConstants.BRANDID to interiorDetailsId)
                    5 -> {
                        try {
                            OpenMallApp.open(context!!, exteriorUrl)
                        } catch (e: OpenMallApp.NotInstalledException) {
                            startActivity<WebViewActivity>(IConstants.title to "详情", IConstants.detailUrl to url)
                        }
                    }
                }
            }
        })
        mLoadMoreAdapter = LoadMoreWrapper(mAdapter)
        mLoadMoreAdapter.setLoadMoreView(mFootView)
        swipe_target.adapter = mLoadMoreAdapter
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
                            //如果页数是1 ，清空内容重新加载
                            if (pageNum == 1) {
                                BoxUtils.removeNewsPager()
                                //清空已选择集合
                                mNewsList.clear()
                                mPageNum = 1
                                mAdapter.updateItems(mNewsList)
                            }
                            //请求后判断数据
                            if (data == null || data?.data!!.isEmpty()) {
                                mHasNextPage = false
                                if (pageNum == 1) {
                                    setFooterStatus(mFootView, 3)
                                } else {
                                    setFooterStatus(mFootView, 2)
                                }
                            } else {
                                mHasNextPage = true
                                mNewsList.addAll(data!!.data)
                                mAdapter.addItems(data!!.data)
                                //保存第一页的数据
                                if (mPageNum == 1) {
                                    BoxUtils.saveNewsPager(mNewsList)
                                }
                                mPageNum++
                                setFooterStatus(mFootView, 1)
                                //如果不满一个屏幕 就隐藏
                                if (mNewsList.size < 8) {
                                    setFooterStatus(mFootView, 3)
                                }
                            }
                            //适配器更新数据
                            mLoadMoreAdapter.notifyDataSetChanged()
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
        ApiUtils.getApi().getBanners(version, 8)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            //清除缓存
                            BoxUtils.removeBanners(imgList)
                            mImgList.clear()
                            it.setVersion()
                            mImgList.addAll(it.banners)
                            imgList = mImgList
                            //存入缓存
                            BoxUtils.saveBanners(imgList)
                            mLoadMoreAdapter.notifyDataSetChanged()
                        }
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, {
                    addSubscription(it)
                })
    }

    private fun getCacheData() {
        Observable.create<NewsPagerBean> {
            val newsList = BoxUtils.getNewsPager()
            BoxUtils.getBannersByType(8)?.let {
                imgList.addAll(it)
            }
            mImgList.clear()
            mNewsList.clear()
            mImgList.addAll(imgList)
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
                    if (mNewsList.isNotEmpty()) {
                        mAdapter.addItems(mNewsList)
                    }
                    mLoadMoreAdapter.notifyDataSetChanged()
                    getNewData()
                }, {
                    getNewData()
                }, {}, { addSubscription(it) })
    }

    private fun endLoadingMore() {
        Logger.d("结束刷新")
        news_swipeLayout.isRefreshing = false
        news_swipeLayout.isLoadingMore = false
        news_swipeLayout.isRefreshEnabled = false
        news_swipeLayout.isLoadMoreEnabled = false
    }

    private fun getNewData() {
        httpLoad(1)
        httpBannerLoad(getVision(1))
    }

    private fun getVision(type: Int): String {
        return when {
            type == 1 && !mImgList.isEmpty() -> mImgList[0].version
            else -> ""
        }
    }
}