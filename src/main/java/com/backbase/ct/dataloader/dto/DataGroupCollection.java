package com.backbase.ct.dataloader.dto;

public class DataGroupCollection {

    private String internationalDataGroupId;
    private String europeDataGroupId;
    private String usDataGroupId;

    public DataGroupCollection() {

    }

    public DataGroupCollection withInternationalDataGroupId(String internalRandomCurrencyDataGroupId) {
        this.internationalDataGroupId = internalRandomCurrencyDataGroupId;
        return this;
    }

    public DataGroupCollection withEuropeDataGroupId(String internalEurCurrencyDataGroupId) {
        this.europeDataGroupId = internalEurCurrencyDataGroupId;
        return this;
    }

    public DataGroupCollection withUsDataGroupId(String internalUsdCurrencyDataGroupId) {
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
