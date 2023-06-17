package ru.practicum.exceptions;

public class BadRequestException extends IllegalArgumentException {
    public BadRequestException(final String message) {
        super(message);
    }
}