package com.backbase.ct.dataloader.input;

import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_JOB_PROFILES_JSON_LOCATION;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

import com.backbase.ct.dataloader.dto.entitlement.JobProfile;
import com.backbase.ct.dataloader.enrich.JobProfileEnricher;
import com.backbase.ct.dataloader.util.ParserUtil;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
        if (jobProfiles == null || jobProfiles.length == 0) {
            throw new InvalidInputException("No jobProfiles have been parsed");
        }
        List<String> names = stream(jobProfiles)
            .map(JobProfile::getJobProfileName)
            .collect(Collectors.toList());
        if (!names.isEmpty() && (
            names.size() != new HashSet<>(names).size())) {
            throw new InvalidInputException(String.format("JobProfiles have duplicate names [%s]", names));
        }
    }
}
