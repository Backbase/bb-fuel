//package com.backbase.testing.dataloader.clients.actionrecipes;
//
//import com.backbase.dbs.actions.actionrecipes.presentation.rest.spec.v2.actionrecipes.ActionRecipesPostRequestBody;
//import com.backbase.testing.dataloader.clients.common.RestClient;
//import com.backbase.testing.dataloader.utils.GlobalProperties;
//import io.restassured.http.ContentType;
//import io.restassured.response.Response;
//
//import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
//import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;
//
//public class ActionRecipesPresentationRestClient extends RestClient {
//
//    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
//    private static final String SERVICE_VERSION = "v2";
//    private static final String ACTION_RECIPES_PRESENTATION_SERVICE = "action-recipes-presentation-service";
//    private static final String ENDPOINT_ACTION_RECIPES = "/action-recipes";
//
//    public ActionRecipesPresentationRestClient() {
//        super(globalProperties.getString(PROPERTY_INFRA_BASE_URI), SERVICE_VERSION);
//        setInitialPath(globalProperties.getString(PROPERTY_GATEWAY_PATH) + "/" + ACTION_RECIPES_PRESENTATION_SERVICE);
//    }
//
//    public Response createActionRecipe(ActionRecipesPostRequestBody body) {
//        return requestSpec()
//                .contentType(ContentType.JSON)
//                .body(body)
//                .post(getPath(ENDPOINT_ACTION_RECIPES));
//    }
//}
