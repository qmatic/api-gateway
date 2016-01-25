package com.qmatic.apigw;

import java.util.UUID;

/**
 * Simple application that generates an UUID and prints it to System.out
 */
public class ApiTokenGenerator {

    private static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static void main(String[] args) {

        String uuid = generateUUID();
        System.out.println(uuid);

    }
}
