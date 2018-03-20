package com.backbase.testing.dataloader.dto;

import java.util.List;

public class LegalEntityWithUsers {

    private String legalEntityExternalId;
    private String parentLegalEntityExternalId;
    private String legalEntityName;
    private String legalEntityType;
    private List<String> userExternalIds;

    public LegalEntityWithUsers() {
    }

    public List<String> getUserExternalIds() {
        return userExternalIds;
    }

    public void setUserExternalIds(List<String> userExternalIds) {
        this.userExternalIds = userExternalIds;
    }

    public String getLegalEntityExternalId() {
        return legalEntityExternalId;
    }

    public void setLegalEntityExternalId(String legalEntityExternalId) {
        this.legalEntityExternalId = legalEntityExternalId;
    }

    public String getLegalEntityName() {
        return legalEntityName;
    }

    public void setLegalEntityName(String legalEntityName) {
        this.legalEntityName = legalEntityName;
    }

    public String getLegalEntityType() {
        return legalEntityType;
    }

    public void setLegalEntityType(String legalEntityType) {
        this.legalEntityType = legalEntityType;
    }

    public String getParentLegalEntityExternalId() {
        return parentLegalEntityExternalId;
    }

    public void setParentLegalEntityExternalId(String parentLegalEntityExternalId) {
        this.parentLegalEntityExternalId = parentLegalEntityExternalId;
    }
}
