package com.veve.flowreader;

import android.graphics.Canvas;
import android.graphics.Point;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.PageRendererFactory;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.model.impl.PageRendererImpl;
import com.veve.flowreader.model.impl.djvu.DjvuBookSource;

import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

import static com.veve.flowreader.Constants.REPORT_URL;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

//    static {
//        System.loadLibrary("native-lib");
//    }

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void sentSimpleHttpRequest() throws Exception {
        URL url = new URL(Constants.REPORT_URL);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        byte[] data = "data".getBytes();
        urlConnection.setDoOutput(true);
        urlConnection.getOutputStream().write(data);
        urlConnection.getOutputStream().flush();
//
//
//        byte[] data = ("Content-Type: multipart/form-data; boundary=----------287032381131322\n" +
//                "Content-Length: 514\n" +
//                "\n" +
//                "------------287032381131322\n" +
//                "Content-Disposition: form-data; name=\"datafile1\"; filename=\"r.gif\"\n" +
//                "Content-Type: image/gif\n" +
//                "\n" +
//                "GIF87a.............,...........D..;\n" +
//                "------------287032381131322\n" +
//                "Content-Disposition: form-data; name=\"datafile2\"; filename=\"g.gif\"\n" +
//                "Content-Type: image/gif\n" +
//                "\n" +
//                "GIF87a.............,...........D..;\n" +
//                "------------287032381131322\n" +
//                "Content-Disposition: form-data; name=\"datafile3\"; filename=\"b.gif\"\n" +
//                "Content-Type: image/gif\n" +
//                "\n" +
//                "GIF87a.............,...........D..;\n" +
//                "------------287032381131322--").getBytes();
//
//        OutputStream os = urlConnection.getOutputStream();
//        byte[] buffer = new byte[100];
//        os.write(data);
//        os.flush();

    }

    @Test
    public void testSocket() throws Exception {
        Socket socket = new Socket("glyph-report.herokuapp.com", 80);
        OutputStream os = socket.getOutputStream();

os.write("POST / HTTP/2.0 \n\r".getBytes());
//os.write("Host: glyph-report.herokuapp.com\n".getBytes());
//os.write("User-Agent: Mozilla/5.0 Gecko/2009042316 Firefox/3.0.10\n".getBytes());
//os.write("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\n".getBytes());
//os.write("Connection: keep-alive\n".getBytes());
//os.write("Referer: http://aram/~martind/banner.htm\n".getBytes());
//os.write("Content-Type: multipart/form-data; boundary=----------287032381131322\n".getBytes());
//os.write("Content-Length: 514\n".getBytes());
os.write("\n\n".getBytes());
os.flush();

InputStream is = socket.getInputStream();
byte[] buffer = new byte[100];
while (is.read(buffer) != -1) {
    System.out.println(new String(buffer));
}


    }


//    @Test
//    public void testDjvu() throws Exception {
//        BookSource bookSource = new DjvuBookSource("src/main/res/raw/djvu_sample.djvu");
//        PageRenderer renderer = new PageRendererImpl(bookSource);
//        DevicePageContext context = new DevicePageContextImpl();
//        renderer.renderPage(context, 1);
//    }

}