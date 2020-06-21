package de.unipassau.fim.projekt40.weblayer.controller;

import de.unipassau.fim.projekt40.data_access_layer.data_access_object.Event;
import de.unipassau.fim.projekt40.data_access_layer.repository.EventRepository;
import de.unipassau.fim.projekt40.data_access_layer.data_access_object.EventType;
import de.unipassau.fim.projekt40.data_access_layer.repository.EventTypeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;


@Controller
class Main {

    private EventRepository eventRepository;
    private EventTypeRepository eventTypeRepository;

    @Autowired
    public Main(EventRepository eventRepository, EventTypeRepository eventTypeRepository) {
        this.eventRepository = eventRepository;
        this.eventTypeRepository = eventTypeRepository;
    }

    @GetMapping()
    public String showAll(HttpServletRequest request, Model model) {
        List<Event> events = new ArrayList<>();
        for (Event event: eventRepository.findAllSort()) {
            if (EventRepository.checkDateIsInFuture(event.getDatum())) {
                events.add(event);
            }
        }
        doPreparations(request, events, model);
        return "index";
    }

    @GetMapping("add")
    public String addEvent(Model model) {
        model.addAttribute("eventTypes", eventTypeRepository.findAll());
        return "add";
    }

    @PostMapping("sort")
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

    @PostMapping("search")
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

    @PostMapping("vote")
    @ResponseBody
    public String vote (HttpServletRequest request, HttpServletResponse response, @RequestParam String id, @RequestParam String ranking) {
        int value = Integer.parseInt(ranking);
        if (Math.abs(value) == 1) {
            int oldVoting = getOldVoting(request, id);
            if (oldVoting == value) {
                response.addCookie(new Cookie(id, "0"));
                value = -1 * oldVoting;
            } else {
                response.addCookie(new Cookie(id, ranking));
                value += -1 * oldVoting;
            }
            long tmp = Long.parseLong(id);
            eventRepository.vote(tmp, value);
            return "\"<script LANGUAGE='JavaScript'>\n" +
                    "    window.alert('Vielen Dank für deinen Vote');\n" +
                    "    window.location.href='/';\n" +
                    "    </script>\"" +
                    "Vielen Dank für deinen Vote";
        }
        return "\"<script LANGUAGE='JavaScript'>\n" +
                "    window.alert('Etwas ist mit der Session schief gelaufen!');\n" +
                "    window.location.href='/';\n" +
                "    </script>\"" +
                "Etwas ist mit der Session schief gelaufen!";
    }

    @GetMapping("event")
    public String showEvent(HttpServletRequest request, Model model, @RequestParam String id) {
        List<Event> events = new ArrayList<>();
        events.add(eventRepository.findById(Integer.parseInt(id)));
        doPreparations(request, events, model);
        return "event";
    }

    private void doPreparations(HttpServletRequest request, List<Event> events, Model model) {
        getLast20(events);
        setEventTypes(model);
        ArrayList<Long> upVote = new ArrayList<>();
        ArrayList<Long> downVote = new ArrayList<>();
        setUpDownVoteLists(request, upVote, downVote);

        model.addAttribute("upVote", upVote);
        model.addAttribute("downVote", downVote);
        model.addAttribute("top3", getTop3());
        model.addAttribute("events", events);
    }

    private void setUpDownVoteLists(HttpServletRequest request, ArrayList<Long> upVote, ArrayList<Long> downVote) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie: cookies) {
                if (cookie.getValue().equals("1")) {
                    upVote.add(Long.parseLong(cookie.getName()));
                } else if (cookie.getValue().equals("-1")){
                    downVote.add(Long.parseLong(cookie.getName()));
                }
            }
        }
    }

    private void setEventTypes(Model model) {
        List<EventType> eventTypes = eventTypeRepository.findAll();
        eventTypes.add(0, new EventType("Alle (auch Vergangenheit)"));
        model.addAttribute("eventTypes", eventTypes);
    }

    private int getOldVoting(HttpServletRequest request, String id) {
        Cookie[] cookies = request.getCookies();
        Cookie voted = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(id)) {
                    voted = cookie;
                }
            }
        }
        if (voted != null) {
            int votedValue = Integer.parseInt(voted.getValue());
            if (Math.abs(votedValue) == 1) {
                return votedValue;
            }
        }
        return 0;
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
