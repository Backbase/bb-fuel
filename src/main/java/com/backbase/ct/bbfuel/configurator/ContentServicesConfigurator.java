package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.ContentServicesDataGenerator.createRepositories;

import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.contentservices.ContentServicesPresentationRestClient;
import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServicesConfigurator {

    private final ContentServicesPresentationRestClient contentServicesPresentationRestClient;
    private final LoginRestClient loginRestClient;

    @SuppressWarnings("squid:S1075")
    public void ingestContentForPayments() {
        
        final String repositoryId = "payments";
        final String targetPath = "/templates";
        final File paymentTemplate = new File("src/main/resources/data/content/paymentTemplate.hbs");

        log.info("Uploading template {} to repository {}", paymentTemplate.getName(), repositoryId);
        
        loginRestClient.loginBankAdmin();
        
        // create repository in content services
        contentServicesPresentationRestClient.createRepositories(createRepositories(repositoryId));

        // upload template file as content to repository
        contentServicesPresentationRestClient.uploadTemplate(repositoryId, paymentTemplate.getName(), targetPath, paymentTemplate);
    }
}
