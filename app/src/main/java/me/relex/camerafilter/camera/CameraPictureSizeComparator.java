package me.relex.camerafilter.camera;

import android.hardware.Camera;
import java.util.Comparator;

public class CameraPictureSizeComparator implements Comparator<Camera.Size> {

    // 拍照尺寸建议从大到小，优先获取较大尺寸
    public int compare(Camera.Size size1, Camera.Size size2) {
        return size2.width - size1.width;
    }
}
