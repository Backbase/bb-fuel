package com.backbase.ct.dataloader.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;

public class ParserUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    public static <T> T convertJsonToObject(String jsonLocation, Class<T> valueType) throws IOException {
        InputStream resourceAsStream = valueType.getClassLoader().getResourceAsStream(jsonLocation);
        return mapper.readValue(resourceAsStream, valueType);
    }
}
