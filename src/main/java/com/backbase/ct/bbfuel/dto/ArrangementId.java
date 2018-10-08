package com.backbase.ct.bbfuel.dto;

public class ArrangementId {

    private String internalArrangementId;
    private String externalArrangementId;

    public ArrangementId(String internalArrangementId, String externalArrangementId) {
        this.internalArrangementId = internalArrangementId;
        this.externalArrangementId = externalArrangementId;
    }

    public String getInternalArrangementId() {
        return internalArrangementId;
    }

    public String getExternalArrangementId() {
        return externalArrangementId;
    }
}
