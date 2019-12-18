package com.ldt.vrview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.ldt.vrview.model.VRPhoto;
import com.ldt.vrview.shader.ShaderKt;
import com.ldt.vrview.transform.base.BaseTransformer;
import com.ldt.vrview.util.GlSelfUtil;

import java.lang.ref.WeakReference;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static java.lang.Math.sqrt;

public class PanoramaSphere {
    private static final String TAG = "PanoramaSphere";
    private static int nextID =1;
    private static int getNextID() {
       return nextID++;
    }
    public int id = getNextID();

    private static final float UNIT_SIZE = 1f;// 单位尺寸
    private float r = 2f; // 球的半径

    private final float radius=2f;

    private int vCount = 0;// 顶点个数，先初始化为0

    public static final float FROM_RADS_TO_DEGS = 57.2957795f;
    private Resources res;

    private int mHProgram;
    private int mHUTexture;
    private int mHProjMatrix;
    private int mHViewMatrix;
    private int mHModelMatrix;
    private int mHRotateMatrix;
    private int mHPosition;
    private int mHCoordinate;
    private int mHEdgeCoord;

    private int textureId = 0;

    private float[] mViewMatrix=new float[16];
    private float[] mProjectMatrix=new float[16];
    private float[] mModelMatrix=new float[16];


    private boolean mIsInitSensor = false;
    private float[] mRotateMatrix = new float[16];

    private float[] mInitRotateVector;
    private float[] mSensorVector;
    private float mTextureAspect = 1f;
    private ArrayList<BaseTransformer> mRotationAngles = new ArrayList<>();
    public void getTextureCoordSize(float[] value2){
        value2[0] = 2*mTextureAspect;
        value2[1] = 2;

    }
    private final float[] mCenterMatrix = new float[] {
            0,0,-1,0,
            -1,0,0,0,
            0,1,0,0,
            0,0,0,1
    };

    /*  1,0,0,0,
           0,0,-1,0,
           0,1,0,0,
           0,0,0,1*/
    private float[] mRotateVector;
    /*
        [0]          [1]        [2]    [3]     [4]           [5]        [6]     [7]     [8]        [9]         [10]
    -0.9956346 -0.008177486 0.09297807 0.0 0.0099688275 -0.99977326 0.018818181 0.0 0.0928031  0.019662913 0.99549025  0.0 0.0 0.0 0.0 1.0
    -0.6924454  0.009915203 0.7214021  0.0 0.71979225   -0.05866182 0.6917064   0.0 0.04917717 0.99822867  0.033483267 0.0 0.0 0.0 0.0 1.0
     */

    DecimalFormat df = new DecimalFormat("0.00");
    private float[] startRotateSensorOrient = new float[3];
    private float[] currentRotateSensorOrient = new float[3];
    private float[] currentOrient = new float[3];


    private Buffer posBuffer;
    private Buffer cooBuffer;
    private int vSize;
    private float skyRate=3f;

    private final WeakReference<VRControlView> mRefView;

    public PanoramaSphere(VRControlView view, Context context) {
        mRefView = new WeakReference<>(view);
    }

    private VRPhoto mVRPhoto;
    private boolean shouldResetVRPhoto = false;
    public void setVRPhoto(VRPhoto photo) {
        shouldResetVRPhoto = true;
        mVRPhoto = photo;
        float[] angles;

        if(mVRPhoto!=null) {
            angles = mVRPhoto.getAngleAreas();
        } else angles = VRPhoto.getDefaultAngleAreas();

        mHFromAngle = angles[0];
        mVFromAngle = angles[1];
        mHToAngle = angles[0] + angles[2];
        mVToAngle = angles[1] + angles[3];

    }

