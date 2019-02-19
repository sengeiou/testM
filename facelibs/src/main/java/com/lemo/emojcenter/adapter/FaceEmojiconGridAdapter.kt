package com.lemo.emojcenter.adapter

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.lemo.emojcenter.R
import com.lemo.emojcenter.bean.EmojBean
import com.lemo.emojcenter.constant.FaceEmojType
import com.lemo.emojcenter.constant.FaceLocalConstant
import com.lemo.emojcenter.manage.FaceDownloadFaceManage
import com.lemo.emojcenter.utils.PathUtils
import java.io.File

/**
 * 表情商城图片表情
 */
class FaceEmojiconGridAdapter(context: Context, textViewResourceId: Int, objects: List<EmojBean>, private val emojiconType: FaceEmojType) : ArrayAdapter<EmojBean>(context, textViewResourceId, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val contentView:View = if (convertView == null) {
            if (emojiconType === FaceEmojType.NORMAL) {
                View.inflate(context, R.layout.face_item_view_new, null)
            } else {
                View.inflate(context, R.layout.face_item_view_new, null)
            }
        } else convertView
        val view = contentView.findViewById<View>(R.id.root_view)
        val imageView = contentView.findViewById<ImageView>(R.id.item_image)
        val textView = contentView.findViewById<TextView>(R.id.item_name)
        val emojicon = getItem(position)

        when(emojiconType) {
            FaceEmojType.NORMAL -> {
                val layoutParams = imageView.layoutParams as RelativeLayout.LayoutParams
                layoutParams.width = (32 * Resources.getSystem().displayMetrics.density).toInt()
                layoutParams.height = (32 * Resources.getSystem().displayMetrics.density).toInt()
                layoutParams.topMargin = (6 * Resources.getSystem().displayMetrics.density).toInt()
                layoutParams.bottomMargin = (4 * Resources.getSystem().displayMetrics.density).toInt()
                imageView.layoutParams = layoutParams
                imageView.scaleType = ImageView.ScaleType.FIT_XY
            }
            FaceEmojType.COLLECTION -> {
                val layoutParams = imageView.layoutParams as RelativeLayout.LayoutParams
                layoutParams.width = (72 * Resources.getSystem().displayMetrics.density).toInt()
                layoutParams.height = (72 * Resources.getSystem().displayMetrics.density).toInt()
                imageView.layoutParams = layoutParams
                //            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            else -> {
                val layoutParams = imageView.layoutParams as RelativeLayout.LayoutParams
                layoutParams.width = (60 * Resources.getSystem().displayMetrics.density).toInt()
                layoutParams.height = (60 * Resources.getSystem().displayMetrics.density).toInt()
                imageView.layoutParams = layoutParams
                //            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        }

        when (emojiconType) {
            FaceEmojType.NORMAL -> {
                textView.visibility = View.GONE
                if (FaceLocalConstant.DELETE_KEY == emojicon!!.emojiText) {
                    imageView.setImageResource(R.drawable.compose_emotion_delete)
                } else {
                    imageView.setImageResource(emojicon.iconRes)
                }
            }
            FaceEmojType.COLLECTION -> {
                textView.visibility = View.GONE
                if (emojicon!!.emojType === FaceEmojType.COLLECTION_ADD) {
                    imageView.setImageResource(R.mipmap.face_tianjia)
                } else {
                    loadImage(imageView, emojicon, false)
                }
            }
            FaceEmojType.EXPRESSION -> {
                textView.visibility = View.VISIBLE
                textView.text = emojicon!!.name
                loadImage(imageView, emojicon, false)
            }
            else -> {
            }
        }

        return contentView
    }

    @Synchronized
    private fun loadImage(imageView: ImageView, emojicon: EmojBean, isFailRes: Boolean) {
        val path = emojicon.pathLocal
        val url = emojicon.url
        //本地没有加载网络图片
        var urlImage = url
        //存在本地图片
        val isExitLocalPath = !TextUtils.isEmpty(path) && File(path!!).exists() && PathUtils.isImageComplete(path)
        if (isExitLocalPath) {
            urlImage = path
        }
        //强制加载网络图片
        if (isFailRes) {
            urlImage = url
        }
        val myOptions = RequestOptions()
        myOptions.skipMemoryCache(false)
        myOptions.placeholder(R.mipmap.face_icon_error)
        //        myOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        val requestBuilder = Glide.with(context)
        //        if (urlImage.endsWith(".gif")) {
        //            requestBuilder.asGif();
        //        }
        val finalUrlImage = urlImage
        Log.d(TAG, "loadImage: " + urlImage!!)
        requestBuilder.load(urlImage).apply(myOptions).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                if (!TextUtils.isEmpty(finalUrlImage) && !finalUrlImage!!.startsWith("http")) {
                    try {
                        val file = File(finalUrlImage)
                        if (file.exists()) {
                            file.delete()
                        }
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }

                    if (!isFailRes) {
                        loadImage(imageView, emojicon, true)
                    }
                }
                return false
            }

            override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                Log.v(TAG, "onResourceReady---> " + path!!)
                if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(path) && path.contains("/") && url!!.startsWith("http")) {
                    val dir = path.substring(0, path.lastIndexOf("/"))
                    if (!File(dir).exists()) {
                        File(dir).mkdirs()
                    }
                    if (!PathUtils.isImageComplete(path)) {
                        FaceDownloadFaceManage.downFaceImage(url, path)
                    }
                }
                return false
            }
        }).into(imageView)
    }

    companion object {
        private val TAG = "EmojiconGridAdapter"
    }

}
