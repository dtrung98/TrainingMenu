package com.ldt.parallaximageview;

import android.graphics.Bitmap;
import android.graphics.Shader;
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

import okhttp3.internal.Util;

class ParallaxRenderer implements GLTextureView.Renderer {
    private static final String TAG = "ParallaxRenderer";
    public static final int TYPE_ORIGINAL = 0;
    public static final int TYPE_DEPTH = 1;

    private int[] textures = new int[2];
    private float mDrawWidth = 0;
    private float mDrawHeight = 0;
    private int[] mViewLocation = new int[2];
    private int[] mWindowsSize = new int[2];
    private float[] mTranslate = new float[2];

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
        requestUpdatePhoto(bitmap,TYPE_ORIGINAL);
    }

    private Bitmap[] mBitmaps = new Bitmap[2];


    private static final float[] TEXTURE_COORDINATES = new float[] {
            0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };

    private static final float[] VERTICES = new float[] {
            -1, -1,
             1, -1,
            -1,  1,
             1,  1
    };

    private static Buffer VERTICES_BUFFER = ByteBuffer.allocateDirect(VERTICES.length * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(VERTICES).rewind();

    private static Buffer TEXCOORD_BUFFER = ByteBuffer.allocateDirect(TEXTURE_COORDINATES.length * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(TEXTURE_COORDINATES).rewind();

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
       // GLES20.glClearColor(1.0f,1.0f,0.0f,0f);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        createScene();
        updatePhotoIfNeed();
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
        if(mWindowsSize[0] == 0) mWindowsSize[0] = 1;
        if(mWindowsSize[1] == 0) mWindowsSize[1] = 1;

    }

    private float imageAspect = 1f;
    private boolean requestUpdateOriginal = false;
    private boolean requestUpdateDepth = false;
    private synchronized void requestUpdatePhoto(Bitmap bitmap, int which) {
        if(which==TYPE_ORIGINAL) {
            requestUpdateOriginal = true;
            mBitmaps[0] = bitmap;
        }
        else if(which== TYPE_DEPTH) {
            requestUpdateDepth = true;
            mBitmaps[1] = bitmap;
        }
    }

    private synchronized void updatePhotoIfNeed() {

        if(requestUpdateOriginal) {
            requestUpdateOriginal = false;
            onPhotosSet();
        }

        if(requestUpdateDepth) {
            requestUpdateDepth = false;
            onPhotosSet();
        }
    }

    public void checkGLError() {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "glError: " + error);
        }
    }

    public void onPhotoSet(Bitmap bitmap, int which) {
        if(which!=TYPE_ORIGINAL&&which!=TYPE_DEPTH) return;
        configAspect();

        // remove old texture if any
        if(textures[which]!=0) {
            GLES20.glDeleteTextures(1,textures,which);
            textures[which] = 0;
        }
        // generate new texture if bitmap available
        if(bitmap!=null) {
            GLES20.glGenTextures(1, textures, which);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[which]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);


        }
        if(textures[0]!=0) {
            int uImageLocation = GLES20.glGetUniformLocation(programId,ShaderInstance.IMAGE0);
            GLES20.glUniform1i(uImageLocation, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        }
        if(textures[1]!=0) {
            int uImageLocation = GLES20.glGetUniformLocation(programId,ShaderInstance.IMAGE1);
            GLES20.glUniform1i(uImageLocation, 1);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
        }
    }

    private void configAspect() {
        if(mBitmaps[0] != null)
            imageAspect = (float) mBitmaps[0].getHeight()/ mBitmaps[0].getWidth();
        else if(mBitmaps[1] != null) imageAspect = (float) mBitmaps[1].getHeight()/mBitmaps[1].getWidth();
        else imageAspect = 1;
        Log.d(TAG, "configAspect: "+imageAspect);

    }

    private void onPhotosSet() {

        // config image aspect

        configAspect();

        if(textures!=null) {
            GLES20.glDeleteTextures(1, textures, 0);
            GLES20.glDeleteTextures(1, textures, 1);
        }

        textures = new int[2];

        GLES20.glGenTextures(2, textures, 0);

        for (int i =0; i < 2; i++) {

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
        updatePhotoIfNeed();
      //  GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
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
        if(mActivePositionTranslate&&mPositionDeterminer!=null) {
            mPositionDeterminer.currentLocation(mViewLocation);
            if(mViewLocation[1] > mWindowsSize[1])
            mTranslate[1] = 0.5f;
            else if(mViewLocation[1]>mWindowsSize[1]/2 - mDrawHeight/2)
                mTranslate[1] = 0.5f * fraction(mWindowsSize[1]/2f - mDrawHeight/2f,mWindowsSize[1],mViewLocation[1]);
            else if(mViewLocation[1] > -mDrawHeight)
                mTranslate[1] =  -0.5f + 0.5f*fraction( -mDrawHeight,mWindowsSize[1]/2f - mDrawHeight/2f,mViewLocation[1]);
            else mTranslate[1] = -0.5f;
        }
        else {
           mTranslate[0] = mTranslate[1] = 0;
        }
        if(mPositionDeterminer instanceof ParallaxImageView)
        Log.d(TAG, "render view "+((ParallaxImageView) mPositionDeterminer).getName()+" with location ["+mViewLocation[0]+", "+mViewLocation[1]+"], translateY= "+mTranslate[1]+", drawHeight = "+mDrawHeight+", windowsSize");
        else
            Log.d(TAG, "render view unknown with location ["+mViewLocation[0]+", "+mViewLocation[1]+"], translateY= "+mTranslate[1]);

        GLES20.glUniform2f(uTranslateLocation,mTranslate[0], mTranslate[1]);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
    }

    private long startTime;

    public static float interpolate(float start,float end,float f) {
        return start + f * (end - start);
    }

    public static float fraction(float start, float end, float current) {
        return (current - start)/(end - start);
    }

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
        requestUpdatePhoto(depthMap,TYPE_DEPTH);
    }

    public void removeBitmaps() {
        requestUpdatePhoto(null, TYPE_ORIGINAL);
        requestUpdatePhoto(null, TYPE_DEPTH);
    }

    private int uResolutionLocation;
    private int uTranslateLocation;
    private int uMouseLocation;
    private int uRatioLocation;
    private int uTimeLocation;
    private int uThresholdLocation;

    private int positionLocation;
}
