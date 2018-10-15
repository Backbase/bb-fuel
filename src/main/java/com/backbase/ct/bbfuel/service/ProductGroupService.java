package com.backbase.ct.bbfuel.service;

import static java.util.Collections.synchronizedMap;
import static org.apache.commons.lang.StringUtils.deleteWhitespace;

import com.backbase.ct.bbfuel.dto.entitlement.ProductGroupSeed;
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

    private Map<String, List<ProductGroupSeed>> assignedProductGroups = new HashMap<>();

    private static String createCacheKey(ProductGroupSeed productGroupSeed) {
        return String.format("%s-%s", productGroupSeed.getExternalServiceAgreementId(),
            deleteWhitespace(productGroupSeed.getProductGroupName()).trim());
    }

    public List<ProductGroupSeed> getAssignedProductGroups(String externalServiceAgreementId) {
        return assignedProductGroups.get(externalServiceAgreementId);
    }

    public void saveAssignedProductGroup(ProductGroupSeed productGroupSeed) {
        this.assignedProductGroups
            .computeIfAbsent(
                productGroupSeed.getExternalServiceAgreementId(), key -> new ArrayList<>())
            .add(productGroupSeed);
    }

    public String retrieveIdFromCache(ProductGroupSeed productGroupSeed) {
        return productGroupCache.get(createCacheKey(productGroupSeed));
    }

    public void storeInCache(ProductGroupSeed productGroupSeed) {
        productGroupCache.put(createCacheKey(productGroupSeed), productGroupSeed.getId());
    }
}
