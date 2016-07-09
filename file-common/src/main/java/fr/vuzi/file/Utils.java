package fr.vuzi.file;

import org.apache.commons.codec.binary.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class Utils {

    public static byte[] createSha1(File f) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        InputStream fis = new FileInputStream(f);
        int n = 0;
        byte[] buffer = new byte[4096];
        while ((n = fis.read(buffer)) > 0) {
            digest.update(buffer, 0, n);
        }
        fis.close();
        return digest.digest();
    }

    public static String createSha1String(File f) throws Exception {
        return Hex.encodeHexString(createSha1(f));
    }

    public static byte[] createSha1(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(bytes);

        return digest.digest();
    }

    public static String createSha1String(byte[] bytes) throws Exception {
        return Hex.encodeHexString(createSha1(bytes));
    }
}
