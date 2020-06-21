package de.unipassau.fim.projekt40.weblayer.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class Main {

    @GetMapping()
    public String showAll() {
        return "index";
    }
}
