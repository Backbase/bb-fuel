package com.backbase.testing.dataloader.dto;

public class ProductSummaryQueryParameters {

    private String legalEntityId;

    private String businessFunction;

    private String resourceName;

    private String privilege;

    private Boolean externalTransferAllowed;

    private Boolean creditAccount;

    private Boolean debitAccount;

    private Integer from;

    private String cursor;

    private Integer size;

    private String orderBy;

    private String direction;

    private String searchTerm;

    public ProductSummaryQueryParameters() {

    }

    public ProductSummaryQueryParameters withLegalEntityId(String legalEntityId) {
        this.legalEntityId = legalEntityId;
        return this;
    }

    public ProductSummaryQueryParameters withResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    public ProductSummaryQueryParameters withSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
        return this;
    }

    public ProductSummaryQueryParameters withCursor(String cursor) {
        this.cursor = cursor;
        return this;
    }

    public ProductSummaryQueryParameters withBusinessFunction(String businessFunction) {
        this.businessFunction = businessFunction;
        return this;
    }

    public ProductSummaryQueryParameters withPrivilege(String privilege) {
        this.privilege = privilege;
        return this;
    }

    public ProductSummaryQueryParameters withExternalTransferAllowed(Boolean externalTransferAllowed) {
        this.externalTransferAllowed = externalTransferAllowed;
        return this;
    }

    public ProductSummaryQueryParameters withCreditAccount(Boolean creditAccount) {
        this.creditAccount = creditAccount;
        return this;
    }

    public ProductSummaryQueryParameters withDebitAccount(Boolean debitAccount) {
        this.debitAccount = debitAccount;
        return this;
    }

    public ProductSummaryQueryParameters withFrom(Integer from) {
        this.from = from;
        return this;
    }

    public ProductSummaryQueryParameters withSize(Integer size) {
        this.size = size;
        return this;
    }

    public ProductSummaryQueryParameters withOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public ProductSummaryQueryParameters withDirection(String direction) {
        this.direction = direction;
        return this;
    }

    public String getLegalEntityId() {
        return legalEntityId;

    }

    public String getBusinessFunction() {
        return businessFunction;
    }

    public String getPrivilege() {
        return privilege;
    }

    public Boolean getExternalTransferAllowed() {
        return externalTransferAllowed;
    }

    public Boolean getCreditAccount() {
        return creditAccount;
    }

    public Boolean getDebitAccount() {
        return debitAccount;
    }

    public Integer getFrom() {
        return from;
    }

    public Integer getSize() {
        return size;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public String getDirection() {
        return direction;
    }

    public String getCursor() {
        return cursor;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public String getResourceName() {
        return resourceName;
    }
}
