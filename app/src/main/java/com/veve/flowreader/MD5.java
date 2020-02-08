package com.veve.flowreader;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public interface MD5 {

    static String fileToMD5(String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte [] md5Bytes = digest.digest();
            String returnVal = "";
            for (int i = 0; i < md5Bytes.length; i++) {
                returnVal += Integer.toString(( md5Bytes[i] & 0xff ) + 0x100, 16).substring(1);
            }
            return returnVal.toUpperCase();        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) { }
            }
        }
    }

    static String fileToMD5(Context context, String filePath) {
        InputStream inputStream = null;
        try {

            inputStream = context.getContentResolver().openInputStream(Uri.fromFile(new File(filePath)));

            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte [] md5Bytes = digest.digest();
            String returnVal = "";
            for (int i = 0; i < md5Bytes.length; i++) {
                returnVal += Integer.toString(( md5Bytes[i] & 0xff ) + 0x100, 16).substring(1);
            }
            return returnVal.toUpperCase();        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) { }
            }
        }
    }

}
