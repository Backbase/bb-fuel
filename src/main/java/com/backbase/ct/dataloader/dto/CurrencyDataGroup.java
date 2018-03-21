package com.backbase.ct.dataloader.dto;

public class CurrencyDataGroup {

    private String internalRandomCurrencyDataGroupId;
    private String internalEurCurrencyDataGroupId;
    private String internalUsdCurrencyDataGroupId;

    public CurrencyDataGroup() {

    }

    public CurrencyDataGroup withInternalRandomCurrencyDataGroupId(String internalRandomCurrencyDataGroupId) {
        this.internalRandomCurrencyDataGroupId = internalRandomCurrencyDataGroupId;
        return this;
    }

    public CurrencyDataGroup withInternalEurCurrencyDataGroupId(String internalEurCurrencyDataGroupId) {
        this.internalEurCurrencyDataGroupId = internalEurCurrencyDataGroupId;
        return this;
    }

    public CurrencyDataGroup withInternalUsdCurrencyDataGroupId(String internalUsdCurrencyDataGroupId) {
        this.internalUsdCurrencyDataGroupId = internalUsdCurrencyDataGroupId;
        return this;
    }

    public String getInternalRandomCurrencyDataGroupId() {
        return internalRandomCurrencyDataGroupId;
    }

    public String getInternalEurCurrencyDataGroupId() {
        return internalEurCurrencyDataGroupId;
    }

    public String getInternalUsdCurrencyDataGroupId() {
        return internalUsdCurrencyDataGroupId;
    }
}
