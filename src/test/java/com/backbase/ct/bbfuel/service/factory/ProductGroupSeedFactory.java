package com.backbase.ct.bbfuel.service.factory;

import com.backbase.ct.bbfuel.dto.entitlement.ProductGroupSeed;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory of {@link ProductGroupSeed}.
 */
public class ProductGroupSeedFactory {

    public static final String PRODUCT_GROUP_NAME_INTTRADE = "International Trade";

    public  static final String PRODUCT_GROUP_NAME_PAYROLL = "Payroll";

    public static List<ProductGroupSeed> createProductGroupSeeds(List<String> productGroupNames) {
        List<ProductGroupSeed> productGroupSeeds = new ArrayList<>();
        productGroupNames.forEach(name -> productGroupSeeds.add(createProductGroupSeed(name)));
        return productGroupSeeds;
    }

    public static ProductGroupSeed createProductGroupSeed(String productGroupName) {
        return ProductGroupSeed.builder()
            .productGroupName(productGroupName)
            .build();
    }
}
