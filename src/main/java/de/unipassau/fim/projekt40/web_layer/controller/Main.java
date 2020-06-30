package de.unipassau.fim.projekt40.web_layer.controller;

import de.unipassau.fim.projekt40.service_layer.EventService;
import de.unipassau.fim.projekt40.service_layer.EventTypeService;
import de.unipassau.fim.projekt40.web_layer.model.EventDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * The main controller of the web application with all but adding a event.
 */
@Controller
class Main {

    private EventService eventService;
    private EventTypeService eventTypeService;

    @Autowired
    public Main(EventService eventService, EventTypeService eventTypeService) {
        this.eventService = eventService;
        this.eventTypeService = eventTypeService;
    }

    /**
     * Shows the standard page with the last 20 events as default.
     * Also the max. number of Events can be changed through {@code size}.
     *
     * @param size Max. number of shown events.
     */
    @GetMapping()
    public String showAll(HttpServletRequest request, Model model,
                          @RequestParam(required = false, defaultValue = "20") String size) {
        doPreparations(request, eventService.getLastNInFuture(parseStringToInt(size)), model);
        return "index";
    }

    /**
     * Shows the add page to add a event.
     */
    @GetMapping("add")
    public String addEvent(Model model) {
        model.addAttribute("eventTypes", eventTypeService.getEventTypes());
        return "add";
    }

    /**
     * Shows the standard page with the last 20 events by eventType as default.
     * {@code sort} is the EventType of the events. Also the max. number of
     * Events can be changed through {@code size}.
     *
     * @param sort String of the EventType of the events that are shown. If
     *             it doesn't match a eventType, no events are shown.
     * @param size Max. number of shown events.
     */
    @GetMapping("sort")
    public String ShowAllWithEventType(HttpServletRequest request, Model model, @RequestParam String sort,
                                       @RequestParam(required = false, defaultValue = "20") String size) {
        doPreparations(request, eventService.getLastNByEventType(sort, parseStringToInt(size)), model);
        return "index";
    }

    /**
     * Shows the standard page with the last 20 events by search as default.
     * If the name or the place of the event contains {@code entry}, the event
     * is shown. Also the max. number of Events can be changed through {@code size}.
     *
     * @param entry String of the EventType of the events that are shown. If
     *              it doesn't match a eventType, no events are shown.
     * @param size  Max. number of shown events.
     */
    @GetMapping("search")
    public String showAllWithSearch(HttpServletRequest request, Model model, @RequestParam String entry,
                                    @RequestParam(required = false, defaultValue = "20") String size) {
        doPreparations(request, eventService.getLastNBySearch(entry, parseStringToInt(size)), model);
        return "index";
    }

    /**
     * Post Request for Voting. {@code ranking} contains +1 for positive, -1
     * for negative vote. The old Voting is read through Cookies with
     * {@link #getOldVoting(HttpServletRequest, String)}. After that both
     * are compared and then the old voting is cancelled and maybe the
     * opposite is voted. After that the new voting is set as a Cookie and
     * a alert is shown.
     *
     * @param id        Id of the event to vote for.
     * @param ranking   Can be +1 or -1 otherwise no vote is carried out.
     */
    @PostMapping("vote")
    @ResponseBody
    public String vote (HttpServletRequest request, HttpServletResponse response,
                        @RequestParam String id, @RequestParam String ranking) {
        long idLong = parseStringToLong(id);
        int value;
        try {
            value = Integer.parseInt(ranking);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

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
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Shows the page of an event.
     *
     * @param id The id of the event that is shown.
     */
    @GetMapping("event")
    public String showEvent(HttpServletRequest request, Model model, @RequestParam String id) {
        doPreparations(request, eventService.getEventById(parseStringToLong(id)), model);
        return "event";
    }

    /**
     * Sets up the model with all information that is needed in the thymeleaf
     * standard page.
     *
     * @param events Events that are shown in the standard page.
     */
    private void doPreparations(HttpServletRequest request, List<EventDto> events, Model model) {
        ArrayList<Long> upVoteIDs = new ArrayList<>();
        ArrayList<Long> downVoteIDs = new ArrayList<>();
        setUpDownVoteLists(request, upVoteIDs, downVoteIDs);

        model.addAttribute("eventTypes", eventTypeService.getEventTypesWithAll());
        model.addAttribute("top3", eventService.getTop3());
        model.addAttribute("events", events);
        model.addAttribute("upVoteIDs", upVoteIDs);
        model.addAttribute("downVoteIDs", downVoteIDs);
        model.addAttribute("inFutureIDs", eventService.getInFutureIDs(events));
    }

    /**
     * Collects all Events that are upvoted and downvoted by the user through
     * Cookies.
     *
     * @param upVote List with all upvoted Events.
     * @param downVote List with all downvoted Events.
     */
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

    /**
     * Gets the old voting of an event by the user through Cookies.
     *
     * @param id Id of the the event.
     */
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

    private int parseStringToInt(String size) {
        try {
            int result = Integer.parseInt(size);
            if (result < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            return result;
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private long parseStringToLong(String size) {
        try {
            return Long.parseLong(size);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
