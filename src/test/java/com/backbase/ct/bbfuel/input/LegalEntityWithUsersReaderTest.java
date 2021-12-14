package com.backbase.ct.bbfuel.input;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import com.backbase.ct.bbfuel.dto.User;
import com.backbase.ct.bbfuel.enrich.LegalEntityWithUsersEnricher;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntityWithUsersReaderTest {

    @InjectMocks
    private LegalEntityWithUsersReader subject;

    @Mock
    private LegalEntityWithUsersEnricher legalEntityWithUsersEnricher;

    @Test
    public void testLoad() {
        List<LegalEntityWithUsers> legalEntities = subject.load();
        assertThat(legalEntities, hasSize(14));
        LegalEntityWithUsers greenBicycleFactory = legalEntities.get(7);
        assertThat("GBF should have admin user ids",
            greenBicycleFactory.getAdminUserExternalIds(), hasSize(2));
        assertThat("GBF should have one manager",
            greenBicycleFactory.filterUserExternalIdsOnRole("manager"), hasSize(1));
        List<String> productGroupsOfJan = greenBicycleFactory.getUsers().stream()
            .filter(user -> "jan_verschoor".equals(user.getExternalId()))
            .findFirst().orElse(new User()).getProductGroupNames();
        assertThat(productGroupsOfJan, hasSize(1));
        assertThat(greenBicycleFactory.getUsers().get(0).getProductGroupNames(), nullValue());
    }

    @Test
    public void testReadingPerformanceBusinessJson() {
        List<LegalEntityWithUsers> legalEntities =
            subject.load("data/performance/performance-test-legal-entities-with-users-business.json");
        assertThat(legalEntities, hasSize(35));
    }

    @Test
    public void testReadingPerformanceRetailJson() {
        List<LegalEntityWithUsers> legalEntities =
            subject.load("data/performance/performance-test-legal-entities-with-users-retail.json");
        assertThat(legalEntities, hasSize(140));
    }

    @Test
    public void testReadingExampleJson() {
        List<LegalEntityWithUsers> legalEntities =
            subject.load("data/example/example-custom-legal-entities-with-users.json");
        assertThat(legalEntities, hasSize(3));
    }

    @Test
    public void testMultiTenancyJsonFiles() {
        assertThat(subject.load("data/multitenancy/tenant_a.json"), hasSize(1));
        assertThat(subject.load("data/multitenancy/tenant_b.json"), hasSize(1));
        assertThat(subject.load("data/multitenancy/tenant_c.json"), hasSize(1));
        assertThat(subject.load("data/multitenancy/tenant_d.json"), hasSize(1));
        assertThat(subject.load("data/multitenancy/tenant_e.json"), hasSize(1));
    }
}
