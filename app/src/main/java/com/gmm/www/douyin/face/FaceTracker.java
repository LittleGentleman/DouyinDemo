package com.gmm.www.douyin.face;

/**
 * @author:gmm
 * @date:2020/7/10
 * @类说明:
 */
public class FaceTracker {
    static {
        System.loadLibrary("FaceTracker");
    }

    //保存指针
    private long mNativeObj = 0;

    //传入人脸模型用于定位
    public FaceTracker(String faceModel, String landmarkerModel) {
        mNativeObj = nativeCreateObject(faceModel, landmarkerModel);
    }

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    public void start() {
        nativeStart(mNativeObj);
    }

    public void stop() {
        nativeStop(mNativeObj);
    }

    //传一张图片给C++做人脸检测，C++将识别的脸部信息再返回给java
    public Face detect(byte[] inputImage,int width,int height,int rotationDegree) {
        return nativeDetect(mNativeObj,inputImage,width,height,rotationDegree);
    }

    private static native long nativeCreateObject(String faceModel, String landmarkerModel);

    private static native void nativeDestroyObject(long thiz);

    private static native void nativeStart(long thiz);

    private static native void nativeStop(long thiz);

    private static native Face nativeDetect(long thiz,byte[] inputImage,int width,int height,int rotationDegree);
}