package com.gmm.www.douyin.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.gmm.www.douyin.filter.AbstractFboFilter;
import com.gmm.www.douyin.filter.AbstractFilter;
import com.gmm.www.douyin.filter.BigEyeFilter;
import com.gmm.www.douyin.filter.CameraFilter;
import com.gmm.www.douyin.filter.FilterChain;
import com.gmm.www.douyin.filter.FilterContext;
import com.gmm.www.douyin.filter.ScreenFilter;
import com.gmm.www.douyin.record.MediaRecorder;
import com.gmm.www.douyin.utils.CameraHelper;
import com.gmm.www.douyin.utils.OpenGLUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.camera.core.Preview;
import androidx.lifecycle.LifecycleOwner;

/**
 * @author:gmm
 * @date:2020/7/8
 * @类说明: OpenGL 渲染器
 */
public class CameraRender implements GLSurfaceView.Renderer,Preview.OnPreviewOutputUpdateListener,SurfaceTexture.OnFrameAvailableListener {

    private CameraHelper cameraHelper;
    private CameraView cameraView;

    //摄像头的图像 用OpenGL ES 画出来
    private SurfaceTexture mCameraTexture;


    private int[] textures;

    float[] mtx = new float[16];

    private MediaRecorder mRecorder;
    private FilterChain filterChain;


    public CameraRender(CameraView cameraView) {
        this.cameraView = cameraView;
        OpenGLUtils.copyAssets2Sdcard(cameraView.getContext(), "lbpcascade_frontalface.xml", "/sdcard/lbpcascade_frontalface.xml");
        OpenGLUtils.copyAssets2Sdcard(cameraView.getContext(), "pd_2_00_pts5.dat", "/sdcard/pd_2_00_pts5.dat");
        LifecycleOwner owner = (LifecycleOwner) cameraView.getContext();
        cameraHelper = new CameraHelper(owner,this);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //请求执行一次 onDrawFrame
        cameraView.requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //创建OpenGL纹理，把摄像头的数据与这个纹理关联
        textures = new int[1];//当做能在opengl用的一个图片的ID
        mCameraTexture.attachToGLContext(textures[0]);
        //当摄像头数据有更新回调 onFrameAvailable
        mCameraTexture.setOnFrameAvailableListener(this);

        Context context = cameraView.getContext();

        List<AbstractFilter> filters = new ArrayList<>();
        filters.add(new CameraFilter(context));
        filters.add(new BigEyeFilter(context));
        filters.add(new ScreenFilter(context));

        filterChain = new FilterChain(filters,0,new FilterContext());

        //录制视频的宽、高
        mRecorder = new MediaRecorder(cameraView.getContext(),"/sdcard/a.mp4",
                EGL14.eglGetCurrentContext(),480,640);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        filterChain.setSize(width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //todo 更新纹理
        mCameraTexture.updateTexImage();

        //获取相机图像矩阵
        mCameraTexture.getTransformMatrix(mtx);

        //把相机图像矩阵传给顶点着色器
        filterChain.setTransformMatrix(mtx);
        filterChain.setFace(cameraHelper.getFace());

        //返回的是FBO中的纹理id  离屏渲染 最后在屏幕上渲染纹理
        int id = filterChain.proceed(textures[0]);


        mRecorder.fireFrame(id,mCameraTexture.getTimestamp());
    }

    /**
     *
     * @param output 预览输出
     */
    @Override
    public void onUpdated(Preview.PreviewOutput output) {
        mCameraTexture = output.getSurfaceTexture();
    }

    public void onSurfaceDestroyed() {
        filterChain.release();
    }

    public void startRecord(float speed) {
        try {
            mRecorder.start(speed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        mRecorder.stop();
    }
}
