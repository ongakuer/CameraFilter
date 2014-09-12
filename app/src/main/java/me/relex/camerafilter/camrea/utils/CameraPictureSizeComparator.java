package me.relex.camerafilter.camrea.utils;

import android.hardware.Camera;
import java.util.Comparator;

public class CameraPictureSizeComparator implements Comparator<Camera.Size> {

    public int compare(Camera.Size size1, Camera.Size size2) {

        return size2.width - size1.width;
    }
}
