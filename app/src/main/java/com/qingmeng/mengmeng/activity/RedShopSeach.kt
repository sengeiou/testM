package com.qingmeng.mengmeng.activity

/**
 * Created by mingyue
 * Date: 2019/1/16
 * mail: 153705849@qq.com
 * describe: 搜索
 */
import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.entity.HotSearchesList
import com.qingmeng.mengmeng.entity.SearchHistoryList
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.BoxUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.view.flowlayout.FlowLayout
import com.qingmeng.mengmeng.view.flowlayout.TagAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_red_shop_seach.*
import kotlinx.android.synthetic.main.layout_head_seach2.*
import org.jetbrains.anko.startActivity


class RedShopSeach : BaseActivity() {
    //适配器数据源
    private var mList = ArrayList<HotSearchesList>()
    //历史搜索数据
    private var mHistorySearch = ArrayList<SearchHistoryList>()
    private lateinit var mAdapter: CommonAdapter<String>
    var hotidSearchData = BoxUtils.getIdHotSearch(1)
    var historySearchData = BoxUtils.getIdSearch(1)
    var shistory = ArrayList<String>()
    //数据库是否有版本号
    var bhotversion: Boolean = false
    //搜索历史是否存在
    var bhistorySearch: Boolean = false
    //版本号变量
    var mversion = ""

    //选择的热门搜索
    var mbackHotSearch = ""
    //选择的历史搜索
    var mbackHistorySearch = ""

    override fun getLayoutId(): Int = R.layout.activity_red_shop_seach

    override fun initObject() {
        super.initObject()
        //头部  返回 隐藏  取消 显示
        head_search_mBack2.visibility = View.GONE
        head_search_mMenu2.visibility = View.VISIBLE
        initAdapter()
    }

    private fun initAdapter() {

    }

