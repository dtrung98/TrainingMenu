package com.ldt.vrview.shader

object ShaderConstant {

    // language=glsl
    const val FRAGMENT = """
        precision highp float;
        uniform sampler2D uTexture;
        varying vec2 vCoordinate;
        
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
        void main(){ 
        
        //vec4 color=texture2D(uTexture,vCoordinate);
        //gl_FragColor=color; 

        gl_FragColor = blur13(uTexture, vCoordinate, vec2(1920,1080), vec2(0,1));
        
        }
        """

    // language=glsl
    const val VERTEX = """
      uniform mat4 uProjMatrix;
      uniform mat4 uViewMatrix;
      uniform mat4 uModelMatrix;
      uniform mat4 uRotateMatrix;
      
      attribute vec3 aPosition;
      attribute vec2 aCoordinate;
      varying vec2 vCoordinate;
      
      void main(){ 
      gl_Position=uProjMatrix*uRotateMatrix*uViewMatrix*uModelMatrix*vec4(aPosition,1);
      vCoordinate = vec2(aCoordinate.x, 1.0 - aCoordinate.y);
      }
    """
}