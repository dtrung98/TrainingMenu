package com.zalo.trainingmenu.fundamental.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import java.io.BufferedReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class BitmapDrawingRenderer implements GLSurfaceView.Renderer {

    private int[] textures;
    private int mImageWidth = 0;
    private int mImageHeight = 0;
    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        mImageWidth = bitmap.getWidth();
        mImageHeight = bitmap.getHeight();
    }

    private Bitmap mBitmap;
    private static final float[] VERTEX_COORDINATES = new float[] {
            -1.0f, +1.0f, 0.0f,
            +1.0f, +1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            +1.0f, -1.0f, 0.0f
    };

    private static final float[] TEXTURE_COORDINATES = new float[] {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };

    private static final float[] VERTICES = new float[] {
            -1, -1,
            1, -1,
            -1, 1,
            1, 1
    };


    private static final Buffer TEXCOORD_BUFFER = ByteBuffer.allocateDirect(TEXTURE_COORDINATES.length * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(TEXTURE_COORDINATES).rewind();
    private static final Buffer VERTEX_BUFFER = ByteBuffer.allocateDirect(VERTEX_COORDINATES.length * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(VERTEX_COORDINATES).rewind();

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        textures = new int[1];
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glGenTextures(1, textures, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        if(mBitmap!=null)
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0,mBitmap, 0);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(VERTICES.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glActiveTexture(GL10.GL_TEXTURE0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, VERTEX_BUFFER);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, TEXCOORD_BUFFER);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
    }
}
