package com.backbase.ct.dataloader.input;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import com.backbase.ct.dataloader.dto.LegalEntityWithUsers;
import com.backbase.ct.dataloader.enrich.LegalEntityWithUsersEnricher;
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
        assertThat(legalEntities, hasSize(8));
        assertThat("GBF should have admin user ids",
            legalEntities.get(7).getAdminUserExternalIds(), hasSize(2));
        assertThat("GBF should have one manager",
            legalEntities.get(7).filterUserExternalIdsOnRole("manager"), hasSize(1));
    }
}
