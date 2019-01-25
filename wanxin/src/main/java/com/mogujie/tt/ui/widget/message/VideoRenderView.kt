package com.mogujie.tt.ui.widget.message

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.leimo.wanxin.R
import com.mogujie.tt.db.entity.MessageEntity
import com.mogujie.tt.db.entity.UserEntity
import com.mogujie.tt.imservice.entity.VideoMessage
import com.mogujie.tt.utils.imageload.ImageLoaderInterpolator
import java.util.*

/**
 * Created by zhujian on 15/3/26.
 */
class VideoRenderView(context: Context, attrs: AttributeSet) : BaseMsgRenderView(context, attrs) {
    lateinit var messageContent: ImageView
        private set

    override fun onFinishInflate() {
        super.onFinishInflate()
        messageContent = findViewById(R.id.message_video)
    }

    /**
     * 控件赋值
     *
     * @param messageEntity
     * @param userEntity
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun render(messageEntity: MessageEntity, userEntity: UserEntity, context: Context) {
        super.render(messageEntity, userEntity, context)
        val videoMessage = VideoMessage(messageEntity)
        if (messageContent != null) {
            val imageLoaderInterpolator = ImageLoaderInterpolator()
            imageLoaderInterpolator.placeholderImgId = R.drawable.tt_default_image
            //            ImageLoader.loadImage(context, messageContent, videoMessage.getThumbUrl(), imageLoaderInterpolator);
            com.app.common.imageloader.ImageLoader.loader().load(context, videoMessage.thumbUrl, messageContent)
        }
        //        ImageLoaderUtil.getImageLoaderInstance().displayImage(videoMessage.getThumbnailUrl(),messageContent);
        logger.d("render: videoMessage=" + videoMessage.toString())
    }

    override fun msgFailure(messageEntity: MessageEntity) {
        super.msgFailure(messageEntity)
    }

    /**
     * ----------------set/get---------------------------------
     */

    fun setMine(isMine: Boolean) {
        this.isMine = isMine
    }


    fun setParentView(parentView: ViewGroup) {
        this.parentView = parentView
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private fun createVideoThumbnail(url: String, width: Int, height: Int): Bitmap? {
        var bitmap: Bitmap? = null
        val retriever = MediaMetadataRetriever()
        val kind = MediaStore.Video.Thumbnails.MINI_KIND
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(url, HashMap())
            } else {
                retriever.setDataSource(url)
            }
            bitmap = retriever.frameAtTime
        } catch (ex: IllegalArgumentException) {
            // Assume this is a corrupt video file
        } catch (ex: RuntimeException) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release()
            } catch (ex: RuntimeException) {
                // Ignore failures while cleaning up.
            }

        }
        if (kind == MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT)
        }
        return bitmap
    }

    companion object {

        fun inflater(context: Context, viewGroup: ViewGroup, isMine: Boolean): VideoRenderView {
            val resource = if (isMine) R.layout.tt_mine_video_message_item else R.layout.tt_other_video_message_item
            val videoRenderView = LayoutInflater.from(context).inflate(resource, viewGroup, false) as VideoRenderView
            videoRenderView.setMine(isMine)
            return videoRenderView
        }
    }
}
