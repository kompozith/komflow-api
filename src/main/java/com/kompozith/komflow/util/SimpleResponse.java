package com.kompozith.komflow.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimpleResponse<T> {
    private String message;
    private T data;

    public static <T> SimpleResponse<T> success(T data) {
        return new SimpleResponse<>("Success", data);
    }

    public static <T> SimpleResponse<T> success(String message, T data) {
        return new SimpleResponse<>(message, data);
    }

    public static <T> SimpleResponse<T> error(String message, T data) {
        return new SimpleResponse<>(message, data);
    }
}
