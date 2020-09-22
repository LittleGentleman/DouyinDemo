package com.gmm.www.douyin.record;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * @author:gmm
 * @date:2020/7/9
 * @类说明: 视频数据使用MediaCodec编码
 */
public class MediaRecorder {
    private EGLContext mGlContext;
    private int mHeight;
    private int mWidth;
    private String mPath;
    private Context mContext;

    private float mSpeed;
    private MediaCodec mMediaCodec;
    private Surface mSurface;
    private MediaMuxer mMuxer;
    private Handler mHandler;
    private boolean isStart;
    private EGLEnv eglEnv;
    private int track;//混合器索引
    private long mLastTimeStamp;

    public MediaRecorder(Context context, String path, EGLContext glContext, int width,
                         int height) {
        mContext = context.getApplicationContext();
        mPath = path;
        mWidth = width;
        mHeight = height;
        mGlContext = glContext;
    }

    public void start(float speed) throws IOException {
        mSpeed = speed;
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                mWidth,mHeight);
        //颜色空间 从 surface当中获得
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        //码率
        format.setInteger(MediaFormat.KEY_BIT_RATE,1500_000);
        //帧率
        format.setInteger(MediaFormat.KEY_FRAME_RATE,25);
        //关键帧间隔
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,10);
        //创建编码器
        mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        //配置编码器
        mMediaCodec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        //这个surface显示的内容就是要编码的画面
        mSurface = mMediaCodec.createInputSurface();

        //混合器（复用器） 将编码后的h264封装为MP4
        mMuxer = new MediaMuxer(mPath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        //开始编码
        mMediaCodec.start();

        //创建OpenGL 的 EGL环境
        HandlerThread handlerThread = new HandlerThread("codec-gl");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //创建EGL环境  将编码器需要的surface与EGLSurface进行关联，这样就获取到视频数据进行编码
                eglEnv = new EGLEnv(mContext,mGlContext,mSurface,mWidth,mHeight);
                isStart = true;
            }
        });
    }

    public void fireFrame(int textureId,long timestamp) {
        if (!isStart) {
            return;
        }
        //录制用的OpenGL已经和handler的线程绑定了，所以需要在这个线程中使用录制的OpenGL
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //画画
                eglEnv.draw(textureId,timestamp);
                codec(false);
            }
        });
    }

    private void codec(boolean endOfStream) {
        //给个结束信号
        if (endOfStream) {
            mMediaCodec.signalEndOfInputStream();
        }

        while (true) {
            //获得输出缓冲区（编码后的数据从输出缓冲区获得）
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int encoderStatus = mMediaCodec.dequeueOutputBuffer(bufferInfo,10_000);
            //需要更多数据
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                //如果是结束那直接退出，否则继续循环
                if (!endOfStream) {
                    break;
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                //输出格式发生改变 第一次总会调用所以在这里开启混合器
                MediaFormat newFormat = mMediaCodec.getOutputFormat();
                track = mMuxer.addTrack(newFormat);
                mMuxer.start();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {

            } else {
                //调整时间戳  mSpeed 小于1，时间戳变大，视频播放变慢，大于1，时间戳变小，视频播放变快
                bufferInfo.presentationTimeUs = (long) (bufferInfo.presentationTimeUs / mSpeed);
                //有时候会出现异常 ： timestampUs xxx < lastTimestampUs yyy for Video track
                if (bufferInfo.presentationTimeUs <= mLastTimeStamp) {
                    bufferInfo.presentationTimeUs = (long) (mLastTimeStamp + 1_000_000 / 25 / mSpeed);
                }
                mLastTimeStamp = bufferInfo.presentationTimeUs;

                //正常则 encoderStatus = 缓冲区下标
                ByteBuffer encodedData = mMediaCodec.getOutputBuffer(encoderStatus);
                //如果当前的buffer是配置信息，不管它，不用写出去
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size  = 0;
                }
                if (bufferInfo.size != 0) {
                    //设置从哪里开始读数据（读出来就是编码后的数据）
                    encodedData.position(bufferInfo.offset);
                    //设置能读数据的总长度
                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
                    //写出为MP4
                    mMuxer.writeSampleData(track,encodedData,bufferInfo);
                }
                //释放这个缓冲区，后续可以存放新的编码后的数据
                mMediaCodec.releaseOutputBuffer(encoderStatus,false);
                //如果给了结束信号 signalEndOfInputStream
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }
        }
    }

    public void stop() {
        //释放
        isStart = false;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                codec(true);
                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;
                mMuxer.stop();
                mMuxer.release();
                eglEnv.release();
                eglEnv = null;
                mMuxer = null;
                mSurface  = null;
                mHandler.getLooper().quitSafely();
                mHandler =  null;
            }
        });
    }
}
