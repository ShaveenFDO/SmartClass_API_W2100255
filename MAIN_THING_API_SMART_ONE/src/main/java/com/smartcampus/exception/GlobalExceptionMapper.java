package com.smartcampus.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {
        // If it's already a JAX-RS HTTP exception (404, 405 etc), pass it through correctly
        // Without this check, a 404 gets turned into a 500 which is wrong
        if (e instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) e;
            int status = wae.getResponse().getStatus();
            LOG.warning("JAX-RS exception [" + status + "]: " + e.getMessage());
            return ErrorResponse.build(status, "HTTP_" + status, e.getMessage());
        }

        // Only true unexpected crashes reach here
        LOG.severe("Unhandled exception [" + e.getClass().getName() + "]: " + e.getMessage());
        return ErrorResponse.build(500, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please contact the system administrator.");
    }
}
