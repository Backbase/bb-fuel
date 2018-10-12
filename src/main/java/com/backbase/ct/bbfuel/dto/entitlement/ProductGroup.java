package com.backbase.ct.bbfuel.dto.entitlement;

import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBodyParent.Currency;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
public class ProductGroup extends DbsEntity {

    private String productGroupName;
    private Boolean isRetail;
    private List<Currency> currencies;
    private List<String> currentAccountNames;
    private List<String> productIds;

    public ProductGroup() {
        super();
    }

    public ProductGroup(ProductGroup source) {
        this.setProductGroupName(source.getProductGroupName());
        this.setIsRetail(source.getIsRetail());
        if (source.getCurrencies() != null) {
            this.setCurrencies(new ArrayList<>(source.getCurrencies()));
        }
        if (source.getCurrentAccountNames() != null) {
            this.setCurrentAccountNames(new ArrayList<>(source.getCurrentAccountNames()));
        }
        if (source.getProductIds() != null) {
            this.setProductIds(new ArrayList<>(source.getProductIds()));
        }
    }
}
