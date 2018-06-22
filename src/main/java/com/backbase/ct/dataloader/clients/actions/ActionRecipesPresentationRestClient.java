package com.backbase.ct.dataloader.clients.actions;

import com.backbase.ct.dataloader.clients.common.AbstractRestClient;
import com.backbase.dbs.actions.actionrecipes.presentation.rest.spec.v2.actionrecipes.ActionRecipesPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ActionRecipesPresentationRestClient extends AbstractRestClient {

    private static final String SERVICE_VERSION = "v2";
    private static final String ACTIONRECIPES_PRESENTATION_SERVICE = "actionrecipes-presentation-service";
    private static final String ENDPOINT_ACTION_RECIPES = "/action-recipes";

    public ActionRecipesPresentationRestClient() {
        super(SERVICE_VERSION);
        setInitialPath(composeInitialPath());
    }

    public Response createActionRecipe(ActionRecipesPostRequestBody actionRecipesPostRequestBody) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(actionRecipesPostRequestBody)
            .post(getPath(ENDPOINT_ACTION_RECIPES));
    }

    @Override
    protected String composeInitialPath() {
        return getGatewayURI() + SLASH + ACTIONRECIPES_PRESENTATION_SERVICE;
    }

}
