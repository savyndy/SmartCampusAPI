package com.mycompany.smartcampusapi.exceptions;
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) { super(message); }
}