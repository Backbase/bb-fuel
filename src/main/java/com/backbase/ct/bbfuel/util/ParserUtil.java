package com.backbase.ct.bbfuel.util;

import static java.util.Arrays.asList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParserUtil {

    private static final ObjectMapper READ_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final ObjectMapper WRITE_MAPPER = new ObjectMapper();

    public static <T> T convertJsonToObject(String jsonLocation, Class<T> valueType) throws IOException {
        InputStream resourceAsStream = valueType.getClassLoader().getResourceAsStream(jsonLocation);
        if (resourceAsStream == null) {
            resourceAsStream = new FileInputStream(jsonLocation);
        }

        return READ_MAPPER.readValue(resourceAsStream, valueType);
    }

    public static <T> List<T> convertJsonToList(String jsonLocation, Class<T> valueType) throws IOException {
        return asList(convertJsonToObject(jsonLocation, valueType));
    }

    public static void convertObjectToJson(OutputStream output, Object object) throws IOException {
        WRITE_MAPPER.writeValue(output, object);
    }

}
