package me.relex.camerafilter.camera;

import android.hardware.Camera;
import java.util.Comparator;

public class CameraPreviewSizeComparator implements Comparator<Camera.Size> {

    // 预览尺寸建议从小到大，优先获取较小的尺寸
    public int compare(Camera.Size size1, Camera.Size size2) {
        return size1.width - size2.width;
    }
}
