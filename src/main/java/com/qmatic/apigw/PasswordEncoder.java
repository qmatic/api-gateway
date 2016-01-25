package com.qmatic.apigw;

import org.apache.commons.codec.binary.Base64;

import java.io.Console;

/**
 * Simple application that encodes a password and prints it to System.out
 */
public class PasswordEncoder {

    private static String encode(String password) {
        return new String(Base64.encodeBase64((password).getBytes(GatewayConstants.UTF8_CHARSET)));
    }

    public static void main(String[] args) {

        Console c = System.console();
        if (c == null) {
            System.err.println("No console.");
            System.exit(1);
        }

        char[] login = c.readPassword("Enter password: ");
        String uuid = encode(String.valueOf(login));
        System.out.println(uuid);

    }
}
