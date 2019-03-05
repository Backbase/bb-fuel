package com.backbase.ct.bbfuel.healthcheck;

import static java.util.Collections.singletonList;

import com.backbase.ct.bbfuel.client.billpay.BillPayPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BillPayHealthCheck {
    
    private GlobalProperties globalProperties = GlobalProperties.getInstance();
    
    private final BillPayPresentationRestClient billPayPresentationRestClient;
    
    public void checkBillPayServicesHealth() {
        HealthCheck healthCheck = new HealthCheck();
        long healthCheckTimeOutInMinutes = globalProperties
                        .getLong(CommonConstants.PROPERTY_HEALTH_CHECK_TIMEOUT_IN_MINUTES);
        boolean ingestBillPay = globalProperties.getBoolean(CommonConstants.PROPERTY_INGEST_BILLPAY);
        
        if (ingestBillPay && healthCheckTimeOutInMinutes > 0) {
            List<RestClient> restClients = singletonList(billPayPresentationRestClient);
            healthCheck.checkServicesHealth(restClients);
        }
    }

}