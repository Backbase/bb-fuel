package com.backbase.ct.bbfuel.service;

import static com.backbase.ct.bbfuel.service.factory.ProductGroupSeedFactory.PRODUCT_GROUP_NAME_PAYROLL;
import static com.backbase.ct.bbfuel.service.factory.ProductGroupSeedFactory.createProductGroupSeed;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.backbase.ct.bbfuel.dto.entitlement.ProductGroupSeed;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductGroupServiceTest {

    @InjectMocks
    private ProductGroupService subject = new ProductGroupService();

    @Test
    public void testFindProductGroup() {
        String id = UUID.randomUUID().toString();
        ProductGroupSeed productGroupSeed = createProductGroupSeed(PRODUCT_GROUP_NAME_PAYROLL);
        productGroupSeed.setId(id);
        subject.saveAssignedProductGroup(productGroupSeed);

        assertThat(id, is(subject.retrieveIdFromCache(productGroupSeed)));
    }
}
