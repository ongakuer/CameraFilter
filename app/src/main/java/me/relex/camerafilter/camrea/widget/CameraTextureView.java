package me.relex.camerafilter.camrea.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.TextureView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.relex.camerafilter.camrea.utils.CameraController;
import me.relex.camerafilter.camrea.utils.CameraHelper;
import me.relex.camerafilter.camrea.utils.CommonHandlerListener;

public class CameraTextureView extends TextureView
        implements TextureView.SurfaceTextureListener, CommonHandlerListener {

    private static final String TAG = "CameraTextureView";

    private static final int HANDLE_SETUP_CAMERA = 201;
    private static final int HANDLE_CONFIGURE_CAMERA = 202;

    private CommonHandler mHandler;

    private SurfaceTexture mSurfaceTexture;

    private ExecutorService mSingleThreadExecutor = Executors.newSingleThreadExecutor();

    private boolean mIsPaused;

    //private CameraSurfaceListener mCameraSurfaceListener;

    private boolean mIsSurfaceDestroyed;

    private int mScreenWidth;

    public CameraTextureView(Context context) {
        super(context);
        init(context);
    }

    public CameraTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mHandler = new CommonHandler(this);
        setSurfaceTextureListener(this);
        setScaleX(1.00001f); // 神奇的抗锯齿…
        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
    }

    private void startCamera() {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override public void run() {
                CameraController.getInstance().setupCamera(mSurfaceTexture);
                mHandler.sendEmptyMessage(HANDLE_CONFIGURE_CAMERA);
            }
        });
    }

    public void onResume() {
        if (!mIsSurfaceDestroyed && mIsPaused) {
            mHandler.sendEmptyMessageDelayed(HANDLE_SETUP_CAMERA, 500);
            startCamera();
        }
        mIsPaused = false;
    }

    public void onPause() {
        mHandler.removeCallbacksAndMessages(null);
        mIsPaused = true;
        CameraController.getInstance().release();
    }

    private void resizeAndConfigure() {
        try {
            if (mIsSurfaceDestroyed) {
                CameraController.getInstance().release();
                return;
            }

            Camera.Size previewSize = CameraHelper.getOptimalPreviewSize(
                    CameraController.getInstance().getCameraParameters(),
                    CameraController.getInstance().mCameraPictureSize);

            if (previewSize != null) {
                initLayout(previewSize, mScreenWidth);
            }

            CameraController.getInstance().configureCameraParameters(previewSize);

            CameraController.getInstance().startCameraPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean initLayout(Camera.Size size, int width) {
        final double aspectRatio = (double) size.width / size.height;

        int surfaceHeight = (int) (width * aspectRatio);

        if (getLayoutParams().width != width && getLayoutParams().height != surfaceHeight) {
            getLayoutParams().width = width;
            getLayoutParams().height = (int) (width * aspectRatio);
            requestLayout();

            return true;
        }
        return false;
    }

    ///////////SurfaceTextureListener///////////

    @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        mSurfaceTexture = surface;
        mIsSurfaceDestroyed = false;

        if (mIsPaused) {
            return;
        }

        mHandler.sendEmptyMessage(HANDLE_SETUP_CAMERA);
    }

    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
            int height) {
    }

    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        CameraController.getInstance().release();
        mIsSurfaceDestroyed = true;

        return false;
    }

    @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private static class CommonHandler extends Handler {
        private CommonHandlerListener listener;

        public CommonHandler(CommonHandlerListener listener) {
            this.listener = listener;
        }

        @Override
        public void handleMessage(Message msg) {
            if (listener != null) {
                listener.handleMessage(msg);
            }
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case HANDLE_SETUP_CAMERA:
                startCamera();
                break;

            case HANDLE_CONFIGURE_CAMERA:
                resizeAndConfigure();
                break;
        }
    }
}
