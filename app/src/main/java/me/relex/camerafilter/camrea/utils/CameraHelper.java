package me.relex.camerafilter.camrea.utils;

import android.hardware.Camera;
import java.util.List;

public class CameraHelper {

    public static Camera.Size getOptimalPreviewSize(Camera.Parameters parameters,
            Camera.Size pictureSize) {

        if (parameters == null || pictureSize == null) {
            return null;
        }

        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) pictureSize.width / pictureSize.height;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = pictureSize.height;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }
}
