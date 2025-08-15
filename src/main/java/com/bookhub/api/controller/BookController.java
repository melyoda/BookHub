package com.bookhub.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @GetMapping
    public String getBooks() {
        return "You have successfully accessed a secured endpoint! Welcome to the book list.";
    }
}