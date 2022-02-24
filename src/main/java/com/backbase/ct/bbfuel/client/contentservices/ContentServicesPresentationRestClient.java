package com.backbase.ct.bbfuel.client.contentservices;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.contentservices.client.v2.model.Repository;
import io.restassured.http.ContentType;
import java.io.File;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentServicesPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;
    
    private static final String ENDPOINT_REPOSITORY_CREATE = "/repositories";
    private static final String ENDPOINT_REPOSITORY_UPLOAD = "/repositories/{repositoryId}/upload";
    private static final String PATH_PARAM_REPOSITORY_ID = "repositoryId";
    private static final String QUERY_PARAM_NAME = "name";
    private static final String QUERY_PARAM_TARGET_PATH = "targetPath";

    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setInitialPath(String.format("%s/%s", config.getDbsServiceNames().getContentservices(), CLIENT_API));
    }

    public void createRepositories(List<Repository> repositories) {
        requestSpec()
            .contentType(ContentType.JSON)
            .body(repositories)
            .post(ENDPOINT_REPOSITORY_CREATE);
    }

    public void uploadTemplate(String repositoryId, String name, String targetPath, File template) {
        requestSpec()
            .pathParam(PATH_PARAM_REPOSITORY_ID, repositoryId)
            .queryParam(QUERY_PARAM_NAME, name)
            .queryParam(QUERY_PARAM_TARGET_PATH, targetPath)
            .multiPart(template)
            .post(ENDPOINT_REPOSITORY_UPLOAD);
    }
}
