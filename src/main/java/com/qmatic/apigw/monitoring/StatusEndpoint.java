package com.qmatic.apigw.monitoring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusEndpoint {
    @GetMapping("/status")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "UP";
    }
}