package com.veve.flowreader;

import android.content.Context;
import android.net.Uri;

import java.text.MessageFormat;

import static android.content.ContentResolver.SCHEME_CONTENT;
import static android.content.ContentResolver.SCHEME_FILE;
import static android.drm.DrmStore.DrmObjectType.CONTENT;

public interface BookContentResolver {

    static final MessageFormat fileInputFormat = new MessageFormat("file://{0}");

    static final MessageFormat contentInputFormat = new MessageFormat("{0}external_files{1}");

    static final MessageFormat outputFormat = new MessageFormat("/storage/emulated/0{0}");

    public static String contentToFile(Context context, Uri uri) throws Exception {
        String path = null;
        if (uri.getScheme().equals(SCHEME_CONTENT)) {
            String inputStr = uri.getEncodedPath();
            String filePath = null;
            path = outputFormat.format(new String[]{(String) (contentInputFormat.parse(inputStr)[1])});
        } else if (uri.getScheme().equals(SCHEME_FILE)) {
            String inputStr = uri.toString();
            path = (String)fileInputFormat.parse(inputStr)[0];
        }
        return path;
    }

}
