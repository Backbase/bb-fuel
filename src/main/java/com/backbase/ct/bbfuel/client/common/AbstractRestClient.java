package com.backbase.ct.bbfuel.client.common;

import com.backbase.ct.bbfuel.util.GlobalProperties;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class AbstractRestClient extends RestClient {

    protected static GlobalProperties globalProperties = GlobalProperties.getInstance();

    protected abstract String composeInitialPath();

}
