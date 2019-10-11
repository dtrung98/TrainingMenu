package com.ldt.parallaximageview;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import com.ldt.parallaximageview.base.GLTextureView;
import com.ldt.parallaximageview.shader.ShaderInstance;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class ParallaxRenderer implements GLTextureView.Renderer {
    private static final String TAG = "ParallaxRenderer";

    private int[] textures;
    private float mImageWidth = 0;
    private float mImageHeight = 0;
    private float mDrawWidth = 0;
    private float mDrawHeight = 0;
    private int[] mViewLocation = new int[2];
    private int[] mWindowsSize = new int[2];

    public ParallaxRenderer(String vertexSet, String fragmentSet) {
        this.vertexSet = vertexSet;
        this.fragmentSet = fragmentSet;
    }

    public ParallaxRenderer() {

    }

    public Bitmap getBitmap() {
        return mBitmaps[0];
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmaps[0] = bitmap;
        mImageWidth = bitmap.getWidth();
        mImageHeight = bitmap.getHeight();

        // ex : 1280 x 1920, bitmap is 800 x 480;
        // => This mean the bitmap will be drawn on 1280 x (1280 * 480 / 800 ) == 1280 x 768
        // => 768 / 1920 which means from - (768/1920)/2 and (768/1920)

        float result = ((mDrawWidth * mImageHeight/mImageWidth) / mDrawHeight);
        VERTEX_COORDINATES[1] = VERTEX_COORDINATES[4] =  result;
        VERTEX_COORDINATES[7] = VERTEX_COORDINATES[10] = - result;
        onPhotosSet();
    }

    private Bitmap[] mBitmaps = new Bitmap[2];
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

    private static Buffer VERTICES_BUFFER = ByteBuffer.allocateDirect(VERTICES.length * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(VERTICES).rewind();

    private static Buffer TEXCOORD_BUFFER = ByteBuffer.allocateDirect(TEXTURE_COORDINATES.length * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(TEXTURE_COORDINATES).rewind();

    private Buffer getVertexBuffer() {
        return ByteBuffer.allocateDirect(VERTEX_COORDINATES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(VERTEX_COORDINATES).rewind();
    }

    private int mBackgroundRenderer = 0;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0,0,0,0);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        createScene();
        onPhotosSet();

    }

    interface PositionDeterminer {
        void screenView(int[] size);
         void currentLocation(int[] location);
    }

    public void setPositionDeterminer(PositionDeterminer positionDeterminer) {
        mPositionDeterminer = positionDeterminer;
    }

    public void removePositionDeterminer() {
        mPositionDeterminer = null;
    }

    private PositionDeterminer mPositionDeterminer;

    public void shouldPositionTranslate(boolean active) {
        mActivePositionTranslate = active;
    }
    private boolean mActivePositionTranslate = false;


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mDrawWidth = width;
        mDrawHeight = height;
        resize();
        GLES20.glViewport(0, 0, width, height);
        if(mPositionDeterminer!=null)
        mPositionDeterminer.screenView(mWindowsSize);
        if(mWindowsSize[0]==0) mWindowsSize[0] = 1;
        if(mWindowsSize[1]==0) mWindowsSize[1] = 1;
    }

    private float imageAspect = 1f;

    private void onPhotosSet() {
      //  if(mBitmaps[0]==null) return;

        if(mBitmaps[0] != null)
        imageAspect = (float) mBitmaps[0].getHeight()/ mBitmaps[0].getWidth();
        else if(mBitmaps[1] != null) imageAspect = (float) mBitmaps[1].getHeight()/mBitmaps[1].getWidth();
        else imageAspect = 1;
        if(textures!=null) {
            int[] tem = new int[] {textures[0],textures[1]};
            GLES20.glDeleteTextures(2, tem, 0);
        }

        textures = new int[2];

        GLES20.glGenTextures(2, textures, 0);

        for (int i =1; i >=0; i--) {

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);


            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            //GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, GLES20.GL_RGBA, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mDepthMap.to);

            if(mBitmaps[i] != null)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmaps[i], 0);
        }

        int u_image0Location = GLES20.glGetUniformLocation(programId, ShaderInstance.IMAGE0);
        int u_image1Location = GLES20.glGetUniformLocation(programId, ShaderInstance.IMAGE1);

        // set which texture units to render with
        GLES20.glUniform1i(u_image0Location,0);
        GLES20.glUniform1i(u_image1Location,1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[0]);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[1]);

    }

    private float vth = 15;
    private float hth = 35;// 35, 15, 14, 35

    private void resize() {
        float a1, a2;
        if(mDrawHeight/ mDrawWidth < imageAspect)  {
            a1 = 1;
            a2 = (mDrawHeight/ mDrawWidth) / imageAspect;
        } else {
            a1 = (mDrawWidth / mDrawHeight) * imageAspect;
            a2 = 1;
        }

        //this.uResolution.set( this.width, this.height, a1, a2 );
        //this.uRatio.set( 1/this.ratio );
        //this.uThreshold.set( this.hth, this.vth );
        //this.gl.viewport( 0, 0, this.width*this.ratio, this.height*this.ratio );

        GLES20.glUniform4f(uResolutionLocation,mDrawWidth, mDrawHeight,a1,a2);
        GLES20.glUniform1f(uRatioLocation,1);

        GLES20.glUniform2f(uThresholdLocation,hth,vth);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Draw
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        long now = System.currentTimeMillis();
        long time = ( now - startTime ) / 1000;

        GLES20.glUniform1f(uTimeLocation,(int)time);
        mMouseX = ((now % 3700)  / 3700f);
        mMouseY = (((now + 573) % 6000 ) / 6000f);

        mMouseX*= 4;
        mMouseY*= 4;

        // 0 ->2 mean -1 -> 1, how ?
        // 2 -> 4 mean 1 ->-1, how ?

        if(mMouseX<2) mMouseX = -1 + mMouseX; // min = -1 + 0 = -1 ; max = -1 + 2 = 1
        else mMouseX = (4 - mMouseX) - 1; // min = 4 - 2 - 1 = 1; max =  -1

        if(mMouseY<2) mMouseY = -1 + mMouseY; // min = -1 + 0 = -1 ; max = -1 + 2 = 1
        else mMouseY = (4 - mMouseY) - 1; // min = 4 - 2 - 1 = 1; max =  -1


       // mMouseX *=3;
       // mMouseY *=3;
        //mMouseY =-3;
       // mMouseX = -3f;

        GLES20.glUniform2f(uMouseLocation,mMouseX, mMouseY);
        if(mActivePositionTranslate&&mPositionDeterminer!=null)
           mPositionDeterminer.currentLocation(mViewLocation);
        else {
            mViewLocation[0] = 0;
            mViewLocation[1] = 0;
        }

        GLES20.glUniform2f(uTranslateLocation,(float)mViewLocation[0]/mWindowsSize[0], (float)mViewLocation[1]/mWindowsSize[1]);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
    }

    private long startTime;

    private void createScene() {
        startTime = System.currentTimeMillis();
        attachShaders();
    }

    private int programId = 0;
    private float mMouseX = 0.5f;
    private float mMouseY = 0.5f;
    private String vertexSet;
    private String fragmentSet;
    private String getVertex() {
        if(vertexSet==null||vertexSet.isEmpty())
            return ShaderInstance.vertexShader;
        return vertexSet;
    }

    private String getFragment() {
        if(fragmentSet==null||fragmentSet.isEmpty())
            return ShaderInstance.INSTANCE.getFragmentShader();
        return fragmentSet;
    }

    private void attachShaders() {
        programId = createProgram(createShader( GLES20.GL_VERTEX_SHADER, getVertex()), createShader( GLES20.GL_FRAGMENT_SHADER, getFragment()));
        GLES20.glUseProgram(programId);

        uResolutionLocation = GLES20.glGetUniformLocation(programId, ShaderInstance.RESOLUTION);
        uTranslateLocation = GLES20.glGetUniformLocation(programId, ShaderInstance.TRANSLATE);

        uMouseLocation = GLES20.glGetUniformLocation(programId, ShaderInstance.MOUSE);
        uTimeLocation = GLES20.glGetUniformLocation(programId, ShaderInstance.TIME);
        uRatioLocation = GLES20.glGetUniformLocation(programId, ShaderInstance.PIXEL_RATIO);
        uThresholdLocation = GLES20.glGetUniformLocation(programId, ShaderInstance.THRESHOLD);

        int buffer[] = new int[1];
        GLES20.glGenRenderbuffers(1,buffer,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,buffer[0]);
        //GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,VERTICES,GLES20.GL_STATIC_DRAW);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,VERTICES.length * 4,VERTICES_BUFFER,GLES20.GL_STATIC_DRAW);

        positionLocation = GLES20.glGetAttribLocation(programId, ShaderInstance.A_POSITION);
        GLES20.glEnableVertexAttribArray(positionLocation);
        GLES20.glVertexAttribPointer(positionLocation,2,GLES20.GL_FLOAT,false,0,0);
    }


    private void addTexture() {

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
        String info = GLES20.glGetShaderInfoLog(id);
        if(!info.isEmpty())
        Log.d(TAG, "createShader: "+info);

        return id;
    }

    public void onSurfaceDestroyed(GL10 gl) {

    }

    public Bitmap getDepthMap() {
        return mBitmaps[1];
    }

    public void setDepthMap(Bitmap depthMap) {
        mBitmaps[1] = depthMap;
        onPhotosSet();
    }

    public void removeBitmaps() {
        mBitmaps[0] = null;
        mBitmaps[1] = null;
        onPhotosSet();
    }

    private int uResolutionLocation;
    private int uTranslateLocation;
    private int uMouseLocation;
    private int uRatioLocation;
    private int uTimeLocation;
    private int uThresholdLocation;

    private int positionLocation;
}
