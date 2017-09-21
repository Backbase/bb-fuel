package com.backbase.testing.dataloader.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class ParserUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    public static <T> T convertJsonToObject(String jsonLocation, Class<T> valueType) throws IOException {
        InputStream resourceAsStream = valueType.getClassLoader().getResourceAsStream(jsonLocation);
        return mapper.readValue(resourceAsStream, valueType);
    }

    public static <T> T convertJsonToObject(String jsonLocation, TypeReference<T> typeRef) throws IOException {
        InputStream resourceAsStream = ClassLoader.getSystemClassLoader().getResourceAsStream(jsonLocation);
        return mapper.readValue(resourceAsStream, typeRef);
    }
}
