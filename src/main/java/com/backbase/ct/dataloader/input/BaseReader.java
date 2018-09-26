package com.backbase.ct.dataloader.input;

import com.backbase.ct.dataloader.util.GlobalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseReader {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected GlobalProperties globalProperties = GlobalProperties.getInstance();
}
