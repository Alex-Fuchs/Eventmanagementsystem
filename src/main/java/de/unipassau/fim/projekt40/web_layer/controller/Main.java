package de.unipassau.fim.projekt40.web_layer.controller;

import de.unipassau.fim.projekt40.service_layer.EventService;
import de.unipassau.fim.projekt40.service_layer.EventTypeService;
import de.unipassau.fim.projekt40.web_layer.model.EventDto;

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

    private EventService eventService;
    private EventTypeService eventTypeService;

    @Autowired
    public Main(EventService eventService, EventTypeService eventTypeService) {
        this.eventService = eventService;
        this.eventTypeService = eventTypeService;
    }

    @GetMapping()
    public String showAll(HttpServletRequest request, Model model,
                          @RequestParam(required = false, defaultValue = "20") String size) {
        doPreparations(request, eventService.getLastNInFuture(Integer.parseInt(size)), model);
        return "index";
    }

    @GetMapping("add")
    public String addEvent(Model model) {
        model.addAttribute("eventTypes", eventTypeService.getEventTypes());
        return "add";
    }

    @GetMapping("sort")
    public String ShowAllWithEventType(HttpServletRequest request, Model model, @RequestParam String sort,
                                       @RequestParam(required = false, defaultValue = "20") String size) {
        doPreparations(request, eventService.getLastNByEventType(sort, Integer.parseInt(size)), model);
        return "index";
    }

    @GetMapping("search")
    public String showAllWithSearch(HttpServletRequest request, Model model, @RequestParam String entry,
                                    @RequestParam(required = false, defaultValue = "20") String size) {
        doPreparations(request, eventService.getLastNBySearch(entry, Integer.parseInt(size)), model);
        return "index";
    }

    @PostMapping("vote")
    @ResponseBody
    public String vote (HttpServletRequest request, HttpServletResponse response,
                        @RequestParam String id, @RequestParam String ranking) {
        long idLong = Long.parseLong(id);
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
            eventService.vote(idLong, value);
            return "\"<script LANGUAGE='JavaScript'>\n" +
                    "    window.alert('Vielen Dank für deinen Vote');\n" +
                    "    window.location.href='/';\n" +
                    "    </script>\"" +
                    "Vielen Dank für deinen Vote";
        }
        return "\"<script LANGUAGE='JavaScript'>\n" +
                "    window.alert('Etwas ist mit dem Vote schief gelaufen!');\n" +
                "    window.location.href='/';\n" +
                "    </script>\"" +
                "Etwas ist mit dem Vote schief gelaufen!";
    }

    @GetMapping("event")
    public String showEvent(HttpServletRequest request, Model model, @RequestParam String id) {
        doPreparations(request, eventService.getEventById(Integer.parseInt(id)), model);
        return "event";
    }

    private void doPreparations(HttpServletRequest request, List<EventDto> events, Model model) {
        ArrayList<Long> upVote = new ArrayList<>();
        ArrayList<Long> downVote = new ArrayList<>();
        setUpDownVoteLists(request, upVote, downVote);

        model.addAttribute("eventTypes", eventTypeService.getEventTypesWithAll());
        model.addAttribute("upVote", upVote);
        model.addAttribute("downVote", downVote);
        model.addAttribute("top3", eventService.getTop3());
        model.addAttribute("top3IDs", getTop3IDs());
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

    private List<Long> getTop3IDs() {
        List<Long> result = new ArrayList<>();
        for (EventDto event: eventService.getTop3()) {
            result.add(event.getId());
        }
        return result;
    }
}
