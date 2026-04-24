package com.mycompany.smartcampusapi.data;

import com.mycompany.smartcampusapi.models.Room;
import com.mycompany.smartcampusapi.models.Sensor;
import com.mycompany.smartcampusapi.models.SensorReading;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseClass {
    
    // The three in-memory databases
    private static Map<String, Room> rooms = new HashMap<>();
    private static Map<String, Sensor> sensors = new HashMap<>();
    private static Map<String, List<SensorReading>> sensorReadings = new HashMap<>();

    // The getters so your other classes can access them
    public static Map<String, Room> getRooms() { 
        return rooms; 
    }
    
    public static Map<String, Sensor> getSensors() { 
        return sensors; 
    }
    
    // THIS is the method line 19 was looking for!
    public static Map<String, List<SensorReading>> getSensorReadings() { 
        return sensorReadings; 
    }
}