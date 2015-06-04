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
    private int mTextureUni;

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

    @Override protected int createProgram(Context applicationContext) {
        return GlUtil.createProgram(applicationContext, R.raw.vertex_shader,
                R.raw.fragment_shader_ext);
    }

    @Override protected void getGLSLValues() {
        mTextureUni = GLES20.glGetUniformLocation(mProgramHandle, "uTexture");
        //GlUtil.checkLocation(mTextureUni, "mTextureUni");

        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        //GlUtil.checkLocation(maPositionLoc, "aPosition");

        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        //GlUtil.checkLocation(muMVPMatrixLoc, "uMVPMatrix");

        muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
        //GlUtil.checkLocation(muTexMatrixLoc, "uTexMatrix");

        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        //GlUtil.checkLocation(maTextureCoordLoc, "aTextureCoord");
    }

    @Override public void onDraw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
            int vertexCount, int coordsPerVertex, int vertexStride, float[] texMatrix,
            FloatBuffer texBuffer, int textureId, int texStride) {

        GlUtil.checkGlError("draw start");

        useProgram();

        bindTexture(textureId);

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
        GLES20.glUniform1i(mTextureUni, 0);
    }

    @Override
    protected void bindGLSLValues(float[] mvpMatrix, FloatBuffer vertexBuffer, int coordsPerVertex,
            int vertexStride, float[] texMatrix, FloatBuffer texBuffer, int texStride) {

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0);
        //GlUtil.checkGlError("glUniformMatrix4fv");

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
        //GlUtil.checkGlError("glUniformMatrix4fv");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        //GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, coordsPerVertex, GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        //GlUtil.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        //GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, texStride,
                texBuffer);
        //GlUtil.checkGlError("glVertexAttribPointer");
    }

    @Override protected void drawArrays(int firstVertex, int vertexCount) {

        GLES20.glClearColor(0.0f, 0.0f, 0.5f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);
        //GlUtil.checkGlError("glDrawArrays");
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
}
