//
//  Reflow.cpp
//  StaticLibrary
//
//  Created by Mikhno Sergey (Galexis) on 20.10.19.
//  Copyright © 2019 Sergey Mikhno. All rights reserved.
//

#include "Reflow.h"
#include "LineSpacing.h"
#include "common.h"
#include <android/log.h>

std::vector<int> Reflow::calculate_line_heights(std::vector<int> line_heights) {

    std::vector<int> new_line_heights;
    std::vector<int>::iterator result = std::min_element(line_heights.begin(), line_heights.end());
    int min_height = *result;
    int addition = (float)min_height/3.0;

    for (int i=0;i<line_heights.size(); i++) {
        new_line_heights.push_back(line_heights.at(i) + addition);
    }

    LineSpacing ls(new_line_heights);
    return ls.get_line_heights();
}


cv::Mat Reflow::reflow(float scale, int page_width, float margin, bool break_on_space, bool show_glyph_borders) {

    int space_count = 0;
    for (int i = 0; i < (int)glyphs.size(); i++) {
        if (glyphs.at(i).is_space) space_count++;
    }
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME,
        "reflow: break_on_space=%d total_glyphs=%d space_glyphs=%d",
        (int)break_on_space, (int)glyphs.size(), space_count);

    int log_limit = glyphs.size() < 80 ? (int)glyphs.size() : 80;
    for (int i = 0; i < log_limit; i++) {
        glyph& gl = glyphs.at(i);
        __android_log_print(ANDROID_LOG_DEBUG, APPNAME,
            "glyph[%d] x=%d y=%d w=%d h=%d baseline_shift=%d is_space=%d is_last=%d indented=%d",
            i, gl.x, gl.y, gl.width, gl.height, gl.baseline_shift,
            gl.is_space, gl.is_last, gl.indented);
    }

    int new_width = page_width;
    //scale = portrait ? scale : scale * screen_ratio;
    //int new_width = ceil(image.size().width);
    //int new_width = ceil(page_width);
    int left_margin = ceil(page_width * 0.075 * margin);
    int paragraph_indent = 30;
    int max_symbol_height = 0;
    std::vector<int> line_heights;
    std::map<int,int> glyph_number_to_line_number;
    std::map<int,std::vector<glyph>> lines;
    std::map<int,int> scaled_glyphs;

    //---------------------------
    // calculate line  heights
    //---------------------------

    int line_sum = left_margin;
    int line_number = 0;
    std::vector<glyph> line;
    bool last = false;

    for (int i=0; i<glyphs.size(); i++) {
        glyph g = glyphs.at(i);
        

        bool indented = g.indented;
        int new_symbol_width = ceil(g.width * scale);
        int new_symbol_height = ceil(g.height * scale);
        if (new_symbol_height > max_symbol_height) {
            max_symbol_height = new_symbol_height;
        }

        if (g.is_space && line.empty()) {
            continue;
        }

        if (last || g.indented) {
            line_sum += paragraph_indent;
        }

        if (line_sum + new_symbol_width < new_width - left_margin && !indented) {
            line.push_back(g);
            line_sum += new_symbol_width;
            glyph_number_to_line_number.insert(std::make_pair(i, line_number));

        } else {

            if (break_on_space && line.size() > 0) {
                int trim_to = -1;
                int overflow_start = -1;
                for (int j = (int)line.size() - 1; j >= 0; j--) {
                    glyph& sg = line.at(j);
                    // Only treat as a real word space if wide enough (width > height/5).
                    // PageSegmenter also flags tiny intra-character gaps as spaces; skip those.
                    if (sg.is_space && sg.width * 5 > sg.height) {
                        trim_to = j;
                        overflow_start = j + 1;
                        break;
                    }
                }
                if (trim_to > 0 && overflow_start >= 0 && overflow_start <= (int)line.size()) {
                    std::vector<glyph> overflow;
                    for (int j = overflow_start; j < (int)line.size(); j++) {
                        overflow.push_back(line.at(j));
                    }
                    overflow.push_back(g);

                    line.resize(trim_to);
                    if (line.size() > 0) {
                        lines.insert(std::make_pair(line_number, line));
                        line_number++;
                    }

                    line = std::vector<glyph>();
                    line_sum = left_margin;
                    for (int j = 0; j < (int)overflow.size(); j++) {
                        glyph_number_to_line_number.insert(std::make_pair(i - ((int)overflow.size() - 1 - j), line_number));
                        line.push_back(overflow.at(j));
                        line_sum += ceil(overflow.at(j).width * scale);
                    }
                    last = g.is_last;
                    continue;
                }
            }

            if (line.size() > 0) {
                lines.insert(std::make_pair(line_number, line));
            }

            if (new_symbol_width <= new_width - 2*left_margin) {

                if (line.size() > 0) {
                    line_number++;
                }

                line = std::vector<glyph>();
                line_sum = left_margin + new_symbol_width;
                max_symbol_height = new_symbol_height;
                glyph_number_to_line_number.insert(std::make_pair(i, line_number));
                line.push_back(g);
                if (last || g.indented) {
                    line_sum += paragraph_indent;
                }
            } else {
                // this big glyph must be on a new line

                int scaled_symbol_width = new_width - 2*left_margin;
                float scaling_coef = scaled_symbol_width/(float)new_symbol_width;
                int scaled_symbol_height = new_symbol_height*scaling_coef;
                scaled_glyphs.insert(std::make_pair(i, scaled_symbol_height));

                line = std::vector<glyph>();
                line.push_back(g);
                if (line_number > 0) {
                    line_number++;
                }

                lines.insert(std::make_pair(line_number, line));
                line = std::vector<glyph>();
                max_symbol_height = 0;
                line_sum = left_margin;
                line_number++;
            }
        }

        last = g.is_last;

    }

    if (line.size() > 0) {
        lines.insert(std::make_pair(line_number, line));
    }

    std::vector<int> line_numbers;;
    for (auto const& element : lines) {
        line_numbers.push_back(element.first);
    }

    auto it = std::max_element(line_numbers.begin(), line_numbers.end());
    line_number = (*it);

    line_heights = std::vector<int>();
    int g_counter = 0;
    for (int k=0; k<=line_number; k++) {
        std::vector<glyph> glyphs = lines.at(k);
        int m = 0;
        for (int l=0;l<glyphs.size(); l++) {
            glyph g = glyphs.at(l);
             if (g.is_space) {
                 g_counter++;
                 continue;
             }
            int new_symbol_height = ceil(g.height * scale);
            if (scaled_glyphs.find(g_counter) != scaled_glyphs.end()) {
                new_symbol_height = scaled_glyphs.at(g_counter);
            }
            if (new_symbol_height > m) {
                m = new_symbol_height;
            }
            g_counter++;
        }
        line_heights.push_back(m);
    }

    line_heights = calculate_line_heights(line_heights);
    int new_height = std::accumulate(line_heights.begin(), line_heights.end(), 0);
    line_sum = left_margin;
    int top_margin = std::min(ceil(new_height * 0.075), left_margin * 1.25);
    int current_vert_pos = top_margin;

    // new image to copy pixels to
    // When glyph borders are shown we need a 3-channel (BGR) image for colored rectangles.
    int output_type = show_glyph_borders ? CV_8UC3 : image.type();
    cv::Mat new_image(new_height + 2*top_margin, new_width, output_type);
    new_image.setTo(cv::Scalar(0));

    current_vert_pos = top_margin;


    for (int i=0; i<=line_number; i++) {
        std::vector<glyph> glyphs = lines.at(i);
        int line_height = line_heights.at(i);
        //cv::line(new_image, cv::Point(0,current_vert_pos), cv::Point(new_width, current_vert_pos), cv::Scalar(255), 5);
        line_sum = left_margin ;
        last = false;
        for (int j=0;j<glyphs.size(); j++) {
            glyph g = glyphs.at(j);
            
            if (j==0 && g.is_space) {
                continue;
            }

            if (last || g.indented) {
                line_sum += paragraph_indent;
            }

            cv::Mat symbol_mat = g.is_picture ? rotated_with_pictures(cv::Rect(g.x, g.y, g.width, g.height)) : image(cv::Rect(g.x, g.y, g.width, g.height));
            int new_symbol_width = ceil(symbol_mat.size().width * scale);
            int new_symbol_height = ceil(symbol_mat.size().height * scale);

            cv::Mat dst(new_symbol_height, new_symbol_width, symbol_mat.type());
            cv::resize(symbol_mat, dst, dst.size(), 0,0, cv::INTER_CUBIC);
            if (show_glyph_borders) {
                cv::cvtColor(dst, dst, cv::COLOR_GRAY2BGR);
            }
            int x_pos = line_sum;

            // Colors pre-compensated for cv::bitwise_not applied by the caller.
            // After inversion + BGR→RGB in PNG, these appear as intended on Android.
            auto borderColor = [&](const glyph& gl) -> cv::Scalar {
                if (gl.is_picture) return cv::Scalar(255, 75,  255); // appears green
                if (gl.is_space)   return cv::Scalar(75,  255, 255); // appears blue
                if (gl.indented)   return cv::Scalar(255, 75,  75);  // appears yellow
                if (gl.is_last)    return cv::Scalar(75,  255, 75);  // appears magenta
                return             cv::Scalar(255, 255, 75);          // appears red
            };

            int y_pos = (current_vert_pos + line_height) + (g.baseline_shift - g.height)*scale;
            if (x_pos + new_symbol_width < new_width - left_margin) {
                cv::Rect dstRect(x_pos, y_pos, new_symbol_width, new_symbol_height);
                if (!g.is_space) {
                    dst.copyTo(new_image(dstRect));
                }
                if (show_glyph_borders) {
                    cv::rectangle(new_image, dstRect, borderColor(g), 1);
                }
            } else {
                int scaled_symbol_width = (new_width - left_margin) - x_pos;
                if (scaled_symbol_width > 0) {

                    // calculate new symbol height

                    float scale_coef = scaled_symbol_width/(float)new_symbol_width;
                    int y_pos = (current_vert_pos + line_height) + (g.baseline_shift - g.height)*scale*scale_coef;
                    int scaled_symbol_height = scale_coef * new_symbol_height;
                    cv::Mat dst2(scaled_symbol_height, scaled_symbol_width, show_glyph_borders ? CV_8UC3 : symbol_mat.type());
                    cv::resize(symbol_mat, dst2, dst2.size(), 0,0, cv::INTER_CUBIC);
                    if (show_glyph_borders) {
                        cv::cvtColor(dst2, dst2, cv::COLOR_GRAY2BGR);
                    }
                    cv::Rect dstRect(x_pos, y_pos, scaled_symbol_width, scaled_symbol_height);
                    if (!g.is_space) {
                        dst2.copyTo(new_image(dstRect));
                    }
                    if (show_glyph_borders) {
                        cv::rectangle(new_image, dstRect, borderColor(g), 1);
                    }
                }

            }
            line_sum += new_symbol_width;
            last = g.is_last;
        }
        current_vert_pos += line_height;


    }

    //cv::imwrite(std::string("/data/local/tmp/im.png"), new_image);
    //cv::imwrite(std::string("/storage/emulated/0/Download/im.png"), new_image);

    //cv::bitwise_not(new_image, new_image);
    return new_image;
}