    public void create(){

        mHProgram= GlSelfUtil.createGlProgram(ShaderKt.VERTEX, ShaderKt.FRAGMENT);
        GLES20.glUseProgram(mHProgram);
        mHProjMatrix=GLES20.glGetUniformLocation(mHProgram,"uProjMatrix");
        mHViewMatrix=GLES20.glGetUniformLocation(mHProgram,"uViewMatrix");
        mHModelMatrix=GLES20.glGetUniformLocation(mHProgram,"uModelMatrix");
        mHRotateMatrix=GLES20.glGetUniformLocation(mHProgram,"uRotateMatrix");
        mHUTexture=GLES20.glGetUniformLocation(mHProgram,"uTexture");
        mHEdgeCoord = GLES20.glGetUniformLocation(mHProgram,"aEdgeCoord");

        mHPosition =GLES20.glGetAttribLocation(mHProgram,"aPosition");
        mHCoordinate=GLES20.glGetAttribLocation(mHProgram,"aCoordinate");

      //  if(shouldResetVRPhoto)
        textureId = createTextureIfAvailable();
        long start = System.currentTimeMillis();
        calculateAttribute();
        Log.d(TAG, "vr "+id+" create in "+(System.currentTimeMillis() - start));
    }

    private float mVFromAngle = 0;
    private float mVToAngle = 180;
    private float mHFromAngle = 0;
    private float mHToAngle = 360;

    public synchronized void overrideAreaAngles(float vFrom, float vTo, float hFrom, float hTo) {
        mVFromAngle = vFrom;
        mVToAngle = vTo;
        mHFromAngle = hFrom;
        mHToAngle = hTo;
        calculateAttribute();
    }

    public float[] getAngles() {
        return new float[] {mVFromAngle, mVToAngle, mHFromAngle, mHToAngle};
    }

