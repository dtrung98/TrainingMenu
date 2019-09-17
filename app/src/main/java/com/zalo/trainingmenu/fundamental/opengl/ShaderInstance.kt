package com.zalo.trainingmenu.fundamental.opengl

object ShaderInstance {
    const val A_POSITION = "a_position"

    const val RESOLUTION = "resolution"
    const val MOUSE = "mouse"
    const val THRESHOLD = "threshold"
    const val TIME = "time"
    const val PIXEL_RATIO = "pixelRatio"
    const val IMAGE0 = "image0"
    const val IMAGE1 = "image1"

    const val vertexShader = """
        attribute vec2 a_position;

        void main() {
         gl_Position = vec4( a_position, 0, 1 );
        }
    """

    const val fragmentShader = """
        precision mediump float;
        
        uniform vec4 resolution;
        uniform vec2 mouse;
        uniform vec2 threshold;
        uniform float time;
        uniform float pixelRatio;
        uniform sampler2D image0;
        uniform sampler2D image1;


        vec2 mirrored(vec2 v) {
                vec2 m = mod(v,2.);
                return mix(m,2.0 - m, step(1.0 ,m));
        }

        void main() {
            // uvs and textures
            vec2 uv = pixelRatio*gl_FragCoord.xy / resolution.xy ;
            vec2 vUv = (uv - vec2(0.5))*resolution.zw + vec2(0.5);
            vUv.y = 1. - vUv.y;
             vec4 tex1 = texture2D(image1,mirrored(vUv));
             vec2 fake3d = vec2(vUv.x + (tex1.r - 0.5)*mouse.x/threshold.x, vUv.y + (tex1.r - 0.5)*mouse.y/threshold.y);
            gl_FragColor = texture2D(image0,mirrored(fake3d));
        }
    """
}