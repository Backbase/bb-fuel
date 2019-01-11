package com.backbase.ct.bbfuel.client.pfm;

import static java.util.Arrays.asList;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.AbstractRestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.presentation.categories.management.rest.spec.v2.categories.id.CategoryGetResponseBody;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoriesPresentationRestClient extends AbstractRestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String CATEGORIES_MANAGEMENT_PRESENTATION_SERVICE = "categories-management-presentation-service";
    private static final String ENDPOINT_CATEGORIES = "/categories";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public List<CategoryGetResponseBody> retrieveCategories() {
        return asList(requestSpec()
            .get(getPath(ENDPOINT_CATEGORIES))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(CategoryGetResponseBody[].class));
    }

    @Override
    protected String composeInitialPath() {
        return CATEGORIES_MANAGEMENT_PRESENTATION_SERVICE;
    }

}
