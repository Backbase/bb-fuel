package com.backbase.ct.bbfuel.input;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_PRODUCT_GROUP_SEED_JSON_LOCATION;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import com.backbase.ct.bbfuel.dto.entitlement.ProductGroup;
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
public class ProductGroupReader extends BaseReader {

    /**
     * Load the configured json file.
     */
    public List<ProductGroup> load() {
        return load(this.globalProperties.getString(PROPERTY_PRODUCT_GROUP_SEED_JSON_LOCATION));
    }

    /**
     * Load json file.
     */
    public List<ProductGroup> load(String uri) {
        List<ProductGroup> productGroups;
        try {
            ProductGroup[] parsedProductGroups = ParserUtil.convertJsonToObject(uri, ProductGroup[].class);
            validate(parsedProductGroups);
            productGroups = asList(parsedProductGroups);
        } catch (IOException e) {
            logger.error("Failed parsing file with entities", e);
            throw new InvalidInputException(e.getMessage(), e);
        }
        return productGroups;
    }
    /**
     * Check on duplicate names.
     */
    private void validate(ProductGroup[] productGroups) {
        if (ArrayUtils.isEmpty(productGroups)) {
            throw new InvalidInputException("No product groups have been parsed");
        }
        List<String> names = stream(productGroups)
            .map(ProductGroup::getProductGroupName)
            .collect(toList());
        Set<String> uniqueNames = new HashSet<>(names);
        if (uniqueNames.size() != productGroups.length) {
            throw new InvalidInputException(String.format("Product groups with duplicate names: %s",
                ListUtils.subtract(names, new ArrayList<>(uniqueNames))));
        }
    }
}
