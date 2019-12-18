package com.ldt.vrview.shader

object ShaderKt {

    // language=glsl
    const val VERTEX = """
      uniform mat4 uProjMatrix;
      uniform mat4 uViewMatrix;
      uniform mat4 uModelMatrix;
      uniform mat4 uRotateMatrix;
      
      attribute vec3 aPosition;
      attribute vec2 aCoordinate;
      varying vec2 vCoordinate; //
      
      uniform vec4 aEdgeCoord; // left, top, right, bottom
      varying vec4 vEdgeCoord; // left, top, right, bottom
      
      void main(){ 
      gl_Position=uProjMatrix*uRotateMatrix*uViewMatrix*uModelMatrix*vec4(aPosition,1);
      vCoordinate = vec2(aCoordinate.x, 1.0 - aCoordinate.y);
      }
    """

    // language=glsl
    const val FRAGMENT = """
        precision mediump float;
        uniform sampler2D uTexture;
        varying vec2 vCoordinate;
        varying vec4 vEdgeCoord; // left, top, right, bottom
        
        vec4 blur13(sampler2D image, vec2 uv, vec2 resolution, vec2 direction) { 
        
        vec4 color = vec4(0.0);
        vec2 off1 = vec2(1.411764705882353) * direction;
        vec2 off2 = vec2(3.2941176470588234) * direction;
        vec2 off3 = vec2(5.176470588235294) * direction;
        
        color += texture2D(image, uv) * 0.1964825501511404;
        color += texture2D(image, uv + (off1 / resolution)) * 0.2969069646728344;
        color += texture2D(image, uv - (off1 / resolution)) * 0.2969069646728344;
        color += texture2D(image, uv + (off2 / resolution)) * 0.09447039785044732;
        color += texture2D(image, uv - (off2 / resolution)) * 0.09447039785044732;
        color += texture2D(image, uv + (off3 / resolution)) * 0.010381362401148057;
        color += texture2D(image, uv - (off3 / resolution)) * 0.010381362401148057;
        return color; 
        }
        
        vec4 bw2(vec4 tc) { 
        
        float _bw = 0.2126 * tc.r + 0.7152 * tc.g + 0.0722 * tc.b;
        return vec4(vec3(_bw*mix(12.92, 1.055, step( 0.0031308, _bw))),1.0); 
        
        }
        
        void main() { 
        
        vec4 tx = texture2D(uTexture,vCoordinate);
                  //   blur13(uTexture, vCoordinate, vec2(1920,1080), vec2(0,1));
        gl_FragColor= tx;
        }
        """
}