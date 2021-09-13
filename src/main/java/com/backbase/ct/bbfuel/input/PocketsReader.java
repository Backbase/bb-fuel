package com.backbase.ct.bbfuel.input;

import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.util.ParserUtil;
import com.backbase.dbs.pocket.tailor.client.v2.model.PocketPostRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PocketsReader extends BaseReader {

    public List<PocketPostRequest> load() {
        return load(globalProperties.getString(CommonConstants.PROPERTY_POCKETS_DATA_JSON));
    }

    private List<PocketPostRequest> load(String uri) {

        try {
            PocketPostRequest[] parsedPockets = ParserUtil.convertJsonToObject(uri, PocketPostRequest[].class);
            return Arrays.asList(parsedPockets);
        } catch (IOException e) {
            log.error("Failed parsing file with Pockets", e);
            throw new InvalidInputException(e.getMessage(), e);
        }
    }
}
