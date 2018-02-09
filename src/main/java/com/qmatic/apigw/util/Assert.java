package com.qmatic.apigw.util;

import org.apache.commons.lang3.StringUtils;

public class Assert extends org.springframework.util.Assert {

    public static void notBlank(String val, String message) {
        isTrue(StringUtils.isNotBlank(val), message);
    }
    
    public static void isFalse(boolean val, String message) {
        isTrue(!val, message);
    }
}