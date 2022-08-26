package com.backbase.ct.bbfuel.dto.accountStatement;


import lombok.Value;

@Value
public class EStatementPreferencesRequest {

    String internalArrangementId;
    String userId;
    Boolean onlineStatement;
    Boolean paperStatement;
}
