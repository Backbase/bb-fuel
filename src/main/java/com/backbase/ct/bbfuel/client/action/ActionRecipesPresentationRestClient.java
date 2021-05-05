package com.backbase.ct.bbfuel.client.action;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.action.client.v2.model.ActionRecipesPostRequestBodyParent;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class ActionRecipesPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String ENDPOINT_ACTION_RECIPES = "/action-recipes";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(config.getDbsServiceNames().getActions() + "/" + CLIENT_API);
    }

    public Response createActionRecipe(ActionRecipesPostRequestBodyParent actionRecipesPostRequestBody) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(actionRecipesPostRequestBody)
            .post(getPath(ENDPOINT_ACTION_RECIPES));
    }
}
