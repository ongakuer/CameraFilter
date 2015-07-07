package me.relex.camerafilter.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.support.annotation.DrawableRes;
import java.nio.FloatBuffer;
import me.relex.camerafilter.R;
import me.relex.camerafilter.gles.GlUtil;

public class CameraFilterBlend extends CameraFilter {

    protected int mExtraTextureId;
    protected int maExtraTextureCoordLoc;
    protected int muExtraTextureLoc;

    public CameraFilterBlend(Context context, @DrawableRes int drawableId) {
        super(context);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;    // No pre-scaling
        final Bitmap bitmap =
                BitmapFactory.decodeResource(context.getResources(), drawableId, options);
        mExtraTextureId = GlUtil.createTexture(GLES20.GL_TEXTURE_2D, bitmap);
    }

    @Override protected int createProgram(Context applicationContext) {
        return GlUtil.createProgram(applicationContext, R.raw.vertex_shader_two_input,
                R.raw.fragment_shader_ext_blend);
    }

    @Override protected void getGLSLValues() {
        super.getGLSLValues();
        maExtraTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aExtraTextureCoord");
        muExtraTextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "uExtraTexture");
    }

    @Override protected void bindTexture(int textureId) {
        super.bindTexture(textureId);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mExtraTextureId);
        GLES20.glUniform1i(muExtraTextureLoc, 1);
    }

    @Override
    protected void bindGLSLValues(float[] mvpMatrix, FloatBuffer vertexBuffer, int coordsPerVertex,
            int vertexStride, float[] texMatrix, FloatBuffer texBuffer, int texStride) {
        super.bindGLSLValues(mvpMatrix, vertexBuffer, coordsPerVertex, vertexStride, texMatrix,
                texBuffer, texStride);
        GLES20.glEnableVertexAttribArray(maExtraTextureCoordLoc);
        GLES20.glVertexAttribPointer(maExtraTextureCoordLoc, 2, GLES20.GL_FLOAT, false, texStride,
                texBuffer);
    }

    @Override protected void unbindGLSLValues() {
        super.unbindGLSLValues();

        GLES20.glDisableVertexAttribArray(maExtraTextureCoordLoc);
    }

    @Override protected void unbindTexture() {
        super.unbindTexture();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }
}