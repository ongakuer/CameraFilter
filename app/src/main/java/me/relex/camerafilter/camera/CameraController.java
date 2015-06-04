package me.relex.camerafilter.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CameraController
        implements Camera.AutoFocusCallback, Camera.ErrorCallback, CommonHandlerListener {

    public static final String BROADCAST_ACTION_OPEN_CAMERA_ERROR =
            "CameraController.BROADCAST_ACTION_OPEN_CAMERA_ERROR";

    public static final String TYPE_OPEN_CAMERA_ERROR_TYPE =
            "CameraController.TYPE_OPEN_CAMERA_ERROR_TYPE";

    public static final int TYPE_OPEN_CAMERA_ERROR_UNKNOWN = 0;
    public static final int TYPE_OPEN_CAMERA_ERROR_PERMISSION_DISABLE = 1;

    private static volatile CameraController sInstance;

    public final static float sCameraRatio = 4f / 3f;
    private final CameraControllerHandler mHandler;

    private Camera mCamera = null;
    public int mCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK;
    public boolean mIsSupportFontFacingCamera = false;
    public boolean mCameraMirrored = false;
    public Camera.Size mCameraPictureSize;

    private final Object mLock = new Object();

    //////////
    private boolean mAutoFocusLocked = false;
    private boolean mIsSupportAutoFocus = false;
    private boolean mIsSupportAutoFocusContinuousPicture = false;

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

    private CameraController() {
        mHandler = new CameraControllerHandler(this);
    }

    public boolean checkSupportFontFacingCamera(boolean frontPriority) {
        try {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int cameraCount = Camera.getNumberOfCameras();
            for (int i = 0; i < cameraCount; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    if (frontPriority) { // 显示前置摄像头
                        mCameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    }
                    mIsSupportFontFacingCamera = true;
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setupCamera(SurfaceTexture surfaceTexture, Context context,
            int desiredPictureWidth) {
        if (mCamera != null) {
            release();
        }

        synchronized (mLock) {
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
                e.printStackTrace();
                mCamera = null;
                e.printStackTrace();
                Intent intent = new Intent(BROADCAST_ACTION_OPEN_CAMERA_ERROR);
                String message = e.getMessage();
                intent.putExtra(TYPE_OPEN_CAMERA_ERROR_TYPE,
                        (!TextUtils.isEmpty(message) && message.contains("permission"))
                                ? TYPE_OPEN_CAMERA_ERROR_PERMISSION_DISABLE
                                : TYPE_OPEN_CAMERA_ERROR_UNKNOWN);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }

            if (mCamera == null) {
                //Toast.makeText(mContext, "Unable to start camera", Toast.LENGTH_SHORT).showFromSession();
                return;
            }

            try {
                findCameraSupportValue(desiredPictureWidth);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void configureCameraParameters(Camera.Size previewSize) {

        try {
            Camera.Parameters cp = getCameraParameters();
            if (cp == null || mCamera == null) {
                return;
            }
            // 对焦模式
            synchronized (mLock) {
                List<String> focusModes = cp.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    mIsSupportAutoFocusContinuousPicture = true;
                    cp.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 自动连续对焦
                } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    mIsSupportAutoFocus = true;
                    cp.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);// 自动对焦
                } else {
                    mIsSupportAutoFocusContinuousPicture = false;
                    mIsSupportAutoFocus = false;
                }
                // 预览尺寸
                if (previewSize != null) {
                    cp.setPreviewSize(previewSize.width, previewSize.height);
                }
                //拍照尺寸
                cp.setPictureSize(mCameraPictureSize.width, mCameraPictureSize.height);

                mCamera.setParameters(cp);
                mCamera.setErrorCallback(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mAutoFocusLocked = false;
    }

    public boolean startCameraPreview() {
        //Log.d(TAG, "打开预览了");
        if (mCamera != null) {
            synchronized (mLock) {
                try {
                    mCamera.startPreview();

                    if (mIsSupportAutoFocusContinuousPicture) {
                        mCamera.cancelAutoFocus();
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    public boolean stopCameraPreview() {

        //Log.d(TAG, "关闭预览了");
        if (mCamera != null) {
            synchronized (mLock) {
                try {
                    mCamera.stopPreview();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    public void release() {
        if (mCamera != null) {
            synchronized (mLock) {
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
    }

    public boolean startAutoFocus(Camera.AutoFocusCallback autoFocusCallback) {
        if ((mIsSupportAutoFocus || mIsSupportAutoFocusContinuousPicture) && mCamera != null) {
            try {

                String focusMode = getCameraParameters().getFocusMode();

                if (!TextUtils.isEmpty(focusMode) && focusMode.
                        equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {  // 如果是连续自动对焦, 来一次对焦处理
                    mCamera.autoFocus(autoFocusCallback);
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        return false;
    }

    public void startTouchAutoFocus(View v, MotionEvent event) {
        if ((mIsSupportAutoFocus || mIsSupportAutoFocusContinuousPicture)
                && mCamera != null
                && !mAutoFocusLocked) {
            try {
                mAutoFocusLocked = true;

                Camera.Parameters parameters = getCameraParameters();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                if (parameters.getMaxNumFocusAreas() > 0) {
                    Rect focusRect =
                            CameraHelper.calculateTapArea(v, event.getX(), event.getY(), 1f);
                    List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                    focusAreas.add(new Camera.Area(focusRect, 1000));
                    parameters.setFocusAreas(focusAreas);
                }

                if (parameters.getMaxNumMeteringAreas() > 0) {
                    Rect meteringRect =
                            CameraHelper.calculateTapArea(v, event.getX(), event.getY(), 1.5f);
                    List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                    meteringAreas.add(new Camera.Area(meteringRect, 1000));
                    parameters.setMeteringAreas(meteringAreas);
                }

                mCamera.setParameters(parameters);
                mCamera.autoFocus(this);
            } catch (Exception e) {
                e.printStackTrace();
                mAutoFocusLocked = false;
            }
        }
    }

    public Camera.Parameters getCameraParameters() {
        if (mCamera != null) {
            synchronized (mLock) {
                try {
                    return mCamera.getParameters();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private void findCameraSupportValue(int desiredWidth) {
        Camera.Parameters cp = getCameraParameters();
        List<Camera.Size> cs = cp.getSupportedPictureSizes();
        if (cs != null && !cs.isEmpty()) {
            Collections.sort(cs, mCameraPictureSizeComparator);
            for (Camera.Size size : cs) {
                if (size.width < desiredWidth && size.height < desiredWidth) {
                    break;
                }
                float ratio = (float) size.width / size.height;
                if (ratio == sCameraRatio) {
                    mCameraPictureSize = size;
                }
            }
        }
    }

    public void takePicture(Camera.ShutterCallback shutter, Camera.PictureCallback raw,
            Camera.PictureCallback jpeg) {
        if (mCamera != null) {
            mCamera.takePicture(shutter, raw, jpeg);
        }
    }

    //////////////////// implements ////////////////////

    //AutoFocusCallback
    @Override public void onAutoFocus(boolean success, Camera camera) {

        mHandler.sendEmptyMessageDelayed(RESET_TOUCH_FOCUS, RESET_TOUCH_FOCUS_DELAY);

        mAutoFocusLocked = false;
    }

    //ErrorCallback
    @Override public void onError(int error, Camera camera) {

    }

    //PictureCallback
    //@Override public void onPictureTaken(byte[] data, Camera camera) {
    //    mIsTakingPicture = false;
    //    //try {
    //    //    Camera.Parameters ps = camera.getParameters();
    //    //    if (ps.getPictureFormat() == ImageFormat.JPEG) {
    //    //        //CommonUtil.executeAsyncTask(new SquareBitmapTask(data, mCameraMirrored) {
    //    //        //    @Override protected void onPostExecute(PublishBean newPost) {
    //    //        //        super.onPostExecute(newPost);
    //    //        //        mIsTakingPicture = false;
    //    //        //        if (mPictureCallback != null) {
    //    //        //            mPictureCallback.onPictureTaken(newPost);
    //    //        //        }
    //    //        //    }
    //    //        //});
    //    //    }
    //    //} catch (Exception e) {
    //    //    e.printStackTrace();
    //    //}
    //}

    //public boolean onClickEvent(View v, MotionEvent event) {
    //    if (mClickGestureDetector.onTouchEvent(event)) {
    //        Log.e("onClickEvent", "onClickEvent 进入了 onSingleTapUp");
    //        startTouchAutoFocus(v, event);
    //        return true;
    //    }
    //
    //    return false;
    //}

    //////////////////// Getter & Setter ////////////////////

    public Camera getCamera() {
        return mCamera;
    }

    public int getCameraIndex() {
        return mCameraIndex;
    }

    public void setCameraIndex(int cameraIndex) {
        this.mCameraIndex = cameraIndex;
    }

    public boolean isSupportFontFacingCamera() {
        return mIsSupportFontFacingCamera;
    }

    private static final int RESET_TOUCH_FOCUS = 301;
    private static final int RESET_TOUCH_FOCUS_DELAY = 3000;

    private static class CameraControllerHandler extends Handler {

        private CommonHandlerListener listener;

        public CameraControllerHandler(CommonHandlerListener listener) {
            super(Looper.getMainLooper());
            this.listener = listener;
        }

        @Override public void handleMessage(Message msg) {
            listener.handleMessage(msg);
        }
    }

    @Override public void handleMessage(Message msg) {
        switch (msg.what) {
            case RESET_TOUCH_FOCUS: {
                if (mCamera == null || mAutoFocusLocked) {
                    return;
                }
                mHandler.removeMessages(RESET_TOUCH_FOCUS);
                try {
                    if (mIsSupportAutoFocusContinuousPicture) {
                        Camera.Parameters cp = getCameraParameters();
                        if (cp != null) {
                            cp.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                            mCamera.setParameters(cp);
                        }
                    }
                    mCamera.cancelAutoFocus();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }
}
