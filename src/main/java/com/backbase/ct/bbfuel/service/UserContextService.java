package com.backbase.ct.bbfuel.service;

import com.backbase.ct.bbfuel.client.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.legalentity.LegalEntityPresentationRestClient;
import com.backbase.ct.bbfuel.client.user.UserPresentationRestClient;
import com.backbase.ct.bbfuel.dto.User;
import com.backbase.ct.bbfuel.dto.UserContext;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.dbs.user.manager.models.v2.LegalEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * A simple local service with no integration at all.
 */
@Service
@RequiredArgsConstructor
public class UserContextService {

    protected GlobalProperties globalProperties = GlobalProperties.getInstance();

    private final LoginRestClient loginRestClient;

    private final UserPresentationRestClient userPresentationRestClient;

    private final UserContextPresentationRestClient userContextPresentationRestClient;

    private final ServiceAgreementsPresentationRestClient serviceAgreementsPresentationRestClient;

    private final LegalEntityPresentationRestClient legalEntityPresentationRestClient;

    public UserContext getUserContextBasedOnMSAByExternalUserId(User user) {
        return getUserContextBasedOnMSAByExternalUserId(user, null);
    }

    public UserContext getUserContextBasedOnMSAByExternalUserId(User user,
        LegalEntity legalEntity) {
        this.loginRestClient.loginBankAdmin();
        this.userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        String internalUserId = this.userPresentationRestClient.getUserByExternalId(user.getExternalId()).getId();

        if (legalEntity == null) {
            legalEntity = this.userPresentationRestClient.retrieveLegalEntityByExternalUserId(user.getExternalId());
        }

        String internalServiceAgreementId = this.legalEntityPresentationRestClient
            .getMasterServiceAgreementOfLegalEntity(legalEntity.getId())
            .getId();

        String externalServiceAgreementId = this.serviceAgreementsPresentationRestClient
            .retrieveServiceAgreement(internalServiceAgreementId)
            .getExternalId();

        return new UserContext()
            .withUser(user)
            .withInternalUserId(internalUserId)
            .withExternalUserId(user.getExternalId())
            .withInternalServiceAgreementId(internalServiceAgreementId)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withInternalLegalEntityId(legalEntity.getId())
            .withExternalLegalEntityId(legalEntity.getExternalId());
    }
}
