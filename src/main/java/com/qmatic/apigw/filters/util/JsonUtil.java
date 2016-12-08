package com.qmatic.apigw.filters.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static String convert(Object object) throws JsonProcessingException {
        return objectMapper.writer().writeValueAsString(object);
    }

    public static <T> T convert(String src, Class<T> clazz) throws IOException {
        return objectMapper.readerFor(clazz).readValue(src);
    }

}
