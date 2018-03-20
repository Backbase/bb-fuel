package com.backbase.testing.dataloader.clients.common;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_CONFIGURATION_SWITCHER;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;

import com.backbase.testing.dataloader.utils.GlobalProperties;

public abstract class AbstractRestClient extends RestClient {

    protected static GlobalProperties globalProperties = GlobalProperties.getInstance();
    protected static final String SLASH = "/";

    static final Boolean USE_LOCAL = globalProperties.getBoolean(PROPERTY_CONFIGURATION_SWITCHER);

    private static final String GATEWAY = globalProperties.getString(PROPERTY_GATEWAY_PATH);
    private static final String INFRA = globalProperties.getString(PROPERTY_INFRA_BASE_URI);

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
