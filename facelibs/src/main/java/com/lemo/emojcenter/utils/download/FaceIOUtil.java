package com.lemo.emojcenter.utils.download;

import java.io.Closeable;
import java.io.IOException;

/**
 */

public class FaceIOUtil {
    public static void closeAll(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
