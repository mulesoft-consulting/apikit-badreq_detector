package com.mulesoft.ps.util;

public class FieldError {
    private String fieldName;
    private String reason;

    public FieldError(String fieldName, String reason) {
        this.fieldName = fieldName;
        this.reason = reason;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
