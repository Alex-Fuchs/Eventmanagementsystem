package de.unipassau.fim.projekt40.weblayer.controller;

import de.unipassau.fim.projekt40.data_access_layer.data_access_object.Event;
import de.unipassau.fim.projekt40.data_access_layer.data_access_object.EventType;
import de.unipassau.fim.projekt40.data_access_layer.repository.EventRepository;
import de.unipassau.fim.projekt40.data_access_layer.repository.EventTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.model.IModel;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class Main {

    private EventRepository eventRepository;
    private EventTypeRepository eventTypeRepository;

    @Autowired
    public Main(EventRepository eventRepository, EventTypeRepository eventTypeRepository) {
        this.eventRepository = eventRepository;
        this.eventTypeRepository = eventTypeRepository;
    }

    @GetMapping()
    public String showAll(Model model) {
        setEventTypes(model);
        return "index";
    }

    @GetMapping("add")
    public String addEvent(Model model) {
        setEventTypes(model);
        return "add";
    }

    @GetMapping("event")
    public String showEvent(Model model) {
        setEventTypes(model);
        return "event";
    }

    private void setEventTypes(Model model) {
        List<EventType> eventTypes = eventTypeRepository.findAll();
        eventTypes.add(0, new EventType("Alle (auch Vergangenheit)"));
        model.addAttribute("eventTypes", eventTypes);
    }

    private List<Event> getTop3() {
        if (eventRepository.findAllSort().size() <= 3) {
            return eventRepository.findAllSort();
        }
        return eventRepository.findAllSort().subList(0, 3);
    }

    private void getLast20(List<Event> events) {
        while (events.size() > 20) {
            events.remove(0);
        }
    }
}
