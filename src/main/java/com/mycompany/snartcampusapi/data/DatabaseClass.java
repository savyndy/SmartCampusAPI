package com.mycompany.snartcampusapi.data;

import com.mycompany.snartcampusapi.models.Room;
import com.mycompany.snartcampusapi.models.Sensor;
import com.mycompany.snartcampusapi.models.SensorReading;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DatabaseClass {
    private static Map<String, Room> rooms = new ConcurrentHashMap<>();
    private static Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    
    // Maps a Sensor ID to a thread-safe list of its readings
    private static Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    public static Map<String, Room> getRooms() { return rooms; }
    public static Map<String, Sensor> getSensors() { return sensors; }
    
    public static Map<String, List<SensorReading>> getSensorReadings() {
        return sensorReadings;
    }
}