    override fun initListener() {
        super.initListener()
        //点击页面其他地方取消EditText的焦点并且隐藏软键盘
        ll_red_shop_seach.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (null != this@RedShopSeach.getCurrentFocus()) {
                    //点击取消EditText的焦点
                    ll_red_shop_seach.setFocusable(true);
                    ll_red_shop_seach.setFocusableInTouchMode(true);
                    ll_red_shop_seach.requestFocus();
                    /** * 点击空白位置 隐藏软键盘  */
                    val mInputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    return mInputMethodManager!!.hideSoftInputFromWindow(this@RedShopSeach.getCurrentFocus()!!.getWindowToken(), 0)
                }
                return false
            }
        })

        head_search_mMenu2.setOnClickListener {
            this.finish()
        }
        head_search2.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                // et.getCompoundDrawables()得到一个长度为4的数组，分别表示左右上下四张图片
                val drawable = head_search2.getCompoundDrawables()[2]
                //如果右边没有图片，不再处理
                if (drawable == null)
                    return false
                //如果不是按下事件，不再处理
                if (event?.getAction() != MotionEvent.ACTION_UP)
                    return false
                if (event?.getX() > head_search2.getWidth()
                        - head_search2.getPaddingRight()
                        - drawable.getIntrinsicWidth()) {
                    head_search2.setText("")
                }
                return false
            }

        })
        head_search2.setOnEditorActionListener(object : TextView.OnEditorActionListener {

            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                //当actionId == XX_SEND 或者 XX_DONE时都触发
                //或者event.getKeyCode == ENTER 且 event.getAction == ACTION_DOWN时也触发
                //注意，这是一定要判断event != null。因为在某些输入法上会返回null。
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event != null && KeyEvent.KEYCODE_ENTER === event!!.getKeyCode() && KeyEvent.ACTION_DOWN === event!!.getAction()) {
                    //处理事件
                    if (!head_search2.text.toString().trim().isEmpty()) {
                        var search = SearchHistoryList().apply { name = head_search2.text.toString().trim() }
                        mHistorySearch.add(search)
                        //检查重复
                        var mHistoryrepeat = BoxUtils.getnameSearch(search.name)
                        if (mHistoryrepeat.isEmpty()) {
                            //空
                            //直接保存
                            BoxUtils.saveSearch(mHistorySearch[0])
                        } else {
                            //重复
                            //删除记录，保存新纪录
                            BoxUtils.removeSearchs(mHistoryrepeat)
                            BoxUtils.saveSearch(mHistorySearch[0])
                        }
                        startActivity<RedShopSeachResult>(IConstants.SEACH_RESULT to search.name)
                        finish()
                    } else {
                        var search = SearchHistoryList().apply { name = head_search2.text.toString().trim() }
                        startActivity<RedShopSeachResult>(IConstants.SEACH_RESULT to search.name)
                        finish()
                    }
                }
                return false
            }


        })
        //删除按钮
        search_cancel_search.setOnClickListener {
            BoxUtils.removeSearchs(historySearchData)
            shistory.clear()
            setSearchHistory(shistory)
            adapter.notifyDataChanged()
        }

    }

    override fun initData() {
        super.initData()
        head_search2.setText("")
        //判断数据库是否为空
        bhotversion = hotidSearchData.isEmpty()
        bhistorySearch = historySearchData.isEmpty()
        //get热门标签
        getHotSearch()
        if (bhistorySearch) {
        } else {
            getSearchHistory()
        }
    }

    //get热门标签
    fun getHotSearch() {
        if (bhotversion) {
            //空
            mversion = ""
        } else {
            //有数据，则获取数据库版本号
            mversion = hotidSearchData[0].version
        }
        //调用API
        ApiUtils.getApi().get_hot_search(mversion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    //获取成功
                    if (bean.code == 12000) {
                        bean.data?.let {
                            mList.clear()
                            if (bhotversion) {
                                //数据库版本号为空
                                //设置数据库版本号
                                it.setVersion()
                                //保存本地
                                BoxUtils.saveHotSearch(it.hotSearchesList)
                                bhotversion = false
                                //适配数据源
                                mList.clear()
                                mList.addAll(it.hotSearchesList)
                            } else {
                                //版本号不为空，说明API更新
                                //清除本地数据，保存新数据
                                var hotversionSearchData = BoxUtils.getHotSearch(mversion)
                                BoxUtils.removeHotSearch(hotversionSearchData)
                                it.setVersion()
                                BoxUtils.saveHotSearch(it.hotSearchesList)
                                bhotversion = false
                                //适配数据源
                                mList.clear()
                                mList.addAll(it.hotSearchesList)
                            }
                        }
                        setHotSearch(mList)
                    } else
                    //版本相同
                        if (bean.code == 20000) {
                            //直接获取本地数据适配
                            var hotversionSearchData = BoxUtils.getHotSearch(mversion)
                            mList.clear()
                            mList.addAll(hotversionSearchData)
                            setHotSearch(mList)
                        } else {
                            ToastUtil.showShort(bean.msg)
                        }
                })
    }

    //set热门标签
    private fun setHotSearch(htagList: List<HotSearchesList>) {
        tfl_hot_search.adapter = object : TagAdapter<HotSearchesList>(htagList) {
            override fun getView(parent: FlowLayout?, position: Int, data: HotSearchesList?): View {
                return LayoutInflater.from(this@RedShopSeach).inflate(R.layout.red_shop_view_flowlayout_reasons_text, tfl_hot_search, false).apply {
                    findViewById<TextView>(R.id.tvTag).setText(data!!.name)
                }
            }

            //选中搜索
            override fun onSelected(position: Int, view: View?) {
                super.onSelected(position, view)
                mbackHotSearch = mList[position].name
                val search = SearchHistoryList().apply { id = 1;name = mbackHotSearch }
                mHistorySearch.add(search)
                //检查重复
                val mHistoryrepeat = BoxUtils.getnameSearch(search.name)
                if (mHistoryrepeat.isEmpty()) {
                    //空
                    //直接保存
                    BoxUtils.saveSearch(mHistorySearch[0])
                } else {
                    //重复
                    //删除记录，保存新纪录
                    BoxUtils.removeSearchs(mHistoryrepeat)
                    BoxUtils.saveSearch(mHistorySearch[0])
                }
                startActivity<RedShopSeachResult>(IConstants.SEACH_RESULT to mbackHotSearch)
                finish()
            }
        }
    }

    //get搜索历史
    fun getSearchHistory() {
        //数据库是否为空
        if (bhistorySearch) {
            //空
        } else {
            //有数据
            //测试数据
            historySearchData.reverse()
            for (i in historySearchData.indices) {
                shistory.add(historySearchData[i].name)
            }
            //只输出10个
            for (i in shistory.indices) {
                if (shistory.size > 10) {
                    shistory.removeAt(shistory.size - 1)
                }
            }
            setSearchHistory(shistory)
        }
    }

    lateinit var adapter: TagAdapter<String>
    //set搜索历史
    private fun setSearchHistory(stagList: List<String>) {

        adapter = object : TagAdapter<String>(stagList) {
            override fun getView(parent: FlowLayout?, position: Int, data: String?): View {
                return LayoutInflater.from(this@RedShopSeach).inflate(R.layout.red_shop_view_flowlayout_reasons_text, tfl_search_history, false).apply {
                    findViewById<TextView>(R.id.tvTag).setText(data)
                }
            }

            //选中搜索
            override fun onSelected(position: Int, view: View?) {
                super.onSelected(position, view)
                mbackHistorySearch = shistory[position]
                val search = SearchHistoryList().apply { id = 1;name = mbackHistorySearch }
                mHistorySearch.add(search)
                //检查重复
                val mHistoryrepeat = BoxUtils.getnameSearch(search.name)
                if (mHistoryrepeat.isEmpty()) {
                    //空
                    //直接保存
                    BoxUtils.saveSearch(mHistorySearch[0])
                } else {
                    //重复
                    //删除记录，保存新纪录
                    BoxUtils.removeSearchs(mHistoryrepeat)
                    BoxUtils.saveSearch(mHistorySearch[0])
                }
                startActivity<RedShopSeachResult>(IConstants.SEACH_RESULT to mbackHistorySearch)
                finish()
            }
        }
        tfl_search_history.adapter = adapter
    }


}
