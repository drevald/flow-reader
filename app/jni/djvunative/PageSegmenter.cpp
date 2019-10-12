//
// Created by Sergey Mikhno on 2019-05-13.
//

#include "common.h"
#include "Enclosure.h"
#include "PageSegmenter.h"



line_limit PageSegmenter::find_baselines(vector<double_pair> &cc) {

    sort(cc.begin(), cc.end(), PairXOrder());
    vector<Rect> line_rects;

    for (int i = 0; i < cc.size(); i++) {
        double_pair t = cc.at(i);
        line_rects.push_back(rd.at(t));
    }

    int max = numeric_limits<int>::min();
    int min = numeric_limits<int>::max();

    vector<int> lowerData;//[line_rects.size()];

    for (int i = 0; i < line_rects.size(); i++) {
        Rect rect = line_rects.at(i);
        if (rect.y < min) {
            min = rect.y;
        }
        if (rect.y + rect.height > max) {
            max = rect.y + rect.height;
        }
        lowerData.push_back(rect.y + rect.height);
    }

    int size = line_rects.size();

    map<int,int> countsLower;

    for (int c = 0; c < size; c++) {
        double m = lowerData[c];
        if (countsLower.find(m) == countsLower.end()){
            countsLower.insert(make_pair(m, 1));
        } else {
            countsLower.at(m) +=1;
        }

    }

    int maxLower = numeric_limits<int>::min();
    int maxLowerIndex = 0;

    for ( auto it = countsLower.begin(); it != countsLower.end(); it++ )
    {
        if (it->second > maxLower) {
            maxLower = it->second;
            maxLowerIndex = it->first;
        }
    }

    return line_limit(min, 0, maxLowerIndex, max);

}

map<int, vector<double_pair>>
PageSegmenter::get_connected_components(vector<double_pair> &center_list, double average_height) {

    map<int, vector<double_pair>> return_value;

    int size = center_list.size();
    double data[size][2];

    for (int i = 0; i < size; i++) {
        data[i][0] = get<0>(center_list.at(i));
        data[i][1] = get<1>(center_list.at(i));
    }

    ::flann::Matrix<double> dataset(&data[0][0], size, 2);

    int k = std::min(100,size);

    ::flann::Index<::flann::L2<double>> index(dataset, ::flann::KDTreeIndexParams(1));
    index.buildIndex();

    ::flann::Matrix<int> indices(new int[size*k], size, k);
    ::flann::Matrix<double> dists(new double[size*k], size, k);

    double q[size][2];

    for (int i = 0; i < size; i++) {
        auto p = center_list[i];
        q[i][0] = get<0>(p);
        q[i][1] = get<1>(p);
    }

    ::flann::Matrix<double> query(&q[0][0], size, 2);
    index.knnSearch(query, indices, dists, k, ::flann::SearchParams());

    map<int,bool> verts;

    for (int i = 0; i < size; i++) {
        auto p = center_list[i];

        vector<double_pair> neighbors;
        bool found_neighbor = false;
        double_pair right_nb;

        double mindist = numeric_limits<double>::max();

        for (int j = 0; j < k; j++) {
            int ind = indices[i][j];
            double_pair nb = center_list[ind];
            if (get<0>(nb) - get<0>(p) != 0) {
                double dist = sqrt(((get<1>(nb) - get<1>(p)) * (get<1>(nb) - get<1>(p))) +
                                   (get<0>(nb) - get<0>(p)) * (get<0>(nb) - get<0>(p)));
                cv::Rect rect1 = rd.at(nb);
                cv::Rect rect2 = rd.at(p);
                int a_s = rect1.y;
                int a_e = rect1.y + rect1.height;
                int b_s = rect2.y;
                int b_e = rect2.y + rect2.height;
                if (b_s <= a_e && a_s <= b_e) {
                    int o_s = std::max(a_s, b_s);
                    int o_e = std::min(a_e, b_e);
                    int diff = o_e - o_s;
                    if (dist < mindist && get<0>(nb) > get<0>(p) &&
                        diff >= 1./2. * average_height) {
                        mindist = dist;
                        right_nb = make_tuple(get<0>(nb), get<1>(nb));
                        found_neighbor = true;
                    }
                }
            }
        }

        if (found_neighbor) {
            double_pair point(get<0>(p), get<1>(p));

            int n = center_numbers.at(point);
            int m = center_numbers.at(right_nb);

            vertex_t v1 = vertex(n, g);
            vertex_t v2 = vertex(m, g);
            add_edge(v1, v2, g);

        }

    }

    std::vector<int> c(num_vertices(g));

    int num = connected_components
            (g, make_iterator_property_map(c.begin(), get(vertex_index, g), c[0]));

    for (int i = 0; i < c.size(); i++) {
        int cn = c[i];
        if (return_value.find(cn) == return_value.end()) {
            return_value.insert(make_pair(cn, vector<double_pair>()));
        }
        return_value.at(cn).push_back(g[i]);

    }

    return return_value;
}


