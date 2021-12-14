package com.backbase.ct.bbfuel.input;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ADDITIONAL_PRODUCT_GROUP_SEED_JSON_LOCATION;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductGroupSeedReader extends BaseReader {

    /**
     * Load the configured json file.
     */
    public List<ProductGroupSeed> load() {

        List<ProductGroupSeed> productGroupSeeds =
            load(this.globalProperties.getString(PROPERTY_PRODUCT_GROUP_SEED_JSON_LOCATION));

        if (this.globalProperties.containsKey(PROPERTY_ADDITIONAL_PRODUCT_GROUP_SEED_JSON_LOCATION)) {
            String additionalProductGroupSeedUri =
                globalProperties.getString(PROPERTY_ADDITIONAL_PRODUCT_GROUP_SEED_JSON_LOCATION);
            productGroupSeeds.addAll(load(additionalProductGroupSeedUri));
        }
        return productGroupSeeds;
    }

    /**
     * Load json file.
     */
    public List<ProductGroupSeed> load(String uri) {
        List<ProductGroupSeed> productGroupSeeds;
        try {
            log.info("Loading product group seeds {}", uri);
            ProductGroupSeed[] parsedProductGroupSeeds = ParserUtil.convertJsonToObject(uri, ProductGroupSeed[].class);
            validate(parsedProductGroupSeeds);
            productGroupSeeds = new ArrayList<>(asList(parsedProductGroupSeeds));
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
