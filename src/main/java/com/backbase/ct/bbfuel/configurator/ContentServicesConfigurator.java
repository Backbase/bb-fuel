package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.ContentServicesDataGenerator.createRepositories;

import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.contentservices.ContentServicesPresentationRestClient;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServicesConfigurator {

    private final ContentServicesPresentationRestClient contentServicesPresentationRestClient;
    private final LoginRestClient loginRestClient;

    public void ingestContentForPayments() {

        final String resourceLocation = "data/content/paymentTemplate.hbs";

        InputStream resource = ContentServicesConfigurator.class.getClassLoader().getResourceAsStream(resourceLocation);
        if (resource == null) {
            log.error("Not able to find the resource at location {}", resourceLocation);
            return;
        }
        File tempFile;
        try {
            tempFile = createTempFile(resource);
        } catch (IOException ex) {
            log.error("Not able to create temp file for resource with location {}", resourceLocation);
            return;
        }
        try {
            uploadTemplate(tempFile);
        } finally {
            try {
                Files.deleteIfExists(tempFile.toPath());
            } catch (IOException ex) {
                log.error("Not able delete temp file {}", tempFile.toPath());
            }
        }
    }

    private File createTempFile(InputStream resourceAsStream) throws IOException {
        File tempFile = Files.createTempFile("paymentTemplate", "hbs").toFile();
        FileUtils.copyInputStreamToFile(resourceAsStream, tempFile);
        return tempFile;
    }

    @SuppressWarnings("squid:S1075")
    private void uploadTemplate(File tempFile) {
        final String fileName = "paymentTemplate.hbs";
        final String repositoryId = "payments";
        final String targetPath = "/templates";

        log.info("Uploading template {} to repository {}", fileName, repositoryId);

        loginRestClient.loginBankAdmin();
        // create repository in content services
        contentServicesPresentationRestClient.createRepositories(createRepositories(repositoryId));
        // upload template file as content to repository
        contentServicesPresentationRestClient.uploadTemplate(repositoryId, fileName, targetPath, tempFile);
    }
}
