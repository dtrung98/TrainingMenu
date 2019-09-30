
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

vec2 getCoord() {
    vec2 uv = pixelRatio*gl_FragCoord.xy / resolution.xy ;
    vec2 vUv = (uv - vec2(0.5))*resolution.zw + vec2(0.5);
    vUv.y = 1. - vUv.y;
    return vUv;
}

void main() {
    vec2 vUv = getCoord();
    vec4 depthTex = texture2D(image1,vUv);
    vec2 onlyMouse = vUv + (mouse/threshold);
    vec2 fake3d = vUv + (depthTex.r - 0.5)*mouse/threshold;
    vec2 disCords = vUv + ( depthTex.r) *  mouse/threshold;

    gl_FragColor = texture2D(image0,fake3d);
}