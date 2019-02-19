package com.lemo.emojcenter.view

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import com.lemo.emojcenter.R
import com.lemo.emojcenter.adapter.FaceEmojiconGridAdapter
import com.lemo.emojcenter.adapter.FaceMyViewPagerAdapter
import com.lemo.emojcenter.bean.EmojBean
import com.lemo.emojcenter.bean.EmojGroupBean
import com.lemo.emojcenter.constant.FaceEmojType
import com.lemo.emojcenter.constant.FaceLocalConstant
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.*

/**
 * Created by wangru
 * Date: 2018/3/26  16:36
 * mail: 1902065822@qq.com
 * describe:表情键盘viewpager容器
 */

class EmojPagerView @JvmOverloads constructor( context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {

    private var emojGroupList: List<EmojGroupBean>? = null

    private var viewpages: MutableList<View>? = null
    private var mViewPagerAdapter: FaceMyViewPagerAdapter? = null

    private var firstGroupPageSize: Int = 0

    private var maxPageCount: Int = 0
    private var previousPagerPosition: Int = 0

    private var mEmojPagerViewListener: EmojPagerViewListener? = null


    fun init(emojiconGroupList: List<EmojGroupBean>?, emijiconColumn: Int, bigEmojiconColumn: Int) {
        if (emojiconGroupList == null) {
            throw RuntimeException("emojiconGroupList is null")
        }

        this.emojGroupList = emojiconGroupList
        emojiconColumns = emijiconColumn
        bigEmojiconColumns = bigEmojiconColumn

        viewpages = ArrayList()

        mViewPagerAdapter = FaceMyViewPagerAdapter(viewpages)
        adapter = mViewPagerAdapter
        setOnPageChangeListener(EmojiPagerChangeListener())
        addView()
    }

    private fun addView() {
        Observable.create(ObservableOnSubscribe<String> { e ->
            for (i in emojGroupList!!.indices) {
                val group = emojGroupList!![i]
                val groupEmojicons = group.emojiconList
                val gridViews = getGroupGridViews(group)
                if (i == 0) {
                    firstGroupPageSize = gridViews.size
                }
                maxPageCount = Math.max(gridViews.size, maxPageCount)
                viewpages!!.addAll(gridViews)
            }
            e.onComplete()
        }).subscribe(object : Observer<String> {
            //接受者,根据事件产生者产生的事件调用不同方法
            override fun onSubscribe(d: Disposable) {}

            override fun onNext(value: String) {}

            override fun onError(e: Throwable) {}

            override fun onComplete() {
                if (mEmojPagerViewListener != null) {
                    mEmojPagerViewListener!!.onPagerViewInited(maxPageCount, firstGroupPageSize)
                }
                viewpages?.let { mViewPagerAdapter!!.setData(it) }
                mViewPagerAdapter!!.notifyDataSetChanged()
            }
        })


    }

    private fun getGroupGridViews(group: EmojGroupBean?): List<View> {
        val views = ArrayList<View>()
        if (group == null || group.emojiconList == null) {
            return views
        }
        val emojList = group.emojiconList
        val totalSize = emojList.size
        var itemSize = bigEmojiconColumns * bigEmojiconRows
        val emojiType = group.emojType
        if (emojiType == FaceEmojType.NORMAL) {
            itemSize = emojiconRows * emojiconColumns - 1
        }
        val pageSize = if (totalSize % itemSize == 0) totalSize / itemSize else totalSize / itemSize + 1

        for (i in 0 until pageSize) {
            val view = View.inflate(getContext(), R.layout.face_emoj_gridview, null)
            val gv = view.findViewById<GridView>(R.id.gridview)
            if (emojiType == FaceEmojType.NORMAL) {
                gv.numColumns = emojiconColumns
            } else {
                gv.numColumns = bigEmojiconColumns
            }
            val list = ArrayList<EmojBean>()
            if (i != pageSize - 1) {
                list.addAll(emojList.subList(i * itemSize, (i + 1) * itemSize))
            } else {
                list.addAll(emojList.subList(i * itemSize, totalSize))
            }
            if (emojiType == FaceEmojType.NORMAL) {
                val deleteIcon = EmojBean()
                deleteIcon.emojiText = FaceLocalConstant.DELETE_KEY
                list.add(deleteIcon)
            }
            val gridAdapter = emojiType?.let { FaceEmojiconGridAdapter(getContext(), 1, list, it) }
            gv.adapter = gridAdapter
            gv.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val emojicon = gridAdapter!!.getItem(position)
                if (mEmojPagerViewListener != null) {
                    val emojiText = emojicon!!.emojiText
                    if (emojiText != null && emojiText == FaceLocalConstant.DELETE_KEY) {
                        mEmojPagerViewListener!!.onDeleteImageClicked()
                    } else {
                        mEmojPagerViewListener!!.onExpressionClicked(emojicon)
                    }
                }
            }

            views.add(view)
        }
        return views
    }

    fun setGroupPosition(position: Int) {
        if (adapter != null && position >= 0 && position < emojGroupList!!.size) {
            var count = 0
            for (i in 0 until position) {
                count += getPageSize(emojGroupList!![i])
            }
            currentItem = count
        }
    }

    /**
     * @param groupEntity
     * @return
     */
    fun getPageSize(groupEntity: EmojGroupBean): Int {
        val emojiconList = groupEntity.emojiconList
        var itemSize = bigEmojiconColumns * bigEmojiconRows
        val totalSize = emojiconList.size
        val emojiType = groupEntity.emojType
        if (emojiType == FaceEmojType.NORMAL) {
            itemSize = emojiconColumns * emojiconRows - 1
        }
        return if (totalSize % itemSize == 0) totalSize / itemSize else totalSize / itemSize + 1
    }

    private inner class EmojiPagerChangeListener : ViewPager.OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            var endSize = 0
            var groupPosition = 0
            for (groupEntity in emojGroupList!!) {
                val groupPageSize = getPageSize(groupEntity)
                //if the position is in current group
                if (endSize + groupPageSize > position) {
                    //this is means user swipe to here from previous page
                    if (previousPagerPosition - endSize < 0) {
                        if (mEmojPagerViewListener != null) {
                            mEmojPagerViewListener!!.onGroupPositionChanged(groupPosition, groupPageSize)
                            mEmojPagerViewListener!!.onGroupPagePostionChangedTo(0)
                        }
                        break
                    }
                    //this is means user swipe to here from back page
                    if (previousPagerPosition - endSize >= groupPageSize) {
                        if (mEmojPagerViewListener != null) {
                            mEmojPagerViewListener!!.onGroupPositionChanged(groupPosition, groupPageSize)
                            mEmojPagerViewListener!!.onGroupPagePostionChangedTo(position - endSize)
                        }
                        break
                    }

                    //page changed
                    if (mEmojPagerViewListener != null) {
                        mEmojPagerViewListener!!.onGroupInnerPagePostionChanged(previousPagerPosition - endSize, position - endSize)
                    }
                    break
                }
                groupPosition++
                endSize += groupPageSize
            }

            previousPagerPosition = position
        }

        override fun onPageScrollStateChanged(arg0: Int) {}

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
    }

    fun setEmojPagerViewListener(mEmojPagerViewListener: EmojPagerViewListener) {
        this.mEmojPagerViewListener = mEmojPagerViewListener
    }

    interface EmojPagerViewListener {
        /**
         * @param groupMaxPageSize     --max pages size
         * @param firstGroupPageSize-- size of first group pages
         */
        fun onPagerViewInited(groupMaxPageSize: Int, firstGroupPageSize: Int)

        /**
         * @param groupPosition--group   position
         * @param pagerSizeOfGroup--page size of group
         */
        fun onGroupPositionChanged(groupPosition: Int, pagerSizeOfGroup: Int)

        /**
         * page position changed
         *
         * @param oldPosition
         * @param newPosition
         */
        fun onGroupInnerPagePostionChanged(oldPosition: Int, newPosition: Int)

        /**
         * group page position changed
         *
         * @param position
         */
        fun onGroupPagePostionChangedTo(position: Int)

        /**
         * max page size changed
         *
         * @param maxCount
         */
        fun onGroupMaxPageSizeChanged(maxCount: Int)

        fun onDeleteImageClicked()

        fun onExpressionClicked(emojicon: EmojBean?)

    }

    companion object {

        private val emojiconRows = 3

        private var emojiconColumns = 7
        //emoj列数
        private val bigEmojiconRows = 2
        //emoj行数
        private var bigEmojiconColumns = 4
    }

}
