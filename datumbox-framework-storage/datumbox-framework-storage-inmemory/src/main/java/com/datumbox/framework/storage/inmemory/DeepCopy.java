/**
 * Copyright (C) 2013-2020 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.storage.inmemory;


import java.io.*;

/**
 * Creates a Deep Copy of an object by serializing and deserializing it.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class DeepCopy {
    
    /**
     * Serialized the Object to byte array.
     * 
     * @param obj
     * @return 
     */
    public static byte[] serialize(Object obj) {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        } 
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
    /**
     * Deserializes the byte array.
     * 
     * @param arr
     * @return 
     */
    public static Object deserialize(byte[] arr) {
        try (InputStream bis = new ByteArrayInputStream(arr);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        } 
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Deep clone Object by serialization and deserialization.
     * 
     * @param <T>
     * @param obj
     * @return 
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T obj) {
        return (T)deserialize(serialize((Object)obj));
    }
}
