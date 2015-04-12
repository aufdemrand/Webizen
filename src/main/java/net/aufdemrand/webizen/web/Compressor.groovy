package net.aufdemrand.webizen.web

import java.util.zip.GZIPOutputStream

/**
 * Created by Jeremy on 3/29/2015.
 */
class Compressor {

    public static String gzip(String str) {
        if (str == null || str.length() == 0) return str;
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        GZIPOutputStream gzip = new GZIPOutputStream(out)
        gzip.write(str.getBytes())
        gzip.close()
        return out.toString("UTF-8")
    }

}
