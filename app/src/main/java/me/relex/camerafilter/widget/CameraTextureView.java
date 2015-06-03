//package me.relex.camerafilter.widget;
//
//import android.content.Context;
//import android.graphics.SurfaceTexture;
//import android.hardware.Camera;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Looper;
//import android.os.Message;
//import android.util.AttributeSet;
//import com.eaglesakura.view.GLTextureView;
//import me.relex.camerafilter.camera.CameraController;
//import me.relex.camerafilter.camera.CameraHelper;
//import me.relex.camerafilter.camera.CameraTextureRenderer;
//import me.relex.camerafilter.camera.CommonHandlerListener;
//
//public class CameraTextureView extends GLTextureView
//        implements CommonHandlerListener, SurfaceTexture.OnFrameAvailableListener {
//
//    private CameraHandler mBackgroundHandler;
//    private HandlerThread mHandlerThread;
//    private CameraTextureRenderer mCameraSurfaceRenderer;
//
//    private int mRatioWidth = 0;
//    private int mRatioHeight = 0;
//
//    public CameraTextureView(Context context) {
//        super(context);
//        init(context);
//    }
//
//    public CameraTextureView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        init(context);
//    }
//
//    public CameraTextureView(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        init(context);
//    }
//
//    private void init(Context context) {
//        setVersion(GLESVersion.OpenGLES20);
//        mHandlerThread = new HandlerThread("CameraHandlerThread");
//        mHandlerThread.start();
//        mBackgroundHandler = new CameraHandler(mHandlerThread.getLooper(), this);
//        mCameraSurfaceRenderer = new CameraTextureRenderer(context, mBackgroundHandler);
//
//        setRenderer(mCameraSurfaceRenderer);
//    }
//
//    public void setAspectRatio(int width, int height) {
//        if (width < 0 || height < 0) {
//            throw new IllegalArgumentException("Size cannot be negative.");
//        }
//        mRatioWidth = width;
//        mRatioHeight = height;
//        requestLayout();
//    }
//
//    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);
//        if (0 == mRatioWidth || 0 == mRatioHeight) {
//            setMeasuredDimension(width, height);
//        } else {
//            if (width < height * mRatioWidth / mRatioHeight) {
//                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
//            } else {
//                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
//            }
//        }
//    }
//
//    @Override public void onResume() {
//        super.onResume();
//    }
//
//    @Override public void onPause() {
//        mBackgroundHandler.removeCallbacksAndMessages(null);
//        CameraController.getInstance().release();
//
//        super.onPause();
//    }
//
//    public void onDestroy() {
//        if (!mHandlerThread.isInterrupted()) {
//            try {
//                mHandlerThread.quit();
//                mHandlerThread.interrupt();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @Override public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//        requestRender();
//    }
//
//    public static class CameraHandler extends Handler {
//        public static final int SETUP_CAMERA = 1001;
//        public static final int CONFIGURE_CAMERA = 1002;
//        public static final int START_CAMERA_PREVIEW = 1003;
//        //public static final int STOP_CAMERA_PREVIEW = 1004;
//        private CommonHandlerListener listener;
//
//        public CameraHandler(Looper looper, CommonHandlerListener listener) {
//            super(looper);
//            this.listener = listener;
//        }
//
//        @Override public void handleMessage(Message msg) {
//            listener.handleMessage(msg);
//        }
//    }
//
//    @Override public void handleMessage(final Message msg) {
//        switch (msg.what) {
//            case CameraHandler.SETUP_CAMERA: {
//                final int width = msg.arg1;
//                final int height = msg.arg2;
//                final SurfaceTexture surfaceTexture = (SurfaceTexture) msg.obj;
//                surfaceTexture.setOnFrameAvailableListener(this);
//
//                mBackgroundHandler.post(new Runnable() {
//                    @Override public void run() {
//                        CameraController.getInstance()
//                                .setupCamera(surfaceTexture, getContext().getApplicationContext(),
//                                        width);
//                        mBackgroundHandler.sendMessage(
//                                mBackgroundHandler.obtainMessage(CameraHandler.CONFIGURE_CAMERA,
//                                        width, height));
//                    }
//                });
//            }
//            break;
//            case CameraHandler.CONFIGURE_CAMERA: {
//                final int width = msg.arg1;
//                final int height = msg.arg2;
//                Camera.Size previewSize = CameraHelper.getOptimalPreviewSize(
//                        CameraController.getInstance().getCameraParameters(),
//                        CameraController.getInstance().mCameraPictureSize, width);
//
//                CameraController.getInstance().configureCameraParameters(previewSize);
//                if (previewSize != null) {
//                    mCameraSurfaceRenderer.setCameraPreviewSize(previewSize.height,
//                            previewSize.width);
//                }
//                mBackgroundHandler.sendEmptyMessage(CameraHandler.START_CAMERA_PREVIEW);
//            }
//            break;
//
//            case CameraHandler.START_CAMERA_PREVIEW:
//                mBackgroundHandler.post(new Runnable() {
//                    @Override public void run() {
//                        CameraController.getInstance().startCameraPreview();
//                    }
//                });
//
//                break;
//            //case CameraHandler.STOP_CAMERA_PREVIEW:
//            //    mBackgroundHandler.post(new Runnable() {
//            //        @Override public void run() {
//            //            CameraController.getInstance().stopCameraPreview();
//            //        }
//            //    });
//            //    break;
//
//            default:
//                break;
//        }
//    }
//}
