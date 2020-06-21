package de.unipassau.fim.projekt40.weblayer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

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

    @GetMapping("event")
    public String showEvent() {
        return "event";
    }
}
