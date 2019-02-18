package com.qingmeng.mengmeng.activity

/**
 * Created by fyf on 2019/1/8
 * 搜索结果页
 */
import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import com.aspsine.swipetoloadlayout.OnLoadMoreListener
import com.aspsine.swipetoloadlayout.OnRefreshListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.constant.IConstants.REDSHOPID
import com.qingmeng.mengmeng.constant.IConstants.REDSHOPNAME
import com.qingmeng.mengmeng.constant.IConstants.SEACH_RESULT
import com.qingmeng.mengmeng.constant.IConstants.firstLevel
import com.qingmeng.mengmeng.entity.SearchDto
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
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
class RedShopSeachResult : BaseActivity(), OnLoadMoreListener, OnRefreshListener {


    private lateinit var mAdapter: CommonAdapter<SearchDto>
    private lateinit var mLauyoutManger: LinearLayoutManager
    private lateinit var popupMenu1: PopSeachSelect
    private lateinit var popupMenu2: PopSeachSelect
    private lateinit var popupMenu3: PopSeachSelect
    private lateinit var popupMenu4: PopSeachCondition
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
    private var mTypeId = String()               //餐饮类型返回参数
    private var mRankingId = String()            //综合排序返回参数
    private var isRefreshing = false
    private var isLoading = false
    override fun getLayoutId(): Int = R.layout.activity_red_shop_seach_result

    override fun initObject() {
        super.initObject()
        initAdapter()
//        setData()
        //传入
        fatherId = intent.getIntExtra(firstLevel, 0)
        typeId = intent.getIntExtra(REDSHOPID, 1)
        keyWord = intent.getStringExtra(SEACH_RESULT) ?: ""


        head_search.setText(keyWord)
        head_search.setSelection(head_search.text.toString().length)
        goToSeach()

    }

    private fun goToSeach() {
        seach_result_swipeLayout.isRefreshing = true
        httpSeach(keyWord, fatherId, typeId, cityIds, capitalIds, modeIds, integratedSortId, mPageNum)
    }

    override fun initData() {
        super.initData()

    }

    private fun initAdapter() {
        //搜索结果 Adapter
        mLauyoutManger = LinearLayoutManager(this)
        swipe_target.layoutManager = mLauyoutManger
        mAdapter = CommonAdapter(this, R.layout.red_shop_search_result_item, mSeachResultList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                Glide.with(this@RedShopSeachResult).load(data.logo).apply(RequestOptions.bitmapTransform(RoundedCorners(10))
                        .placeholder(R.drawable.default_img_icon).error(R.drawable.default_img_icon)).into(getView(R.id.search_result_bigLogo))
                val spanString = SpannableString("证\t${data.name}")
                val drawable = resources.getDrawable(R.drawable.detail_icon_certification)
                val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
                spanString.setSpan(imageSpan, 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                val density = resources.displayMetrics.density
                drawable.setBounds(0, (7 * density).toInt(), (14 * density).toInt(), (21 * density).toInt())
                setSpannableStringText(R.id.search_result_name, spanString)
                setText(R.id.search_result_capitalName, "¥\t${data.capitalName}")
                if (data.joinStoreNum > 9999) {

                } else {
                    setText(R.id.search_result_joinStoreNum, data.joinStoreNum.toString())
                }
//                if (data.joinStoreNum > 9999) {
//
//                } else {
//                    setText(R.id.search_result_directStoreNum, data.directStoreNum.toString())
//                }
                setTagFlowLayout(getView(R.id.seach_result_tagFliwLayout), data.affiliateSupport as ArrayList<String>)
                getView<LinearLayout>(R.id.search_linearlayout).setOnClickListener {
                    startActivity<ShopDetailActivity>(IConstants.BRANDID to data.id)
                }
            }
        }, onItemClick = { view, holder, position ->

        })
        swipe_target.adapter = mAdapter
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

