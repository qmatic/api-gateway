package com.qmatic.apigw;

import java.nio.charset.Charset;

public class GatewayConstants {

    private GatewayConstants() {}

    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    public static final String AUTH_TOKEN = "auth-token";
    public static final String RESPONSE_STATUS_CODE = "responseStatusCode";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String VISIT_ID = "visitId";
    public static final int FIRST_REQUEST_PARAM_IF_MANY_WITH_EQUAL_NAME = 0;

}
