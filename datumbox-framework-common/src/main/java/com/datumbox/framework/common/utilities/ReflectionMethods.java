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
package com.datumbox.framework.common.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * The ReflectionMethods class contains a list of utility methods related to Reflection.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ReflectionMethods {

    /**
     * Gets all the fields recursively from all the parent classes.
     *
     * @param fields
     * @param type
     * @return
     */
    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    /**
     * Finds the public, protected, default or private method of the object with the provided name and parameters.
     *
     * @param obj
     * @param methodName
     * @param params
     * @return
     */
    public static Method findMethod(Object obj, String methodName, Object... params) {
        Class<?>[] classArray = new Class<?>[params.length];
        for (int i = 0; i < params.length; i++) {
            classArray[i] = params[i].getClass();
        }
        try {
            //look on all the public, protected, default and private methods of the class hierarchy.
            Class<?> klass = obj.getClass();
            while (klass != null) {
                for (Method method : klass.getDeclaredMethods()) {
                    //check if the method name matches along with the number of parameters
                    if (method.getName().equals(methodName) && method.getParameterCount() == classArray.length) {
                        Class<?>[] paramClasses = method.getParameterTypes();

                        //Then check one by one all the provided parameters and see if they match the defined ones.
                        boolean parametersMatch = true;
                        for(int i = 0; i < params.length; i++) {
                            if(!paramClasses[i].isAssignableFrom(classArray[i])) {
                                parametersMatch = false;
                                break;
                            }
                        }

                        if(parametersMatch) { //exact match, return the method
                            return method;
                        }
                    }
                }
                klass = klass.getSuperclass();
            }

            //nothing found
            throw new NoSuchMethodException();
        }
        catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Invokes a method on the object using reflection.
     *
     * @param obj
     * @param method
     * @param params
     * @return
     */
    public static Object invokeMethod(Object obj, Method method, Object... params) {
        try {
            method.setAccessible(true);
            return method.invoke(obj, params);
        }
        catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Invokes a method on the object using reflection.
     *
     * @param obj
     * @param methodName
     * @param params
     * @return
     */
    public static Object invokeMethod(Object obj, String methodName, Object... params) {
        Method method = findMethod(obj, methodName, params);
        return invokeMethod(obj, method, params);
    }
}
