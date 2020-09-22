//
// Created by gmm on 2020/7/10.
//

#include "FaceTrack.h"

FaceTrack::FaceTrack(const char *faceModel, const char *landmarkerModel) {
    Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(faceModel));

    Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(faceModel));

    //跟踪器
    DetectionBasedTracker::Parameters DetectorParams;
    tracker = new DetectionBasedTracker(DetectionBasedTracker(mainDetector,trackingDetector,DetectorParams));

    ModelSetting::Device device = seeta::ModelSetting::CPU;
    int id = 0;
    ModelSetting FD_model(landmarkerModel, device, id);

    landmarker = new FaceLandmarker(FD_model);
}

FaceTrack::~FaceTrack() {
    delete tracker;
    delete landmarker;
}

void FaceTrack::stop() {
    tracker->stop();
}

void FaceTrack::run() {
    tracker->run();
}

void FaceTrack::process(Mat src, cv::Rect &face, std::vector<SeetaPointF> &points) {
    tracker->process(src);
    std::vector<cv::Rect> faces;
    tracker->getObjects(faces);//检测到的所有脸部矩形放入集合中

    //有人脸
    if (faces.size() != 0) {
        face = faces[0];//暂时只取一张脸
        seeta::ImageData simage(src.data,src.cols,src.rows,src.channels());
        seeta::Rect rect(face.x,face.y,face.width,face.height);
        points = landmarker->mark(simage,rect);
    }
}
