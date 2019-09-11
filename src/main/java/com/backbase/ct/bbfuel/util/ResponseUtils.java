package com.backbase.ct.bbfuel.util;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import io.restassured.response.Response;

public class ResponseUtils {

    private static BadRequestException asBadRequestException(Response response) {
        return response.then()
            .extract()
            .as(BadRequestException.class);
    }

    private static String getBadRequestMessage(Response response) {
        return asBadRequestException(response)
            .getErrors()
            .get(0)
            .getMessage();
    }

    public static boolean isBadRequestExceptionMatching(Response response, String messageRegex) {
        return response.statusCode() == SC_BAD_REQUEST && getBadRequestMessage(response).matches(messageRegex);
    }

    public static boolean isBadRequestException(Response response, String withMessage) {
        return response.statusCode() == SC_BAD_REQUEST && getBadRequestMessage(response).equals(withMessage);
    }

    public static boolean isBadRequestExceptionWithErrorKey(Response response, String withErrorKey) {
        return response.statusCode() == SC_BAD_REQUEST
            && asBadRequestException(response)
            .getErrors()
            .get(0)
            .getKey()
            .equals(withErrorKey);
    }

}
