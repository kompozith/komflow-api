package com.kompozith.komflow.configuration.util;

import com.kompozith.komflow.configuration.exception.ObjectNotFoundException;

import java.util.Optional;

public class RequireExist {

    public static <T> T of(Optional<T> optional, String className) {
        return optional.orElseThrow(() -> new ObjectNotFoundException(className));
    }

    public static <T> T of(Optional<T> optional, String className, Long id) {
        return optional.orElseThrow(() -> new ObjectNotFoundException(className, id));
    }

    public static <T> T of(Optional<T> optional, String className, String missedParameter, String givenValue) {
        return optional.orElseThrow(() -> new ObjectNotFoundException(className, missedParameter, givenValue));
    }
}