/*
 * Copyright 2020 Régis BERTHELOT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
