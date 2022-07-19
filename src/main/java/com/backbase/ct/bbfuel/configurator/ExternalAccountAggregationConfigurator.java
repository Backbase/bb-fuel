package com.backbase.ct.bbfuel.configurator;

import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.eaa.ExternalAccountAggregatorActuatorClient;
import com.backbase.ct.bbfuel.client.eaa.ExternalAccountAggregatorRestClient;
import com.backbase.ct.bbfuel.client.legalentity.LegalEntityPresentationRestClient;
import com.backbase.ct.bbfuel.dto.LegalEntityWithUsers;
import com.backbase.ct.bbfuel.dto.User;
import com.backbase.dbs.accesscontrol.client.v2.model.LegalEntityBase;
import com.backbase.dbs.eaa.client.v1.model.AccountAggregationFlow;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalAccountAggregationConfigurator {

    private final ExternalAccountAggregatorActuatorClient externalAccountAggregatorActuatorClient;
    private final ExternalAccountAggregatorRestClient externalAccountAggregatorRestClient;
    private final LegalEntityPresentationRestClient legalEntityPresentationRestClient;
    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;

    /**
     * Triggers external account aggregation.
     *
     * @param legalEntities legal entities to aggregate data for.
     */
    public void aggregateExternalAccounts(List<LegalEntityWithUsers> legalEntities) {
        log.debug("Going to trigger external account aggregation for [{}] legal entities", legalEntities.size());

        loginRestClient.loginBankAdmin();
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        legalEntities.forEach(this::makeAggregationDataAvailable);
        legalEntities.forEach(legalEntity -> legalEntity.getUsers().forEach(this::executeAggregationFlow));
    }

    private void makeAggregationDataAvailable(LegalEntityWithUsers legalEntityWithUsers) {
        log.debug("Making aggregation data available for legal entity ID [{}]",
            legalEntityWithUsers.getLegalEntityExternalId());
        LegalEntityBase legalEntityBase = legalEntityPresentationRestClient.retrieveLegalEntityByExternalId(
            legalEntityWithUsers.getLegalEntityExternalId());
        externalAccountAggregatorActuatorClient.makeAggregationDataAvailable(legalEntityBase);
    }

    private void executeAggregationFlow(User user) {
        log.debug("Execution external account aggregation for user [{}]", user);
        loginRestClient.login(user.getExternalId(), user.getExternalId());
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        AccountAggregationFlow flow = externalAccountAggregatorRestClient.createFlow();
        externalAccountAggregatorRestClient.finishFlow(flow);
    }
}
