package com.zalo.trainingmenu.fundamental.opengl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import com.zalo.trainingmenu.fundamental.opengl.texture.GLTextureView;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class BitmapDrawingRenderer implements GLTextureView.Renderer {

    private int[] textures;
    private float mImageWidth = 0;
    private float mImageHeight = 0;
    private float mDrawWidth = 0;
    private float mDrawHeight = 0;
    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        mImageWidth = bitmap.getWidth();
        mImageHeight = bitmap.getHeight();

        // ex : 1280 x 1920, bitmap is 800 x 480;
        // => This mean the bitmap will be drawn on 1280 x (1280 * 480 / 800 ) == 1280 x 768
        // => 768 / 1920 which means from - (768/1920)/2 and (768/1920)

        float result = ((mDrawWidth * mImageHeight/mImageWidth) / mDrawHeight);
        VERTEX_COORDINATES[1] = VERTEX_COORDINATES[4] =  result;
        VERTEX_COORDINATES[7] = VERTEX_COORDINATES[10] = - result;
    }

    private Bitmap mBitmap = BitmapFactory.decodeFile("/storage/emulated/0/Download/ball.jpg");;
    private Bitmap mDepthMap = BitmapFactory.decodeFile("/storage/emulated/0/Download/ball_depth.jpg");
    private static float[] VERTEX_COORDINATES = new float[] {
            -1f, +0.75f, 0.0f,
            +1.0f, +0.75f, 0.0f,
            -1.0f, -0.75f, 0.0f,
            +1.0f, -0.75f, 0.0f
    };

    private static final float[] TEXTURE_COORDINATES = new float[] {
            0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };

    private static final float[] VERTICES = new float[] {
            -1, -1f,
            1, -1,
            -1, 1,
            1, 1
    };




    private static Buffer TEXCOORD_BUFFER = ByteBuffer.allocateDirect(TEXTURE_COORDINATES.length * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(TEXTURE_COORDINATES).rewind();

    private Buffer getVertexBuffer() {
        return ByteBuffer.allocateDirect(VERTEX_COORDINATES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(VERTEX_COORDINATES).rewind();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        textures = new int[2];
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glGenTextures(2, textures, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        gl.glActiveTexture(GL10.GL_TEXTURE0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        if(mBitmap!=null)
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0,mBitmap, 0);


        // Depth

        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[1]);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        gl.glActiveTexture(GL10.GL_TEXTURE1);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

       // if(mDepthMap!=null)
          //  GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0,mDepthMap, 0);


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mDrawWidth = width;
        mDrawHeight = height;
        gl.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        gl.glActiveTexture(GL10.GL_TEXTURE0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

       /* gl.glActiveTexture(GL10.GL_TEXTURE1);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[1]);*/

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, getVertexBuffer());
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, TEXCOORD_BUFFER);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
    }


    @Override
    public void onSurfaceDestroyed(GL10 gl) {

    }
}
