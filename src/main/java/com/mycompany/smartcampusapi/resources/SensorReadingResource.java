package com.mycompany.snartcampusapi.resources;

import com.mycompany.snartcampusapi.data.DatabaseClass;
import com.mycompany.snartcampusapi.models.Sensor;
import com.mycompany.snartcampusapi.models.SensorReading;

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

    // Constructor to receive the context from the parent SensorResource
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    @GET
    public List<SensorReading> getReadings() {
        List<SensorReading> history = readingsMap.get(sensorId);
        if (history == null) {
            return new ArrayList<>(); // Return empty list if no readings yet
        }
        return history;
    }

    // POST /api/v1/sensors/{sensorId}/readings
    @POST
    public Response addReading(SensorReading reading) {
        // Ensure the list exists for this sensor
        readingsMap.putIfAbsent(sensorId, new CopyOnWriteArrayList<>());
        readingsMap.get(sensorId).add(reading);

        
        Sensor parentSensor = sensors.get(sensorId);
        if (parentSensor != null) {
            if ("MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus())) {
                throw new com.mycompany.snartcampusapi.exceptions.SensorUnavailableException("Sensor is in maintenance mode and cannot accept readings.");
        }
            parentSensor.setCurrentValue(reading.getValue());
}
        // SIDE EFFECT: Update the currentValue on the parent Sensor object
        Sensor parentSensor = sensors.get(sensorId);
        if (parentSensor != null) {
            parentSensor.setCurrentValue(reading.getValue());
        }

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}