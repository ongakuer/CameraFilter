package me.relex.camerafilter.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import java.nio.FloatBuffer;
import me.relex.camerafilter.R;
import me.relex.camerafilter.gles.GlUtil;

public class CameraFilter extends AbstractFilter implements IFilter {

    protected int mProgramHandle;
    private int maPositionLoc;
    private int muMVPMatrixLoc;
    private int muTexMatrixLoc;
    private int maTextureCoordLoc;
    private int mTextureLoc;

    protected int mIncomingWidth, mIncomingHeight;

    public CameraFilter(Context applicationContext) {
        mProgramHandle = createProgram(applicationContext);
        if (mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        getGLSLValues();
    }

    @Override public int getTextureTarget() {
        return GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
    }

    @Override public void setTextureSize(int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }
        if (width == mIncomingWidth && height == mIncomingHeight) {
            return;
        }
        mIncomingWidth = width;
        mIncomingHeight = height;
    }

    @Override protected int createProgram(Context applicationContext) {
        return GlUtil.createProgram(applicationContext, R.raw.vertex_shader,
                R.raw.fragment_shader_ext);
    }

    @Override protected void getGLSLValues() {
        mTextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexture");
        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
    }

    @Override public void onDraw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
            int vertexCount, int coordsPerVertex, int vertexStride, float[] texMatrix,
            FloatBuffer texBuffer, int textureId, int texStride) {

        GlUtil.checkGlError("draw start");

        useProgram();

        bindTexture(textureId);

        //runningOnDraw();

        bindGLSLValues(mvpMatrix, vertexBuffer, coordsPerVertex, vertexStride, texMatrix, texBuffer,
                texStride);

        drawArrays(firstVertex, vertexCount);

        unbindGLSLValues();

        unbindTexture();

        disuseProgram();
    }

    @Override protected void useProgram() {
        GLES20.glUseProgram(mProgramHandle);
        //GlUtil.checkGlError("glUseProgram");
    }

    @Override protected void bindTexture(int textureId) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(getTextureTarget(), textureId);
        GLES20.glUniform1i(mTextureLoc, 0);
    }

    @Override
    protected void bindGLSLValues(float[] mvpMatrix, FloatBuffer vertexBuffer, int coordsPerVertex,
            int vertexStride, float[] texMatrix, FloatBuffer texBuffer, int texStride) {

        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GLES20.glVertexAttribPointer(maPositionLoc, coordsPerVertex, GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, texStride,
                texBuffer);
    }

    @Override protected void drawArrays(int firstVertex, int vertexCount) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);
    }

    @Override protected void unbindGLSLValues() {
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
    }

    @Override protected void unbindTexture() {
        GLES20.glBindTexture(getTextureTarget(), 0);
    }

    @Override protected void disuseProgram() {
        GLES20.glUseProgram(0);
    }

    @Override public void releaseProgram() {
        GLES20.glDeleteProgram(mProgramHandle);
        mProgramHandle = -1;
    }

    /////////// Set Runnable ////////////
    //protected void addRunnableOnDraw(final Runnable runnable) {
    //    synchronized (mRunnableOnDraw) {
    //        mRunnableOnDraw.addLast(runnable);
    //    }
    //}
    //
    //protected void setFloat(final int location, final float floatValue) {
    //    addRunnableOnDraw(new Runnable() {
    //        @Override public void run() {
    //            GLES20.glUniform1f(location, floatValue);
    //        }
    //    });
    //}
    //
    //@Override protected void runningOnDraw() {
    //    while (!mRunnableOnDraw.isEmpty()) {
    //        mRunnableOnDraw.removeFirst().run();
    //    }
    //}
}
