//
// Created by Sergey Mikhno on 2019-05-13.
//

#include "PageSegmenter.h"



vector<line_limit> PageSegmenter::get_line_limits() {

    Mat image;
    cvtColor(mat, image, COLOR_BGR2GRAY);
    bitwise_not(image,image);
    threshold(image,image,0,255, THRESH_OTSU | THRESH_BINARY);
    const Mat kernel = getStructuringElement(MORPH_RECT, Size(3, 3));
    erode(image, image, kernel, Point(-1,-1), 2);
    dilate(image, image, kernel, Point(-1,-1), 2);
    vector<line_limit> v;
    return v;
}

vector<cc_result> PageSegmenter::get_cc_results(Mat &image) {

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


	Graph g;

    add_edge(0, 1, g);
    add_edge(1, 4, g);
    add_edge(4, 0, g);
    add_edge(2, 5, g);

    std::vector<int> c(num_vertices(g));

    int num = connected_components
            (g, make_iterator_property_map(c.begin(), get(vertex_index, g), c[0]));



    vector<cc_result> v;
    return v;

}


vector<glyph> PageSegmenter::get_glyphs() {

    // preprocess for the first step
    Mat image;
    cvtColor(mat, image, COLOR_BGR2GRAY);
    bitwise_not(image,image);
    const Mat kernel = getStructuringElement(MORPH_RECT, Size(8, 2));
    dilate(image, image, kernel, Point(-1,-1), 2);




    // just a try
    float data[10][2];

    for (int i=0; i<10;i++) {
        data[i][0] = i;
        data[i][1] = i;
    }

    Matrix<float> dataset(&data[0][0], 10, 2);

    int ind[10][2];

    Matrix<int> indices(&ind[0][0], 10, 2);

    float d[10][2];

    Matrix<float> dists(&d[0][0], 10, 2);


    Index<L2<float>> index(dataset, KDTreeIndexParams(1));
    index.buildIndex();

    float q[1][2];
    q[0][0] = 5.2;
    q[0][1] = 5.3;

    Matrix<float> query(&q[0][0], 1, 2);
    // do a knn search, using 128 checks
    index.knnSearch(query, indices, dists, 30, flann::SearchParams());

    vector<glyph> v;
    return v;
}




