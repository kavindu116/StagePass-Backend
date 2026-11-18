package com.kavindu.stagepass.common.exception;

import java.util.Objects;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message, Object key) {
        super(message + " " + key +" Was Not Found");
    }
}
