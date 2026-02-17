package com.axacrate.wms.dto;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    //This is a generic API response class to standardize the API responses

    private final String status;
    private final String message;
    private final T data; // T is for generic data type if we want to return any data along with status and message

    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;

    }

    // This is for convenience methods to create success and error responses
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", null, data);

    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>("Success", message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null);
    }

}
