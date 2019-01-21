package com.qingmeng.mengmeng.fragment

import android.annotation.SuppressLint
import android.support.design.widget.AppBarLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
import com.qingmeng.mengmeng.adapter.JoinMenuAdapter
import com.qingmeng.mengmeng.adapter.JoinRecommendAdapter
import com.qingmeng.mengmeng.adapter.UnderLineNavigatorAdapter
import com.qingmeng.mengmeng.entity.Banner
import com.qingmeng.mengmeng.entity.JoinRecommendBean
import com.qingmeng.mengmeng.entity.StaticBean
import com.qingmeng.mengmeng.entity.StaticDataBean
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.BoxUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_join.*
import kotlinx.android.synthetic.main.layout_banner.*
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import org.jetbrains.anko.support.v4.startActivity

@SuppressLint("CheckResult")
class JoinFragment : BaseFragment(), OnRefreshListener, OnLoadMoreListener, AppBarLayout.OnOffsetChangedListener,
        BGABanner.Delegate<ImageView, Banner>, BGABanner.Adapter<ImageView, Banner> {
    private lateinit var listPagerAdapter: PagerAdapter
    private lateinit var indicatorAdapter: UnderLineNavigatorAdapter
    private lateinit var commonNavigator: CommonNavigator
    private lateinit var mMenuAdapter: JoinMenuAdapter

    private val mImgList = ArrayList<Banner>()
    private val menuList = ArrayList<StaticBean>()
    private val tabList = ArrayList<StaticBean>()
    private val viewSparseArray = SparseArray<RecyclerView>()

    private var isLoading = false
    private var isRefresh = false
    private var offset = 1

    override fun getLayoutId(): Int = R.layout.fragment_join

    override fun initData() {
        getCacheData()
    }

    //获取缓存数据
    private fun getCacheData() {
        Observable.create<JoinRecommendBean> {
            val bannerData = BoxUtils.getBannersByType(1)
            val menuData = BoxUtils.getStaticByType(1)
            val tabData = BoxUtils.getStaticByType(2)
            mImgList.addAll(bannerData)
            menuList.addAll(menuData)
            tabList.addAll(tabData)
            var recommendBean = JoinRecommendBean(ArrayList())
            if (!tabList.isEmpty()) {
                recommendBean = JoinRecommendBean.fromString(tabList[vpList.currentItem].id)
            }
            it.onNext(recommendBean)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    initView()
                    initVPIndicator()
                    setBanner()
                    if (!tabList.isEmpty()) {
                        val view = getView(tabList[vpList.currentItem].id)
                        val adapter = view.adapter as JoinRecommendAdapter
                        adapter.updateItems(it.data)
                        view.tag = 2
                    }
                    getNewData()
                }, {
                    getNewData()
                }, {}, { addSubscription(it) })
    }

    override fun initListener() {
        //暴露接口测试
        test_intface.setOnClickListener {
            startActivity<LoginMainActivity>()
        }

        barLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            //verticalOffset始终为0以下的负数
            val percent = Math.abs(verticalOffset * 1.0f) / appBarLayout.totalScrollRange
            mSearchBg.alpha = percent
            if (percent == 0f) {
                bottomSearch.visibility = View.VISIBLE
                topSearch.visibility = View.GONE
            } else {
                bottomSearch.visibility = View.GONE
                topSearch.visibility = View.VISIBLE
            }
            if (offset > verticalOffset && percent > 0.8474676) {
                mBaffle.visibility = View.VISIBLE
            } else if (percent < 1) {
                mBaffle.visibility = View.GONE
            }
            offset = verticalOffset
        })
    }

    //设置分类icon
    private fun setMenus(bean: StaticDataBean) {
        if (!menuList.isEmpty()) {
            BoxUtils.removeStatic(menuList)
            menuList.clear()
        }
        BoxUtils.saveStatic(bean.systemStatic)
        menuList.addAll(bean.systemStatic)
        mMenuAdapter.notifyDataSetChanged()
    }

    //设置分类导航
    private fun setTabs(bean: StaticDataBean) {
        if (!tabList.isEmpty()) {
            BoxUtils.removeStatic(tabList)
            tabList.clear()
        }
        BoxUtils.saveStatic(bean.systemStatic)
        tabList.addAll(bean.systemStatic)
        listPagerAdapter.notifyDataSetChanged()
        indicatorAdapter.notifyDataSetChanged()
        commonNavigator.notifyDataSetChanged()
        getData(tabList[vpList.currentItem].id)
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
        mMenuAdapter = JoinMenuAdapter(menuList, context!!)
        mJoinMenu.adapter = mMenuAdapter
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
            }
        })
    }

    private fun getView(tagId: Int): RecyclerView {
        return viewSparseArray.get(tagId, null) ?: generateView(tagId)
    }

    private fun generateView(tagId: Int): RecyclerView {
        val recyclerView = RecyclerView(context!!)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = JoinRecommendAdapter(context!!) {

        }
        recyclerView.adapter = adapter
        viewSparseArray.put(tagId, recyclerView)
        return recyclerView
    }

    override fun onLoadMore() {
        isRefresh = false
        getData(tabList[vpList.currentItem].id)
    }

    override fun onRefresh() {
        getNewData()
    }

    private fun getNewData() {
        isRefresh = true
        getBanners(getVersion(0))
        getStaticData(getVersion(1), 1)
        getStaticData(getVersion(2), 2)
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
        ApiUtils.getApi().getRecommend(0, page)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    endLoadEverything()
                    isLoading = false
                    if (bean.code == 12000) {
                        bean.data?.let {
                            if (isRefresh) {
                                page = 1
                                it.upDate(id)
                                adapter.updateItems(it.data)
                                isRefresh = false
                            } else {
                                adapter.addItems(it.data)
                                swipeLayout.isLoadMoreEnabled = false
                            }
                            view.tag = ++page
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

    /**
     * 获取静态数据
     * @param type 类型：1.首页banner8个icon 2.首页列表模块 3.列表筛选标题 4.综合排序 5.反馈类型
     */
    private fun getStaticData(version: String, type: Int) {
        ApiUtils.getApi().getStaticInfo(version, type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            if (!it.systemStatic.isEmpty()) {
                                it.setVersion()
                                when (type) {
                                    1 -> setMenus(it)
                                    2 -> setTabs(it)
                                }
                            } else if (type == 2) {
                                endLoadEverything()
                            }
                        }
                    } else {
                        if (type == 2) endLoadEverything()
                        if (bean.code != 20000) ToastUtil.showShort(bean.msg)
                        if (type == 2 && bean.code == 20000 && !tabList.isEmpty()) {
                            getData(tabList[vpList.currentItem].id)
                        }
                    }
                }, {
                    if (type == 2) endLoadEverything()
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    //获取banner图
    private fun getBanners(version: String) {
        ApiUtils.getApi().getBanners(version, 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            if (!it.banners.isEmpty()) {
                                if (!mImgList.isEmpty()) {
                                    BoxUtils.removeBanners(mImgList)
                                    mImgList.clear()
                                }
                                it.setVersion()
                                mImgList.addAll(it.banners)
                                BoxUtils.saveBanners(mImgList)
                                setBanner()
                            }
                        }
                    } else if (bean.code != 20000) {
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
    override fun onBannerItemClick(banner: BGABanner?, itemView: ImageView?, model: Banner?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //banner加载图片
    override fun fillBannerItem(banner: BGABanner?, itemView: ImageView, model: Banner?, position: Int) {
        model?.let {
            Glide.with(this).load(it.imgUrl).apply(RequestOptions()
                    .placeholder(R.drawable.image_holder).error(R.drawable.image_holder)
                    .centerCrop()).into(itemView)
        }
    }

    private fun setBanner() {
        mJoinBanner.setAdapter(this)//必须设置此适配器，否则不会调用接口方法来填充图片
        mJoinBanner.setDelegate(this)//设置点击事件，重写点击回调方法
        mJoinBanner.setData(mImgList, null)
        if (mImgList.size > 1) {
            mJoinBanner.setAutoPlayAble(true)
        } else {
            mJoinBanner.setAutoPlayAble(false)
        }
    }

    /**
     * @param type 0:banner 1:icon 2:title
     **/
    private fun getVersion(type: Int): String {
        return when {
            type == 0 && !mImgList.isEmpty() -> mImgList[0].version
            type == 1 && !menuList.isEmpty() -> menuList[0].version
            type == 2 && !tabList.isEmpty() -> tabList[0].version
            else -> ""
        }
    }
}