package com.veve.flowreader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class NetworkTest {

    private static final String BOUNDARY = "---------------------------104659796718797242641237073228";
    private static final String BOUNDARY_PART = "--";

    @Test
    public void sendRequest() {
//            publishProgress();
        try {
            URL url = new URL(Constants.REPORT_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            conn.setDoOutput(true);
            conn.connect();
            OutputStream os = conn.getOutputStream();
            //os.write(getTextData("text", "fieldName", true));
            os.write(getFileData("aaa".getBytes(), "image/jpeg", "originalImage", "originalImage.jpeg", true));
            os.flush();


            Log.v("HIROKU_RESPONSE", "response code" + conn.getResponseCode());
            InputStream is = conn.getInputStream();
            byte[] buffer = new byte[100];
            while (is.read(buffer) != -1) {
                Log.v("HIROKU_RESPONSE", new String(buffer));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private byte[] getTextData(String text, String fieldName, boolean lastOne) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(BOUNDARY_PART.getBytes());
        byteArrayOutputStream.write(BOUNDARY.getBytes());
        byteArrayOutputStream.write("\r\n".getBytes());
        byteArrayOutputStream.write("Content-Disposition: form-data; ".getBytes());
        byteArrayOutputStream.write(("name=\"" + fieldName + "\"").getBytes());
        byteArrayOutputStream.write("\r\n".getBytes());
        byteArrayOutputStream.write("\r\n".getBytes());
        byteArrayOutputStream.write(text.getBytes());
        byteArrayOutputStream.write("\r\n".getBytes());
        byteArrayOutputStream.write(BOUNDARY_PART.getBytes());
        byteArrayOutputStream.write(BOUNDARY.getBytes());
        if (lastOne)
            byteArrayOutputStream.write(BOUNDARY_PART.getBytes());
        byteArrayOutputStream.write("\r\n".getBytes());
        return byteArrayOutputStream.toByteArray();
    }

    private byte[] getFileData(byte[] data, String contentType, String fieldName, String fileName, boolean lastOne) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(BOUNDARY_PART.getBytes());
        byteArrayOutputStream.write(BOUNDARY.getBytes());
        byteArrayOutputStream.write("\r\n".getBytes());
        byteArrayOutputStream.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").getBytes());
        byteArrayOutputStream.write("\r\n".getBytes());
        byteArrayOutputStream.write(("Content-Type: " + contentType).getBytes());
        byteArrayOutputStream.write("\r\n".getBytes());
        byteArrayOutputStream.write("\r\n".getBytes());
        byteArrayOutputStream.write(data);
        byteArrayOutputStream.write("\r\n".getBytes());
        byteArrayOutputStream.write(BOUNDARY_PART.getBytes());
        byteArrayOutputStream.write(BOUNDARY.getBytes());
        if (lastOne)
            byteArrayOutputStream.write(BOUNDARY_PART.getBytes());
        byteArrayOutputStream.write("\r\n".getBytes());
        return byteArrayOutputStream.toByteArray();
    }



}
