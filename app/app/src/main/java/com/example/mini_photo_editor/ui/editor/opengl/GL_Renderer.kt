package com.example.mini_photo_editor.ui.editor.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.ETC1Util.loadTexture
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer : GLSurfaceView.Renderer {
//  openGL渲染器
    private var textureId = 0
    private var bitmap: Bitmap? = null

    // 顶点着色器代码 - 简单的纹理映射
    private val vertexShaderCode = """
        #version 300 es
        layout(location = 0) in vec4 vPosition;
        layout(location = 1) in vec2 vTexCoord;
        out vec2 fTexCoord;
        void main() {
            gl_Position = vPosition;
            fTexCoord = vTexCoord;
        }
    """.trimIndent()

    // 片段着色器代码 - 纹理采样
    private val fragmentShaderCode = """
        #version 300 es
        precision mediump float;
        in vec2 fTexCoord;
        out vec4 fragColor;
        uniform sampler2D uTexture;
        void main() {
            fragColor = texture(uTexture, fTexCoord);
        }
    """.trimIndent()

    private var program = 0

    // 顶点数据：一个矩形（两个三角形）
    private val vertices = floatArrayOf(
        // 位置 (x, y)     纹理坐标 (u, v)
        -1.0f,  1.0f,     0.0f, 0.0f,  // 左上
        -1.0f, -1.0f,     0.0f, 1.0f,  // 左下
        1.0f, -1.0f,     1.0f, 1.0f,  // 右下
        1.0f,  1.0f,     1.0f, 0.0f   // 右上
    )

    // 索引数据
    private val indices = shortArrayOf(
        0, 1, 2,  // 第一个三角形
        0, 2, 3   // 第二个三角形
    )

    private var vertexBufferId = 0
    private var indexBufferId = 0

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        println("📸 GLRenderer 收到 Bitmap: ${bitmap.width}x${bitmap.height}")
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        println("🟢 OpenGL Surface 创建，初始化着色器和缓冲区")
        // 设置背景色为黑色（方便看到图片）
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // 启用深度测试和混合（如果需要）
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        // 编译着色器
        compileShaders()

        // 创建缓冲区
        createBuffers()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        println("📐 Surface 尺寸改变: ${width}x${height}")
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // 清除颜色和深度缓冲区
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // 使用着色器程序
        GLES30.glUseProgram(program)

        // 如果有位图，加载为纹理
        bitmap?.let {
            if (textureId == 0) {
                textureId = loadTexture(it)
                println("🖼️ 纹理加载完成，ID: $textureId")
            }
        }

        // 绑定纹理
        if (textureId != 0) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)

            // 设置纹理参数
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        }

        // 绘制
        drawRectangle()
    }
    private fun compileShaders() {
        // 编译顶点着色器
        val vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        GLES30.glShaderSource(vertexShader, vertexShaderCode)
        GLES30.glCompileShader(vertexShader)

        // 检查编译状态
        val vertexCompileStatus = IntArray(1)
        GLES30.glGetShaderiv(vertexShader, GLES30.GL_COMPILE_STATUS, vertexCompileStatus, 0)
        if (vertexCompileStatus[0] == 0) {
            println("❌ 顶点着色器编译失败: ${GLES30.glGetShaderInfoLog(vertexShader)}")
        }

        // 编译片段着色器
        val fragmentShader = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        GLES30.glShaderSource(fragmentShader, fragmentShaderCode)
        GLES30.glCompileShader(fragmentShader)

        // 检查编译状态
        val fragmentCompileStatus = IntArray(1)
        GLES30.glGetShaderiv(fragmentShader, GLES30.GL_COMPILE_STATUS, fragmentCompileStatus, 0)
        if (fragmentCompileStatus[0] == 0) {
            println("❌ 片段着色器编译失败: ${GLES30.glGetShaderInfoLog(fragmentShader)}")
        }

        // 创建程序
        program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)
        GLES30.glLinkProgram(program)

        // 检查链接状态
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            println("❌ 程序链接失败: ${GLES30.glGetProgramInfoLog(program)}")
        }

        println("✅ 着色器编译成功，程序ID: $program")
    }

    private fun createBuffers() {
        // 创建顶点缓冲区
        val buffers = IntArray(2)
        GLES30.glGenBuffers(2, buffers, 0)
        vertexBufferId = buffers[0]
        indexBufferId = buffers[1]

        // 绑定顶点缓冲区并上传数据
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBufferId)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            vertices.size * 4, // 每个float 4字节
            java.nio.ByteBuffer.allocateDirect(vertices.size * 4)
                .order(java.nio.ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices)
                .flip(),
            GLES30.GL_STATIC_DRAW
        )

        // 绑定索引缓冲区并上传数据
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBufferId)
        GLES30.glBufferData(
            GLES30.GL_ELEMENT_ARRAY_BUFFER,
            indices.size * 2, // 每个short 2字节
            java.nio.ByteBuffer.allocateDirect(indices.size * 2)
                .order(java.nio.ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(indices)
                .flip(),
            GLES30.GL_STATIC_DRAW
        )

        println("✅ 缓冲区创建完成，顶点缓冲区ID: $vertexBufferId, 索引缓冲区ID: $indexBufferId")
    }

    private fun loadTexture(bitmap: Bitmap): Int {
        val textureIds = IntArray(1)
        GLES30.glGenTextures(1, textureIds, 0)

        if (textureIds[0] == 0) {
            println("❌ 纹理生成失败")
            return 0
        }


        // 绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0])

        // 设置纹理参数
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)

        // 加载位图到 OpenGL 纹理
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)

        // 解绑纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)

        return textureIds[0]
    }

    private fun drawRectangle() {
        // 启用顶点属性
        GLES30.glEnableVertexAttribArray(0) // 位置
        GLES30.glEnableVertexAttribArray(1) // 纹理坐标

        // 绑定顶点缓冲区
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBufferId)

        // 设置位置属性 (每顶点2个float，间隔16字节，从0开始)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 16, 0)

        // 设置纹理坐标属性 (每顶点2个float，间隔16字节，从8开始)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 16, 8)

        // 绑定索引缓冲区
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBufferId)

        // 绘制
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.size, GLES30.GL_UNSIGNED_SHORT, 0)

        // 禁用顶点属性
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
    }
}