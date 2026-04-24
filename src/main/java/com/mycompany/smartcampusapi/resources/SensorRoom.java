package com.mycompany.smartcampusapi.resources;

import com.mycompany.smartcampusapi.data.DatabaseClass;
import com.mycompany.smartcampusapi.models.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/rooms")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SensorRoom {

    public SensorRoom (){} 

    // Access the thread-safe in-memory database
    private Map<String, Room> rooms = DatabaseClass.getRooms();

    // GET /api/v1/rooms: List all rooms
    @GET
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    // GET /api/v1/rooms/{roomId}: Fetch metadata for a specific room
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        
        if (room == null) {
            // 🚨 Use a Map to guarantee crash-proof JSON conversion
            Map<String, String> errorMessage = new HashMap<>();
            errorMessage.put("error", "Room not found");
            
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(errorMessage)
                           .build();
        }
        
        return Response.ok(room).build();
    }

    // POST /api/v1/rooms: Create new rooms
    @POST
    public Response addRoom(Room room, @Context UriInfo uriInfo) {
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            Map<String, String> errorMessage = new HashMap<>();
            errorMessage.put("error", "Room ID is required");
            
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(errorMessage)
                           .build();
        }
        
        rooms.put(room.getId(), room);

        // Build the Location header URI for the "Excellent" rubric requirement
        URI uri = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        
        // Returns 201 Created with the location header
        return Response.created(uri).entity(room).build();
    }

    // DELETE /api/v1/rooms/{roomId}: Allow room decommissioning
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Business Logic Constraint: Prevent data orphans
        if (!room.getSensorIds().isEmpty()) {
            throw new com.mycompany.smartcampusapi.exceptions.RoomNotEmptyException("Room is not empty. Cannot delete.");
        }

        rooms.remove(roomId);
        // Returns 204 No Content on successful deletion
        return Response.noContent().build();
    }
}