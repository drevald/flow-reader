//
// Created by sergey on 01.09.19.
//

#ifndef FLOW_READER_XYCUT_H
#define FLOW_READER_XYCUT_H

#include "ImageNode.h"
#include <opencv2/opencv.hpp>

struct Output {
    cv::Mat image;
    int x;
    int y;
};

class Xycut {

public:
    Xycut(cv::Mat image) {
        this->image = image.clone();
        cv::cvtColor(this->image, this->image, cv::COLOR_BGR2GRAY);
        cv::bitwise_not(this->image, this->image);
        //cv::threshold(copy, copy, 0, 255, cv::THRESH_BINARY_INV | cv::THRESH_OTSU);
        cv::adaptiveThreshold(this->image,this->image, 255,cv::ADAPTIVE_THRESH_GAUSSIAN_C,cv::THRESH_BINARY_INV,11,2);
    }

    std::vector<ImageNode> get_image_parts();
private:
    cv::Mat image;
    Output image_without_white_borders();
    void xycut_step(cv::Mat img, double threshold, ImageNode* tree);
};


#endif //FLOW_READER_XYCUT_H
