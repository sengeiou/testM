package com.lemo.emojcenter.utils

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Description :设置recycleview条目间距
 *
 *
 * Author:fangxu
 *
 *
 * Email:634804858@qq.com
 *
 *
 * Date: 2018/1/26
 */

class EmojDetailInfoItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val pos = parent.getChildAdapterPosition(view)
        //        outRect.bottom=6;
        //        outRect.left=6;
        //        if(pos<4){
        //            outRect.top=6;
        //        }
        //        if (pos %4==0) {
        //            outRect.left=0;
        //        }
        outRect.left = 6
        outRect.top = 6


    }
}
