package de.unipassau.fim.projekt40.service_layer;

import de.unipassau.fim.projekt40.data_access_layer.data_access_object.Event;
import de.unipassau.fim.projekt40.data_access_layer.repository.EventRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    private EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getLast20InFuture() {
        return getLastN(eventRepository.findAllInFuture(), 20);
    }

    public List<Event> getLastN(int n) {
        return getLastN(eventRepository.findAll(), n);
    }

    public List<Event> getEventsBySearch(String entry) {
        List<Event> searchedEvents = new ArrayList<>();
        for (Event Event : eventRepository.findAll()) {
            if (Event.getVer_name().toLowerCase().contains(entry.toLowerCase()) ||
                    Event.getPlace().toLowerCase().contains(entry.toLowerCase())) {
                searchedEvents.add(Event);
            }
        }
        return searchedEvents;
    }

    public List<Event> getLast20FilteredByEventType(String sort) {
        if (sort.equals("Alle (auch Vergangenheit)")) {
            return getLastN(eventRepository.findAll(), 20);
        } else {
            List<Event> events = new ArrayList<>();
            for (Event event: getLastN(eventRepository.findAll(), 20)) {
                if (event.getEventType().equals(sort)) {
                    events.add(event);
                }
            }
            return events;
        }
    }

    public Event getEventById(long id) {
        return eventRepository.findById(id);
    }

    public List<Event> getTop3() {
        if (eventRepository.findAllSort().size() <= 3) {
            return eventRepository.findAllSort();
        }
        return eventRepository.findAllSort().subList(0, 3);
    }

    public void vote(long id, int value) {
        eventRepository.vote(id, value);
    }

    public boolean isEventNameAlreadyUsed(String name) {
        return (eventRepository.findByName(name) != null);
    }

    public void insert(String name, String place, String datum, String description, String eventType) {
        eventRepository.insert(new Event(name, place, datum, description, eventType));
    }

    private List<Event> getLastN(List<Event> events, int n) {
        while (events.size() > n) {
            events.remove(0);
        }
        return events;
    }
}
