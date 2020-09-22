package com.gmm.www.douyin.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.gmm.www.douyin.utils.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author:gmm
 * @date:2020/7/9
 * @类说明:
 */
public class AbstractFilter {
    FloatBuffer vertexBuffer;//顶点坐标缓存区
    FloatBuffer textureBuffer;//纹理坐标
    int program;
    int vPosition;
    int vCoord;
    int vTexture;

    public AbstractFilter(Context context, int vertexShaderId, int fragmentShaderId) {
        //准备数据          4：4个顶点       4：float占4个字节       2：(x,y)坐标
        vertexBuffer = ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder()).asFloatBuffer();

        textureBuffer = ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder()).asFloatBuffer();

        initCoord();
        initGL(context, vertexShaderId, fragmentShaderId);
    }

    private void initCoord() {
        vertexBuffer.clear();
        vertexBuffer.put(OpenGLUtils.VERTEX);

        textureBuffer.clear();
        textureBuffer.put(OpenGLUtils.TEXTURE);
    }

    protected void initGL(Context context, int vertexShaderId, int fragmentShaderId) {
        //顶点着色器的字符串
        String vertexShader = OpenGLUtils.readRawTextFile(context, vertexShaderId);
        //片元着色器的字符串
        String fragShader = OpenGLUtils.readRawTextFile(context, fragmentShaderId);

        //准备着色器程序
        program = OpenGLUtils.loadParam(vertexShader, fragShader);

        //获取程序中的变量索引
        vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        vCoord = GLES20.glGetAttribLocation(program, "vCoord");
        vTexture = GLES20.glGetUniformLocation(program, "vTexture");
    }

    /**
     * @param texture 绘制的纹理ID
     */
    public int onDraw(int texture, FilterChain filterChain) {
        FilterContext filterContext = filterChain.filterContext;
        //设置绘制区域
        GLES20.glViewport(0, 0, filterContext.width, filterContext.height);

        //使用着色器程序
        GLES20.glUseProgram(program);

        vertexBuffer.position(0);
        // 4、归一化 normalized  [-1,1] . 把[2,2]转换为[-1,1]   给顶点着色器中的vPosition传数据  传递顶点坐标
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        //CPU传数据到GPU，默认情况下着色器无法读到这个数据，需要我们启用一下才可以读取
        GLES20.glEnableVertexAttribArray(vPosition);


        textureBuffer.position(0);
        // 给顶点着色器中的vCoord传数据(纹理坐标) 传递纹理坐标
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        //CPU传数据到GPU，默认情况下着色器无法读取到这个数据。 需要我们启用一下才可以读取
        GLES20.glEnableVertexAttribArray(vCoord);

        //相当于激活一个用来显示图片的画框
        GLES20.glActiveTexture(GLES20.GL_TEXTURE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        //0:图层ID
        GLES20.glUniform1i(vTexture, 0);

//        //传入Camera图像的矩阵，即使之前设置的纹理坐标有方向问题，这个矩阵也能矫正方向
//        GLES20.glUniformMatrix4fv(vMatrix,1,false,mtx,0);

        beforeDraw(filterContext);

        //通知画画
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //渲染完成后解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        return texture;
    }

    //供子类添加额外功能
    public void beforeDraw(FilterContext filterContext) {

    }

    public void release() {
        GLES20.glDeleteProgram(program);
    }
}
