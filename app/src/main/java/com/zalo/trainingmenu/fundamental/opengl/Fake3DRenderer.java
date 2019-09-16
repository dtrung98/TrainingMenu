package com.zalo.trainingmenu.fundamental.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.zalo.trainingmenu.fundamental.texture.GLTextureView;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.zalo.trainingmenu.fundamental.opengl.ShaderInstance.fragmentShader;
import static com.zalo.trainingmenu.fundamental.opengl.ShaderInstance.vertexShader;

class Fake3DRenderer implements GLTextureView.Renderer {

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

    private Bitmap mBitmap;
    private Bitmap mDepthMap;
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

    private int mBackgroundRenderer = 0;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0,0,0,0);
        enableTransparency();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mDrawWidth = width;
        mDrawHeight = height;
        gl.glViewport(0, 0, width, height);
        initialize();
    }

    private void initialize() {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Draw
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

    }

    private void enableTransparency() {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        attachShaders();
    }

    private int mProgramId = 0;
    private int mMouseX = 0;
    private int mMouseY = 0;


    private void attachShaders() {
        mProgramId = createProgram(createShader( GLES20.GL_VERTEX_SHADER, vertexShader),
                createShader( GLES20.GL_FRAGMENT_SHADER, fragmentShader));
        GLES20.glUseProgram(mProgramId);
    }

    private int  createProgram(int vertexShader, int fragmentShader) {
        int id =  GLES20.glCreateProgram();
        GLES20.glAttachShader(id, vertexShader);
        GLES20.glAttachShader(id, fragmentShader);
        GLES20.glLinkProgram(id);
        return id;
    }

    private int createShader(int type, String shader) {
        int id = GLES20.glCreateShader(type);
        GLES20.glShaderSource(id, shader);
        GLES20.glCompileShader(id);

        return id;
    }

    @Override
    public void onSurfaceDestroyed(GL10 gl) {

    }

    public Bitmap getDepthMap() {
        return mDepthMap;
    }

    public void setDepthMap(Bitmap depthMap) {
        mDepthMap = depthMap;
    }
}
