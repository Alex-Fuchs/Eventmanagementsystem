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
    public String showAll(HttpServletRequest request, Model model) {
        doPreparations(request, eventRepository.findAllSort(), model);
        return "index";
    }

    @GetMapping("add")
    public String addEvent(Model model) {
        setEventTypes(model);
        return "add";
    }

    @GetMapping("event")
    public String showEvent(HttpServletRequest request, Model model, @RequestParam String id) {
        List<Event> events = new ArrayList<>();
        events.add(eventRepository.findById(Integer.parseInt(id)));
        doPreparations(request, events, model);
        return "event";
    }

    @GetMapping("search")
    public String showAllWithSearch(HttpServletRequest request, Model model, @RequestParam String entry) {
        List<Event> searchedEvents = new ArrayList<>();
        for (Event Event : eventRepository.findAllSort()) {
            if (Event.getVer_name().toLowerCase().contains(entry.toLowerCase()) ||
                    Event.getPlace().toLowerCase().contains(entry.toLowerCase())) {
                searchedEvents.add(Event);
            }
        }
        doPreparations(request, searchedEvents, model);
        return "index";
    }

    @GetMapping("sort")
    public String ShowAllWithEventType(HttpServletRequest request, Model model, @RequestParam String sort) {
        List<Event> events;
        if (sort.equals("Alle (auch Vergangenheit)")) {
            events = eventRepository.findAllSort();
        } else {
            events = eventRepository.findByEventType(sort);
        }
        doPreparations(request, events, model);
        return "index";
    }

    private void setEventTypes(Model model) {
        List<EventType> eventTypes = eventTypeRepository.findAll();
        eventTypes.add(0, new EventType("Alle (auch Vergangenheit)"));
        model.addAttribute("eventTypes", eventTypes);
    }

    private void doPreparations(HttpServletRequest request, List<Event> events, Model model) {
        getLast20(events);
        setEventTypes(model);

        model.addAttribute("top3", getTop3());
        model.addAttribute("events", events);
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
