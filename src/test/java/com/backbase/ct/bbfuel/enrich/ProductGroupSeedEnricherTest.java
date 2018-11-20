package com.backbase.ct.bbfuel.enrich;

import static com.backbase.ct.bbfuel.service.factory.LegalEntityWithUsersFactory.createLegalEntityWithUsers;
import static com.backbase.ct.bbfuel.service.factory.ProductGroupSeedFactory.PRODUCT_GROUP_NAME_INTTRADE;
import static com.backbase.ct.bbfuel.service.factory.ProductGroupSeedFactory.PRODUCT_GROUP_NAME_PAYROLL;
import static com.backbase.ct.bbfuel.service.factory.ProductGroupSeedFactory.createProductGroupSeeds;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import com.backbase.ct.bbfuel.dto.entitlement.ProductGroupSeed;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductGroupSeedEnricherTest {

    @InjectMocks
    private ProductGroupSeedEnricher subject;

    @Test
    public void testEnrich() {
        LegalEntityWithUsers legalEntity = createLegalEntityWithUsers(asList("U0001", "U0002", "U0003"));
        List<ProductGroupSeed> productGroups = createProductGroupSeeds(
            asList(PRODUCT_GROUP_NAME_INTTRADE, PRODUCT_GROUP_NAME_PAYROLL));

        legalEntity.getUsers().forEach(user -> {
            assertThat("User has not been assigned product groups",
                user.getProductGroupNames(), nullValue());
        });
        legalEntity.getUsers().get(1).setProductGroupNames(asList(PRODUCT_GROUP_NAME_INTTRADE));
        legalEntity.getUsers().get(2).setProductGroupNames(emptyList());
        subject.enrichLegalEntitiesWithUsers(singletonList(legalEntity), productGroups);

        assertThat("User has been enriched with all product groups",
            legalEntity.getUsers().get(0).getProductGroupNames(), hasSize(productGroups.size()));
        assertThat("User has not been enriched",
            legalEntity.getUsers().get(1).getProductGroupNames(), hasSize(1));
        assertThat("User has not been enriched",
            legalEntity.getUsers().get(2).getProductGroupNames(), hasSize(0));
    }
}
