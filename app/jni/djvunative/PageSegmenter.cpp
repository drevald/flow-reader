//
// Created by Sergey Mikhno on 2019-05-13.
//

#include "PageSegmenter.h"
#include "Enclosure.h"

static int* calc_histogram(double* data, int size, double min, double max, int numBins) {
    int* result = new int[numBins];
    const double binSize = (max - min)/numBins;

    for (int i=0; i<size; i++) {
        double d = data[i];
        int bin = (int) ((d - min) / binSize);
        if (bin < 0) { /* this data is smaller than min */ }
        else if (bin >= numBins) { /* this data point is bigger than max */ }
        else {
            result[bin] += 1;
        }
    }
    return result;
}

line_limit PageSegmenter::find_baselines(vector<double_pair>& cc) {
    line_limit ll;
    return ll;
}


void PageSegmenter::preprocess_for_line_limits(const Mat &image) {
    threshold(image,image,0,255, THRESH_OTSU | THRESH_BINARY);
    const Mat kernel = getStructuringElement(MORPH_RECT, Size(3, 3));
    erode(image, image, kernel, Point(-1,-1), 2);
    dilate(image, image, kernel, Point(-1,-1), 2);
}

vector<vector<std::tuple<double,double>>> PageSegmenter::get_connected_components(vector<double_pair>& center_list, double averahe_hight) {
    vector<std::tuple<double,double>> return_value;

    // just a try

    int size = center_list.size();
    double data[size][2];

    for (int i=0; i<size;i++) {
        data[i][0] = get<0>(center_list.at(i));
        data[i][1] = get<1>(center_list.at(i));
    }

    Matrix<double> dataset(&data[0][0], size, 2);

    int ind[size][2];

    int k = 30;

    Index<L2<double>> index(dataset, KDTreeIndexParams(1));
    index.buildIndex();

    Matrix<int> indices(&ind[0][0], size, k);
    double d[10][2];
    Matrix<double> dists(&d[0][0], size, k);

    double q[size][2];

    for (int i=0; i<size; i++) {
        auto p = center_list[i];
        q[i][0] = get<0>(p);
        q[i][1] = get<0>(p);
    }


    Matrix<double> query(&q[0][0], size, 2);
    // do a knn search, using 128 checks
    index.knnSearch(query, indices, dists, 30, flann::SearchParams());

    //return return_value;
}


vector<line_limit> PageSegmenter::get_line_limits() {

    const Mat& image = gray_inverted_image.clone();
    preprocess_for_line_limits(image);

    const cc_result cc_results = get_cc_results(image);
    double average_height = cc_results.average_hight;
    cc_results.centers;

    this->line_height = (int)average_height * 2;



    vector<line_limit> v;
    return v;
}

cc_result PageSegmenter::get_cc_results(const Mat &image) {

    Mat labeled(image.size(), image.type());
    Mat rectComponents = Mat::zeros(Size(0, 0), 0);
    Mat centComponents = Mat::zeros(Size(0, 0), 0);
    connectedComponentsWithStats(image, labeled, rectComponents, centComponents);

    int count = rectComponents.rows - 1;
    double heights[count];

    vector<double_pair> center_list;
    vector<Rect> rects;

    for(int i = 1; i < rectComponents.rows; i++) {
        int x = rectComponents.at<int>(Point(0, i));
        int y = rectComponents.at<int>(Point(1, i));
        int w = rectComponents.at<int>(Point(2, i));
        int h = rectComponents.at<int>(Point(3, i));
        Rect rectangle(x, y, w, h);
        rects.push_back(rectangle);
        heights[i-1] = h;

        double cx = centComponents.at<double>(i, 0);
        double cy = centComponents.at<double>(i, 1);

    }

    Scalar m, stdv;
    Mat hist(1,count, CV_64F, &heights);
    meanStdDev(hist, m, stdv);

    double average_height = m(0);
    double std = stdv(0);
    
    Enclosure enc(rects);
    const set<array<int, 4>>& s = enc.solve();


    int i = 0;
    for (auto it = s.begin(); it != s.end(); ++it)
    {
        array<int, 4> a = *it;
        int x = get<0>(a);
        int y = get<1>(a);
        int width = get<2>(a);
        int height = get<3>(a);

        double cx = (x+width)/2.0;
        double cy = (y+height)/2.0;
        rd[make_tuple((x+width)/2.0, (y+height)/2.0)] = Rect(x,y,width,height);

        double_pair center = make_pair(cx,cy);
        center_list.push_back(center);
        i++;
     }


	Graph g;

    add_edge(0, 1, g);
    add_edge(1, 4, g);
    add_edge(4, 0, g);
    add_edge(2, 5, g);

    std::vector<int> c(num_vertices(g));

    int num = connected_components
            (g, make_iterator_property_map(c.begin(), get(vertex_index, g), c[0]));


    cc_result v;
    v.average_hight = average_height;
    v.centers = center_list;
    return v;

}


vector<std::tuple<int,int>> PageSegmenter::one_runs(const Mat& hist) {
    int w = hist.cols;

    vector<std::tuple<int,int>> return_value;

    int pos = 0;
    for (int i=0;i<w;i++) {
        if ((i==0 && hist.at<int>(0,i) == 1) || (i> 0 && hist.at<int>(0,i) == 1 && hist.at<int>(0,i-1) == 0)) {
            pos = i;
        }

        if ((i==w-1 && hist.at<int>(0,i) == 1) || (i<w-1 && hist.at<int>(0,i) == 1 && hist.at<int>(0,i+1) == 0 ) ) {
            return_value.push_back(make_tuple(pos, i));
        }
    }
    return return_value;
}


vector<glyph> PageSegmenter::get_glyphs() {

    // preprocess for the first step
    Mat image;
    int width = image.cols;
    cvtColor(mat, image, COLOR_BGR2GRAY);
    bitwise_not(image,image);
    const Mat kernel = getStructuringElement(MORPH_RECT, Size(8, 2));
    dilate(image, image, kernel, Point(-1,-1), 2);
    
    vector<line_limit> line_limits = get_line_limits();

    vector<glyph> return_value;

    for (line_limit& ll : line_limits) {
        int l = ll.lower;
        int bl = ll.lower_baseline;
        int u = ll.upper;
        int bu = ll.upper_baseline;

        Mat lineimage(image, Rect(0, u, width, l-u));
        threshold(lineimage, lineimage,0,255, THRESH_BINARY | THRESH_OTSU);
        Mat horHist;
        reduce(lineimage, horHist, 0, REDUCE_SUM, CV_32F);

        int w = horHist.cols;

        for (int i=0;i<w;i++) {
            if (horHist.at<int>(0,i) > 0) {
                horHist.at<int>(0,i) = 1;
            } else {
                horHist.at<int>(0,i) = 0;
            }
        }

        const vector<std::tuple<int, int>> &oneRuns = one_runs(horHist);

        horHist.release();

        for (const std::tuple<int,int>& r : oneRuns) {

            int left = get<0>(r);
            int right = get<1>(r);

            glyph g;
            g.x = left;
            g.y = u;
            g.width = right-left;
            g.height = l-u;
            return_value.push_back(g);

        }


    }

    return return_value;
}




