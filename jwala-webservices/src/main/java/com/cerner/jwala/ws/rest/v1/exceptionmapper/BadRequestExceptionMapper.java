package com.cerner.jwala.ws.rest.v1.exceptionmapper;

import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    @Override
    public Response toResponse(final BadRequestException exception) {
        return ResponseBuilder.notOk(Response.Status.BAD_REQUEST,
                                     exception);
    }
}
