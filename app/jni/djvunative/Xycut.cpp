//
// Created by sergey on 01.09.19.
//

#include "Xycut.h"
#include <iostream>
#include <vector>

#include "common.h"

std::vector<ImageNode> Xycut::get_image_parts() {

    cv::Mat labeled(image.size(), image.type());
    cv::Mat rectComponents = cv::Mat::zeros(cv::Size(0, 0), 0);
    cv::Mat centComponents = cv::Mat::zeros(cv::Size(0, 0), 0);
    connectedComponentsWithStats(image, labeled, rectComponents, centComponents);

    int count = rectComponents.rows - 1;
    double heights[count];

    for (int i = 1; i < rectComponents.rows; i++) {
        int h = rectComponents.at<int>(cv::Point(3, i));
        heights[i - 1] = h;
    }

    if (count == 0) {
        ImageNode node(image, 0,0);
        std::vector<ImageNode> images;
        images.push_back(node);
        return images;
    }

    cv::Scalar m, stdv;
    cv::Mat hist(1, count, CV_64F, &heights);
    meanStdDev(hist, m, stdv);
    double threshold = 2*m(0);

    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "threshold = %f\n", threshold);
    Output out = image_without_white_borders();
    cv::Mat without_borders = out.image;
    ImageNode tree(without_borders, out.x,out.y);
    xycut_step(without_borders, threshold, &tree);
    std::vector<ImageNode> images = tree.to_vector();

    return images;
}


Output Xycut::image_without_white_borders() {
    cv::Mat hist;
    //horizontal
    cv::reduce(image, hist, 0, cv::REDUCE_SUM, CV_32F);
    std::vector<cv::Point> locations;
    cv::findNonZero(hist, locations);


    int left = locations.at(0).x;
    int right = locations.at(locations.size()-1).x;
    //vertical
    reduce(image, hist, 1, cv::REDUCE_SUM, CV_32F);
    cv::findNonZero(hist, locations);

    int upper = locations.at(0).y;
    int lower = locations.at(locations.size()-1).y;

    cv::Rect rect(left, upper, right-left, lower-upper);

    Output out;
    out.image = image(rect);
    out.x = left;
    out.y = upper;

    return out;
}

void Xycut::xycut_step(cv::Mat img, double threshold, ImageNode* tree) {

    cv::Size s = img.size();
    int h = s.height;
    int w = s.width;

    cv::Mat hist;
    //horizontal
    reduce(img, hist, 0, cv::REDUCE_SUM, CV_32F);
    std::vector<std::tuple<int,int>> zr = zero_runs_hor(hist);

    int maxind = max_ind(zr, threshold);

    if (maxind >= 0) {
        int _from = std::get<0>(zr.at(maxind));
        int _to = std::get<1>(zr.at(maxind));
        cv::Rect left_rect(0,0,_from,h);
        cv::Rect right_rect(_to,0,w-_to,h);
        cv::Mat left_image = img(left_rect);
        cv::Size s = left_image.size();
        int h = s.height;
        int w = s.width;
        if (h > 1 && w > 1) {
            ImageNode lt(left_image, tree->get_x(), tree->get_y());
            ImageNode *left_tree = new ImageNode(lt);
            tree->set_left(left_tree);
            xycut_step(left_image, threshold, left_tree);
        }
        cv::Mat right_image = img(right_rect);
        s = right_image.size();
        h = s.height;
        w = s.width;
        if (h > 1 && w > 1) {
            ImageNode rt(right_image, _to + tree->get_x(), tree->get_y());
            ImageNode* right_tree = new ImageNode(rt);
            tree->set_right(right_tree);
            xycut_step(right_image, threshold, right_tree);
        }

    } else {
        reduce(img, hist, 1, cv::REDUCE_SUM, CV_32F);
        zr = zero_runs(hist);
        int maxind = max_ind(zr, threshold);

        if (maxind >= 0) {
            int _from = std::get<0>(zr.at(maxind));
            int _to = std::get<1>(zr.at(maxind));
            cv::Rect upper_rect(0, 0, w, _from);
            cv::Rect lower_rect(0, _to, w, h-_to);
            cv::Mat upper_image = img(upper_rect);
            cv::Size s = upper_image.size();
            int h = s.height;
            int w = s.width;
            if (h > 1 && w > 1) {
                ImageNode ut(upper_image, tree->get_x(), tree->get_y());
                ImageNode* upper_tree = new ImageNode(ut);
                tree->set_left(upper_tree);
                xycut_step(upper_image, threshold, upper_tree);
            }
            cv::Mat lower_image = img(lower_rect);
            s = lower_image.size();
            h = s.height;
            w = s.width;
            if (h > 1 && w > 1) {
                ImageNode lt(lower_image, tree->get_x(), _to + tree->get_y());
                ImageNode* lower_tree = new ImageNode(lt);
                tree->set_right(lower_tree);
                xycut_step(lower_image, threshold, lower_tree);
            }
        }


    }


}
