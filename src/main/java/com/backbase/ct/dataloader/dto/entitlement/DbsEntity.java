package com.backbase.ct.dataloader.dto.entitlement;

import lombok.Data;

/**
 * Object that is storeable in DBS.
 */
@Data
public abstract class DbsEntity {

    /**
     *  Id gets assigned when it has been stored successfully.
     */
    private String id;
    /**
     * If an entity is dedicated to a service agreement the serviceAgreementId should be set.
     */
    private String externalServiceAgreementId;
}
