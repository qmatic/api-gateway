package com.qmatic.apigw;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CorsMockController {

    @GetMapping("/foo")
    public String foo() {
        return "foo";
    }

    @GetMapping("/bar")
    public String bar() {
        return "bar";
    }
}