package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Single filter class implementing both request and response filtering.
 * This is a "cross-cutting concern" — it runs for every endpoint automatically
 * without needing Logger.info() calls inside each resource method.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOG.info("Incoming request: [" + requestContext.getMethod() + "] "
                + requestContext.getUriInfo().getRequestUri());
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOG.info("Outgoing response: HTTP " + responseContext.getStatus()
                + " for [" + requestContext.getMethod() + "] "
                + requestContext.getUriInfo().getRequestUri());
    }
}