    private void calculateVertices() {

        int angleSpanInDegree = 2;
        float angleSpan = angleSpanInDegree/FROM_RADS_TO_DEGS;

        double vAngleInRad = 0;
        double hAngleInRad = 0;

        int alSize = (180/angleSpanInDegree) * (360/angleSpanInDegree) * (18);
        int texSize = (180/angleSpanInDegree) * (360/angleSpanInDegree) * (12);

        float[] alVertix = new float[alSize];
        float[] texVertix = new float[texSize];

        float x0,y0,z0,x1,y1,z1,x2,y2,z2,x3,y3,z3;
        float s0,s1,t0,t1;

        int alPosition = 0;
        int texPosition = 0;
        for (int vAngleDegree = 0; vAngleDegree < 180; vAngleDegree += angleSpanInDegree){
            vAngleInRad = vAngleDegree/FROM_RADS_TO_DEGS;
            for (int hAngleDegree = 0; hAngleDegree < 360; hAngleDegree += angleSpanInDegree){
                hAngleInRad = hAngleDegree/FROM_RADS_TO_DEGS;
                x0 = (float) (radius* Math.sin(vAngleInRad) * Math.cos(hAngleInRad));
                y0 = (float) (radius* Math.sin(vAngleInRad) * Math.sin(hAngleInRad));
                z0 = (float) (radius * Math.cos((vAngleInRad)));

                x1 = (float) (radius* Math.sin(vAngleInRad) * Math.cos(hAngleInRad + angleSpan));
                y1 = (float) (radius* Math.sin(vAngleInRad) * Math.sin(hAngleInRad + angleSpan));
                z1 = (float) (radius * Math.cos(vAngleInRad));

                x2 = (float) (radius* Math.sin(vAngleInRad + angleSpan) * Math.cos(hAngleInRad + angleSpan));
                y2 = (float) (radius* Math.sin(vAngleInRad + angleSpan) * Math.sin(hAngleInRad + angleSpan));
                z2 = (float) (radius * Math.cos(vAngleInRad + angleSpan));

                x3 = (float) (radius* Math.sin(vAngleInRad + angleSpan) * Math.cos(hAngleInRad));
                y3 = (float) (radius* Math.sin(vAngleInRad + angleSpan) * Math.sin(hAngleInRad));
                z3 = (float) (radius * Math.cos(vAngleInRad + angleSpan));


                alVertix[alPosition  ] = x1;
                alVertix[alPosition+1] = y1;
                alVertix[alPosition+2] = z1;
                alVertix[alPosition+3] = x0;
                alVertix[alPosition+4] = y0;
                alVertix[alPosition+5] = z0;
                alVertix[alPosition+6] = x3;
                alVertix[alPosition+7] = y3;
                alVertix[alPosition+8] = z3;

                // hAngleInRad = hAngleDegree/FROM_RADS_TO_DEGS;
                // = hAngleDegree/ (180/PI)
                // = PI * hAngleDegree/180
                s0 = hAngleDegree/360f;// (float) (hAngleInRad / Math.PI/2); // == hAngleDegree/360
                s1 = (hAngleDegree + angleSpanInDegree) / 360f ;//(float) ((hAngleInRad + angleSpan)/Math.PI/2);
                t0 = vAngleDegree/180f; //(float) (vAngleInRad / Math.PI);
                t1 = (vAngleDegree+ angleSpanInDegree)/ 180f; //(float) ((vAngleInRad + angleSpan) / Math.PI);


                texVertix[texPosition  ] = s1;
                texVertix[texPosition+1] = t0;
                texVertix[texPosition+2] = s0;
                texVertix[texPosition+3] = t0;
                texVertix[texPosition+4] = s0;
                texVertix[texPosition+5] = t1;

                alVertix[alPosition+ 9] = x1;
                alVertix[alPosition+10] = y1;
                alVertix[alPosition+11] = z1;
                alVertix[alPosition+12] = x3;
                alVertix[alPosition+13] = y3;
                alVertix[alPosition+14] = z3;
                alVertix[alPosition+15] = x2;
                alVertix[alPosition+16] = y2;
                alVertix[alPosition+17] = z2;

                texVertix[texPosition+6] = s1; // x1 y1
                texVertix[texPosition+7] = t0;
                texVertix[texPosition+8] = s0; // x3 y3
                texVertix[texPosition+9] = t1;
                texVertix[texPosition+10] = s1; // x2 y3
                texVertix[texPosition+11] = t1;

                alPosition+=18;
                texPosition+=12;

            }
        }

        vCount = alSize/3;

        posBuffer = ByteBuffer.allocateDirect(alSize*4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(alVertix)
                .position(0);

        cooBuffer = ByteBuffer.allocateDirect(texSize*4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(texVertix)
                .position(0);
    }

    private synchronized void calculateAttribute(){
        Log.d(TAG, "calculate attribute with angle: hFrom = "+mVFromAngle+", vFrom = "+mHFromAngle+", hTo = "+mHToAngle+", vTo = "+mVToAngle);
        ArrayList<Float> alVertix = new ArrayList<>();
        ArrayList<Float> textureVertix = new ArrayList<>();
        float angleSpanDegree = 2; // 2 degree, which mean 180/2 = 90 loop
        float angleSpan = angleSpanDegree/FROM_RADS_TO_DEGS;

        double vAngle = 0;
        double hAngle = 0;
        /*
        Chạy từ dưới cùng lên trên cùng
         */


        //float vFrom = vFromDeg/FROM_RADS_TO_DEGS;
        //float vTo = vToDeg/FROM_RADS_TO_DEGS;
        for (double vAngleDegree = 0; vAngleDegree < 180; vAngleDegree += angleSpanDegree){
            vAngle = vAngleDegree/FROM_RADS_TO_DEGS;
            for (double hAngleDegree = mHFromAngle; hAngleDegree < 360; hAngleDegree += angleSpanDegree){
                hAngle = hAngleDegree/FROM_RADS_TO_DEGS;

                // radius là 2, nghĩa là viewport = 1 nửa texture
                float x0 = (float) (radius* Math.sin(vAngle) * Math.cos(hAngle));
                float y0 = (float) (radius* Math.sin(vAngle) * Math.sin(hAngle));
                float z0 = (float) (radius * Math.cos((vAngle)));

                float x1 = (float) (radius* Math.sin(vAngle) * Math.cos(hAngle + angleSpan));
                float y1 = (float) (radius* Math.sin(vAngle) * Math.sin(hAngle + angleSpan));
                float z1 = (float) (radius * Math.cos(vAngle));

                float x2 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.cos(hAngle + angleSpan));
                float y2 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.sin(hAngle + angleSpan));
                float z2 = (float) (radius * Math.cos(vAngle + angleSpan));

                float x3 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.cos(hAngle));
                float y3 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.sin(hAngle));
                float z3 = (float) (radius * Math.cos(vAngle + angleSpan));

                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x0);
                alVertix.add(y0);
                alVertix.add(z0);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);

                float s0 = (float) (hAngleDegree - mHFromAngle) / ((mHToAngle - mHFromAngle));
                float s1 = (float) (hAngleDegree - mHFromAngle + angleSpanDegree) / ((mHToAngle - mHFromAngle));
                float t0 = (float) ((vAngleDegree - mVFromAngle) / (mVToAngle - mVFromAngle)); // from 0 to (
                float t1 = (float) ((vAngleDegree + angleSpanDegree - mVFromAngle) / (mVToAngle - mVFromAngle));

                textureVertix.add(s1);// x1 y1对应纹理坐标
                textureVertix.add(t0);
                textureVertix.add(s0);// x0 y0对应纹理坐标
                textureVertix.add(t0);
                textureVertix.add(s0);// x3 y3对应纹理坐标
                textureVertix.add(t1);

                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);
                alVertix.add(x2);
                alVertix.add(y2);
                alVertix.add(z2);

                textureVertix.add(s1);// x1 y1对应纹理坐标
                textureVertix.add(t0);
                textureVertix.add(s0);// x3 y3对应纹理坐标
                textureVertix.add(t1);
                textureVertix.add(s1);// x2 y3对应纹理坐标
                textureVertix.add(t1);
            }
        }
        vCount = alVertix.size() / 3;
        posBuffer = convertToFloatBuffer(alVertix);
        cooBuffer = convertToFloatBuffer(textureVertix);
    }

    private static FloatBuffer convertToFloatBuffer(ArrayList<Float> data){
        float[] d=new float[data.size()];
        for (int i=0;i<d.length;i++){
            d[i]=data.get(i);
        }

        ByteBuffer buffer=ByteBuffer.allocateDirect(data.size()*4);
        buffer.order(ByteOrder.nativeOrder());
        FloatBuffer ret=buffer.asFloatBuffer();
        ret.put(d);
        ret.position(0);

        return ret;
    }

    public Bitmap mBitmap;
    private Bitmap getBitmap() {
        if(mVRPhoto==null) return null;
        return mVRPhoto.getBitmap();
    }
    int[] texture=new int[1];

    private int createTextureIfAvailable(){

        shouldResetVRPhoto = false;

        // delete previous texture
        if(textureId!=0) {
            texture[0] = textureId;
            GLES20.glDeleteTextures(1,texture,0);
            Log.d(TAG, "deleted texture id "+textureId);
        }

        if (getBitmap() != null && !getBitmap().isRecycled()) {
            int[] maxSize = new int[1];
            //GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxSize, 0);
            //Log.d(TAG, "max possible texture size is "+maxSize[0]);

                //生成纹理
                GLES20.glGenTextures(1, texture, 0);
                //生成纹理
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
                //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_MIRRORED_REPEAT);
                //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_MIRRORED_REPEAT);
                //根据以上指定的参数，生成一个2D纹理
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, getBitmap(), 0);
                return texture[0];
            }
        return 0;
    }

    private int w, h;
    public void setSize(int width,int height){
        //计算宽高比
        w = width;
        h = height;
        mTextureAspect=(float)width/height;
        skyRate = 1;
        //设置透视投影
        //Matrix.frustumM(mProjectMatrix, 0, -ratio*skyRate, ratio*skyRate, -1*skyRate, 1*skyRate, 1, 300);
        //透视投影矩阵/视锥

        //Matrix.perspectiveM(mProjectMatrix,0,90f,mTextureAspect,1,300);
        updateTransformValue();
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f,0.0f, 0.0f, 0.0f,1f, 0f,1.0f, 0.0f);
        //模型矩阵
        Matrix.setIdentityM(mModelMatrix,0);

        Log.d(TAG, "vr "+id+" set size");
    }

    public void draw(){
        if(shouldResetVRPhoto) {
            textureId = createTextureIfAvailable();
            calculateAttribute();
        }
        //GLES20.glClearColor(1,1,1,1);
        Log.d(TAG, "vr "+id+" draw with texture is "+((mVRPhoto==null) ? "null": "available")+", texture id = "+textureId+", size = "+w+", "+h);
        if(textureId!=0) {
            try {

                // cập nhật transform matrix mới nhất
                GLES20.glUniformMatrix4fv(mHProjMatrix, 1, false, mProjectMatrix, 0);
                GLES20.glUniformMatrix4fv(mHViewMatrix, 1, false, mViewMatrix, 0);
                GLES20.glUniformMatrix4fv(mHModelMatrix, 1, false, mModelMatrix, 0);
                GLES20.glUniformMatrix4fv(mHRotateMatrix, 1, false, mRotateMatrix, 0);

                GLES20.glUniform4f(mHEdgeCoord,0,0,0,0);


                // kích hoạt texture
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

                // kích hoạt
                GLES20.glVertexAttribPointer(mHPosition, 3, GLES20.GL_FLOAT, false, 0, posBuffer);
                GLES20.glEnableVertexAttribArray(mHPosition);


                GLES20.glVertexAttribPointer(mHCoordinate, 2, GLES20.GL_FLOAT, false, 0, cooBuffer);
                GLES20.glEnableVertexAttribArray(mHCoordinate);

                // xài vCount từng này điểm trong 2 cái buffer trên kia
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

                // vô hiệu hóa 2 cái buffer đó đi
                GLES20.glDisableVertexAttribArray(mHPosition);
                GLES20.glDisableVertexAttribArray(mHCoordinate);
            } catch (Exception ignored) {
                Log.d(TAG, "exception");
            }
        }
    }


  /*  private void calculateBasedOnGravity(float[] vectors) {
        SensorManager.getRotationMatrixFromVector(mRotateMatrix,vectors);
    }


    private void formatMatrix(float[] M, float[] in) {
        System.arraycopy(in,0,M,0,16);
        double xLen = sqrt(M[0]*M[0] + M[1]*M[1]); // Singularity if either of these
        double yLen = sqrt(M[4]*M[4] + M[5]*M[5]); //  is equal to zero.

        M[0]/=xLen; M[1]/=xLen; M[2]=0; // Set the x column
        M[4]/=yLen; M[5]/=yLen; M[6]=0; // Set the y column
        M[8]=0; M[9]=0; M[10]=1;        // Set the z column
    }*/

    /*private void calculateV4(float[] vectors) {
        if(!mIsInitSensor) {
            mIsInitSensor = true;
            SensorManager.getRotationMatrixFromVector(initSensorM,vectors);
            for (BaseTransformer rotation :
                    mRotationAngles) {
                rotation.reset();
            }
        }
        SensorManager.getRotationMatrixFromVector(currentSensorM,vectors);

        applyRotate(vectors);

        switch (mOrientation) {
            case Surface.ROTATION_0:
                Matrix.rotateM(temp,0,mCenterMatrix,0,angleRotate[1],0,1,0); // rotate up - down

                float[] preResultM = new float[16];
                Matrix.rotateM(preResultM,0,temp,0,angleRotate[0],0,0,1); // rotate left - right
          *//*      float[] invertInit = new float[16];
                Matrix.invertM(invertInit,0,initSensorM,0);
                float[] transformM = new float[16];
                Matrix.multiplyMM(transformM,0,currentSensorM,0,invertInit,0);
                float[] resultM = new float[16];

                Matrix.multiplyMM(resultM,0,transformM,0,preResultM,0);

                float[] formatM = new float[16];
                //formatMatrix(formatM,resultM);*//*


                synchronized (this) {
                    System.arraycopy(preResultM, 0, mRotateMatrix, 0, 16);
                }
                break;
            case Surface.ROTATION_90:
                Matrix.rotateM(temp,0,mCenterMatrix,0,angleChange[1]*FROM_RADS_TO_DEGS,0,0,1);
                Matrix.rotateM(mRotateMatrix,0,temp,0,angleChange[2]*FROM_RADS_TO_DEGS,0,1,0);
                break;
            case Surface.ROTATION_180:
                Matrix.rotateM(temp,0,mCenterMatrix,0,-angleChange[1]*FROM_RADS_TO_DEGS,0,1,0);
                Matrix.rotateM(mRotateMatrix,0,temp,0,angleChange[2]*FROM_RADS_TO_DEGS,0,0,-1);
                break;
            case Surface.ROTATION_270:
                Matrix.rotateM(temp,0,mCenterMatrix,0,-angleChange[1]*FROM_RADS_TO_DEGS,0,0,1);
                Matrix.rotateM(mRotateMatrix,0,temp,0,-angleChange[2]*FROM_RADS_TO_DEGS,0,1,0);
                break;
        }
    }*/

    public final float[] mTransformValue = new float[] {0,0,0,1};

    public void setTransformValue(final float[] value4) {
        if(value4!=null&&value4.length>=4) {
            synchronized (mTransformValue) {
                System.arraycopy(value4,0,mTransformValue,0,4);
            }
            updateTransformValue();
        }
    }

    public static void perspectiveM(float[] m, int offset,
                                    float f, float aspect, float zNear, float zFar) {
       // float f = 1.0f / (float) Math.tan(fovy * (Math.PI / 360.0));
        float rangeReciprocal = 1.0f / (zNear - zFar);

        m[offset] = f / aspect;
        m[offset + 1] = 0.0f;
        m[offset + 2] = 0.0f;
        m[offset + 3] = 0.0f;

        m[offset + 4] = 0.0f;
        m[offset + 5] = f;
        m[offset + 6] = 0.0f;
        m[offset + 7] = 0.0f;

        m[offset + 8] = 0.0f;
        m[offset + 9] = 0.0f;
        m[offset + 10] = (zFar + zNear) * rangeReciprocal;
        m[offset + 11] = -1.0f;

        m[offset + 12] = 0.0f;
        m[offset + 13] = 0.0f;
        m[offset + 14] = 2.0f * zFar * zNear * rangeReciprocal;
        m[offset + 15] = 0.0f;
    }

    private float[] temp = new float[16];

    public void updateTransformValue() {
        switch (mOrientation) {
            case Surface.ROTATION_0:
            default:
                Matrix.rotateM(temp,0,mCenterMatrix,0,mTransformValue[1],0,1,0); // rotate up - down

                float[] preResultM = new float[16];
                Matrix.rotateM(preResultM,0,temp,0,mTransformValue[0],0,0,1); // rotate left - right
          /*      float[] invertInit = new float[16];
                Matrix.invertM(invertInit,0,initSensorM,0);
                float[] transformM = new float[16];
                Matrix.multiplyMM(transformM,0,currentSensorM,0,invertInit,0);
                float[] resultM = new float[16];

                Matrix.multiplyMM(resultM,0,transformM,0,preResultM,0);

                float[] formatM = new float[16];
                //formatMatrix(formatM,resultM);*/

                System.arraycopy(preResultM, 0, mRotateMatrix, 0, 16);

                float[] tProjectM = new float[16];

                // the value should be smaller than 142 degree
                perspectiveM(tProjectM,0,mTransformValue[3],mTextureAspect,0.1f,4); // do not change value 1 & 300
                System.arraycopy(tProjectM,0,mProjectMatrix,0,16);
                break;
      /*      case Surface.ROTATION_90:
                Matrix.rotateM(temp,0,mCenterMatrix,0,mTransformValue[1]*FROM_RADS_TO_DEGS,0,0,1);
                Matrix.rotateM(mRotateMatrix,0,temp,0,mTransformValue[2]*FROM_RADS_TO_DEGS,0,1,0);
                break;
            case Surface.ROTATION_180:
                Matrix.rotateM(temp,0,mCenterMatrix,0,-mTransformValue[1]*FROM_RADS_TO_DEGS,0,1,0);
                Matrix.rotateM(mRotateMatrix,0,temp,0,mTransformValue[2]*FROM_RADS_TO_DEGS,0,0,-1);
                break;
            case Surface.ROTATION_270:
                Matrix.rotateM(temp,0,mCenterMatrix,0,-mTransformValue[1]*FROM_RADS_TO_DEGS,0,0,1);
                Matrix.rotateM(mRotateMatrix,0,temp,0,-mTransformValue[2]*FROM_RADS_TO_DEGS,0,1,0);
                break;*/
        }
    }

  /*  private void calculateV3(float[] vectors) {

        if(!mIsInitSensor) {
            mIsInitSensor = true;
            retrieveOrientationType1(vectors, startRotateSensorOrient);
        }

        retrieveOrientationType1(vectors, currentRotateSensorOrient);

        for (int i = 0; i < 3; i++) {
            currentOrient[i] = currentRotateSensorOrient[i] - startRotateSensorOrient[i];
        }

        log3("rotate sensor", currentRotateSensorOrient);
        log3("rotate sphere", currentOrient);
        Log.d(TAG, "------------");

        float[] temp = new float[16];
        Matrix.rotateM(temp,0,mCenterMatrix,0, currentOrient[0],0,1,0);
        //  float[] temp1 = new float[16];
        //   Matrix.rotateM(temp1,0,temp,0,currentOrient[1],1,0,0);
        Matrix.rotateM(mRotateMatrix,0,temp,0, currentOrient[2],0,0,-1);
    }*/

    /*private void retrieveOrientationType2(float[] vectors, float[] outYPR) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors);
        int worldAxisX = SensorManager.AXIS_X;
        int worldAxisZ = SensorManager.AXIS_Z;
        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX, worldAxisZ, adjustedRotationMatrix);
        float[] orientation = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientation);
        float yaw = orientation[0] * FROM_RADS_TO_DEGS;
        float pitch = orientation[1] * FROM_RADS_TO_DEGS;
        float roll = orientation[2] * FROM_RADS_TO_DEGS;
        outYPR[0] = pitch; // x
        outYPR[1] = roll; // y
        outYPR[2] = yaw; // z
    }

    private void retrieveOrientationType1(float[] vectors, float[] outYPR) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors);
        int worldAxisX = SensorManager.AXIS_X;
        int worldAxisZ = SensorManager.AXIS_Z;
        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX, worldAxisZ, adjustedRotationMatrix);
        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);
        float yaw = orientation[0] * FROM_RADS_TO_DEGS;
        float pitch = orientation[1] * FROM_RADS_TO_DEGS;
        float roll = orientation[2] * FROM_RADS_TO_DEGS;
        outYPR[0] = pitch; // x
        outYPR[1] = roll; // y
        outYPR[2] = yaw; // z
    }

    private void log3(String name, float[] xyz) {
        Log.d(TAG, "report "+name+": x = "+df.format(xyz[0])+", y = "+ df.format(xyz[1])+", z = " + df.format(xyz[2]));
    }*/

    private int mOrientation = Surface.ROTATION_0;
    public void onChangeOrientation(int o) {
        mOrientation = o;
    }

}
