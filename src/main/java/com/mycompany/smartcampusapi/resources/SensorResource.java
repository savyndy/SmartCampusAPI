package com.mycompany.smartcampusapi.resources;

import com.mycompany.smartcampusapi.data.DatabaseClass;
import com.mycompany.smartcampusapi.models.Room;
import com.mycompany.smartcampusapi.models.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private Map<String, Sensor> sensors = DatabaseClass.getSensors();
    private Map<String, Room> rooms = DatabaseClass.getRooms();

    // GET /api/v1/sensors?type=... : Filtered Retrieval & Search [cite: 133, 134]
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(sensors.values());

        // If the 'type' query parameter is provided, filter the list [cite: 135]
        if (type != null && !type.trim().isEmpty()) {
            sensorList = sensorList.stream()
                    .filter(s -> type.equalsIgnoreCase(s.getType()))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensorList).build();
    }

    // POST /api/v1/sensors: Register a new sensor 
    @POST
    public Response addSensor(Sensor sensor, @Context UriInfo uriInfo) {
        
        // Validation: Verify that the roomId specified actually exists in the system 
        if (sensor.getRoomId() == null || !rooms.containsKey(sensor.getRoomId())) {
            throw new com.mycompany.smartcampusapi.exceptions.LinkedResourceNotFoundException("Linked Room ID does not exist");
}

        sensors.put(sensor.getId(), sensor);

        // Link the sensor to the room's internal list for data integrity
        Room parentRoom = rooms.get(sensor.getRoomId());
        parentRoom.getSensorIds().add(sensor.getId());

        URI uri = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(uri).entity(sensor).build();
    }
    
    
    // GET /api/v1/sensors/{sensorId}: Fetch specific sensor
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\":\"Sensor not found\"}")
                           .build();
        }
        return Response.ok(sensor).build();
    }
    
    // SUB-RESOURCE LOCATOR
    // Notice there is NO HTTP method annotation (@GET, @POST, etc.) here!
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        // Verify the parent sensor exists before delegating
        if (!sensors.containsKey(sensorId)) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found\"}")
                    .build());
        }
        // Delegate responsibility to the sub-resource class
        return new SensorReadingResource(sensorId);
    }
}