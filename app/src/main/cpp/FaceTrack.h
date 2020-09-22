//
// Created by gmm on 2020/7/10.
//

#ifndef DOUYIN_FACETRACK_H
#define DOUYIN_FACETRACK_H

#include <seeta/FaceLandmarker.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/types_c.h>

using namespace cv;
using namespace std;
using namespace seeta;

class CascadeDetectorAdapter : public DetectionBasedTracker::IDetector {
public:
    //分类器 检测器
    CascadeDetectorAdapter(Ptr<CascadeClassifier> detector) :
            IDetector(),
            Detector(detector) {
        CV_Assert(detector);
    }

    void detect(const cv::Mat &image,std::vector<cv::Rect> &objects) {
        Detector->detectMultiScale(image,objects,scaleFactor,minNeighbours,0,minObjSize,maxObjSize);
    }

    virtual ~CascadeDetectorAdapter() {

    }

private:
    CascadeDetectorAdapter();

    cv::Ptr<CascadeClassifier> Detector;
};


class FaceTrack {
public:
    FaceTrack(const char *faceModel, const char *landmarkerModel);

    ~FaceTrack();

    void stop();

    void run();


    void process(Mat src, cv::Rect &face, std::vector<SeetaPointF> &points);

private:
    DetectionBasedTracker *tracker;
    FaceLandmarker *landmarker;
};


#endif //DOUYIN_FACETRACK_H
