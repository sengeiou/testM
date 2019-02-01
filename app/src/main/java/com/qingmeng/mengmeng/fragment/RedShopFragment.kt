package com.qingmeng.mengmeng.fragment

import android.annotation.SuppressLint
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.RelativeLayout
import com.qingmeng.mengmeng.BaseFragment
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.RedShopSeachResult
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.entity.RedShopBean
import com.qingmeng.mengmeng.entity.RedShopLeftBean
import com.qingmeng.mengmeng.utils.*
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_red_shop.*
import kotlinx.android.synthetic.main.layout_red_news_head.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.support.v4.startActivity

@SuppressLint("CheckResult")
class RedShopFragment : BaseFragment() {
    private lateinit var mLauyoutManger: LinearLayoutManager
    private lateinit var mGridLayoutManager: GridLayoutManager
    private lateinit var mLeftAdapter: CommonAdapter<RedShopLeftBean>
    private lateinit var mRightAdapterType: CommonAdapter<RedShopLeftBean>
    private lateinit var mRightAdapterHost: CommonAdapter<RedShopLeftBean>
    private var mLeftList = ArrayList<RedShopLeftBean>()
    private var mRightInListType = ArrayList<RedShopLeftBean>()
    private var mRightInListHost = ArrayList<RedShopLeftBean>()
    //    private var mRedShopLeftListBean = RedShopLeftListBean()
//    private var mRedShopRightListBean = RedShopLeftListBean()
    override fun getLayoutId(): Int = R.layout.fragment_red_shop

    override fun initObject() {
        super.initObject()
        mRedNewsTitle.setText(R.string.tab_name_red_shop)
        // 获得状态栏高度
        val statusBarHeight = getBarHeight(context!!)
        //给布局的高度重新设置一下 加上状态栏高度
        mRedNewsHead.layoutParams.height = mRedNewsHead.layoutParams.height + getBarHeight(context!!)
        mRedNewsTitle.setMarginExt(top = statusBarHeight + context!!.dp2px(60))
        mRedNewsBack.visibility = View.GONE

        initLeftAdapter()
        initRightAdapter()
    }

    override fun initData() {
        super.initData()
        getCacheData()

    }

    override fun initListener() {
        super.initListener()

    }

    private fun getCacheData() {
        Observable.create<RedShopBean> {
            val leftList = BoxUtils.getAllRedShop(0, 0)
            val RightInListType = BoxUtils.getAllRedShop(1, 2)
            val RightInListHost = BoxUtils.getAllRedShop(1, 1)
            if (!mLeftList.isEmpty()) {
                BoxUtils.removeAllRedShop(mLeftList)
                mLeftList.clear()
            }
            if (!mRightInListType.isEmpty()) {
                BoxUtils.removeAllRedShop(mRightInListType)
                mRightInListType.clear()
            }
            if (!mRightInListHost.isEmpty()) {
                BoxUtils.removeAllRedShop(mRightInListHost)
                mRightInListHost.clear()
            }
            mLeftList.addAll(leftList)
            mRightInListType.addAll(RightInListType)
            mRightInListHost.addAll(RightInListHost)
            var mRedShop = RedShopBean(ArrayList())
            if (!mLeftList.isEmpty()) {
                mRedShop = RedShopBean.fromString(mLeftList[0].id)
            }
            if (!mRightInListType.isEmpty()) {
                mRedShop = RedShopBean.fromString(mRightInListType[0].id)
            }
            if (!mRightInListHost.isEmpty()) {
                mRedShop = RedShopBean.fromString(mRightInListHost[0].id)
            }
            it.onNext(mRedShop)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (!mLeftList.isEmpty()) {
                        mLeftAdapter.notifyDataSetChanged()
                    }
                    if (!mRightInListType.isEmpty()) {
                        mRightAdapterType.notifyDataSetChanged()
                    }
                    mRightAdapterHost.notifyDataSetChanged()
                    httpLoad()
                    httpLoad2(1)
                }, {

                    httpLoad()
                    httpLoad2(1)
                }, {}, { addSubscription(it) })

    }

    private fun getClickCache(fahterId: Long) {
        Observable.create<RedShopBean> {
            val RightInListType = BoxUtils.getAllRedShop(fahterId, 2)
            val RightInListHost = BoxUtils.getAllRedShop(fahterId, 1)
            if (!mRightInListType.isEmpty()) {
                BoxUtils.removeAllRedShop(mRightInListType)
            }
            mRightInListType.clear()
            if (!mRightInListHost.isEmpty()) {
                BoxUtils.removeAllRedShop(mRightInListHost)
            }
            mRightInListHost.clear()
            mRightInListType.addAll(RightInListType)
            mRightInListHost.addAll(RightInListHost)
            var mRedShop = RedShopBean(ArrayList())
            if (!mRightInListType.isEmpty()) {
                mRedShop = RedShopBean.fromString(mRightInListType[0].id)
            }
            if (!mRightInListHost.isEmpty()) {
                mRedShop = RedShopBean.fromString(mRightInListHost[0].id)
            }
            it.onNext(mRedShop)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (!mRightInListType.isEmpty()) {
                        mRightAdapterType.notifyDataSetChanged()
                    }
                    mRightAdapterHost.notifyDataSetChanged()
                    httpLoad2(fahterId)
                }, {
                    httpLoad2(fahterId)
                }, {}, { addSubscription(it) })

    }

    //加载   左边适配
    private fun initLeftAdapter() {
        mLauyoutManger = LinearLayoutManager(context)
        red_shop_left_recyclerview.layoutManager = mLauyoutManger
        mLeftAdapter = CommonAdapter(context!!, R.layout.red_shop_left_item, mLeftList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                if (position == 0) {
                    data.checkState = true
                }
                if (data.checkState) {
                    getView<RelativeLayout>(R.id.red_shop_left_lineralayout).backgroundColor = resources.getColor(R.color.white)
                } else {
                    getView<RelativeLayout>(R.id.red_shop_left_lineralayout).backgroundColor = resources.getColor(R.color.page_background_f5)
                }
                setText(R.id.red_shop_left_textview, data.name)
            }

        }, onItemClick = { view, holder, position ->
            mLeftList.forEach {
                it.checkState = false
            }
            mLeftList[position].checkState = true
            getClickCache(mLeftList[position].id.toLong())
//            if (isNetWorkConnented(red_shop_left_recyclerview.context)) {
//                httpLoad2(mLeftList[position].id)
//            } else {
//                getRightCache(mLeftList[position].id.toLong())
//            }
            mLeftAdapter.notifyDataSetChanged()
        })
        red_shop_left_recyclerview.adapter = mLeftAdapter
    }
