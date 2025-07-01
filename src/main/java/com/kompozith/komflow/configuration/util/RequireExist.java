package com.kompozith.komflow.configuration.util;

import com.kompozith.komflow.configuration.exception.ObjectNotFoundException;

import java.util.Optional;

public class RequireExist {

    public static <T> T of(Optional<T> optional, String message) {
        return optional.orElseThrow(() -> new ObjectNotFoundException(message));
    }
}