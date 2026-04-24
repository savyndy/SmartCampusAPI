package com.mycompany.smartcampusapi.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

// This maps strictly to the base URL (/api/v1)
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getDiscoveryInfo() {
        // Create the main JSON object
        Map<String, Object> discoveryData = new HashMap<>();
        discoveryData.put("api_version", "1.0");
        discoveryData.put("description", "Smart Campus REST API");
        discoveryData.put("author", "Your Name Here"); // You can put your name!

        // Create the HATEOAS links sub-object
        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");

        // Add the links to the main object
        discoveryData.put("_links", links);

        // Return it with a 200 OK status
        return Response.ok(discoveryData).build();
    }
}