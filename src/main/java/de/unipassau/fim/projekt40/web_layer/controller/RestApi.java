package de.unipassau.fim.projekt40.web_layer.controller;

import com.google.gson.Gson;

import de.unipassau.fim.projekt40.service_layer.EventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RestApi {

    private EventService eventService;

    @Autowired
    public RestApi(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("events")
    @ResponseBody
    public String json(@RequestParam String sizeString) {
        return new Gson().toJson(eventService.getLastN(Integer.parseInt(sizeString)));
    }
}
