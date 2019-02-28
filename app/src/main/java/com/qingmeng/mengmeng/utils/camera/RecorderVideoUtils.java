package com.qingmeng.mengmeng.utils.camera;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;

import java.util.Comparator;
import java.util.List;

/**
 * @author fengjing:
 * @function
 * @date ：2016年6月13日 下午2:29:22
 * @mail 164303256@qq.com
 */
public class RecorderVideoUtils {
    public static List<Size> getResolutionList(Camera camera) {
        Parameters parameters = camera.getParameters();
        List<Size> previewSizes = parameters.getSupportedPreviewSizes();
        return previewSizes;
    }

    public static class ResolutionComparator implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            if (lhs.height != rhs.height) {
                return lhs.height - rhs.height;
            } else {
                return lhs.width - rhs.width;
            }
        }

    }

    /**
     * 检测Sdcard是否存在
     *
     * @return
     */
    public static boolean isExitsSdcard() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }
}
