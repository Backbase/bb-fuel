package com.backbase.ct.bbfuel.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Config for DBS services. The URI's point to the integration services.
 */
@Getter
@Setter
public class DbsConfig {

    /**
     * URI to actions.
     */
    private String actions;
    /**
     * URI to approvals.
     */
    private String approvals;

    /**
     * URI to Transactions.
     */
    private String transactions;

    /**
     * URI to Contact Manager.
     */
    private String contactmanager;

    /**
     * URI to messages.
     */
    private String messages;

    /**
     * URI to user.
     */
    private String user;

    /**
     * URI to legalentity.
     */
    private String legalentity;

    /**
     * URI to accessgroup.
     */
    private String accessgroup;

    /**
     * URI to arrangements.
     */
    private String arrangements;

    /**
     * URI to notifications.
     */
    private String notifications;

    /**
     *  URI to billpay mockbank.
     */
    private String billpay;

    /**
     * URI to accounts.
     */
    private String accounts;

    /**
     * URI to limits.
     */
    private String limits;

    /**
     * URI to payments.
     */
    private String payments;

    /**
     * URI to Personal Finance Management.
     */
    private String pfm;

    /**
     * URI to Products.
     */
    private String products;

    /**
     * URI to accountStatement.
     */
    private String accountStatement;

    /**
     * URI to Pockets.
     */
    private String pockets;

    /**
     * URI to positivePay.
     */
    private String positivePay;

    /**
     * URI to contentServices.
     */
    private String contentservices;

    /**
     * URI to userProfileManager.
     */
    private String userProfileManager;
}
