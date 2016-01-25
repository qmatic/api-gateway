package com.qmatic.apigw.rest;

import com.qmatic.apigw.exception.CentralCommunicationException;
import org.apache.commons.io.IOUtils;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.util.List;

/**
 * Handle errors received from Central when using RestTemplate
 */
public class CentralHttpErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError()
            || response.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        throw new CentralCommunicationException(
            getHeaderValue(CentralCommunicationException.CENTRAL_ERROR_MESSAGE_HEADER, response),
            getHeaderValue(CentralCommunicationException.CENTRAL_ERROR_CODE_HEADER, response),
            response.getStatusCode().value(),
            IOUtils.toString(response.getBody()));
    }

    private String getHeaderValue(String headerKey, ClientHttpResponse response) {
        List<String> headerValues =  response.getHeaders().get(headerKey);
        StringBuilder message = new StringBuilder();
        if (headerValues != null) {
            for (String value : headerValues) {
                message.append(value);
            }
        }
        return message.toString();
    }
}
