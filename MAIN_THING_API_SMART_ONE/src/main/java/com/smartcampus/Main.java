package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class Main {

    public static void main(String[] args) throws Exception {

        ResourceConfig config = new ResourceConfig();
        config.packages("com.smartcampus.resource");
        config.packages("com.smartcampus.exception");
        config.packages("com.smartcampus.filter");
        config.register(JacksonFeature.class);

        String baseUri = "http://0.0.0.0:8080/api/v1/";
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), config);

        System.out.println("=====================================================");
        
        System.out.println("Main Server Started (Http) : http://localhost:8080/api/v1");
        System.out.println("-------------------------------------------------------------");
        System.out.println("Test: http://localhost:8080/api/v1/rooms");
        System.out.println("Test: http://localhost:8080/api/v1/sensors");
        
        System.out.println("========================================================");
        System.out.println("Press ENTER to stop...");
        System.in.read();
        server.shutdown();
    }
}
