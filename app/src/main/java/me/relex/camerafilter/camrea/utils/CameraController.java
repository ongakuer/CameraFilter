package me.relex.camerafilter.camrea.utils;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import java.util.Collections;
import java.util.List;

public class CameraController implements Camera.ErrorCallback {

    private static final String TAG = "CameraController";
    private static volatile CameraController sInstance;
    public static final int CAMERA_PICTURE_DEFAULT_WIDTH = 640;
    public final static float sCameraRatio = 4f / 3f;

    private Camera mCamera = null;
    public int mCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK;
    public boolean mIsSupportFontFacingCamera = false;
    public boolean mCameraMirrored = false;
    public Camera.Size mCameraPictureSize;

    //////////
    private CameraPictureSizeComparator mCameraPictureSizeComparator =
            new CameraPictureSizeComparator();

    //////////

    public static CameraController getInstance() {
        if (sInstance == null) {
            synchronized (CameraController.class) {
                if (sInstance == null) {
                    sInstance = new CameraController();
                }
            }
        }
        return sInstance;
    }

    public boolean checkSupportFontFacingCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT; // 默认显示前置摄像头
                mIsSupportFontFacingCamera = true;
                return true;
            }
        }

        return false;
    }

    public void setupCamera(SurfaceTexture surfaceTexture) {
        if (mCamera != null) {
            release();
        }

        try {
            if (Camera.getNumberOfCameras() > 0) {
                mCamera = Camera.open(mCameraIndex);
            } else {
                mCamera = Camera.open();
            }
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraIndex, cameraInfo);
            mCameraMirrored = (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (Exception e) {
            mCamera = null;
            e.printStackTrace();
        }

        if (mCamera == null) {
            return;
        }

        try {
            findCameraSupportValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void configureCameraParameters(Camera.Size previewSize) {
        try {
            Camera.Parameters cp = getCameraParameters();
            if (cp == null || mCamera == null) {
                return;
            }
            List<String> focusModes = cp.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                cp.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 自动连续对焦
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                cp.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);// 自动对焦
            }
            if (previewSize != null) {
                cp.setPreviewSize(previewSize.width, previewSize.height);
            }
            cp.setPictureSize(mCameraPictureSize.width, mCameraPictureSize.height);

            mCamera.setParameters(cp);
            mCamera.setErrorCallback(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCameraPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    public void stopCameraPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    public void release() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mCamera = null;
            }
        }
    }

    public Camera.Parameters getCameraParameters() {
        if (mCamera != null) {
            return mCamera.getParameters();
        }

        return null;
    }

    private void findCameraSupportValue() {
        Camera.Parameters cp = getCameraParameters();
        List<Camera.Size> cs = cp.getSupportedPictureSizes();
        if (cs != null && !cs.isEmpty()) {  //这里限定在640左右
            Collections.sort(cs, mCameraPictureSizeComparator);
            for (Camera.Size size : cs) {
                if (size.width < CAMERA_PICTURE_DEFAULT_WIDTH
                        || size.height < CAMERA_PICTURE_DEFAULT_WIDTH) {
                    break;
                }
                float ratio = (float) size.width / size.height;
                if (ratio == sCameraRatio) {
                    mCameraPictureSize = size;
                }
            }
        }
    }

    @Override public void onError(int error, Camera camera) {
        Log.e(TAG, "onError error=" + error);
    }

    public Camera getCamera() {
        return mCamera;
    }

    public int getCameraIndex() {
        return mCameraIndex;
    }
}
