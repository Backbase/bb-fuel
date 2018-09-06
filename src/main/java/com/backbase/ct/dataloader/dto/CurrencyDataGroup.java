package com.backbase.ct.dataloader.dto;

public class CurrencyDataGroup {

    private String internationalDataGroupId;
    private String europeDataGroupId;
    private String usDataGroupId;

    public CurrencyDataGroup() {

    }

    public CurrencyDataGroup withInternationalDataGroupId(String internalRandomCurrencyDataGroupId) {
        this.internationalDataGroupId = internalRandomCurrencyDataGroupId;
        return this;
    }

    public CurrencyDataGroup withEuropeDataGroupId(String internalEurCurrencyDataGroupId) {
        this.europeDataGroupId = internalEurCurrencyDataGroupId;
        return this;
    }

    public CurrencyDataGroup withUsDataGroupId(String internalUsdCurrencyDataGroupId) {
        this.usDataGroupId = internalUsdCurrencyDataGroupId;
        return this;
    }

    public String getInternationalDataGroupId() {
        return internationalDataGroupId;
    }

    public String getEuropeDataGroupId() {
        return europeDataGroupId;
    }

    public String getUsDataGroupId() {
        return usDataGroupId;
    }
}
