package com.qingmeng.mengmeng.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.entity.StaticBean

@Suppress("UNREACHABLE_CODE", "ALWAYS_NULL")
class JoinMenuAdapter(val list: MutableList<StaticBean>, private val mContext: Context) : BaseAdapter() {
    override fun getItem(position: Int): Any = list[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = list.size

    override fun getView(position: Int, v: View?, parent: ViewGroup?): View {
        var convertView = v
        val viewHolder: ViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_join_menu, parent, false)
            viewHolder = ViewHolder(convertView!!)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        Glide.with(mContext).load(list[position].icon).into(viewHolder.icon)
        viewHolder.name.text = list[position].title
        return convertView
    }

    private inner class ViewHolder(view: View) {
        val icon: ImageView = view.findViewById(R.id.join_menu_icon)
        val name: TextView = view.findViewById(R.id.join_menu_name)
    }
}