package com.qingmeng.mengmeng.view.dialog

import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.view.View
import com.qingmeng.mengmeng.R

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MyBottomDialog(context: Context, style: Int = 0) : BottomSheetDialog(context, style) {
    var windowHeight = 0

    override fun show() {
        super.show()
        window.apply {
            val mContent = decorView.findViewById<View>(R.id.design_bottom_sheet)
            val orginLayoutParams = mContent.layoutParams
            orginLayoutParams.height = windowHeight
            mContent.layoutParams = orginLayoutParams
            val mDialogBehavior = BottomSheetBehavior.from(mContent)
            mDialogBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            //跳过折叠状态
            mDialogBehavior.skipCollapsed = true
        }
    }
}