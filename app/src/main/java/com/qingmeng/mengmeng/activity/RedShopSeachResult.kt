package com.qingmeng.mengmeng.activity

/**
 * Created by fyf on 2019/1/8
 * 搜索结果页
 */
import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.adapter.LoadMoreWrapper
import com.qingmeng.mengmeng.adapter.util.FooterView
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.constant.IConstants.SEACH_RESULT
import com.qingmeng.mengmeng.constant.IConstants.THREE_LEVEL
import com.qingmeng.mengmeng.constant.IConstants.firstLevel
import com.qingmeng.mengmeng.constant.IConstants.secondLevel
import com.qingmeng.mengmeng.entity.SearchDto
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.setFooterStatus
import com.qingmeng.mengmeng.view.GlideRoundTransformCenterCrop
import com.qingmeng.mengmeng.view.dialog.PopSeachCondition
import com.qingmeng.mengmeng.view.dialog.PopSeachSelect
import com.qingmeng.mengmeng.view.flowlayout.FlowLayout
import com.qingmeng.mengmeng.view.flowlayout.TagAdapter
import com.qingmeng.mengmeng.view.flowlayout.TagFlowLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_red_shop_seach_result.*
import kotlinx.android.synthetic.main.layout_head_seach.*
import kotlinx.android.synthetic.main.red_shop_search_result_item.*
import org.jetbrains.anko.startActivity


@SuppressLint("CheckResult")
class RedShopSeachResult : BaseActivity() {
    private lateinit var mAdapter: LoadMoreWrapper<SearchDto>
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var popupMenu1: PopSeachSelect
    private lateinit var popupMenu2: PopSeachSelect
    private lateinit var popupMenu3: PopSeachSelect
    private lateinit var mFootView: FooterView
    private var popupMenu4: PopSeachCondition? = null
    private var mIsInstantiationOne = false
    private var mIsInstantiationTwo = false
    private var mIsInstantiationThree = false
    private var mIsInstantiationFour = false
    private var mSeachResultList = ArrayList<SearchDto>()
    //必填
    private var mPageNum: Int = 1              //页数1页10条
    //选填
    private var keyWord: String = ""           //搜索关键字
    private var fatherId = 0              //餐饮类型父ID
    private var typeId = 0                //餐饮类型ID
    private var cityIds = String()            //加盟区域ID
    private var capitalIds = String()         //投资金额ID
    private var modeIds = String()           //加盟模式ID
    private var integratedSortId = 0      //综合排序（12345）
    private var mCanHttpLoad = true                //是否可以请求接口
    private var mHasNextPage = true                //是否有下一页
    private var mShowTittle: String = ""            //进入选中

    override fun getLayoutId(): Int = R.layout.activity_red_shop_seach_result

    override fun initObject() {
        super.initObject()

        mFootView = FooterView(this)
        setFooterStatus(mFootView, 3)

        initAdapter()
        //传入
        keyWord = intent.getStringExtra(SEACH_RESULT) ?: ""
        if (keyWord.isEmpty()) {
            fatherId = intent.getIntExtra(firstLevel, 0)
            typeId = intent.getIntExtra(secondLevel, 0)
            mShowTittle = intent.getStringExtra(THREE_LEVEL) ?: ""
        } else {
            fatherId = 0
            typeId = 0
            mShowTittle = ""
        }
        if (mShowTittle.isEmpty()) {
            search_food_type.text = "餐饮类型"
            search_food_type.setTextColor(resources.getColor(R.color.color_999999))
        } else {
            search_food_type.text = mShowTittle
        }

        head_search.setText(keyWord)
        goToSeach()
    }

