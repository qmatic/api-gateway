package com.qmatic.apigw.connector;

import com.qmatic.apigw.exception.CentralCommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public abstract class GatewayService {

    private static final Logger log = LoggerFactory.getLogger(GatewayService.class);

    /**
     * Handles central communication errors, thrown when RestTemplate is used.
     */
    @ExceptionHandler(CentralCommunicationException.class)
    public ResponseEntity<CentralCommunicationException> rulesForCentralException(HttpServletRequest request, Exception e) {
        CentralCommunicationException exception = (CentralCommunicationException) e;
        HttpHeaders headers = new HttpHeaders();
        headers.put(CentralCommunicationException.CENTRAL_ERROR_CODE_HEADER, Arrays.asList(new String[] {exception.getErrorCode()}));
        headers.put(CentralCommunicationException.CENTRAL_ERROR_MESSAGE_HEADER, Arrays.asList(new String[] { exception.getMessage()}));
        log.debug("Service received unhandled central communication exception. Message: {}", e.getMessage(), e);
        return new ResponseEntity<>(null, headers, HttpStatus.valueOf(exception.getHttpStatusCode()));
    }

    /**
     * Handle all other errors that crop up. We handle these just like central exceptions by adding headers
     * for the error code and message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Exception> rulesForQException(HttpServletRequest request, Exception e) {
        HttpHeaders headers = new HttpHeaders();
        headers.put(CentralCommunicationException.CENTRAL_ERROR_CODE_HEADER, Arrays.asList(new String[] {"1000"}));
        headers.put(CentralCommunicationException.CENTRAL_ERROR_MESSAGE_HEADER, Arrays.asList(new String[] { e.getMessage()}));
        log.debug("Service received unhandled exception. Message: {}", e.getMessage(), e);
        return new ResponseEntity<>(null, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
