package com.backbase.ct.bbfuel.input;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_PRODUCTS_JSON_LOCATION;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import com.backbase.ct.bbfuel.util.ParserUtil;
import com.backbase.integration.arrangement.rest.spec.v2.products.ProductsPostRequestBody;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductReader extends BaseReader {

    /**
     * Load the configured json file.
     */
    public List<ProductsPostRequestBody> load() {
        return load(this.globalProperties.getString(PROPERTY_PRODUCTS_JSON_LOCATION));
    }

    /**
     * Load json file.
     */
    public List<ProductsPostRequestBody> load(String uri) {
        List<ProductsPostRequestBody> products;
        try {
            ProductsPostRequestBody[] parsedProducts = ParserUtil.convertJsonToObject(uri, ProductsPostRequestBody[].class);
            validate(parsedProducts);
            products = asList(parsedProducts);
        } catch (IOException e) {
            log.error("Failed parsing file with entities", e);
            throw new InvalidInputException(e.getMessage(), e);
        }
        return products;
    }
    /**
     * Check on duplicate ids.
     */
    private void validate(ProductsPostRequestBody[] products) {
        if (ArrayUtils.isEmpty(products)) {
            throw new InvalidInputException("No products have been parsed");
        }
        List<String> ids = stream(products)
            .map(ProductsPostRequestBody::getId)
            .collect(toList());
        Set<String> uniqueIds = new HashSet<>(ids);
        if (uniqueIds.size() != products.length) {
            throw new InvalidInputException(String.format("Products with duplicate ids: %s",
                ListUtils.subtract(ids, new ArrayList<>(uniqueIds))));
        }
    }
}
