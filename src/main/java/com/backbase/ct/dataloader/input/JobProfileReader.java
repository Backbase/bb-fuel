package com.backbase.ct.dataloader.input;

import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_JOB_PROFILES_JSON_LOCATION;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import com.backbase.ct.dataloader.dto.entitlement.JobProfile;
import com.backbase.ct.dataloader.enrich.JobProfileEnricher;
import com.backbase.ct.dataloader.util.ParserUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobProfileReader extends BaseReader {

    private final JobProfileEnricher jobProfileEnricher;

    /**
     * Load the configured json file.
     */
    public List<JobProfile> load() {
        return load(this.globalProperties.getString(PROPERTY_JOB_PROFILES_JSON_LOCATION));
    }

    /**
     * Load json file.
     */
    public List<JobProfile> load(String uri) {
        List<JobProfile> jobProfiles;
        try {
            JobProfile[] parsedJobProfiles = ParserUtil.convertJsonToObject(uri, JobProfile[].class);
            validate(parsedJobProfiles);
            jobProfiles = asList(parsedJobProfiles);
            jobProfileEnricher.enrich(jobProfiles);
        } catch (IOException e) {
            logger.error("Failed parsing file with entities", e);
            throw new InvalidInputException(e.getMessage(), e);
        }
        return jobProfiles;
    }
    /**
     * Check on duplicate names.
     */
    private void validate(JobProfile[] jobProfiles) {
        if (ArrayUtils.isEmpty(jobProfiles)) {
            throw new InvalidInputException("No job profiles have been parsed");
        }
        List<String> names = stream(jobProfiles)
            .map(JobProfile::getJobProfileName)
            .collect(toList());
        Set<String> uniqueNames = new HashSet<>(names);
        if (uniqueNames.size() != jobProfiles.length) {
            throw new InvalidInputException(String.format("Job profiles with duplicate names: %s",
                ListUtils.subtract(names, new ArrayList<>(uniqueNames))));
        }
    }
}