    override fun initListener() {

        //下拉刷新
        seach_result_swipeLayout.setOnRefreshListener {
            httpSeach(keyWord, fatherId, typeId, cityIds, capitalIds, modeIds, integratedSortId, 1)
        }

        //上滑加载
        seach_result_swipeLayout.setOnLoadMoreListener {
            httpSeach(keyWord, fatherId, typeId, cityIds, capitalIds, modeIds, integratedSortId, mPageNum)
        }

        head_search.setOnClickListener {
            super.initListener()
            startActivity<RedShopSeach>(IConstants.BACK_SEACH to keyWord)
        }

        head_search_mBack.setOnClickListener {
            //if (popupMenu1.isShowing) popupMenu1.dismiss()
            //if (popupMenu2.isShowing) popupMenu2.dismiss()
            //if (popupMenu3.isShowing) popupMenu3.dismiss()
            //if (popupMenu4.isShowing) popupMenu4.dismiss()
            this.finish()
        }

        mSeachToTop.setOnClickListener {
            swipe_target.smoothScrollToPosition(0)
            mSeachToTop.visibility = View.GONE
            seach_result_swipeLayout.isLoadMoreEnabled = false
        }

        search_food_type.setOnClickListener {
            if (!mIsInstantiationOne) {
                if (fatherId == 0) popupMenu1 = PopSeachSelect(this, 1, typeId) else popupMenu1 = PopSeachSelect(this, 1, fatherId)
            }
            popupMenu1.setOnSelectListener(object : PopSeachSelect.SelectCallBack {
                override fun onSelectCallBack(selectId: Int, selectFatherId: Int, selectName: String) {
                    typeId = selectId
                    fatherId = selectFatherId
                    search_food_type.text = selectName
                    mPageNum = 1
                    if (keyWord.isEmpty()) {
                        goToSeach()
                    } else {
                        keyWord = ""
                        head_search.setText("")
                        goToSeach()
                    }
                }
            })
            mIsInstantiationOne = true
            setShow(1)
        }

        search_add_area.setOnClickListener {
            if (!mIsInstantiationTwo) {
                popupMenu2 = PopSeachSelect(this, 2, -1)
            }
            //回调数据    传入搜索接口
            popupMenu2.setOnSelectListener(object : PopSeachSelect.SelectCallBack {
                override fun onSelectCallBack(selectId: Int, selectFatherId: Int, selectName: String) {
                    cityIds = selectId.toString()
                    search_add_area.text = selectName
                    mPageNum = 1
                    goToSeach()
                }
            })
            mIsInstantiationTwo = true
            setShow(2)
        }

        search_ranking.setOnClickListener {
            if (!mIsInstantiationThree) {
                popupMenu3 = PopSeachSelect(this, 3, -1)
            }
            popupMenu3.setOnSelectListener(object : PopSeachSelect.SelectCallBack {
                override fun onSelectCallBack(selectId: Int, selectFatherId: Int, selectName: String) {
                    integratedSortId = selectId
                    search_ranking.text = selectName
                    mPageNum = 1
                    goToSeach()
                }
            })
            mIsInstantiationThree = true
            setShow(3)
        }

        search_screning_conditon.setOnClickListener {
            if (!mIsInstantiationFour && popupMenu4 == null) {
                popupMenu4 = PopSeachCondition(this)
            }
            popupMenu4?.setOnSelectListener(selectListener = object : PopSeachCondition.SelectCallBack {
                override fun onSelectCallBack(selectMoney: StringBuffer, selectType: StringBuffer) {
                    capitalIds = selectMoney.toString()        //投资金额ID
                    modeIds = selectType.toString()         //加盟模式ID
                    mPageNum = 1
                    goToSeach()
                }
            })
            mIsInstantiationFour = true
            setShow(4)
        }

        swipe_target.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            //滚动状态改变时
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //滑到顶部了
                if (!recyclerView.canScrollVertically(-1)) {
                    mSeachToTop.visibility = View.GONE
                    if (!seach_result_swipeLayout.isLoadingMore) {
                        seach_result_swipeLayout.isRefreshEnabled = true
                        seach_result_swipeLayout.isLoadMoreEnabled = false
                    }
                    //没有滑动时 在最下面
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE && mLayoutManager.findLastVisibleItemPosition() + 1 == mAdapter.itemCount) {
                    mSeachToTop.visibility = View.VISIBLE
                    //如果下拉刷新没有刷新的话
                    if (!seach_result_swipeLayout.isRefreshing) {
                        if (mSeachResultList.isNotEmpty()) {
                            //是否有下一页
                            if (mHasNextPage) {
                                //是否可以请求接口
                                if (mCanHttpLoad) {
                                    seach_result_swipeLayout.isRefreshEnabled = false
//                                    seach_result_swipeLayout.isLoadMoreEnabled = true
                                    httpSeach(keyWord, fatherId, typeId, cityIds, capitalIds, modeIds, integratedSortId, mPageNum)
                                }
                            }
                        }
                    }
                } else {
                    mSeachToTop.visibility = View.VISIBLE
                    seach_result_swipeLayout.isRefreshEnabled = false
                    seach_result_swipeLayout.isLoadMoreEnabled = false
                }
            }
        })
    }

    private fun initAdapter() {
        //搜索结果 Adapter
        mLayoutManager = LinearLayoutManager(this)
        swipe_target.layoutManager = mLayoutManager
        val adapter = CommonAdapter(this, R.layout.red_shop_search_result_item, mSeachResultList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                Glide.with(this@RedShopSeachResult)
                        .load(data.logo).apply(RequestOptions()
                                .transform(GlideRoundTransformCenterCrop())
                                .placeholder(R.drawable.default_img_icon).error(R.drawable.default_img_icon)).into(getView(R.id.search_result_bigLogo))
                if (data.status == 1) {
                    val spanString = SpannableString("证\t\t\t${data.name}")
                    val drawable = resources.getDrawable(R.drawable.detail_icon_certification)
                    val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
                    spanString.setSpan(imageSpan, 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    val density = resources.displayMetrics.density
                    drawable.setBounds(0, (7 * density).toInt(), (14 * density).toInt(), (21 * density).toInt())
                    setSpannableStringText(R.id.search_result_name, spanString)
                } else {
                    setText(R.id.search_result_name, data.name)
                }
                if (data.capitalName.isNullOrEmpty()) {
                    setText(R.id.search_result_capitalName, "¥\t面议")
                } else {
                    setText(R.id.search_result_capitalName, "¥\t${data.capitalName}")
                }
                val storeNum = data.joinStoreNum + data.directStoreNum
                getView<LinearLayout>(R.id.search_result_joinStoreNum_linear).visibility =
                        if (storeNum > 0) {
                            if (storeNum >= 10000) {
                                val formart = java.text.DecimalFormat("0.0")
                                setText(R.id.search_result_joinStoreNum, "${formart.format(storeNum / 10000.0)}万")
                            } else setText(R.id.search_result_joinStoreNum, "$storeNum")
                            View.VISIBLE
                        } else View.GONE
                if (data.affiliateSupport == null) {
                    setTagFlowLayout(getView(R.id.seach_result_tagFliwLayout), ArrayList())
                } else {
                    setTagFlowLayout(getView(R.id.seach_result_tagFliwLayout), data.affiliateSupport as ArrayList<String>)
                }
                getView<LinearLayout>(R.id.search_linearlayout).setOnClickListener {
                    startActivity<ShopDetailActivity>(IConstants.BRANDID to data.id)
                }
            }
        })
        mAdapter = LoadMoreWrapper(adapter)
        //加载更多布局
        mAdapter.setLoadMoreView(mFootView)
        swipe_target.adapter = mAdapter
    }

    /**搜索接口  pageNum 必选
    参数：keyWord  搜索关键字  fatherId: 餐饮类型父级id  typeId: 餐饮类型id  cityIds: 爱加盟区域id
    capitalIds: 投资金额id（多个投资金额用英文逗号隔开如：1,2,3）
    modeIds: 加盟模式id （多个加盟模式用英文逗号隔开如：1,2,3）
    integratedSortId: 综合排序为：1 人气优先为：2 留言优先为：3 低价优先为：4 高价优先为：5
    pageNum: 页数（默认1页每页10条）
     **/
    private fun httpSeach(keyWord: String, fatherId: Int, typeId: Int, cityIds: String, capitalIds: String, modeIds: String, integratedSortId: Int, pageNum: Int) {
        mCanHttpLoad = false
        ApiUtils.getApi().let {
            if (!keyWord.isEmpty()) {
                it.join_search_brands(keyWord, cityIds, capitalIds, modeIds, integratedSortId, pageNum)
            } else {
                it.join_search_brands(fatherId, typeId, cityIds, capitalIds, modeIds, integratedSortId, pageNum)
            }
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    //刷新状态关闭
                    endLoad()
                    mCanHttpLoad = true
                    if (bean.code == 12000) {
                        if (pageNum == 1) {
                            mSeachResultList.clear()
                            mPageNum = 1
                        }
                        if (bean.data == null || bean.data?.data!!.isEmpty()) {
                            mHasNextPage = false
                            //如果没数据 就显示搜索不到页面
                            if (pageNum == 1) {
                                seach_result_nothing.visibility = View.VISIBLE
                                seach_result_swipeLayout.isRefreshEnabled = true
                                setFooterStatus(mFootView, 3)
                            } else {
                                setFooterStatus(mFootView, 2)
                            }
                        } else {
                            mHasNextPage = true
                            //如果有数据 就隐藏搜索不到页面
                            if (pageNum == 1) {
                                seach_result_nothing.visibility = View.GONE
                            }
                            mSeachResultList.addAll(bean.data?.data!!)
                            mPageNum++
                            setFooterStatus(mFootView, 1)
                            //如果不满一个屏幕 就隐藏
                            if (mSeachResultList.size < 10) {
                                setFooterStatus(mFootView, 3)
                            }
                        }
                        mAdapter.notifyDataSetChanged()
                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    //刷新状态关闭
                    endLoad()
                    mCanHttpLoad = true
                    seach_result_nothing.visibility = View.VISIBLE
                    seach_result_swipeLayout.isRefreshEnabled = true
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    private fun setTagFlowLayout(view: TagFlowLayout, maffiliateSupportList: ArrayList<String>) {
        view.adapter = object : TagAdapter<String>(maffiliateSupportList) {
            override fun getView(parent: FlowLayout?, position: Int, data: String?): View {
                return LayoutInflater.from(this@RedShopSeachResult).inflate(R.layout.item_seach_result_tab_flow_layout, seach_result_tagFliwLayout, false)
                        .apply {
                            findViewById<TextView>(R.id.Tag_seach_result).setText(data)
                        }
            }
        }
    }

    private fun goToSeach() {
        httpSeach(keyWord, fatherId, typeId, cityIds, capitalIds, modeIds, integratedSortId, mPageNum)
    }

    private fun endLoad() {
        seach_result_swipeLayout.isRefreshing = false
        seach_result_swipeLayout.isLoadingMore = false
        seach_result_swipeLayout.isRefreshEnabled = false
        seach_result_swipeLayout.isLoadMoreEnabled = false
    }

    /**
     * 获取静态数据
     * @param type 类型：1.首页banner8个icon 2.首页列表模块 3.列表筛选标题 4.综合排序 5.反馈类型
     */
    //筛选栏标题
//    private fun httpSeachTittle(version: String, type: Int) {
//        ApiUtils.getApi()
//                .getStaticInfo(version, type)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({ bean ->
//                    if (bean.code == 12000) {
//                        bean.data?.let {
//                            if (!it.systemStatic.isEmpty()) {
//                                it.setVersion()
//                                if (!mTittleList.isEmpty()) {
//                                    //清除缓存
//                                    mTittleList.clear()
//                                }
//                                //加入缓存
//                                mTittleList.addAll(it.systemStatic)
//                            }
//                        }
//                    }
//
//                }, {
//                    ToastUtil.showNetError()
//                }, {}, { addSubscription(it) })
//    }

    private fun setShow(position: Int) {
        //避免点击之后另外三个窗口还存在情况
        if (mIsInstantiationOne && position != 1) {
            search_food_type_bottom.visibility = View.GONE
            search_food_type.setTextColor(resources.getColor(R.color.color_999999))
            popupMenu1.dismiss()
        }
        if (mIsInstantiationTwo && position != 2) {
            search_add_area_bottom.visibility = View.GONE
            search_add_area.setTextColor(resources.getColor(R.color.color_999999))
            popupMenu2.dismiss()
        }
        if (mIsInstantiationThree && position != 3) {
            search_ranking_bottom.visibility = View.GONE
            search_ranking.setTextColor(resources.getColor(R.color.color_999999))
            popupMenu3.dismiss()
        }
        if (mIsInstantiationFour && position != 4) {
            //如果已经选择过 筛选条件变绿
            if (!capitalIds.isEmpty() || !modeIds.isEmpty()) {
                search_screning_conditon_bottom.visibility = View.GONE
                search_screning_conditon.setTextColor(resources.getColor(R.color.color_5ab1e1))
            } else {
                search_screning_conditon_bottom.visibility = View.GONE
                search_screning_conditon.setTextColor(resources.getColor(R.color.color_999999))
            }
            popupMenu4?.dismiss()
        }
        when (position) {
            1 -> {
                //实现连续点击
                if (!popupMenu1.isShowing) {
                    search_food_type.setTextColor(resources.getColor(R.color.color_5ab1e1))
                    search_food_type_bottom.visibility = View.VISIBLE
                    popupMenu1.showAsDropDown(search_result_view)
                } else {
                    search_food_type_bottom.visibility = View.GONE
                    search_food_type.setTextColor(resources.getColor(R.color.color_999999))
                    popupMenu1.dismiss()
                }
            }
            2 -> {
                if (!popupMenu2.isShowing) {
                    search_add_area_bottom.visibility = View.VISIBLE
                    search_add_area.setTextColor(resources.getColor(R.color.color_5ab1e1))
                    popupMenu2.showAsDropDown(search_result_view)
                } else {
                    search_add_area_bottom.visibility = View.GONE
                    search_add_area.setTextColor(resources.getColor(R.color.color_999999))
                    popupMenu2.dismiss()
                }
            }
            3 -> {
                if (!popupMenu3.isShowing) {
                    search_ranking_bottom.visibility = View.VISIBLE
                    search_ranking.setTextColor(resources.getColor(R.color.color_5ab1e1))
                    popupMenu3.showAsDropDown(search_result_view)
                } else {
                    search_ranking_bottom.visibility = View.GONE
                    search_ranking.setTextColor(resources.getColor(R.color.color_999999))
                    popupMenu3.dismiss()
                }
            }
            4 -> {
                if (popupMenu4?.isShowing != true) {
                    search_screning_conditon_bottom.visibility = View.VISIBLE
                    search_screning_conditon.setTextColor(resources.getColor(R.color.color_5ab1e1))
                    popupMenu4?.showAsDropDown(search_result_view)
                } else {
                    //如果已经选择过 筛选条件变绿
                    if (!capitalIds.isEmpty() || !modeIds.isEmpty()) {
                        search_screning_conditon_bottom.visibility = View.GONE
                        search_screning_conditon.setTextColor(resources.getColor(R.color.color_5ab1e1))
                    } else {
                        search_screning_conditon_bottom.visibility = View.GONE
                        search_screning_conditon.setTextColor(resources.getColor(R.color.color_999999))
                    }
                    popupMenu4?.dismiss()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        keyWord = intent!!.getStringExtra(SEACH_RESULT) ?: ""
        mPageNum = 1
        if (!keyWord.isEmpty()) {
            fatherId = 0
            typeId = 0
            mShowTittle = ""
        } else {
            fatherId = intent!!.getIntExtra(firstLevel, 0)
            typeId = intent!!.getIntExtra(secondLevel, 0)
            mShowTittle = intent!!.getStringExtra(THREE_LEVEL) ?: ""
        }
        if (mShowTittle.isEmpty()) {
            search_food_type.text = "餐饮类型"
            search_food_type.setTextColor(resources.getColor(R.color.color_999999))
        } else {
            search_food_type.text = mShowTittle
        }
        head_search.setText(keyWord)
        goToSeach()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        if (popupMenu1.isShowing) popupMenu1.dismiss()
//        if (popupMenu2.isShowing) popupMenu2.dismiss()
//        if (popupMenu3.isShowing) popupMenu3.dismiss()
//        if (popupMenu4.isShowing) popupMenu4.dismiss()
//    }
}
