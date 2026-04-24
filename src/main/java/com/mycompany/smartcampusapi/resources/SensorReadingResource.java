package com.mycompany.smartcampusapi.resources;

import com.mycompany.smartcampusapi.data.DatabaseClass;
import com.mycompany.smartcampusapi.models.Sensor;
import com.mycompany.smartcampusapi.models.SensorReading;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private Map<String, List<SensorReading>> readingsMap = DatabaseClass.getSensorReadings();
    private Map<String, Sensor> sensors = DatabaseClass.getSensors();
    private String sensorId;

    public SensorReadingResource() {
    }

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public List<SensorReading> getReadings() {
        List<SensorReading> history = readingsMap.get(sensorId);
        return (history == null) ? new ArrayList<>() : history;
    }

    @POST
    public Response addReading(SensorReading reading) {
        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // 1. Fetch the parent sensor early
        Sensor parentSensor = sensors.get(sensorId);

        // 2. THE BOUNCER: Check if it's in MAINTENANCE mode
        if (parentSensor != null && "MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus())) {
            return Response.status(Response.Status.FORBIDDEN)
                           .entity("{\"error\": \"Sensor is currently in MAINTENANCE mode.\"}")
                           .build();
        }

        // 3. If it's not in maintenance, save the reading normally
        readingsMap.putIfAbsent(sensorId, new CopyOnWriteArrayList<>());
        readingsMap.get(sensorId).add(reading);

        // 4. Trigger the side-effect (update the sensor's current value)
        if (parentSensor != null) {
            parentSensor.setCurrentValue(reading.getValue());
        }

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}