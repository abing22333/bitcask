package com.abing.kv.common.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author abing
 * @date 2023/9/26
 */
public class CloseUtil {

    public static void close(Closeable... closeableArray) {
        for (Closeable closeable : closeableArray) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
