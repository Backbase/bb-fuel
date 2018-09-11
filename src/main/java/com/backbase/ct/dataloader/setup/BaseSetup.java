package com.backbase.ct.dataloader.setup;

import com.backbase.ct.dataloader.client.common.LoginRestClient;
import com.backbase.ct.dataloader.util.GlobalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseSetup {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected GlobalProperties globalProperties = GlobalProperties.getInstance();

    protected LoginRestClient loginRestClient = new LoginRestClient();
}
