package com.backbase.ct.bbfuel.enrich;

import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import com.backbase.ct.bbfuel.dto.User;
import com.backbase.ct.bbfuel.dto.entitlement.ProductGroupSeed;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ProductGroupSeedEnricher {

    /**
     * Enrich all users of all legal entities.
     */
    public void enrichLegalEntitiesWithUsers(List<LegalEntityWithUsers> legalEntityWithUsers,
        List<ProductGroupSeed> productGroupSeeds) {
        List<String> productGroupNames = productGroupSeeds.stream()
            .map(ProductGroupSeed::getProductGroupName)
            .collect(Collectors.toList());
        legalEntityWithUsers.forEach(le -> {
            enrichUsers(le.getUsers(), productGroupNames);
        });
    }

    /**
     * Assign all available product group names when not explicitly set.
     *
     * @param users list of users
     * @param productGroupNames list of product group names
     */
    private void enrichUsers(List<User> users, List<String> productGroupNames) {
        users.forEach(user -> {
            if (user.getProductGroupNames() == null) {
                user.setProductGroupNames(productGroupNames);
            }
        });
    }
}
