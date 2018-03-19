package com.backbase.testing.dataloader.clients.common;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_CONFIGURATION_SWITCHER;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_LOCAL_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_LOCAL_INFRA_BASE_URI;

import com.backbase.testing.dataloader.utils.GlobalProperties;

public abstract class AbstractRestClient extends RestClient {

    protected static GlobalProperties globalProperties = GlobalProperties.getInstance();
    protected static final Boolean USE_LOCAL = globalProperties.getBoolean(PROPERTY_CONFIGURATION_SWITCHER);
    protected static final String SLASH = "/";

    private static final String GATEWAY = globalProperties.getString(PROPERTY_GATEWAY_PATH);
    private static final String LOCAL_GATEWAY = globalProperties.getString(PROPERTY_LOCAL_GATEWAY_PATH);
    private static final String LOCAL_INFRA = globalProperties.getString(PROPERTY_LOCAL_INFRA_BASE_URI);
    private static final String INFRA = globalProperties.getString(PROPERTY_INFRA_BASE_URI);

    public AbstractRestClient() {
        super(USE_LOCAL ? LOCAL_INFRA : INFRA);
    }

    public AbstractRestClient(String version) {
        super(USE_LOCAL ? LOCAL_INFRA : INFRA, version);
    }

    public AbstractRestClient(String baseUri, String version) {
        super(baseUri, version);
    }

    protected final String getGatewayURI() {
        return USE_LOCAL ? LOCAL_GATEWAY : GATEWAY;
    }

    protected abstract String composeInitialPath();

}
