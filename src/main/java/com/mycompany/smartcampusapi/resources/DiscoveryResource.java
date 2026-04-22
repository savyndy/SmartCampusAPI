package com.mycompany.mavenproject1.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

// Mapped to the root path. Since the ApplicationPath is /api/v1, this responds to GET /api/v1
@Path("/")
public class DiscoveryResource {

    // Returns a JSON object providing essential API metadata 
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscoveryMetadata() {
        
        // Create the main metadata map for versioning and contact info [cite: 109]
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", "v1");
        metadata.put("contact", "admin@smartcampus.westminster.ac.uk");
        
        // Create the resource map for primary collections [cite: 109]
        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        
        metadata.put("resources", resources);
        
        // Return a 200 OK response with the JSON body
        return Response.ok(metadata).build();
    }
}