//获取网络连接状态
//    private fun isNetWorkConnented(context: Context?): Boolean {
//        if (context != null) {
//            var aa: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            var bb: NetworkInfo? = aa.activeNetworkInfo
//            if (bb != null) {
//                return bb.isAvailable
//            }
//        }
//        return false
//    }

    //右边适配
    private fun initRightAdapter() {
        //分类列表
        mGridLayoutManager = GridLayoutManager(context, 3)
        red_shop_right_recyclerview_type.layoutManager = mGridLayoutManager
        //recyclerView禁止滑动
        red_shop_right_recyclerview_type.isNestedScrollingEnabled = false
        mRightAdapterType = CommonAdapter(context!!, R.layout.red_shop_right_in_item, mRightInListType, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                if (mRightInListType.isNotEmpty()) {
                    red_shop_right_text_type.visibility = View.VISIBLE
                } else {
                    red_shop_right_text_type.visibility = View.GONE
                }
                setText(R.id.red_shop_right_inContent, data.name)
                GlideLoader.load(this@RedShopFragment, data.logo, getView(R.id.red_shop_right_inImageView))
            }
        }, onItemClick = { view, holder, position ->
            startActivity<RedShopSeachResult>()
        })
        red_shop_right_recyclerview_type.adapter = mRightAdapterType

        //热门品牌列表
        mGridLayoutManager = GridLayoutManager(context, 3)
        red_shop_right_recyclerview_host.layoutManager = mGridLayoutManager
        red_shop_right_recyclerview_host.isNestedScrollingEnabled = false
        mRightAdapterHost = CommonAdapter(context!!, R.layout.red_shop_right_in_item, mRightInListHost, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                if (mRightInListHost.isNotEmpty()) {
                    red_shop_right_text_host.visibility = View.VISIBLE
                } else {
                    red_shop_right_text_host.visibility = View.GONE
                }
                setText(R.id.red_shop_right_inContent, data.name)
                GlideLoader.load(this@RedShopFragment, data.logo, getView(R.id.red_shop_right_inImageView))
            }
        }, onItemClick = { view, holder, position ->
            startActivity<RedShopSeachResult>()
        })
        red_shop_right_recyclerview_host.adapter = mRightAdapterHost
    }

    @SuppressLint("CheckResult")
    private fun httpLoad(type: Long = 0, version: String? = null) {
        ApiUtils.getApi()
                .getRedShopRight(type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        if (code == 12000) {
                            data?.let {
                                if (!mLeftList.isEmpty()) {
                                    BoxUtils.removeAllRedShop(mLeftList)
                                    mLeftList.clear()
                                }
                                it.setVersion()
                                BoxUtils.saveAllRedShop(mLeftList)
                                mLeftList.addAll(it.type.typeList)//1级分类
                                mLeftAdapter.notifyDataSetChanged()
                            }
                        } else {
                            ToastUtil.showShort(it.msg)
                        }
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

//    private fun getNewData() {
//        httpLoad()
//        httpLoad2()
//    }

    private fun httpLoad2(type: Long, version: String? = null) {
        ApiUtils.getApi()
                .getRedShopRight(type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        if (code == 12000) {
                            data?.let {
                                if (!mRightInListType.isEmpty()) {
                                    BoxUtils.removeAllRedShop(mRightInListType)
                                    mRightInListType.clear()
                                }
                                if (!mRightInListHost.isEmpty()) {
                                    BoxUtils.removeAllRedShop(mRightInListHost)
                                    mRightInListHost.clear()
                                }
                                it.setVersion()
                                mRightInListType.addAll(it.type.typeList)
                                BoxUtils.saveAllRedShop(mRightInListType)
                                mRightInListHost.addAll(it.popularBrands.hotBrands!!)
                                BoxUtils.saveAllRedShop(mRightInListHost)
                                mRightAdapterType.notifyDataSetChanged()
                                mRightAdapterHost.notifyDataSetChanged()
                            }
                        } else {
                            ToastUtil.showShort(it.msg)
                        }
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }


//    private fun httpLoad(type: Int = 0, version: String = "") {
//        ApiUtils.getApi().let {
//            if (version != "") {
//                it.getRedShopRight(type)
//            } else {
//                it.getRedShopRight(type, version)
//            }
//        }
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({
//                    it.apply {
//                        if (code == 12000) {
//                            if (type == 0) {
//                                data?.let {
//                                    BoxUtils.removeAllRedShop(mRedShopLeftListBean)
//                                    mRedShopLeftListBean = it
//                                    BoxUtils.saveAllRedShop(redShopLeftListBean = mRedShopLeftListBean)
//                                    getNewData(it.type.typeList)
//                                }
//                                httpLoad(1)
//                            } else {
//                                data?.let {
//                                    BoxUtils.removeAllRedShop(mRedShopRightListBean)
//                                    mRedShopRightListBean = it
//                                    BoxUtils.saveAllRedShop(true, redShopLeftListBean = mRedShopRightListBean)
//                                    getNewData(it.type.typeList, it.popularBrands.hotBrands)
//                                }
//                            }
//                        } else if (code == 20000) {
//
//                        } else {
//                            ToastUtil.showShort(it.msg)
//                        }
//                    }
//                }, {
//                    ToastUtil.showNetError()
//                }, {}, { addSubscription(it) })
//    }
//
//    private fun getCacheData() {
//        Observable.create<RedShopLeftListBean> {
//            mRedShopLeftListBean = BoxUtils.getAllRedShop()
//            mRedShopRightListBean = BoxUtils.getAllRedShop(true)
////            var mRedShop = RedShopBean(ArrayList())
//            it.onNext(if (mRedShopLeftListBean.dataStr == "") {
//                mRedShopRightListBean
//            } else {
//                mRedShopLeftListBean
//            })
//        }.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    getNewData(mRedShopLeftListBean.type.typeList)
//                    getNewData(mRedShopLeftListBean.type.typeList, mRedShopLeftListBean.popularBrands.hotBrands)
//                    httpLoad(version = mRedShopLeftListBean.version)
//                }, {
//                    httpLoad()
//                }, {}, { addSubscription(it) })
//
//    }
//
//    private fun getNewData(leftList: List<RedShopLeftBean>) {
//        mLeftList.clear()
//        mLeftList.addAll(leftList)//1级分类
//    }
//
//    private fun getNewData(rightInListType: List<RedShopLeftBean>, rightInListHost: List<RedShopLeftBean>) {
//        mRightInListType.clear()
//        mRightInListHost.clear()
//        mRightInListType.addAll(rightInListType)//分类
//        mRightInListHost.addAll(rightInListHost)//热门品牌
//        mLeftAdapter.notifyDataSetChanged()
//        mRightAdapterType.notifyDataSetChanged()
//        mRightAdapterHost.notifyDataSetChanged()
//    }


}