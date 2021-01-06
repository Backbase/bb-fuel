package com.backbase.ct.bbfuel.dto.accountStatement;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Categories {

    ACCOUNT("Account"),
    CREDIT_CARD("Credit Card"),
    MORTGAGE("Mortgage"),
    TAX_FORM("Tax Form"),
    LOC("LOC"),
    EELOC("EELOC");

    private final String category;

    private Categories(String category) {
        this.category = category;
    }

    @JsonValue
    public String getCategory() {
        return category;
    }
}
