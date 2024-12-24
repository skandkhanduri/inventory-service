package com.secor.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor

public class InventoryResponseDto {
    private String message;
    private String requestId;
    private String status;

    public InventoryResponseDto(String message, String requestId, String status) {
        this.message = message;
        this.requestId = requestId;
        this.status = status;
    }

    public InventoryResponseDto() {

    }


    public String getMessage() {
        return message;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
