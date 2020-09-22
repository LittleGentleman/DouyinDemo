package com.gmm.www.douyin.record;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

import com.gmm.www.douyin.filter.FilterChain;
import com.gmm.www.douyin.filter.FilterContext;
import com.gmm.www.douyin.filter.RecordFilter;

import java.util.ArrayList;


/**
 * @author:gmm
 * @date:2020/7/9
 * @类说明:
 */
public class EGLEnv {

    private final EGLDisplay mEglDisplay;
    private final EGLConfig mEglConfig;
    private final android.opengl.EGLContext mEglContext;
    private final EGLSurface mEglSurface;
    private final RecordFilter recordFilter;
    private final FilterChain filterChain;

    public EGLEnv(Context context, EGLContext mGlContext, Surface surface, int width, int height)  {
        //获得显示窗口，作为OpenGL的绘制目标
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        //初始化窗口
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEglDisplay,version,0,version,1)) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        //配置 属性选型
        int[] configAttribute = {
                EGL14.EGL_RED_SIZE, 8, //颜色缓冲区中红色位数
                EGL14.EGL_GREEN_SIZE, 8,//颜色缓冲区中绿色位数
                EGL14.EGL_BLUE_SIZE, 8, //
                EGL14.EGL_ALPHA_SIZE, 8,//
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, //opengl es 2.0
                EGL14.EGL_NONE
        };
        int[] numConfigs = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        //EGL 根据属性选择一个配置
        if (!EGL14.eglChooseConfig(mEglDisplay,configAttribute,0,configs,0,configs.length,
                numConfigs,0)) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        mEglConfig = configs[0];

        /**
         * EGL上下文
         */
        int[] context_attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION,2,
                EGL14.EGL_NONE
        };
        //与 GLSurfaceView中的EGLContext 共享数据，只有这样才能拿到处理完之后展示的图像纹理
        mEglContext = EGL14.eglCreateContext(mEglDisplay,mEglConfig,mGlContext,context_attrib_list,0);

        if (mEglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        /**
         * 创建EGLSurface
         */
        int[] surface_attribute_list = {
                EGL14.EGL_NONE
        };
        mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay,mEglConfig,surface,surface_attribute_list,0);
        if (mEglSurface == null) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        /**
         * 绑定当前线程的显示器display
         */
        if (!EGL14.eglMakeCurrent(mEglDisplay,mEglSurface,mEglSurface,mEglContext)) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        recordFilter = new RecordFilter(context);
        FilterContext filterContext = new FilterContext();
        filterContext.setSize(width,height);
        filterChain = new FilterChain(new ArrayList<>(),0,filterContext);
    }

    public void draw(int textureId, long timestamp) {
        recordFilter.onDraw(textureId,filterChain);
        EGLExt.eglPresentationTimeANDROID(mEglDisplay,mEglSurface,timestamp);
        //EGLSurface是双重缓冲模式
        EGL14.eglSwapBuffers(mEglDisplay,mEglSurface);
    }

    public void release() {
        EGL14.eglDestroySurface(mEglDisplay,mEglSurface);
        EGL14.eglMakeCurrent(mEglDisplay,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(mEglDisplay,mEglContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(mEglDisplay);
        recordFilter.release();
    }
}
