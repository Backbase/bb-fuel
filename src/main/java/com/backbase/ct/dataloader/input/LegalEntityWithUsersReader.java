package com.backbase.ct.dataloader.input;

import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON;
import static java.util.Arrays.asList;

import com.backbase.ct.dataloader.dto.LegalEntityWithUsers;
import com.backbase.ct.dataloader.enrich.LegalEntityWithUsersEnricher;
import com.backbase.ct.dataloader.util.GlobalProperties;
import com.backbase.ct.dataloader.util.ParserUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LegalEntityWithUsersReader {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private GlobalProperties globalProperties = GlobalProperties.getInstance();

    private final LegalEntityWithUsersEnricher legalEntityWithUsersEnricher;

    /**
     * Load the configured json file.
     */
    public List<LegalEntityWithUsers> load() {
        return load(this.globalProperties.getString(PROPERTY_LEGAL_ENTITIES_WITH_USERS_JSON));
    }

    /**
     * Load json file.
     */
    public List<LegalEntityWithUsers> load(String uri) {
        List<LegalEntityWithUsers> entities;
        try {
            LegalEntityWithUsers[] parsedEntities = ParserUtil.convertJsonToObject(uri, LegalEntityWithUsers[].class);
            validate(parsedEntities);
            entities = asList(parsedEntities);
            legalEntityWithUsersEnricher.enrich(entities);
        } catch(IOException e) {
            logger.error("Failed parsing file with entities", e);
            throw new InvalidInputException(e.getMessage(), e);
        }
        return entities;
    }

    /**
     * Check on duplicate ids.
     */
    private void validate(LegalEntityWithUsers[] legalEntityWithUsers) {
        if (legalEntityWithUsers == null || legalEntityWithUsers.length == 0) {
            throw new InvalidInputException("No legal entities have been parsed");
        }
        Arrays.stream(legalEntityWithUsers).forEach(le -> {
            List<String> ids = le.getUserExternalIds();
            if (ids != null && le.getUsers() != null && (ids.size() != le.getUsers().size())) {
                throw new InvalidInputException(String.format("LE [%s] has [%s] duplicate userExternalIds",
                    le.getLegalEntityExternalId(), (le.getUsers().size() - le.getUserExternalIds().size())));
            }
        });
    }
}
