package com.backbase.ct.bbfuel.data;

import com.backbase.dbs.contentservices.client.v3.model.AntivirusScanTrigger;
import com.backbase.dbs.contentservices.client.v3.model.Repository;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ContentServicesDataGenerator {

    private ContentServicesDataGenerator() {
    }
    
    public static List<Repository> createRepositories(String... repositoryIds) {
        return Arrays.stream(repositoryIds)
            .map(repositoryId ->
                new Repository()
                    .repositoryId(repositoryId)
                    .name(repositoryId)
                    .description(String.format("Repository for %s", repositoryId))
                    .implementation("DB")
                    .versioningEnabled(false)
                    .isPrivate(false)
                    .antivirusScanTrigger(AntivirusScanTrigger.NONE))
            .collect(Collectors.toList());
    }
}