    /**搜索接口  Keyword、pageNum 必选
    参数：keyWord  搜索关键字  fatherId: 餐饮类型父级id  typeId: 餐饮类型id  cityIds: 爱加盟区域id
    capitalIds: 投资金额id（多个投资金额用英文逗号隔开如：1,2,3）
    modeIds: 加盟模式id （多个加盟模式用英文逗号隔开如：1,2,3）
    integratedSortId: 综合排序为：1 人气优先为：2 留言优先为：3 低价优先为：4 高价优先为：5
    pageNum: 页数（默认1页每页10条）
     **/
    private fun httpSeach(keyWord: String, fatherId: Int, typeId: Int, cityIds: String, capitalIds: String, modeIds: String, integratedSortId: Int, pageNum: Int) {
        mCanHttpLoad = false
        ApiUtils.getApi().let {
            if (fatherId == 0) {
                it.join_search_brands(keyWord, typeId, cityIds, capitalIds, modeIds, integratedSortId, pageNum)
            } else {
                it.join_search_brands(keyWord, fatherId, typeId, cityIds, capitalIds, modeIds, integratedSortId, pageNum)
            }
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    //刷新状态关闭
                    endLoad()
                    mCanHttpLoad = true
                    if (bean.code == 12000) {
                        if (bean.data == null || bean.data?.data!!.isEmpty()) {
                            mHasNextPage = false
                            //如果没数据 就显示搜索不到页面
                            if (pageNum == 1) {
                                seach_result_nothing.visibility = View.VISIBLE
                            }
                        } else {
                            mHasNextPage = true
                            //如果有数据 就隐藏搜索不到页面
                            if (pageNum == 1) {
                                seach_result_nothing.visibility = View.GONE
                            }
                            bean.data?.let {
                                if (!it.data.isEmpty()) {
                                    if (pageNum == 1) {
                                        mSeachResultList.clear()
                                        mPageNum = 1
                                        if (!mSeachResultList.isEmpty()) {
                                            mSeachResultList.clear()
                                        }
                                    }
                                    mSeachResultList.addAll(it.data)
                                    seach_result_swipeLayout.isLoadMoreEnabled = false
                                    mPageNum++
                                }
                            }
                        }
                        mAdapter.notifyDataSetChanged()
                    }
                }, {
                    //刷新状态关闭
                    endLoad()
                    mCanHttpLoad = true
                    seach_result_nothing.visibility = View.VISIBLE
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    private fun endLoad() {
        if (seach_result_swipeLayout.isRefreshing) {
            seach_result_swipeLayout.endRefresh()
        }
        if (seach_result_swipeLayout.isLoadingMore) {
            seach_result_swipeLayout.endLoadMore()
        }
    }

    override fun onRefresh() {
        httpSeach(keyWord, fatherId, typeId, cityIds, capitalIds, modeIds, integratedSortId, 1)
    }

    override fun onLoadMore() {
        //getdata 数据
        seach_result_swipeLayout.isRefreshing = false
        httpSeach(keyWord, fatherId, typeId, cityIds, capitalIds, modeIds, integratedSortId, mPageNum)
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

    override fun initListener() {
        super.initListener()
//        seach_result_allScreen.setOnTouchListener(object : View.OnTouchListener {
//            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                if (null != this@RedShopSeachResult.getCurrentFocus()) {
//                    //点击取消EditText的焦点
//                    seach_result_allScreen.setFocusable(true)
//                    seach_result_allScreen.setFocusableInTouchMode(true)
//                    seach_result_allScreen.requestFocus()
//                    /** * 点击空白位置 隐藏软键盘  */
//                    val mInputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                    return mInputMethodManager!!.hideSoftInputFromWindow(this@RedShopSeachResult.getCurrentFocus()!!.getWindowToken(), 0)
//                }
//                return false
//            }
//        })
        //软键盘点击完成后触发
        head_search.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event != null && KeyEvent.KEYCODE_ENTER === event!!.getKeyCode() && KeyEvent.ACTION_DOWN === event!!.getAction()) {
                    keyWord = head_search.text.toString()
                    goToSeach()
                    val mInputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    return mInputMethodManager!!.hideSoftInputFromWindow(this@RedShopSeachResult.getCurrentFocus()!!.getWindowToken(), 0)
                }
                return false
            }
        })
        head_search_mBack.setOnClickListener { this.finish() }
        search_food_type.setOnClickListener {
            if (!mIsInstantiationOne) {
                popupMenu1 = PopSeachSelect(this, 1)
            }
            popupMenu1.setOnSelectListener(object : PopSeachSelect.SelectCallBack {
                override fun onSelectCallBack(selectId: Int, selectName: String) {
                    typeId = selectId
                    mTypeId = selectId.toString()
                    search_food_type.text = selectName
                    goToSeach()
                }
            })
            mIsInstantiationOne = true
            setShow(1)
        }
        search_add_area.setOnClickListener {
            if (!mIsInstantiationTwo) {
                popupMenu2 = PopSeachSelect(this, 2)
            }
            //回调数据    传入搜索接口
            popupMenu2.setOnSelectListener(object : PopSeachSelect.SelectCallBack {
                override fun onSelectCallBack(selectId: Int, selectName: String) {
                    cityIds = selectId.toString()
                    search_add_area.text = selectName
                    goToSeach()
                }
            })
            mIsInstantiationTwo = true
            setShow(2)
        }
        search_ranking.setOnClickListener {
            if (!mIsInstantiationThree) {
                popupMenu3 = PopSeachSelect(this, 3)
            }
            popupMenu3.setOnSelectListener(object : PopSeachSelect.SelectCallBack {
                override fun onSelectCallBack(selectId: Int, selectName: String) {
                    integratedSortId = selectId
                    mRankingId = selectId.toString()
                    search_ranking.text = selectName
                    goToSeach()
                }
            })
            mIsInstantiationThree = true
            setShow(3)
        }
        search_screning_conditon.setOnClickListener {
            if (!mIsInstantiationFour) {
                popupMenu4 = PopSeachCondition(this)
            }
            popupMenu4.setOnSelectListener(selectListener = object : PopSeachCondition.SelectCallBack {
                override fun onSelectCallBack(selectMoney: StringBuffer, selectType: StringBuffer) {
                    capitalIds = selectMoney.toString()        //投资金额ID
                    modeIds = selectType.toString()         //加盟模式ID
                    goToSeach()
                }
            })
            mIsInstantiationFour = true
            setShow(4)
        }
        swipe_target.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            internal var lastVisibleItemPosition: Int = 0
            //滚动状态改变时
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //没有滑动时 在最下面
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItemPosition + 1 == mAdapter.itemCount) {
                    seach_result_swipeLayout.isLoadMoreEnabled = true
                }
            }

            //滑动
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lastVisibleItemPosition = mLauyoutManger.findLastVisibleItemPosition()
            }
        })
        seach_result_swipeLayout.setOnRefreshListener(this)
        seach_result_swipeLayout.setOnLoadMoreListener(this)
    }

    private fun setShow(position: Int) {
        when (position) {
            1 -> {
                //避免点击之后另外三个窗口还存在情况
                if (mIsInstantiationTwo) {
                    if (!cityIds.isEmpty()) {
                        search_add_area_bottom.visibility = View.GONE
                        search_add_area.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu2.dismiss()
                    } else {
                        search_add_area_bottom.visibility = View.GONE
                        search_add_area.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu2.dismiss()
                    }
                }
                if (mIsInstantiationThree) {
                    if (!mRankingId.isEmpty()) {
                        search_ranking_bottom.visibility = View.GONE
                        search_ranking.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu3.dismiss()
                    } else {
                        search_ranking_bottom.visibility = View.GONE
                        search_ranking.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu3.dismiss()
                    }
                }
                if (mIsInstantiationFour) {
                    //如果已经选择过 筛选条件变绿
                    if (!capitalIds.isEmpty() || !modeIds.isEmpty()) {
                        search_screning_conditon_bottom.visibility = View.GONE
                        search_screning_conditon.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu4.dismiss()
                    } else {
                        search_screning_conditon_bottom.visibility = View.GONE
                        search_screning_conditon.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu4.dismiss()
                    }
                }
                //实现连续点击
                if (!popupMenu1.isShowing) {
                    search_food_type.setTextColor(resources.getColor(R.color.color_5ab1e1))
                    search_food_type_bottom.visibility = View.VISIBLE
                    popupMenu1.showAsDropDown(search_result_view)
                } else {
                    if (!mTypeId.isEmpty()) {
                        search_food_type_bottom.visibility = View.GONE
                        search_food_type.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu1.dismiss()
                    } else {
                        search_food_type_bottom.visibility = View.GONE
                        search_food_type.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu1.dismiss()
                    }
                }
            }
            2 -> {
                if (mIsInstantiationOne) {
                    if (!mTypeId.isEmpty()) {
                        search_food_type_bottom.visibility = View.GONE
                        search_food_type.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu1.dismiss()
                    } else {
                        search_food_type_bottom.visibility = View.GONE
                        search_food_type.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu1.dismiss()
                    }
                }
                if (mIsInstantiationThree) {
                    if (!mRankingId.isEmpty()) {
                        search_ranking_bottom.visibility = View.GONE
                        search_ranking.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu3.dismiss()
                    } else {
                        search_ranking_bottom.visibility = View.GONE
                        search_ranking.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu3.dismiss()
                    }
                }
                if (mIsInstantiationFour) {
                    //如果已经选择过 筛选条件变绿
                    if (!capitalIds.isEmpty() || !modeIds.isEmpty()) {
                        search_screning_conditon_bottom.visibility = View.GONE
                        search_screning_conditon.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu4.dismiss()
                    } else {
                        search_screning_conditon_bottom.visibility = View.GONE
                        search_screning_conditon.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu4.dismiss()
                    }
                }
                if (!popupMenu2.isShowing) {
                    search_add_area_bottom.visibility = View.VISIBLE
                    search_add_area.setTextColor(resources.getColor(R.color.color_5ab1e1))
                    popupMenu2.showAsDropDown(search_result_view)
                } else {
                    if (!cityIds.isEmpty()) {
                        search_add_area_bottom.visibility = View.GONE
                        search_add_area.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu2.dismiss()
                    } else {
                        search_add_area_bottom.visibility = View.GONE
                        search_add_area.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu2.dismiss()
                    }
                }
            }
            3 -> {
                if (mIsInstantiationOne) {
                    if (!mTypeId.isEmpty()) {
                        search_food_type_bottom.visibility = View.GONE
                        search_food_type.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu1.dismiss()
                    } else {
                        search_food_type_bottom.visibility = View.GONE
                        search_food_type.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu1.dismiss()
                    }
                }
                if (mIsInstantiationTwo) {
                    if (!cityIds.isEmpty()) {
                        search_add_area_bottom.visibility = View.GONE
                        search_add_area.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu2.dismiss()
                    } else {
                        search_add_area_bottom.visibility = View.GONE
                        search_add_area.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu2.dismiss()
                    }
                }
                if (mIsInstantiationFour) {
                    //如果已经选择过 筛选条件变绿
                    if (!capitalIds.isEmpty() || !modeIds.isEmpty()) {
                        search_screning_conditon_bottom.visibility = View.GONE
                        search_screning_conditon.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu4.dismiss()
                    } else {
                        search_screning_conditon_bottom.visibility = View.GONE
                        search_screning_conditon.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu4.dismiss()
                    }
                }
                if (!popupMenu3.isShowing) {
                    search_ranking_bottom.visibility = View.VISIBLE
                    search_ranking.setTextColor(resources.getColor(R.color.color_5ab1e1))
                    popupMenu3.showAsDropDown(search_result_view)
                } else {
                    if (!mRankingId.isEmpty()) {
                        search_ranking_bottom.visibility = View.GONE
                        search_ranking.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu3.dismiss()
                    } else {
                        search_ranking_bottom.visibility = View.GONE
                        search_ranking.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu3.dismiss()
                    }
                }
            }
            4 -> {
                if (mIsInstantiationOne) {
                    if (!mTypeId.isEmpty()) {
                        search_food_type_bottom.visibility = View.GONE
                        search_food_type.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu1.dismiss()
                    } else {
                        search_food_type_bottom.visibility = View.GONE
                        search_food_type.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu1.dismiss()
                    }
                }
                if (mIsInstantiationTwo) {
                    if (!cityIds.isEmpty()) {
                        search_add_area_bottom.visibility = View.GONE
                        search_add_area.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu2.dismiss()
                    } else {
                        search_add_area_bottom.visibility = View.GONE
                        search_add_area.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu2.dismiss()
                    }
                }
                if (mIsInstantiationThree) {
                    if (!mRankingId.isEmpty()) {
                        search_ranking_bottom.visibility = View.GONE
                        search_ranking.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu3.dismiss()
                    } else {
                        search_ranking_bottom.visibility = View.GONE
                        search_ranking.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu3.dismiss()
                    }
                }
                if (!popupMenu4.isShowing) {
                    search_screning_conditon_bottom.visibility = View.VISIBLE
                    search_screning_conditon.setTextColor(resources.getColor(R.color.color_5ab1e1))
                    popupMenu4.showAsDropDown(search_result_view)
                } else {
                    //如果已经选择过 筛选条件变绿
                    if (!capitalIds.isEmpty() || !modeIds.isEmpty()) {
                        search_screning_conditon_bottom.visibility = View.GONE
                        search_screning_conditon.setTextColor(resources.getColor(R.color.color_5ab1e1))
                        popupMenu4.dismiss()
                    } else {
                        search_screning_conditon_bottom.visibility = View.GONE
                        search_screning_conditon.setTextColor(resources.getColor(R.color.color_999999))
                        popupMenu4.dismiss()
                    }
                }
            }
        }
    }

}
