package com.qingmeng.mengmeng.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import cn.bingoogolapple.bgabanner.BGABanner
import com.aspsine.swipetoloadlayout.OnLoadMoreListener
import com.aspsine.swipetoloadlayout.OnRefreshListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.qingmeng.mengmeng.BaseFragment
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.JoinFeedbackActivity
import com.qingmeng.mengmeng.activity.LoginMainActivity
import com.qingmeng.mengmeng.adapter.JoinRecommendAdapter
import com.qingmeng.mengmeng.adapter.UnderLineNavigatorAdapter
import com.qingmeng.mengmeng.entity.BannersBean
import com.qingmeng.mengmeng.entity.JoinRecommendBean
import com.qingmeng.mengmeng.entity.StaticDataBean
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_join.*
import kotlinx.android.synthetic.main.layout_banner.*
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import org.jetbrains.anko.support.v4.startActivity

@SuppressLint("CheckResult")
class JoinFragment : BaseFragment(), OnRefreshListener, OnLoadMoreListener, AppBarLayout.OnOffsetChangedListener,
        BGABanner.Delegate<ImageView, BannersBean.BannerBean>, BGABanner.Adapter<ImageView, String> {
    private lateinit var listPagerAdapter: PagerAdapter
    private lateinit var indicatorAdapter: UnderLineNavigatorAdapter
    private lateinit var commonNavigator: CommonNavigator

    private val mImgList = ArrayList<BannersBean.BannerBean>()
    private val tabList = ArrayList<StaticDataBean.StaticBean>()
    private val recommendList = ArrayList<JoinRecommendBean.JoinBean>()
    private val viewSparseArray = SparseArray<RecyclerView>()

    private var isLoading = false
    private var isRefresh = false
    private var offset = 1

    override fun getLayoutId(): Int = R.layout.fragment_join

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
        initVPIndicator()
    }

    override fun initData() {
        getBanners()
        getTabs()
    }

    override fun initListener() {
        //暴露接口测试
        test_intface.setOnClickListener {
            startActivity<JoinFeedbackActivity>()
        }

        barLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            //verticalOffset始终为0以下的负数
            val percent = Math.abs(verticalOffset * 1.0f) / appBarLayout.totalScrollRange
            Log.e("joinFragment", "percent == $percent")
            Log.e("joinFragment", "verticalOffset == $verticalOffset")
            mSearchBg.alpha = percent
            if (percent == 0f) {
                bottomSearch.visibility = View.VISIBLE
                topSearch.visibility = View.GONE
            } else {
                bottomSearch.visibility = View.GONE
                topSearch.visibility = View.VISIBLE
            }
            if (offset > verticalOffset && percent > 0.67550504) {
                mBaffle.visibility = View.VISIBLE
            } else if (percent < 1) {
                mBaffle.visibility = View.GONE
            }
            offset = verticalOffset
        })
    }

    private fun getTabs() {
        ApiUtils.getApi().getStaticInfo("", 2)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            if (!it.systemStatic.isEmpty()) {
                                if (!tabList.isEmpty()) {
                                    tabList.clear()
                                }
                                tabList.addAll(it.systemStatic)
                                listPagerAdapter.notifyDataSetChanged()
                                indicatorAdapter.notifyDataSetChanged()
                                commonNavigator.notifyDataSetChanged()
                                onRefresh()
                            } else {
                                endLoadEverything()
                            }
                        }
                    } else {
                        endLoadEverything()
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    endLoadEverything()
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    private fun initView() {
        swipeLayout.setOnRefreshListener(this)
        swipeLayout.setOnLoadMoreListener(this)
        barLayout.addOnOffsetChangedListener(this)

        listPagerAdapter = object : PagerAdapter() {
            override fun getCount(): Int {
                return tabList.size
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val recyclerView = getView(tabList[position].id)
                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        swipeLayout.isLoadMoreEnabled = isBottom()
                    }
                })
                container.addView(recyclerView)
                return recyclerView
            }

            override fun isViewFromObject(view: View, any: Any): Boolean {
                return view === any
            }

            override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
                container.removeView(any as View)
            }
        }
        vpList.adapter = listPagerAdapter
    }

    private fun initVPIndicator() {
        commonNavigator = CommonNavigator(context)
        indicatorAdapter = UnderLineNavigatorAdapter(tabList)
        indicatorAdapter.setRelateViewPager(vpList)
        commonNavigator.adapter = indicatorAdapter
        channelIndicator.navigator = commonNavigator
        vpList.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                channelIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                endLoadEverything()
                channelIndicator.onPageSelected(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                channelIndicator.onPageScrollStateChanged(state)
                if (state == ViewPager.SCROLL_STATE_IDLE) {

                }
            }
        })
    }

    private fun getView(tagId: Int): RecyclerView {
        return viewSparseArray.get(tagId, null) ?: generateView(tagId)
    }

    private fun generateView(tagId: Int): RecyclerView {
        val recyclerView = RecyclerView(context!!)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = JoinRecommendAdapter(context!!, recommendList) {}
        recyclerView.adapter = adapter
        viewSparseArray.put(tagId, recyclerView)
        return recyclerView
    }

    override fun onLoadMore() {
        isRefresh = false
        getData(tabList[vpList.currentItem].id)
    }

    override fun onRefresh() {
        isRefresh = true
        getData(tabList[vpList.currentItem].id)
    }

    /**
     * 拉取数据
     *
     * @param id
     */
    private fun getData(id: Int) {
        val view = getView(id)
        var page = 1
        if (isRefresh) {
            page = 1
        } else {
            view.tag?.let { page = it as Int }
        }
        val adapter = view.adapter as JoinRecommendAdapter
        ApiUtils.getApi().getRecommend(id, page)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    endLoadEverything()
                    isLoading = false
                    if (bean.code == 12000) {
                        bean.data?.let {
                            val recommend = ArrayList<JoinRecommendBean.JoinBean>()
                            (0..2).forEach {
                                recommend.add(JoinRecommendBean.JoinBean(it + page * 10, "${it + page * 10}", "${it + page * 10}", "${it + page * 10}"))
                            }
                            if (isRefresh) {
                                page = 1
                                adapter.updateItems(recommend)
                                isRefresh = false
                            } else {
                                adapter.addItems(recommend)
                                swipeLayout.isLoadMoreEnabled = false
                            }
                            getView(id).tag = ++page
                        }
                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    endLoadEverything()
                    isLoading = false
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    //获取banner图
    private fun getBanners() {
        ApiUtils.getApi().getBanners("", 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            if (!it.banners.isEmpty()) {
                                if (!mImgList.isEmpty()) {
                                    mImgList.clear()
                                }
                                mImgList.addAll(it.banners)
                                setBanner()
                            }
                        }
                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    private fun endLoadEverything() {
        if (swipeLayout.isRefreshing) {
            swipeLayout.endRefresh()
        }
        if (swipeLayout.isLoadingMore) {
            swipeLayout.endLoadMore()
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        if (tabList.isEmpty()) {
            return
        }
        swipeLayout.isRefreshEnabled = verticalOffset >= 0 && isTop()
    }

    private fun isTop(): Boolean {
        val tagId = tabList[vpList.currentItem].id
        return !getView(tagId).canScrollVertically(-1)
    }

    fun isBottom(): Boolean {
        val tagId = tabList[vpList.currentItem].id
        return !getView(tagId).canScrollVertically(1)
    }

    //banner点击事件
    override fun onBannerItemClick(banner: BGABanner?, itemView: ImageView?, model: BannersBean.BannerBean?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //banner加载图片
    override fun fillBannerItem(banner: BGABanner?, itemView: ImageView, model: String?, position: Int) {
        model?.let {
            Glide.with(this).load(it).apply(RequestOptions()
                    .placeholder(R.drawable.image_holder).error(R.drawable.image_holder)
                    .centerCrop()).into(itemView)
        }
    }

    private fun setBanner() {
        mJoinBanner.setAdapter(this)//必须设置此适配器，否则不会调用接口方法来填充图片
        mJoinBanner.setDelegate(this)//设置点击事件，重写点击回调方法
        mJoinBanner.setData(mImgList, null)
    }
}