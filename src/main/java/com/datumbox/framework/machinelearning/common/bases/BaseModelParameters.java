/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.datumbox.framework.machinelearning.common.bases;

import com.datumbox.common.objecttypes.Learnable;
import com.datumbox.common.persistentstorage.factories.DatabaseFactory;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Base class for every ModelParameter class in the framework. It automatically
 * initializes all the BidMap fields by using reflection.
 * 
 * @author bbriniotis
 */
public abstract class BaseModelParameters implements Learnable {
    
    public BaseModelParameters(DatabaseFactory dbf) {
        //Initialize all the BigMap fields
        
        //get all the fields from all the inherited classes
        for(Field field : getAllFields(new LinkedList<>(), this.getClass())){
            
            if (field.isAnnotationPresent(BigMap.class)) {
                field.setAccessible(true);
                
                try {
                    field.set(this, dbf.getMap(field.getName()));
                } 
                catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
                
            }
        }
    }
    
    private List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }
}
