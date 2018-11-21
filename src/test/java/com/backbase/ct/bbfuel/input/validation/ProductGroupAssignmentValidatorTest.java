package com.backbase.ct.bbfuel.input.validation;

import static com.backbase.ct.bbfuel.service.factory.ProductGroupSeedFactory.PRODUCT_GROUP_NAME_INTTRADE;
import static com.backbase.ct.bbfuel.service.factory.ProductGroupSeedFactory.createProductGroupSeeds;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import com.backbase.ct.bbfuel.dto.User;
import com.backbase.ct.bbfuel.input.InvalidInputException;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductGroupAssignmentValidatorTest {

    @InjectMocks
    private ProductGroupAssignmentValidator subject;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static LegalEntityWithUsers createLegalEntityWithUsers(List<String> productGroupNames) {
        User user = User.builder().externalId("U0001").productGroupNames(
            productGroupNames).build();
        LegalEntityWithUsers legalEntity = new LegalEntityWithUsers();
        legalEntity.setUsers(singletonList(user));
        return legalEntity;
    }

    @Test
    public void verifyExistingProductGroup() {
        List<String> productGroupNames = singletonList(PRODUCT_GROUP_NAME_INTTRADE);
        LegalEntityWithUsers legalEntityWithUsers = createLegalEntityWithUsers(productGroupNames);
        this.subject.verify(singletonList(legalEntityWithUsers), createProductGroupSeeds(productGroupNames));
    }

    @Test
    public void verifyNonExistingProductGroups() {
        List<String> nonExistant = asList("Local Trade", "Community Trade");
        LegalEntityWithUsers legalEntityWithUsers = createLegalEntityWithUsers(nonExistant);

        expectedException.expect(InvalidInputException.class);
        expectedException.expectMessage("User U0001 has been assigned non existing product groups: "
            + nonExistant);
        this.subject.verify(singletonList(legalEntityWithUsers),
            createProductGroupSeeds(singletonList(PRODUCT_GROUP_NAME_INTTRADE)));
    }
}
