package com.veve.flowreader;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.MessageFormat;

import static android.content.ContentResolver.SCHEME_CONTENT;
import static android.content.ContentResolver.SCHEME_FILE;
import static android.drm.DrmStore.DrmObjectType.CONTENT;

public interface BookContentResolver {

    static final MessageFormat fileInputFormat = new MessageFormat("file://{0}");

    static final MessageFormat contentInputFormat = new MessageFormat("{0}_files{1}");

    static final MessageFormat outputFormat = new MessageFormat("/storage/emulated/0{0}");

    public static String contentToFile(Context context, Uri uri) throws Exception {
        String path = null;
        try {
            if (uri.getScheme().equals(SCHEME_CONTENT)) {
                String inputStr = uri.getEncodedPath();
                String filePath = null;
                path = outputFormat.format(new String[]{(String) (contentInputFormat.parse(inputStr)[1])});
            } else if (uri.getScheme().equals(SCHEME_FILE)) {
                String inputStr = uri.toString();
                path = (String)fileInputFormat.parse(inputStr)[0];
            }
            path = URLDecoder.decode(path, "UTF-8");
        } catch (Exception e) {
            Log.v(BookContentResolver.class.getName(), "Opening " + uri.toString());
            InputStream is = context.getContentResolver().openInputStream(uri);

            String uriString = URLDecoder.decode(uri.toString(), "UTF-8");

            File file;
            String fileName = uriString.substring(1 + uri.toString().lastIndexOf('/'));
            if (uriString.toLowerCase().endsWith(".pdf")
                    ||uriString.toLowerCase().endsWith(".djvu")) {
                file = new File(context.getExternalFilesDir(null), fileName);
            } else {

                ContentResolver cR =  context.getContentResolver();
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String type = mime.getExtensionFromMimeType(cR.getType(uri));

                if ("djvu".equals(type)) {
                    file = new File(context.getExternalFilesDir(null), fileName + ".djvu");
                } else if ("pdf".equals(type)) {
                    file = new File(context.getExternalFilesDir(null), fileName + ".pdf");
                } else {
                    file = File.createTempFile(
                            "flow.", null, context.getExternalFilesDir(null));
                }

            }
            OutputStream os = new FileOutputStream(file);
            Utils.copy(is, os);
            path = file.getPath();
        }
        return path;
    }

}

//content://ru.yandex.disk.filescache/d0/BOOKS/KVANT/04_Opyty_Domashney_Laboratorii.djvu
