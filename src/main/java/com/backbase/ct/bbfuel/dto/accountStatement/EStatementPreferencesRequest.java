package com.backbase.ct.bbfuel.dto.accountStatement;


import lombok.Value;

@Value
public class EStatementPreferencesRequest {

    String externalArrangementId;
    String userId;
    Boolean onlineStatement;
    Boolean paperStatement;
}
