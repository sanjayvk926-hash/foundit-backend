package com.foundit.dto.response;

import java.time.LocalDateTime;

public class BaseResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String timestamp;

    public BaseResponse() {}

    public BaseResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().toString();
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, message, data);
    }

    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(false, message, null);
    }

    public boolean getSuccess() { return this.success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return this.message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return this.data; }
    public void setData(T data) { this.data = data; }

    public String getTimestamp() { return this.timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
