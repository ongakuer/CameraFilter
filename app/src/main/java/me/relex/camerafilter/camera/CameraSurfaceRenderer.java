package me.relex.camerafilter.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import me.relex.camerafilter.filter.FilterManager;
import me.relex.camerafilter.filter.FilterManager.FilterType;
import me.relex.camerafilter.gles.FullFrameRect;
import me.relex.camerafilter.gles.GlUtil;
import me.relex.camerafilter.widget.CameraSurfaceView;

public class CameraSurfaceRenderer implements GLSurfaceView.Renderer {

    private final Context mContext;
    private final CameraSurfaceView.CameraHandler mCameraHandler;
    private int mTextureId = GlUtil.NO_TEXTURE;
    private FullFrameRect mFullScreen;
    private SurfaceTexture mSurfaceTexture;
    private final float[] mSTMatrix = new float[16];

    private int mSurfaceWidth, mSurfaceHeight;
    private FilterType mCurrentFilterType;
    private FilterType mNewFilterType;

    public CameraSurfaceRenderer(Context context, CameraSurfaceView.CameraHandler cameraHandler) {
        mContext = context;
        mCameraHandler = cameraHandler;
        mCurrentFilterType = mNewFilterType = FilterType.Normal;
    }

    public void setCameraPreviewSize(int width, int height) {

        float scaleHeight = mSurfaceWidth / (width * 1f / height * 1f);
        float surfaceHeight = mSurfaceHeight;

        if (mFullScreen != null) {
            mFullScreen.scaleMVPMatrix(1f, scaleHeight / surfaceHeight);
        }
    }

    @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Matrix.setIdentityM(mSTMatrix, 0);
        mFullScreen =
                new FullFrameRect(FilterManager.getCameraFilter(mCurrentFilterType, mContext));
        mTextureId = mFullScreen.createTexture();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
    }

    @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        if (gl != null) {
            gl.glViewport(0, 0, width, height);
        }
        mCameraHandler.sendMessage(
                mCameraHandler.obtainMessage(CameraSurfaceView.CameraHandler.SETUP_CAMERA, width,
                        height, mSurfaceTexture));
    }

    @Override public void onDrawFrame(GL10 gl) {
        mSurfaceTexture.updateTexImage();

        if (mNewFilterType != mCurrentFilterType) {
            mFullScreen.changeProgram(FilterManager.getCameraFilter(mNewFilterType, mContext));
            mCurrentFilterType = mNewFilterType;
        }

        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mFullScreen.drawFrame(mTextureId, mSTMatrix);
    }

    public void notifyPausing() {

        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }

        if (mFullScreen != null) {
            mFullScreen.release(false);     // assume the GLSurfaceView EGL context is about
            mFullScreen = null;             // to be destroyed
        }

        //mIncomingWidth = mIncomingHeight = -1;
    }

    public void changeFilter(FilterType filterType) {
        mNewFilterType = filterType;
    }
}
