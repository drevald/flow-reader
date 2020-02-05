package com.veve.flowreader;


import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class UtilTest {

    File fileIn;
    File fileOut;

    @Test
    public void testCopy() throws Exception {
        fileIn = new File("src/main/res/raw/sample.pdf");
        fileOut = new File("src/test/res/sample.pdf");
        String md5In = MD5.fileToMD5(fileIn.getPath());
        InputStream is = new FileInputStream(fileIn);
        OutputStream os = new FileOutputStream(fileOut);
        Utils.copy(is, os);
        String md5out = MD5.fileToMD5(fileOut.getPath());
        Assert.assertEquals(md5In, md5out);
    }

    @After
    public void cleanUp() {
        fileOut.delete();
    }

}
