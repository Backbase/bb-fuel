package com.backbase.ct.bbfuel.service;

import static java.util.Collections.synchronizedMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.deleteWhitespace;

import com.backbase.ct.bbfuel.IngestException;
import com.backbase.ct.bbfuel.dto.User;
import com.backbase.ct.bbfuel.dto.entitlement.DbsEntity;
import com.backbase.ct.bbfuel.dto.entitlement.ProductGroupSeed;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

    public List<ProductGroupSeed> findAssignedProductGroups(String externalServiceAgreementId) {
        return assignedProductGroups.get(externalServiceAgreementId);
    }

    /**
     * Find unique ids of ingested product groups for given externalServiceAgreementId.
     *
     * @param externalServiceAgreementId external id of the service agreement
     * @return list of ids of product groups that have been assigned to the service agreement
     */
    public List<String> findAssignedProductGroupsIds(String externalServiceAgreementId) {
        return findAssignedProductGroups(externalServiceAgreementId)
            .stream()
            .map(DbsEntity::getId)
            .distinct()
            .collect(toList());
    }

    /**
     * Find unique ids of ingested product groups for given externalServiceAgreementId and a user's productGroupNames.
     *
     * @param externalServiceAgreementId external id of the service agreement
     * @return list of ids of product groups that match on name and have been assigned to the service agreement
     */
    public List<String> findAssignedProductGroupsIds(String externalServiceAgreementId, User user) {
        return findAssignedProductGroups(externalServiceAgreementId)
            .stream()
            .filter(productGroupSeed -> !CollectionUtils.isEmpty(user.getProductGroupNames())
                && user.getProductGroupNames().contains(productGroupSeed.getProductGroupName()))
            .map(DbsEntity::getId)
            .distinct()
            .collect(toList());
    }

    public void saveAssignedProductGroup(ProductGroupSeed productGroupSeed) {
        if (StringUtils.isEmpty(productGroupSeed.getId())) {
            throw new IngestException("No id set on product group " + productGroupSeed.getProductGroupName());
        }
        this.assignedProductGroups
            .computeIfAbsent(
                productGroupSeed.getExternalServiceAgreementId(), key -> new ArrayList<>())
            .add(productGroupSeed);
        storeInCache(productGroupSeed);
    }

    public String retrieveIdFromCache(ProductGroupSeed productGroupSeed) {
        return productGroupCache.get(createCacheKey(productGroupSeed));
    }

    private void storeInCache(ProductGroupSeed productGroupSeed) {
        productGroupCache.put(createCacheKey(productGroupSeed), productGroupSeed.getId());
    }
}
