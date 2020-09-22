#include <jni.h>
#include <string>
#include "FaceTrack.h"

extern "C"
JNIEXPORT jlong JNICALL
Java_com_gmm_www_douyin_face_FaceTracker_nativeCreateObject(JNIEnv *env, jclass type,
                                                            jstring faceModel_,
                                                            jstring landmarkerModel_) {
    const char *faceModel = env->GetStringUTFChars(faceModel_, 0);
    const char *landmarkerModel = env->GetStringUTFChars(landmarkerModel_, 0);

    //创建指针，指向一块在堆上存放FaceTrack的地址
    FaceTrack *faceTrack = new FaceTrack(faceModel,landmarkerModel);

    env->ReleaseStringUTFChars(faceModel_, faceModel);
    env->ReleaseStringUTFChars(landmarkerModel_, landmarkerModel);

    return (jlong)(faceTrack);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_gmm_www_douyin_face_FaceTracker_nativeDestroyObject(JNIEnv *env, jclass type, jlong thiz) {

    if (thiz != 0) {
        FaceTrack *faceTrack = reinterpret_cast<FaceTrack *>(thiz);//只要这个指针指向的堆上的内容没有释放，那么得到这个内容的地址就能访问这个内容
        faceTrack->stop();
        delete faceTrack;
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_gmm_www_douyin_face_FaceTracker_nativeStart(JNIEnv *env, jclass type, jlong thiz) {

    if (thiz != 0) {
        FaceTrack *faceTrack = reinterpret_cast<FaceTrack *>(thiz);
        faceTrack->run();
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_gmm_www_douyin_face_FaceTracker_nativeStop(JNIEnv *env, jclass type, jlong thiz) {

    if (thiz != 0) {
        FaceTrack *faceTrack = reinterpret_cast<FaceTrack *>(thiz);
        faceTrack->stop();
    }

}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_gmm_www_douyin_face_FaceTracker_nativeDetect(JNIEnv *env, jclass type, jlong thiz,
                                                      jbyteArray inputImage_, jint width,
                                                      jint height, jint rotationDegree) {

    if (thiz == 0) {
        return 0;
    }
    FaceTrack *faceTrack = reinterpret_cast<FaceTrack *>(thiz);
    jbyte *inputImage = env->GetByteArrayElements(inputImage_, NULL);

    //I420
    /**
     *  将图片转换成I420图像格式的矩阵Mat
     * YYYY
     * YYYY
     * YYYY
     * YYYY
     * UUUU
     * VVVV
     */
    Mat src(height*3/2,width,CV_8UC1,inputImage);//row * col 的矩阵   row = 高 ，col = 宽

    //转为RGBA
    cvtColor(src,src,CV_YUV2RGBA_I420);
    //旋转
    if (rotationDegree == 90) {
        rotate(src,src,ROTATE_90_CLOCKWISE);
    } else if (rotationDegree == 270) {
        rotate(src,src,ROTATE_90_COUNTERCLOCKWISE);
    }
    //镜像问题，可以使用此方法进行垂直翻转
//    flip(src,src,1);

    //转为灰度图（降噪）
    Mat gray;//灰度图矩阵
    cvtColor(src,gray,CV_RGBA2GRAY);
    equalizeHist(gray,gray);//提升对比度  黑的更黑 白的更白

    cv::Rect face;//脸部矩形
    std::vector<SeetaPointF> points;//保存人脸的5个特征点的集合，0位置：左眼 1位置：右眼 2位置：鼻子 3位置：左嘴角 4位置：右嘴角
    faceTrack->process(gray,face,points);

    int w = src.cols;
    int h = src.rows;
    gray.release();
    src.release();
    env->ReleaseByteArrayElements(inputImage_, inputImage, 0);

    if (!face.empty() && !points.empty()) {
        //将脸部信息传给java
        jclass cls = env->FindClass("com/gmm/www/douyin/face/Face");
        jmethodID construct = env->GetMethodID(cls,"<init>","(IIIIIIFFFF)V");
        SeetaPointF left = points[0];
        SeetaPointF right = points[1];
        jobject  obj = env->NewObject(cls,construct,face.width,face.height,w,h,face.x,face.y,left.x,left.y,right.x,right.y);
        return obj;
    }

    return 0;
}