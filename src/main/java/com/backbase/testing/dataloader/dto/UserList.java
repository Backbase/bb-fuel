package com.backbase.testing.dataloader.dto;

import java.util.List;

public class UserList {

    List<String> externalUserIds;

    public UserList() {
    }

    public List<String> getExternalUserIds() {
        return externalUserIds;
    }

    public void setExternalUserIds(List<String> externalUserIds) {
        this.externalUserIds = externalUserIds;
    }
}
