package com.veve.flowreader;

import android.content.Context;
import android.net.Uri;

import java.text.MessageFormat;

public interface BookContentResolver {

    static final MessageFormat inputFormat = new MessageFormat("{0}external_files{1}");

    static final MessageFormat outputFormat = new MessageFormat("/storage/emulated/0{0}");

    public static String contentToFile(Context context, Uri uri) throws Exception {
        String filePath = null;
        String inputStr = uri.getEncodedPath();
        filePath = outputFormat.format(new String[]{(String)(inputFormat.parse(inputStr)[1])});
        return filePath;
    }

}
