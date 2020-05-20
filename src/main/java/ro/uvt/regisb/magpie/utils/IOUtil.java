package ro.uvt.regisb.magpie.utils;

import java.io.*;
import java.util.Base64;

public class IOUtil {
    public static String serializeToBase64(Object obj) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream so = new ObjectOutputStream(bo);

        so.writeObject(obj);
        so.flush();
        return new String(Base64.getEncoder().encode(bo.toByteArray()));
    }

    public static Object deserializeFromBase64(String obj) throws IOException, ClassNotFoundException {
        byte[] b = Base64.getDecoder().decode(obj.getBytes());
        ByteArrayInputStream bi = new ByteArrayInputStream(b);
        ObjectInputStream si = new ObjectInputStream(bi);

        return si.readObject();
    }
}
