package com.qingmeng.mengmeng.fragment

import android.annotation.SuppressLint
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import cn.bingoogolapple.bgabanner.BGABanner
import com.aspsine.swipetoloadlayout.OnLoadMoreListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.qingmeng.mengmeng.BaseFragment
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.HeadDetailsActivity
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.entity.Banner
import com.qingmeng.mengmeng.entity.NewsPagerList
import com.qingmeng.mengmeng.utils.*
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_head_newspaper.*
import kotlinx.android.synthetic.main.layout_red_news_head.*
import kotlinx.android.synthetic.main.news_bgabanner_item.*
import org.jetbrains.anko.support.v4.startActivity

@SuppressLint("CheckResult")
class NewsPaperFragment : BaseFragment(), OnLoadMoreListener, BGABanner.Delegate<ImageView, Banner>, BGABanner.Adapter<ImageView, String> {
    private lateinit var mLauyoutManger: LinearLayoutManager
    private lateinit var mAdapter: CommonAdapter<NewsPagerList>
    private lateinit var mImgList: ArrayList<Banner>
    private var mCanHttpLoad = true                          //是否请求接口
    private var mHasNextPage = true                          //是否请求下一页
    private var mPageNum: Int = 1                            //接口请求页数
    private var isRferesh = false                              //加载更多
    private var mList = ArrayList<NewsPagerList>()       //接口请求数据
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
        httpLoad(1)
        httpBannerLoad("1")
    }

    override fun initListener() {
        super.initListener()

    }

    private fun initAdapter() {
        mLauyoutManger = LinearLayoutManager(context)
        news_pager_RecyclerView.layoutManager = mLauyoutManger
        mAdapter = CommonAdapter(context!!, R.layout.news_paper_item, mList, holderConvert = { holder, data, position, payloads ->
            holder.apply {
                GlideLoader.load(this@NewsPaperFragment, data.banner, getView(R.id.news_pager_icon))
                setText(R.id.news_pager_tittle, data.title)
                setText(R.id.news_pager_content, data.content)
                setText(R.id.news_pager_date, data.formatTime)
            }
        }, onItemClick = { view, holder, position ->
            startActivity<HeadDetailsActivity>()
        })
        news_pager_RecyclerView.adapter = mAdapter
    }

    override fun initData() {
        super.initData()
        getCacheData()
    }

    private fun getCacheData() {
        initView()
    }

    private fun initView() {
        //news_swipeLayout.setOnLoadMoreListener(this)

    }

    override fun onLoadMore() {
        isRferesh = false
    }

    private fun getNewData() {
        isRferesh = true
        httpLoad(1)
        httpBannerLoad("")
    }

    //头报列表文章
    private fun httpLoad(pageNum: Int) {
        mCanHttpLoad = false
        ApiUtils.getApi().getNewsHeadList(pageNum)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    //关闭刷新 未写
                    //setRefreshAsFalse()
                    it.apply {
                        if (code == 12000) {
                            //如果页数是1 ，清空内容重新加载
                            if (pageNum == 1) {
                                //清空已选择集合
                                mList.clear()
                                mPageNum = 1
                            }
                            //请求后判断数据
                            if (data == null || data?.data!!.isEmpty()) {
                                mHasNextPage = false
                                if (pageNum == 1) {
                                    //拿数据库数据  未写
                                }
                            } else {
                                mHasNextPage = true
//                                if(pageNum==1){
//
//                                }
                                data?.let {
                                    mList.addAll(it.data)
                                    //数据库存入缓存 未写

                                }
                                mPageNum++
                            }
                            //适配器更新数据 未写
                            mAdapter.notifyDataSetChanged()
                        }
                    }
                }, {
                    //上划加载 打开 未写
                    mCanHttpLoad = true
                })
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
                                //数据库存入缓存
                                BoxUtils.saveBanners(mImgList)
                                setBanner()
                            }
                        }
                    } else if (bean.code != 12000) {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, {
                    addSubscription(it)
                })
    }

    //Banner 点击事件    跳转链接
    override fun onBannerItemClick(banner: BGABanner?, itemView: ImageView?, model: Banner?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //Banner 加载图片
    override fun fillBannerItem(banner: BGABanner?, itemView: ImageView, model: String?, position: Int) {
        model?.let {
            Glide.with(this).load(it).apply(RequestOptions()
                    .placeholder(R.drawable.default_img_banner).error(R.drawable.default_img_banner)
                    .centerCrop()).into(itemView)
        }
    }

    private fun setBanner() {
        news_pager_bgaBanner.setAdapter(this) //必须设置此适配器，否则方法不会调用接口来填充图片
        news_pager_bgaBanner.setDelegate(this) //设置点击事件，重写点击回调方法
        news_pager_bgaBanner.setData(mImgList, null)
        if (mImgList.size > 1) {
            news_pager_bgaBanner.setAutoPlayAble(true)
        } else {
            news_pager_bgaBanner.setAutoPlayAble(false)
        }
    }
}