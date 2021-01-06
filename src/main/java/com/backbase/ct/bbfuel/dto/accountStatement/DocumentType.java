package com.backbase.ct.bbfuel.dto.accountStatement;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentType {

    MSWORD("application/msword"),
    JPEG("image/jpeg"),
    PDF("application/pdf"),
    TEXT("text/plain"),
    CSV("text/csv");

    private final String documentType;

    private DocumentType(String documentType) {
        this.documentType = documentType;
    }

    @JsonValue
    public String getDocumentType() {
        return documentType;
    }

}
