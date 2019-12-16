package com.zalo.trainingmenu.fundamental.opengl.airhockey;


import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;

import com.ldt.vrview.shader.ShaderKt;
import com.ldt.vrview.util.GlSelfUtil;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.fundamental.opengl.texture.GLTextureView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class AirHockeyRenderer implements GLTextureView.Renderer {

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;
    private float[] tableVertices = {
            -0.5f, -0.5f,
            0.5f, 0.5f,
            -0.5f, 0.5f,

            -0.5f,-0.5f,
            0.5f, -0.5f,
            0.5f, 0.5f,

            // Line 1

            -0.5f, -0.005f,
            0.5f,   0.005f,
            -0.5f,  0.005f,

            -0.5f, -0.005f,
            0.5f,  -0.005f,
            0.5f,   0.005f,

            // Mallets
            0f, -0.25f,
            0f,  0.25f
    };

    private final Context mContext;
    public AirHockeyRenderer(Context context) {
        this.mContext = context;
        vertexData = ByteBuffer
                .allocateDirect(tableVertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(tableVertices);
    }

    private final FloatBuffer vertexData;
    private int programId;
    private static final String U_COLOR = "u_Color";
    private static final String A_POSITION = "a_Position";
    private int uColorLocation;
    private int aPositionLocation;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        String vertex = readTextFileFromResource(mContext, R.raw.air_hockey_vertex);
        String fragment = readTextFileFromResource(mContext, R.raw.air_hockey_fragment);

        programId= GlSelfUtil.createGlProgram(vertex, fragment);
        glUseProgram(programId);


        uColorLocation = glGetUniformLocation(programId, U_COLOR);
        aPositionLocation = glGetAttribLocation(programId, A_POSITION);
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0, vertexData);
        glEnableVertexAttribArray(aPositionLocation);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);

        // draw rectangle
        glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        glUniform4f(uColorLocation, 1.0f, 0.0f, 1f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 6,6);

        glUniform4f(uColorLocation, 0f,0f,1f,1f);
        glDrawArrays(GL_POINTS, 12 ,1);

        glUniform4f(uColorLocation,1f,0f,0f,1f);
        glDrawArrays(GL_POINTS,13,1);
    }

    @Override
    public void onSurfaceDestroyed(GL10 gl) {

    }


    public static String readTextFileFromResource(Context context, int resourceID) {
        StringBuilder body = new StringBuilder();

        try {
            InputStream inputStream =
                    context.getResources().openRawResource(resourceID);
            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not open resource: " + resourceID, e);
        } catch (Resources.NotFoundException nfe) {
            throw new RuntimeException("Resource not found: " + resourceID, nfe);
        }
        return body.toString();
    }
}
