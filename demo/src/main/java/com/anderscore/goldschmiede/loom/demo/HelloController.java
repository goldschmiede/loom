package com.anderscore.goldschmiede.loom.demo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class HelloController {

    @GetMapping(path = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    public String hello(String name) {
        delay();
        return "Hello " + (name == null ? "World" : name) + "!";
    }

    void delay() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ex) {
            throw new IllegalStateException("interrupted", ex);
        }
    }

}
