package com.komflow.kompozith.features.core.util;

import com.komflow.kompozith.features.configuration.exception.ObjectNotFoundException;

import java.util.Optional;
import java.util.function.Supplier;

public class RequireExist {

    public static <T> T of(Optional<T> optional, String message) {
        return optional.orElseThrow(() -> new ObjectNotFoundException(message));
    }
}