package com.kompozith.komflow.exception;

import java.util.HashMap;

public class ObjectNotFoundException extends RuntimeException {

    // Only class name given
    public ObjectNotFoundException(String className) {
        super(className+ " not found.");
    }

    // Class name and object id given
    public ObjectNotFoundException(String className, Long id) {
        super(className+ " not found with id " + id + ".");
    }

    // Class name, some parameter and his value given
    public ObjectNotFoundException(String className, String fieldName, String fieldValue) {
        super(className + " not found with " + fieldName+ " " + fieldValue + ".");
    }

}