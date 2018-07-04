package com.backbase.ct.dataloader.client.common;

import com.backbase.ct.dataloader.data.CommonConstants;
import com.backbase.ct.dataloader.util.GlobalProperties;

public abstract class AbstractRestClient extends RestClient {

    protected static GlobalProperties globalProperties = GlobalProperties.getInstance();
    protected static final String SLASH = "/";

    static final Boolean USE_LOCAL = globalProperties.getBoolean(CommonConstants.PROPERTY_CONFIGURATION_SWITCHER);

    private static final String GATEWAY = globalProperties.getString(CommonConstants.PROPERTY_GATEWAY_PATH);
    private static final String INFRA = globalProperties.getString(CommonConstants.PROPERTY_INFRA_BASE_URI);

    public AbstractRestClient() {
        super(INFRA);
    }

    public AbstractRestClient(String version) {
        super(INFRA, version);
    }

    public AbstractRestClient(String baseUri, String version) {
        super(baseUri, version);
    }

    protected final String getGatewayURI() {
        return GATEWAY;
    }

    protected abstract String composeInitialPath();

}
