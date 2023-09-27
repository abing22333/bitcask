package com.abing.bitcask.common.util;

import java.io.*;

/**
 * @author Administrator
 */
public class SerializationUtil {

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serialize(out, obj);
        return out.toByteArray();
    }


    public static void serialize(OutputStream out, Object obj) throws IOException {
        ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(out));
        os.writeObject(obj);
        os.flush();
    }

    public static <T> T deserialize(byte[] data) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        return deserialize(in);
    }

    public static <T> T deserialize(InputStream inputStream) throws IOException {

        ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(inputStream));
        try {
            return (T) is.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
