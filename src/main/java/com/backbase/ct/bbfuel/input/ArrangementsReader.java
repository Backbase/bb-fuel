package com.backbase.ct.bbfuel.input;

import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.util.ParserUtil;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;

public class ArrangementsReader extends BaseReader {

    public List<ArrangementsPostRequestBody> load(String externalLegalEntityId) {
        List<ArrangementsPostRequestBody> arrangements;

        try {
            ArrangementsPostRequestBody[] parsedArrangements = ParserUtil.convertJsonToObject(globalProperties.getString(
                    CommonConstants.PROPERTY_PRODUCTS_DATA_JSON),
                    ArrangementsPostRequestBody[].class);

            for (ArrangementsPostRequestBody arrangement: parsedArrangements) {
                arrangement
                        .withId(UUID.randomUUID().toString())
                        .withLegalEntityIds(Collections.singleton(externalLegalEntityId));
            }

            arrangements = asList(parsedArrangements);
        } catch (IOException e) {
            logger.error("Failed parsing file with Arrangements", e);
            throw new InvalidInputException(e.getMessage(), e);
        }
        return arrangements;
    }
}
