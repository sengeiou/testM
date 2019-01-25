package com.qingmeng.mengmeng.activity

/**
 * Created by fyf on 2019/1/8
 * 搜索结果页
 */
import android.annotation.SuppressLint
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.entity.SearchDto
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.view.dialog.PopSeachCondition
import com.qingmeng.mengmeng.view.dialog.PopSeachSelect
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_red_shop_seach_result.*
import kotlinx.android.synthetic.main.layout_head_seach.*
import org.jetbrains.anko.startActivity
import java.util.*

@SuppressLint("CheckResult")
class RedShopSeachResult : BaseActivity() {
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
    private var keyWord: String = "小吃"            //搜索关键字
    private var mPageNum: Int = 1              //页数1页10条
    //选填
    private var fatherId: Int = 0               //餐饮类型父ID
    private var typeId: Int = 0                 //餐饮类型ID
    private var cityIds: String = ""            //爱加盟区域ID
    private var capitalIds: String = ""         //投资金额ID
    private var modeIds: String = ""            //加盟模式ID
    private var integratedSortId: Int = 0       //综合排序（12345）


    private var mCanHttpLoad = true                //是否可以请求接口
    private var mHasNextPage = true                //是否有下一月
    override fun getLayoutId(): Int = R.layout.activity_red_shop_seach_result

    override fun initObject() {
        super.initObject()
        initAdapter()
//        setData()
        httpSeach(keyWord, typeId, fatherId, cityIds, capitalIds, modeIds, integratedSortId, mPageNum)
    }

    override fun initData() {
        super.initData()

    }

    private fun initAdapter() {
        //搜索结果 Adapter
        mLauyoutManger = LinearLayoutManager(this)
        search_result_recylerview.layoutManager = mLauyoutManger
        mAdapter = CommonAdapter(this, R.layout.red_shop_search_result_item, mSeachResultList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                setText(R.id.search_result_name, "          " + data.name + "我是填充测试文本我是填充测试文本我是填充测试文本我是填充测试文本")
                setText(R.id.search_result_capitalName, data.capitalName)
                setText(R.id.search_result_joinStoreNum, data.joinStoreNum.toString())
                setText(R.id.search_result_directStoreNum, data.directStoreNum.toString())
                getView<LinearLayout>(R.id.search_linearlayout).setOnClickListener {
                    ToastUtil.showShort("我是搜索之后的详情页")
                }
            }

        }, onItemClick = { view, holder, position ->

        })
        search_result_recylerview.adapter = mAdapter
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
//                    setRefreshAsFalse()
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
                            }
                        } else {
                            mHasNextPage = true
                            //如果有数据 就隐藏搜索不到页面
                            if (pageNum == 1) {
                                seach_result_nothing.visibility = View.GONE
                            }
                            bean.data?.let {
                                if (!it.data.isEmpty()) {
                                    if (!mSeachResultList.isEmpty()) {
                                        mSeachResultList.clear()
                                    }
                                    mSeachResultList.addAll(it.data)
                                }
                            }
                            mPageNum++
                        }
                        mAdapter.notifyDataSetChanged()
                    } else if (bean.code != 12000) {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    //刷新状态关闭
                    // setRefreshAsFalse()
                    mCanHttpLoad = true
                    seach_result_nothing.visibility = View.VISIBLE
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
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
//                    } else if (bean.code != 12000) {
//                        ToastUtil.showShort(bean.msg)
//                    }
//
//                }, {
//                    ToastUtil.showNetError()
//                }, {}, { addSubscription(it) })
//    }


    override fun initListener() {
        super.initListener()
        head_search.setOnClickListener { startActivity<RedShopSeach>() }
        head_search_mBack.setOnClickListener { this.finish() }
        search_food_type.setOnClickListener {
            if (!mIsInstantiationOne) {
                popupMenu1 = PopSeachSelect(this, 1)
            }
            popupMenu1.setOnSelectListener(object : PopSeachSelect.SelectCallBack {
                override fun onSelectCallBack(selectId: Int) {
                    ToastUtil.showShort("$selectId")
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
                override fun onSelectCallBack(selectId: Int) {
                    ToastUtil.showShort("$selectId")
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
                override fun onSelectCallBack(selectId: Int) {
                    ToastUtil.showShort("$selectId")
                }
            })
            mIsInstantiationThree = true
            setShow(3)
        }
        search_screning_conditon.setOnClickListener {
            if (!mIsInstantiationFour) {
                popupMenu4 = PopSeachCondition(this)
            }
            mIsInstantiationFour = true
            setShow(4)
        }
    }

    private fun setShow(position: Int) {
        when (position) {
            1 -> {
                //避免点击之后另外三个窗口还存在情况
                if (mIsInstantiationTwo) {
                    popupMenu2.dismiss()
                }
                if (mIsInstantiationThree) {
                    popupMenu3.dismiss()
                }
                if (mIsInstantiationFour) {
                    popupMenu4.dismiss()
                }
                //实现连续点击
                if (!popupMenu1.isShowing) {
                    popupMenu1.showAsDropDown(search_result_view)
                } else {
                    popupMenu1.dismiss()
                }
            }
            2 -> {
                if (mIsInstantiationOne) {
                    popupMenu1.dismiss()
                }
                if (mIsInstantiationThree) {
                    popupMenu3.dismiss()
                }
                if (mIsInstantiationFour) {
                    popupMenu4.dismiss()
                }
                if (!popupMenu2.isShowing) {
                    popupMenu2.showAsDropDown(search_result_view)
                } else {
                    popupMenu2.dismiss()
                }
            }
            3 -> {
                if (mIsInstantiationOne) {
                    popupMenu1.dismiss()
                }
                if (mIsInstantiationTwo) {
                    popupMenu2.dismiss()
                }
                if (mIsInstantiationFour) {
                    popupMenu4.dismiss()
                }
                if (!popupMenu3.isShowing) {
                    popupMenu3.showAsDropDown(search_result_view)
                } else {
                    popupMenu3.dismiss()
                }
            }
            4 -> {
                if (mIsInstantiationOne) {
                    popupMenu1.dismiss()
                }
                if (mIsInstantiationTwo) {
                    popupMenu2.dismiss()
                }
                if (mIsInstantiationThree) {
                    popupMenu3.dismiss()
                }
                if (!popupMenu4.isShowing) {
                    popupMenu4.showAsDropDown(search_result_view)
                } else {
                    popupMenu4.dismiss()
                }
            }
        }
    }

//    private fun setData() {
//        mList.clear()
//        for (i in 0 until 20) {
//            mList.add("")
//        }
//        mAdapter.notifyDataSetChanged()
//    }
}
