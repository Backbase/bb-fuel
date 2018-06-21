package com.backbase.ct.dataloader.clients.turnovers;

import static java.util.Arrays.asList;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.dataloader.clients.common.AbstractRestClient;
import com.backbase.presentation.categories.management.rest.spec.v2.categories.id.CategoryGetResponseBody;
import java.util.List;

public class CategoriesPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String CATEGORIES_MANAGEMENT_PRESENTATION_SERVICE = "categories-management-presentation-service";
    private static final String ENDPOINT_CATEGORIES = "/categories";

    public CategoriesPresentationRestClient() {
        super(SERVICE_VERSION);
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
        return getGatewayURI() + SLASH + CATEGORIES_MANAGEMENT_PRESENTATION_SERVICE;
    }

}
