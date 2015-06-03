//package me.relex.camerafilter.camera;
//
//import android.content.Context;
//import android.graphics.SurfaceTexture;
//import com.eaglesakura.view.GLTextureView;
//import javax.microedition.khronos.egl.EGLConfig;
//import javax.microedition.khronos.opengles.GL10;
//import me.relex.camerafilter.filter.Filter;
//import me.relex.camerafilter.gles.FullFrameRect;
//import me.relex.camerafilter.widget.CameraTextureView;
//
//public class CameraTextureRenderer implements GLTextureView.Renderer {
//
//    private final Context mContext;
//    private final CameraTextureView.CameraHandler mCameraHandler;
//    private int mTextureId = -1;
//    private FullFrameRect mFullScreen;
//    private SurfaceTexture mSurfaceTexture;
//    private final float[] mSTMatrix = new float[16];
//
//    //private boolean mIncomingSizeUpdated;
//    //private int mIncomingWidth, mIncomingHeight;
//    private int mSurfaceWidth, mSurfaceHeight;
//
//    public CameraTextureRenderer(Context context, CameraTextureView.CameraHandler cameraHandler) {
//        mContext = context;
//        mCameraHandler = cameraHandler;
//        //mIncomingSizeUpdated = false;
//        //mIncomingWidth = mIncomingHeight = -1;
//    }
//
//    public void setCameraPreviewSize(int width, int height) {
//        //mIncomingWidth = width;
//        //mIncomingHeight = height;
//        //mIncomingSizeUpdated = true;
//
//        float scaleHeight = mSurfaceWidth / (width * 1f / height * 1f);
//        float surfaceHeight = mSurfaceHeight;
//
//        if (mFullScreen != null) {
//            mFullScreen.scaleMVPMatrix(1f, scaleHeight / surfaceHeight);
//        }
//    }
//
//    @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        //Log.e("CameraSurfaceRenderer", "onSurfaceCreated");
//        mFullScreen = new FullFrameRect(new Filter(mContext));
//        mTextureId = mFullScreen.createTextureObject();
//        mSurfaceTexture = new SurfaceTexture(mTextureId);
//    }
//
//    @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
//        //Log.e("CameraSurfaceRenderer",
//        //        "onSurfaceChanged width = " + width + "; height = " + height);
//        mSurfaceWidth = width;
//        mSurfaceHeight = height;
//
//        if (gl != null) {
//            gl.glViewport(0, 0, width, height);
//        }
//
//        mCameraHandler.sendMessage(
//                mCameraHandler.obtainMessage(CameraTextureView.CameraHandler.SETUP_CAMERA, width,
//                        height, mSurfaceTexture));
//    }
//
//    @Override public void onDrawFrame(GL10 gl) {
//
//        mSurfaceTexture.updateTexImage();
//
//        //if (mIncomingSizeUpdated) {
//        //    mFullScreen.getProgram().setTexSize(mIncomingWidth, mIncomingHeight);
//        //    mIncomingSizeUpdated = false;
//        //}
//
//        mSurfaceTexture.getTransformMatrix(mSTMatrix);
//        mFullScreen.drawFrame(mTextureId, mSTMatrix);
//    }
//
//    @Override public void onSurfaceDestroyed(GL10 gl) {
//        notifyPausing();
//    }
//
//    public void notifyPausing() {
//        if (mSurfaceTexture != null) {
//            mSurfaceTexture.release();
//            mSurfaceTexture = null;
//        }
//
//        if (mFullScreen != null) {
//            mFullScreen.release(false);     // assume the GLSurfaceView EGL context is about
//            mFullScreen = null;             //  to be destroyed
//        }
//
//        //mIncomingWidth = mIncomingHeight = -1;
//    }
//}
