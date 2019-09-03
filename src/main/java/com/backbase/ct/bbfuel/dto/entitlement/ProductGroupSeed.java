package com.backbase.ct.bbfuel.dto.entitlement;

import static org.springframework.beans.BeanUtils.copyProperties;

import com.backbase.ct.bbfuel.dto.AmountRange;
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
public class ProductGroupSeed extends DbsEntity {

    private String productGroupName;
    private Boolean isRetail;
    private List<String> currencies;
    private List<String> currentAccountNames;
    private List<String> savingAccountNames;
    private List<String> productIds;
    private List<String> nonCurrentAccountProductIds;
    private AmountRange numberOfArrangements;
    private AmountRange numberOfDebitCards;

    public ProductGroupSeed() {
        super();
    }

    public ProductGroupSeed(ProductGroupSeed source) {
        copyProperties(source, this);
        this.setIsRetail(source.getIsRetail() == null ? Boolean.valueOf(false) : source.getIsRetail());
        if (source.getCurrencies() != null) {
            this.setCurrencies(new ArrayList<>(source.getCurrencies()));
        }
        if (source.getCurrentAccountNames() != null) {
            this.setCurrentAccountNames(new ArrayList<>(source.getCurrentAccountNames()));
        }
        if (source.getSavingAccountNames() != null) {
            this.setSavingAccountNames(new ArrayList<>(source.getSavingAccountNames()));
        }
        if (source.getProductIds() != null) {
            this.setProductIds(new ArrayList<>(source.getProductIds()));
        }
        if (source.getNonCurrentAccountProductIds() != null) {
            this.setNonCurrentAccountProductIds(new ArrayList<>(source.getNonCurrentAccountProductIds()));
        }
    }
}
