package com.qmatic.apigw;

import java.nio.charset.Charset;

public class GatewayConstants {

    private GatewayConstants() {}

    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    public static final String AUTH_TOKEN = "auth-token";
    public static final String RESPONSE_STATUS_CODE = "responseStatusCode";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";

}