vector<line_limit> PageSegmenter::get_line_limits(std::vector<cv::Rect>& big_rects) {

    const cc_result cc_results = get_cc_results(big_rects);

    if (cc_results.whole_page) {
        vector<line_limit> v;
        line_limit ll(0,0,mat.rows,mat.rows);
        line_height = mat.size().height;
        v.push_back(ll);
        return v;
    } else {
        double average_height = cc_results.average_hight;
        vector<double_pair> centers = cc_results.centers;

        const map<int, vector<double_pair>> components = get_connected_components(centers, average_height);

        line_height = (int) average_height * 2;

        vector<int> keys;

        boost::copy(components | boost::adaptors::map_keys,
                    std::back_inserter(keys));

        vector<line_limit> v;
        for (int i=0;i<keys.size(); i++) {
            int cn = keys.at(i);
            vector<double_pair> cc = components.at(cn);
            line_limit ll = find_baselines(cc);
            if(cc.size() > 1) {
                v.push_back(ll);
            }

        }

        return v;
    }


}

cc_result PageSegmenter::get_cc_results(std::vector<cv::Rect>& big_rects) {

    Mat image = gray_inverted_image;

    Mat labeled(image.size(), image.type());
    Mat rectComponents = Mat::zeros(Size(0, 0), 0);
    Mat centComponents = Mat::zeros(Size(0, 0), 0);
    connectedComponentsWithStats(image, labeled, rectComponents, centComponents);

    int count = rectComponents.rows - 1;

    double heights[count];

    vector<double_pair> center_list;
    vector<Rect> rects;

    for (int i = 1; i < rectComponents.rows; i++) {
        int x = rectComponents.at<int>(Point(0, i));
        int y = rectComponents.at<int>(Point(1, i));
        int w = rectComponents.at<int>(Point(2, i));
        int h = rectComponents.at<int>(Point(3, i));
        Rect rectangle(x, y, w, h);
        rects.push_back(rectangle);
        heights[i - 1] = h;

        double cx = centComponents.at<double>(i, 0);
        double cy = centComponents.at<double>(i, 1);

    }

    for (int i=0; i<rects.size(); i++) {
        cv::Rect r = rects.at(i);
        double  ratio = r.height/(double)r.width;
        if (ratio > 5) {
            big_rects.push_back(r);
        }
    }

    if (count == 0) {
        cc_result v;
        v.whole_page = true;
        v.average_hight = image.size().height;
        v.centers = center_list;
        return v;
    }

    Scalar m, stdv;
    Mat hist(1, count, CV_64F, &heights);
    meanStdDev(hist, m, stdv);

    double min, max;
    cv::minMaxLoc(hist, &min, &max);
    double average_height = m(0);


    double std = stdv(0);

    bool whole_page = false;

    Enclosure enc(rects);
    const set<array<int, 4>> &s = enc.solve();

    int i = 0;
    for (auto it = s.begin(); it != s.end(); ++it) {
        array<int, 4> a = *it;
        int x = -get<0>(a);
        int y = -get<1>(a);
        int width = get<2>(a) - x;
        int height = get<3>(a) - y;
        if (height > average_height - std) {
            double cx = (x + width) / 2.0;
            double cy = (y + height) / 2.0;
            rd[make_tuple((x + width) / 2.0, (y + height) / 2.0)] = Rect(x, y, width, height);
            double_pair center = std::tuple<double,double>(cx, cy);
            center_list.push_back(center);
            center_numbers.insert(make_pair(center, i));
            vertex_t v = add_vertex(g);
            g[v] = center;
            i++;
        }
    }

    if (center_list.size() < 30) {
        whole_page = true;
    }



    cc_result v;
    v.whole_page = whole_page;
    v.average_hight = average_height;
    v.centers = center_list;
    return v;

}


