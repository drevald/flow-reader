package com.veve.flowreader;

import android.graphics.Canvas;
import android.graphics.Point;
import android.os.AsyncTask;

import com.android.volley.toolbox.HttpResponse;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.PageRendererFactory;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.model.impl.PageRendererImpl;
import com.veve.flowreader.model.impl.djvu.DjvuBookSource;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

import static com.veve.flowreader.Constants.REPORT_URL;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private static final String BOUNDARY = "---------------------------104659796718797242641237073228";
    private static final String BOUNDARY_PART = "--";

    @Test
    public void testRequest() throws Exception {
        URL url = new URL("https://glyph-report.herokuapp.com/upload");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            conn.setDoOutput(true);
            conn.connect();
            OutputStream os = conn.getOutputStream();
            os.write(getTextData("text", "fieldName", true));
            //os.write(data);
            os.flush();
            if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                //conn.setRequestMethod("POST");

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //conn.disconnect();
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
