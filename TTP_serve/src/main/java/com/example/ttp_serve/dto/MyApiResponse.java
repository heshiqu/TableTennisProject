package com.example.ttp_serve.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyApiResponse<T> {

    private int code;
    private String message;
    private T data;
    private long timestamp;

    public MyApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // 成功响应
    public static <T> MyApiResponse<T> success(String message, T data) {
        return new MyApiResponse<>(200, message, data);
    }

    public static <T> MyApiResponse<T> success(T data) {
        return new MyApiResponse<>(200, "操作成功", data);
    }

    public static MyApiResponse<Void> success(String message) {
        return new MyApiResponse<>(200, message, null);
    }

    public static MyApiResponse<Void> success() {
        return new MyApiResponse<>(200, "操作成功", null);
    }

    // 错误响应
    public static <T> MyApiResponse<T> error(int code, String message) {
        return new MyApiResponse<>(code, message, null);
    }

    public static <T> MyApiResponse<T> error(String message) {
        return new MyApiResponse<>(500, message, null);
    }

    public static <T> MyApiResponse<T> error() {
        return new MyApiResponse<>(500, "系统错误", null);
    }

    // 未授权响应
    public static <T> MyApiResponse<T> unauthorized(String message) {
        return new MyApiResponse<>(401, message, null);
    }

    public static <T> MyApiResponse<T> unauthorized() {
        return new MyApiResponse<>(401, "未授权访问", null);
    }

    // 禁止访问响应
    public static <T> MyApiResponse<T> forbidden(String message) {
        return new MyApiResponse<>(403, message, null);
    }

    public static <T> MyApiResponse<T> forbidden() {
        return new MyApiResponse<>(403, "禁止访问", null);
    }

    // 未找到响应
    public static <T> MyApiResponse<T> notFound(String message) {
        return new MyApiResponse<>(404, message, null);
    }

    public static <T> MyApiResponse<T> notFound() {
        return new MyApiResponse<>(404, "资源未找到", null);
    }
}
