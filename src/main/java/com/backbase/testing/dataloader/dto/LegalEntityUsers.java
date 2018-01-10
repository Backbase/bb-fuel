package com.backbase.testing.dataloader.dto;

import java.util.List;

public class LegalEntityUsers {

    private List<String> externalUserIds;

    public LegalEntityUsers(List<String> externalUserIds) {
        this.externalUserIds = externalUserIds;
    }

    public List<String> getExternalUserIds() {
        return this.externalUserIds;
    }

    public void setUsers(List<String> externalUserIds) {
        this.externalUserIds = externalUserIds;
    }

    public LegalEntityUsers withUsers(List<String> externalUserIds) {
        this.externalUserIds = externalUserIds;
        return this;
    }
}
