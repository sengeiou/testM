package com.qingmeng.mengmeng.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.qingmeng.mengmeng.R

@Suppress("DEPRECATION")
/**
 * Created by zq on 2018/8/6
 */
class MyItemView : RelativeLayout {
    private lateinit var tvTitle: TextView
    private lateinit var tvContent: TextView

    constructor(context: Context) : super(context, null, 0) {
        initView()
    }

    @SuppressLint("Recycle")
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs, 0) {
        initView()
        val array = context.obtainStyledAttributes(attrs, R.styleable.MyItemView)
        tvTitle.text = array.getText(R.styleable.MyItemView_my_item_title)
        tvContent.text = array.getText(R.styleable.MyItemView_my_item_content)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private fun initView() {
        View.inflate(context, R.layout.layout_my_item, this)
        tvTitle = findViewById(R.id.tv_title)
        tvContent = findViewById(R.id.tv_content)
    }

    fun setTitle(resId: Int) {
        tvTitle.setText(resId)
    }

    fun setContent(text: CharSequence) {
        tvContent.text = text
    }
}