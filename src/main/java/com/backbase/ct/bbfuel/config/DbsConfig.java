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
     *  URI to billpay mockbank
     */
    private String billpay;

    /**
     * URI to accounts
     */
    private String accounts;

}
