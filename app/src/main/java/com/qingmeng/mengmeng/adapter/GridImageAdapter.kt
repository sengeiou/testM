package com.qingmeng.mengmeng.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.tools.DateUtils
import com.luck.picture.lib.tools.StringUtils
import com.qingmeng.mengmeng.R
import java.io.File
import java.util.*

/**
 * author：luck
 * project：PictureSelector
 * package：com.luck.pictureselector.adapter
 * email：893855882@qq.com
 * data：16/7/27
 */
/**
 * Created by mingyue
 * Date: 2019/1/14
 * mail: 153705849@qq.com
 * describe:
 */
class GridImageAdapter(private val context: Context, private val onAddClick: () -> Unit,
                       private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<GridImageAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var list: MutableList<LocalMedia> = ArrayList()
    private var selectMax = 9

    fun setSelectMax(selectMax: Int) {
        this.selectMax = selectMax
    }

    fun setList(list: MutableList<LocalMedia>) {
        this.list = list
    }

    override fun getItemCount(): Int {
        return if (list.size < selectMax) {
            list.size + 1
        } else {
            list.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isShowAddItem(position)) {
            TYPE_CAMERA
        } else {
            TYPE_PICTURE
        }
    }

    /**
     * 创建ViewHolder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.item_feedback_image,
                viewGroup, false)
        return ViewHolder(view)
    }

    private fun isShowAddItem(position: Int): Boolean {
        val size = if (list.size == 0) 0 else list.size
        return position == size
    }

    /**
     * 设置值
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bindViewHolder(position)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var mImg: ImageView = view.findViewById(R.id.img_join_feedback_fiv)
        private var llDel: ImageView = view.findViewById(R.id.ll_del)
        private var tvDuration: TextView = view.findViewById(R.id.tv_join_feedback_duration)

        fun bindViewHolder(position:Int){
            //少于8张，显示继续添加的图标
            if (getItemViewType(position) == TYPE_CAMERA) {
                mImg.setImageResource(R.drawable.join_add_image)
                mImg.setOnClickListener { onAddClick() }
                llDel.visibility = View.INVISIBLE
            } else {
                llDel.visibility = View.VISIBLE
                llDel.setOnClickListener {
                    // 这里有时会返回-1造成数据下标越界,具体可参考getAdapterPosition()源码，
                    // 通过源码分析应该是bindViewHolder()暂未绘制完成导致，知道原因的也可联系我~感谢
                    if (position != RecyclerView.NO_POSITION) {
                        list.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, list.size)
                        //      DebugUtil.i("delete position:", index.toString() + "--->remove after:" + list.size)
                    }
                }
                val media = list[position]
                val mimeType = media.mimeType
                var path = ""
                path = if (media.isCut && !media.isCompressed) {
                    // 裁剪过
                    media.cutPath
                } else if (media.isCompressed || media.isCut && media.isCompressed) {
                    // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
                    media.compressPath
                } else {
                    // 原图
                    media.path
                }
                // 图片
                if (media.isCompressed) {
                    Log.i("compress image result:", (File(media.compressPath).length() / 1024).toString() + "k")
                    Log.i("压缩地址::", media.compressPath)
                }

                Log.i("原图地址::", media.path)
                val pictureType = PictureMimeType.isPictureType(media.pictureType)
                if (media.isCut) {
                    Log.i("裁剪地址::", media.cutPath)
                }
                val duration = media.duration
                tvDuration.visibility = if (pictureType == PictureConfig.TYPE_VIDEO) View.VISIBLE else View.GONE
                if (mimeType == PictureMimeType.ofAudio()) {
                    tvDuration.visibility = View.VISIBLE
                    val drawable = ContextCompat.getDrawable(context, R.drawable.picture_audio)
                    StringUtils.modifyTextViewDrawable(tvDuration, drawable, 0)
                } else {
                    val drawable = ContextCompat.getDrawable(context, R.drawable.video_icon)
                    StringUtils.modifyTextViewDrawable(tvDuration, drawable, 0)
                }
                tvDuration.text = DateUtils.timeParse(duration)
                if (mimeType == PictureMimeType.ofAudio()) {
                    mImg.setImageResource(R.drawable.audio_placeholder)
                } else {
                    val options = RequestOptions()
                            .centerCrop()
                            .placeholder(R.color.color_5ab1e1)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    Glide.with(itemView.context)
                            .load(path)
                            .apply(options)
                            .into(mImg)
                }
                //itemView 的点击事件
                itemView.setOnClickListener { onItemClick(position) }
            }
        }
    }

    companion object {
        val TYPE_CAMERA = 1
        val TYPE_PICTURE = 2
    }
}
