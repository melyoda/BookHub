package com.bookhub.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class HelloWorldController {

    @GetMapping("user")
    public String sayHello(HttpServletRequest request) {
        return "hello world: session id " + request.getSession().getId();
    }
}
