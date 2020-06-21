package de.unipassau.fim.projekt40.weblayer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Main {

    @GetMapping()
    public String showAll() {
        return "index";
    }

    @GetMapping("add")
    public String addEvent() {
        return "add";
    }
}
