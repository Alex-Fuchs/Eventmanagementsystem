package de.unipassau.fim.projekt40.service_layer;

import de.unipassau.fim.projekt40.data_access_layer.data_access_object.Event;
import de.unipassau.fim.projekt40.data_access_layer.repository.EventRepository;
import de.unipassau.fim.projekt40.web_layer.model.EventDto;

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

    public List<EventDto> getLastN(int n) {
        return convertToDtos(getLastN(eventRepository.findAll(), n));
    }

    public List<EventDto> getLastNInFuture(int n) {
        return convertToDtos(getLastN(eventRepository.findAllInFuture(), n));
    }

    public List<EventDto> getLastNBySearch(String entry, int n) {
        List<Event> searchedEvents = new ArrayList<>();
        for (Event Event : eventRepository.findAll()) {
            if (Event.getVer_name().toLowerCase().contains(entry.toLowerCase()) ||
                    Event.getPlace().toLowerCase().contains(entry.toLowerCase())) {
                searchedEvents.add(Event);
            }
        }
        return convertToDtos(getLastN(searchedEvents, n));
    }

    public List<EventDto> getLastNByEventType(String sort, int n) {
        if (sort.equals("Alle mit Vergangenheit")) {
            return getLastN(n);
        } else {
            return convertToDtos(getLastN(eventRepository.findByEventType(sort), n));
        }
    }

    public List<EventDto> getEventById(long id) {
        Event event = eventRepository.findById(id);
        List<Event> events = new ArrayList<>();
        events.add(event);
        return convertToDtos(events);
    }

    public List<EventDto> getTop3() {
        if (eventRepository.findAllSort().size() <= 3) {
            return convertToDtos(eventRepository.findAllSort());
        }
        return convertToDtos(eventRepository.findAllSort().subList(0, 3));
    }

    public List<Long> getTop3IDs() {
        List<Long> result = new ArrayList<>();
        for (EventDto event: getTop3()) {
            result.add(event.getId());
        }
        return result;
    }

    public List<Long> getInFutureIDs() {
        List<Long> result = new ArrayList<>();
        for (Event event: eventRepository.findAllInFuture()) {
            result.add(event.getId());
        }
        return result;
    }

    public void vote(long id, int value) {
        eventRepository.vote(id, value);
    }

    public void insert(String name, String place, String datum, String description, String eventType) {
        eventRepository.insert(new Event(name, place, datum, description, eventType));
    }

    public boolean isEventNameAlreadyUsed(String name) {
        return (eventRepository.findByName(name) != null);
    }

    private List<Event> getLastN(List<Event> events, int n) {
        while (events.size() > n) {
            events.remove(0);
        }
        return events;
    }

    private List<EventDto> convertToDtos(List<Event> events) {
        List<EventDto> eventDtos = new ArrayList<>();
        for (Event event: events) {
            eventDtos.add(new EventDto(event.getId(), event.getVer_name(), event.getPlace(),
                    event.getDatum(), event.getDescription(), event.getEventType(),
                    event.getWeather(), event.getRank()));
        }
        return eventDtos;
    }
}
