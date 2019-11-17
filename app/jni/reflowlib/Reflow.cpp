//
//  Reflow.cpp
//  StaticLibrary
//
//  Created by Mikhno Sergey (Galexis) on 20.10.19.
//  Copyright © 2019 Sergey Mikhno. All rights reserved.
//

#include "Reflow.h"

std::vector<int> Reflow::calculate_line_heights(std::vector<int> line_heights) {
    
    std::vector<int> new_line_heights;
    std::vector<int>::iterator result = std::min_element(line_heights.begin(), line_heights.end());
    int min_height = *result;
    int addition = (float)min_height/3.0;
    
    for (int i=0;i<line_heights.size(); i++) {
        new_line_heights.push_back(line_heights.at(i) + addition);
    }
    return new_line_heights;
}




cv::Mat Reflow::reflow(float scale) {
    
    int new_width = ceil(image.size().width);
    int left_margin = ceil(new_width * 0.075);
    int max_symbol_height = 0;
    std::vector<int> line_heights;
    std::map<int,int> glyph_number_to_line_number;
    std::map<int,std::vector<glyph>> lines;
    
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
        
        if (last || g.indented) {
           line_sum += 30;
        }
    
        
        if ((line_sum + new_symbol_width < new_width - 2*left_margin && !indented) || i == 0) {
            line.push_back(g);
            line_sum += new_symbol_width;
            glyph_number_to_line_number.insert(std::make_pair(i, line_number));
            
        } else {
            
            line_heights.push_back(max_symbol_height);
            line_number++;
            glyph_number_to_line_number.insert(std::make_pair(i, line_number));
            
            if (new_symbol_width <= new_width + 2*left_margin) {
                lines.insert(std::make_pair(line_number, line));
                line = std::vector<glyph>();
                line_sum = new_symbol_width;
                max_symbol_height = new_symbol_width;
                line.push_back(g);
                if (last || g.indented) {
                   line_sum += 30;
                }
            } else {
                line_sum = 0;
                line_heights.push_back(new_symbol_height);
                line = std::vector<glyph>();
                line.push_back(g);
                lines.insert(std::make_pair(line_number, line));
                line_number++;
                max_symbol_height = 0;
            }
        }
        
        last = g.is_last;

        
    }

    line_heights = std::vector<int>();
    for (int k=1; k<=line_number; k++) {
        std::vector<glyph> glyphs = lines.at(k);
        int m = 0;
        for (int l=0;l<glyphs.size(); l++) {
            glyph g = glyphs.at(l);
            int new_symbol_height = ceil(g.height * scale);
            if (new_symbol_height > m) {
                m = new_symbol_height;
            }
        }
        line_heights.push_back(m);
    }

    
    line_heights = calculate_line_heights(line_heights);
    int new_height = std::accumulate(line_heights.begin(), line_heights.end(), 0);
    line_sum = left_margin;
    int top_margin = std::min(ceil(new_height * 0.075), left_margin * 1.5);
    int current_vert_pos = top_margin;
    
    // new image to copy pixels to
    
    cv::Mat new_image(new_height + 2*top_margin, new_width, image.type());
    new_image.setTo(cv::Scalar(0,0,0));
    
    current_vert_pos = top_margin;
    
    
    for (int i=1; i<=line_number; i++) {
        std::vector<glyph> glyphs = lines.at(i);
        int line_height = line_heights.at(i-1);
        line_sum = left_margin ;
        last = false;
        for (int j=0;j<glyphs.size(); j++) {
            
            glyph g = glyphs.at(j);
            
            if (last || g.indented) {
               line_sum += 30;
            }
            
            cv::Mat symbol_mat = image(cv::Rect(g.x, g.y, g.width, g.height));
            int new_symbol_width = ceil(symbol_mat.size().width * scale);
            int new_symbol_height = ceil(symbol_mat.size().height * scale);
                   
            cv::Mat dst(new_symbol_height, new_symbol_width, symbol_mat.type());
            cv::resize(symbol_mat, dst, dst.size(), 0,0, cv::INTER_AREA);
            int x_pos = line_sum;
            int y_pos = (current_vert_pos + line_height) + (g.baseline_shift - g.height)*scale;
            if (x_pos + new_symbol_width < new_width - left_margin) {
                cv::Rect dstRect(x_pos, y_pos, new_symbol_width, new_symbol_height);
                           dst.copyTo(new_image(dstRect));
            } else {
                int scaled_symbol_width = (new_width - left_margin) - x_pos;
                if (scaled_symbol_width > 0) {
                    cv::Mat dst(new_symbol_height, scaled_symbol_width, symbol_mat.type());
                    cv::resize(symbol_mat, dst, dst.size(), 0,0, cv::INTER_AREA);
                    cv::Rect dstRect(x_pos, y_pos, scaled_symbol_width, new_symbol_height);
                    dst.copyTo(new_image(dstRect));
                }
               
            }
            line_sum += new_symbol_width;
            last = g.is_last;
        }
        current_vert_pos += line_height;
    }
    
    
    //cv::bitwise_not(new_image, new_image);
    return new_image;
}
