package com.backbase.ct.bbfuel.util;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;


import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.user.rest.spec.v2.users.ConflictException;
import io.restassured.response.Response;

public class ResponseUtils {

    private static BadRequestException asBadRequestException(Response response) {
        return response.then()
            .extract()
            .as(BadRequestException.class);
    }

    private static ConflictException asConflictException(Response response) {
        return response.then()
            .extract()
            .as(ConflictException.class);
    }

    private static NotFoundException asNotFoundException(Response response) {
        return response.then()
            .extract()
            .as(NotFoundException.class);
    }

    private static String getBadRequestMessage(Response response) {
        return asBadRequestException(response)
            .getErrors()
            .get(0)
            .getMessage();
    }

    private static String getConflictException(Response response) {
        return asConflictException(response)
            .getErrors()
            .get(0)
            .getMessage();
    }

    private static String getNotFoundException(Response response) {
        return asNotFoundException(response)
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

    public static boolean isConflictException(Response response, String withMessage) {
        return response.statusCode() == SC_CONFLICT && getConflictException(response).equals(withMessage);
    }

    public static boolean isNotFoundException(Response response, String withMessage){
        return response.statusCode() == SC_NOT_FOUND && getNotFoundException(response).equals(withMessage);
    }

    public static boolean isBadRequestExceptionWithErrorKey(Response response, String withErrorKey) {
        return response.statusCode() == SC_BAD_REQUEST
            && asBadRequestException(response)
            .getErrors()
            .get(0)
            .getKey()
            .equals(withErrorKey);
    }

    public static boolean isConflictExceptionWithErrorKey(Response response, String withErrorKey) {
        return response.statusCode() == SC_CONFLICT
            && asConflictException(response)
            .getErrors()
            .get(0)
            .equals(withErrorKey);
    }

    public static boolean isNotFoundExceptionWithErrorKey(Response response, String withErrorKey) {
        return response.statusCode() == SC_NOT_FOUND
            && asNotFoundException(response)
            .getErrors()
            .get(0)
            .getKey()
            .equals(withErrorKey);
    }

}
