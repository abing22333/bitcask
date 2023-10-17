package com.abing.kv.common.util;

import java.io.*;

/**
 * @author Administrator
 */
public class SerializationUtil {

    public static byte[] serialize(Object obj) throws RuntimeException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            serialize(out, obj);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void serialize(OutputStream out, Object obj) throws RuntimeException {

        try {
            ObjectOutputStream os = new ObjectOutputStream(out);

            os.writeObject(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserialize(byte[] data) throws RuntimeException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        return deserialize(in);
    }

    public static <T> T deserialize(InputStream inputStream) throws RuntimeException {

        try {
            ObjectInputStream is = new ObjectInputStream(inputStream);
            return (T) is.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
