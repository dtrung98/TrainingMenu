package com.ldt.vrview.shader;

public final class Shader {
    // language=glsl
    public static String FRAGMENT =
            "precision highp float;" +
            "uniform sampler2D uTexture;" +
            "varying vec2 vCoordinate;" +

            "void main(){" +
            "   vec4 color=texture2D(uTexture,vCoordinate);" +
            "   gl_FragColor=color;" +
            "}";

    // language=glsl
    public static String VERTEX =
            "uniform mat4 uProjMatrix;" +
            "uniform mat4 uViewMatrix;" +
            "uniform mat4 uModelMatrix;" +
            "uniform mat4 uRotateMatrix;" +

            "attribute vec3 aPosition;" +
            "attribute vec2 aCoordinate;" +

            "varying vec2 vCoordinate;" +

            "void main(){" +
            "    gl_Position=uProjMatrix*uRotateMatrix*uViewMatrix*uModelMatrix*vec4(aPosition,1);\n" +
            "    vCoordinate = vec2(aCoordinate.x, 1.0 - aCoordinate.y);\n" +
            "}";
}
