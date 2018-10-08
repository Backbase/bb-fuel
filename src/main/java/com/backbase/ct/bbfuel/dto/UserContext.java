package com.backbase.ct.bbfuel.dto;

public class UserContext {

    private User user;

    private String internalUserId;

    private String externalUserId;

    private String internalServiceAgreementId;

    private String externalServiceAgreementId;

    private String internalLegalEntityId;

    private String externalLegalEntityId;

    public String getExternalUserId() {
        return externalUserId;
    }

    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    public UserContext withExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
        return this;
    }

    public String getInternalUserId() {
        return internalUserId;
    }

    public void setInternalUserId(String internalUserId) {
        this.internalUserId = internalUserId;
    }

    public User getUser() {
        return this.user;
    }

    public UserContext withInternalUserId(String internalUserId) {
        this.internalUserId = internalUserId;
        return this;
    }

    public UserContext withUser(User user) {
        this.user= user;
        return this;
    }

    public String getExternalServiceAgreementId() {
        return externalServiceAgreementId;
    }

    public void setExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
    }

    public UserContext withExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
        return this;
    }

    public String getInternalServiceAgreementId() {
        return internalServiceAgreementId;
    }

    public void setInternalServiceAgreementId(String internalServiceAgreementId) {
        this.internalServiceAgreementId = internalServiceAgreementId;
    }

    public UserContext withInternalServiceAgreementId(String internalServiceAgreementId) {
        this.internalServiceAgreementId = internalServiceAgreementId;
        return this;
    }

    public String getExternalLegalEntityId() {
        return externalLegalEntityId;
    }

    public void setExternalLegalEntityId(String externalLegalEntityId) {
        this.externalLegalEntityId = externalLegalEntityId;
    }

    public UserContext withExternalLegalEntityId(String externalLegalEntityId) {
        this.externalLegalEntityId = externalLegalEntityId;
        return this;
    }

    public String getInternalLegalEntityId() {
        return internalLegalEntityId;
    }

    public void setInternalLegalEntityId(String internalLegalEntityId) {
        this.internalLegalEntityId = internalLegalEntityId;
    }

    public UserContext withInternalLegalEntityId(String internalLegalEntityId) {
        this.internalLegalEntityId = internalLegalEntityId;
        return this;
    }
}
