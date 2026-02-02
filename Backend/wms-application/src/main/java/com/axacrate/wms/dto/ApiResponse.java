package com.axacrate.wms.dto;

import lombok.Getter;

@Getter
public class ApiResponse {

    private final String status;
    private final String message;

    public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

}
