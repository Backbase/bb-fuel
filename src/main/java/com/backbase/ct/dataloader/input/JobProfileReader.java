package com.backbase.ct.dataloader.input;

import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_JOB_PROFILES_JSON_LOCATION;
import static java.util.Arrays.asList;

import com.backbase.ct.dataloader.dto.entitlement.JobProfile;
import com.backbase.ct.dataloader.util.ParserUtil;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JobProfileReader extends BaseReader {

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
        List<JobProfile> entities;
        try {
            JobProfile[] parsedEntities = ParserUtil.convertJsonToObject(uri, JobProfile[].class);
            entities = asList(parsedEntities);
        } catch (IOException e) {
            logger.error("Failed parsing file with entities", e);
            throw new InvalidInputException(e.getMessage(), e);
        }
        return entities;
    }
}
