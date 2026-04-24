package com.mycompany.smartcampusapi.exceptions;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        return Response.status(422) // 422 Unprocessable Entity
                .entity("{\"error\":\"" + ex.getMessage() + "\"}")
                .type("application/json")
                .build();
    }
}