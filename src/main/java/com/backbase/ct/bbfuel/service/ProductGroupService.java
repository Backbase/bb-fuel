package com.backbase.ct.bbfuel.service;

import static java.util.Collections.synchronizedMap;
import static org.apache.commons.lang.StringUtils.deleteWhitespace;

import com.backbase.ct.bbfuel.dto.entitlement.ProductGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * A simple local service with no integration at all.
 */
@Service
@RequiredArgsConstructor
public class ProductGroupService {

    private Map<String, String> productGroupCache = synchronizedMap(new HashMap<>());

    private Map<String, List<ProductGroup>> assignedProductGroups = new HashMap<>();

    private static String createCacheKey(ProductGroup productGroup) {
        return String.format("%s-%s", productGroup.getExternalServiceAgreementId(),
            deleteWhitespace(productGroup.getProductGroupName()).trim());
    }

    public List<ProductGroup> getAssignedProductGroups(String externalServiceAgreementId) {
        return assignedProductGroups.get(externalServiceAgreementId);
    }

    public void saveAssignedProductGroup(ProductGroup productGroup) {
        this.assignedProductGroups
            .computeIfAbsent(
                productGroup.getExternalServiceAgreementId(), key -> new ArrayList<>())
            .add(productGroup);
    }

    public String retrieveIdFromCache(ProductGroup productGroup) {
        return productGroupCache.get(createCacheKey(productGroup));
    }

    public void storeInCache(ProductGroup productGroup) {
        productGroupCache.put(createCacheKey(productGroup), productGroup.getId());
    }
}
