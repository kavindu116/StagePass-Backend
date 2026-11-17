package com.kavindu.stagepass.common.exception;

import java.util.Objects;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message, Objects key) {
        super(message + " " + key +" Was Not Found");
    }
}
