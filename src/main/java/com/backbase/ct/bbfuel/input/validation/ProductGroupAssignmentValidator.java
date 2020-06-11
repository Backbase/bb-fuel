package com.backbase.ct.bbfuel.input.validation;

import static java.util.stream.Collectors.toList;

import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import com.backbase.ct.bbfuel.dto.entitlement.ProductGroupSeed;
import com.backbase.ct.bbfuel.input.InvalidInputException;
import java.util.List;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Validator of product group references with other input data.
 */
@Component
public class ProductGroupAssignmentValidator {

    /**
     * Verify whether the assigned product groups for each legal entity match up with given productGroupSeeds.
     *
     * @param legalEntities a list of legal entity with their direct users
     * @param productGroupSeeds a list of product group objects
     */
    public void verify(List<LegalEntityWithUsers> legalEntities, List<ProductGroupSeed> productGroupSeeds) {
        List<String> productGroupNames = productGroupSeeds.stream()
            .map(ProductGroupSeed::getProductGroupName)
            .collect(toList());

        legalEntities.forEach(legalEntityWithUsers -> verify(legalEntityWithUsers, productGroupNames));
    }

    /**
     * Verify whether the assigned product groups for each user do exist.
     *
     * @param legalEntityWithUsers a legal entity with its direct users
     * @param productGroupNames a list of product group names
     */
    public void verify(LegalEntityWithUsers legalEntityWithUsers, List<String> productGroupNames) {
        legalEntityWithUsers.getUsers().forEach(user -> {
            if (!CollectionUtils.isEmpty(user.getProductGroupNames())
                && !productGroupNames.containsAll(user.getProductGroupNames())) {
                throw new InvalidInputException(
                    String.format("User %s has been assigned non existing product groups: %s",
                        user.getExternalId(), ListUtils.subtract(user.getProductGroupNames(), productGroupNames)));
            }
        });
    }
}
