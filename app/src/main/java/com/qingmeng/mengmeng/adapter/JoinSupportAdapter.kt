package com.qingmeng.mengmeng.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.qingmeng.mengmeng.R

class JoinSupportAdapter(val list: ArrayList<String>) : RecyclerView.Adapter<JoinSupportAdapter.JoinSupportViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, type: Int): JoinSupportViewHolder {
        return JoinSupportViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_join_support, parent, false))
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: JoinSupportViewHolder, position: Int) {
        holder.bindViewHolder(list[position])
    }

    inner class JoinSupportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val text:TextView = itemView.findViewById(R.id.join_support_text)

        fun bindViewHolder(string: String){
            text.text = string
        }
    }
}