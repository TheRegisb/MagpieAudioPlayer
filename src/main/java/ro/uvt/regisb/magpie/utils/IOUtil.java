package ro.uvt.regisb.magpie.utils;

import java.io.*;
import java.util.Base64;

/**
 * Input Output static utilities.
 * Used for quick serialization and deserialization.
 */
public class IOUtil {
    /**
     * Serialize an object to a Base64 String.
     *
     * @param obj Object to serialize.
     * @return Base64 string.
     * @throws IOException On serialization failure.
     */
    public static String serializeToBase64(Object obj) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream so = new ObjectOutputStream(bo);

        so.writeObject(obj);
        so.flush();
        return new String(Base64.getEncoder().encode(bo.toByteArray()));
    }

    /**
     * Deserialize a Base64 String to an object.
     *
     * @param obj Base64 string object literal.
     * @return Object instance.
     * @throws IOException            On deserialization failure.
     * @throws ClassNotFoundException When obj is not a valid Base64 object literal.
     */
    public static Object deserializeFromBase64(String obj) throws IOException, ClassNotFoundException {
        byte[] b = Base64.getDecoder().decode(obj.getBytes());
        ByteArrayInputStream bi = new ByteArrayInputStream(b);
        ObjectInputStream si = new ObjectInputStream(bi);

        return si.readObject();
    }
}
