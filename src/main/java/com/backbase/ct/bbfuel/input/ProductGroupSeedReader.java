package com.backbase.ct.bbfuel.input;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_PRODUCT_GROUP_SEED_JSON_LOCATION;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import com.backbase.ct.bbfuel.dto.entitlement.ProductGroupSeed;
import com.backbase.ct.bbfuel.util.ParserUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductGroupSeedReader extends BaseReader {

    /**
     * Load the configured json file.
     */
    public List<ProductGroupSeed> load() {
        return load(this.globalProperties.getString(PROPERTY_PRODUCT_GROUP_SEED_JSON_LOCATION));
    }

    /**
     * Load json file.
     */
    public List<ProductGroupSeed> load(String uri) {
        List<ProductGroupSeed> productGroupSeeds;
        try {
            ProductGroupSeed[] parsedProductGroupSeeds = ParserUtil.convertJsonToObject(uri, ProductGroupSeed[].class);
            validate(parsedProductGroupSeeds);
            productGroupSeeds = asList(parsedProductGroupSeeds);
        } catch (IOException e) {
            log.error("Failed parsing file with entities", e);
            throw new InvalidInputException(e.getMessage(), e);
        }
        return productGroupSeeds;
    }
    /**
     * Check on duplicate names.
     */
    private void validate(ProductGroupSeed[] productGroupSeeds) {
        if (ArrayUtils.isEmpty(productGroupSeeds)) {
            throw new InvalidInputException("No product groups have been parsed");
        }
        List<String> names = stream(productGroupSeeds)
            .map(ProductGroupSeed::getProductGroupName)
            .collect(toList());
        Set<String> uniqueNames = new HashSet<>(names);
        if (uniqueNames.size() != productGroupSeeds.length) {
            throw new InvalidInputException(String.format("Product groups with duplicate names: %s",
                ListUtils.subtract(names, new ArrayList<>(uniqueNames))));
        }
    }
}
