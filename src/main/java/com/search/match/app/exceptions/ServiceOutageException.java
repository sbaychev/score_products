package com.search.match.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ServiceOutageException extends ResponseStatusException {

    public ServiceOutageException(String message){
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

//    @Override
//    public HttpHeaders getResponseHeaders() {
//    }
}