vector<glyph> PageSegmenter::get_glyphs() {

    // preprocess for the first step
    //Mat image = mat.clone();
    //cvtColor(mat, image, COLOR_BGR2GRAY);
    //bitwise_not(image, image);
    //const Mat kernel = getStructuringElement(MORPH_RECT, Size(8, 2));
    //dilate(image, image, kernel, Point(-1, -1), 2);
    //threshold(image, image, 0, 255, THRESH_BINARY | THRESH_OTSU);

    // line detection
    bool lines_detected = true;//build_well_formed_page(image, gray_inverted_image);

    // end line detection
    vector<glyph> return_value;


    std::vector<cv::Rect> big_rects;
    vector<line_limit> line_limits = get_line_limits(big_rects);


    // detect lines inside other lines

    set<int> small_line_limits;
    for (int i=0; i<line_limits.size(); i++) {
        for (int j=0; j!=i && j<line_limits.size(); j++) {
            line_limit lli = line_limits.at(i);
            line_limit llj = line_limits.at(j);
            if (lli.upper >= llj.upper && lli.lower <= llj.lower) {
                small_line_limits.insert(j);
            }
        }
    }

    std::vector<line_limit> new_line_limits;
    for (int i=0; i<line_limits.size(); i++) {
        if (small_line_limits.find(i) == small_line_limits.end() ){
            new_line_limits.push_back(line_limits.at(i));
        }
    }

    line_limits = new_line_limits;



    // end detect


    sort(line_limits.begin(), line_limits.end(), SortLineLimits());

    for (int i=0; i<big_rects.size(); i++) {
        cv::Rect r = big_rects.at(i);
        cv::Mat region = mat(r);
        std::vector<std::vector<cv::Point>> contours;
        cv::findContours(region, contours, cv::RETR_LIST, cv::CHAIN_APPROX_NONE);

        for (int j=0; j<contours.size(); j++) {
            cv::RotatedRect rect = cv::minAreaRect(contours.at(j));
            cv::Rect r = rect.boundingRect();

            //if (r.height / (double)r.width > 10) {
            std::vector<std::vector<cv::Point>> cnts;
            cnts.push_back(contours.at(j));
            cv::drawContours(mat, cnts, -1, cv::Scalar(255,255,255), -1);
            //}
        }
    }


    Mat hist;
    reduce(gray_inverted_image, hist, 0, REDUCE_SUM, CV_32F);

    int w =hist.cols;

    // left indent is the first nonzero sum in the histogram, which doesn't always work
    //

    std::vector<cv::Point> points;

    for (int i=0;i<line_limits.size(); i++){
        line_limit ll = line_limits.at(i);
        int l = ll.lower;
        int bl = ll.lower_baseline;
        int u = ll.upper;
        int bu = ll.upper_baseline;

        Mat lineimage(gray_inverted_image, Rect(0, u, w, l - u));
        //threshold(lineimage, lineimage, 0, 255, THRESH_BINARY | THRESH_OTSU);
        Mat horHist;
        reduce(lineimage, horHist, 0, REDUCE_SUM, CV_32F);

        int w = horHist.cols;

        for (int i = 0; i < w; i++) {
            if (horHist.at<int>(0, i) > 0) {
                horHist.at<int>(0, i) = 1;
            } else {
                horHist.at<int>(0, i) = 0;
            }
        }

        const vector<std::tuple<int, int>> &oneRuns = one_runs(horHist);

        int x1 = oneRuns.size() > 0 ? get<0>(oneRuns.at(0)) : 0;
        int y1 = u;

        points.push_back(cv::Point(x1,y1));

    }
    cv::Vec4f params;
    cv::fitLine(points, params, cv::DIST_L2, 0, 0.01, 0.01);

    // begin line

    float u = params[0];
    float v = params[1];
    float x0 = params[2];
    float y0 = params[3];

    float a = v/u;
    float b = y0 - (v/u)*x0;

    int left_indent = 0;
    bool left_indent_found = false;


    if (u == 0) {
        for (int i = 0; i < w; i++) {
            if (hist.at<float>(0, i) > 0) {
                if (!left_indent_found) {
                    left_indent = i;
                    left_indent_found = true;
                }
                hist.at<float>(0, i) = 1;
            } else {
                hist.at<float>(0, i) = 0;
            }
        }
    }


    // end line

    for (line_limit &ll : line_limits) {
        int l = ll.lower;
        int bl = ll.lower_baseline;
        int u = ll.upper;
        int bu = ll.upper_baseline;

        Mat lineimage(gray_inverted_image, Rect(0, u, w, l - u));
        //threshold(lineimage, lineimage, 0, 255, THRESH_BINARY | THRESH_OTSU);
        Mat horHist;
        reduce(lineimage, horHist, 0, REDUCE_SUM, CV_32F);

        int w = horHist.cols;

        for (int i = 0; i < w; i++) {
            if (horHist.at<int>(0, i) > 0) {
                horHist.at<int>(0, i) = 1;
            } else {
                horHist.at<int>(0, i) = 0;
            }
        }

        const vector<std::tuple<int, int>> &oneRuns = one_runs(horHist);

        horHist.release();

        //int c = 0;
        int space_width = 0;
        std::vector<double> spaces;
        for (int k=0; k<oneRuns.size(); k++) {
            std::tuple<int, int> r = oneRuns.at(k);
            int left = get<0>(r);
            int right = get<1>(r);

            glyph g;


            if (k == 0 && left_indent_found && (left - left_indent) > 0.02 * w) {
                g.indented = true;
            } else if (k==0 && !left_indent_found) {
                float x = (u - b)/a;
                if (left - x > 0.02*w) {
                    g.indented = true;
                }
            }
            else {
                g.indented = false;
            }




            // detect real height

            Mat hist;
            cv::Mat letter = lineimage(cv::Rect(left, 0, right-left, l-u));
            cv::Size size = letter.size();
            if (size.width == 0 || size.height == 0) {
                continue;
            }
            reduce(letter, hist, 1, REDUCE_SUM, CV_32F);
            const vector<std::tuple<int, int>> vert_one_runs = one_runs(hist);

            int shift = vert_one_runs.size() > 0 ? get<0>(vert_one_runs.at(0)) : 0;

            // end detect

            g.x = left;
            g.y = u + shift;
            g.width = right - left;

            g.height = l - u - shift;
            g.baseline_shift = l - bl   ;
            g.line_height = line_height;
            g.is_space = false;
            if (g.width > 0) {
                return_value.push_back(g);
            }


            glyph space;
            space.x = right;
            //int nextx = k != oneRuns.size()-1 ? get<0>(oneRuns.at(k+1)) : right + std::accumulate(spaces.begin(), spaces.end(), 0.0)/spaces.size();
            int lastspacewidth = std::min(5, w - right);
            int nextx = k != oneRuns.size()-1 ? get<0>(oneRuns.at(k+1)) : right + lastspacewidth;

            space_width = nextx - right;
            spaces.push_back(space_width);
            space.width = space_width;
            space.y = u + shift;
            space.baseline_shift = l - bl;
            space.line_height = line_height;
            space.height = l - u - shift;
            space.indented = false;
            space.is_space = true;
            if (space.width>0) {
                return_value.push_back(space);
            }


        }

    }

    mat.release();
    gray_inverted_image.release();

    return return_value;


}

