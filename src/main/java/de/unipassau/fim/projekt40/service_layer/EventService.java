package de.unipassau.fim.projekt40.service_layer;

import de.unipassau.fim.projekt40.data_access_layer.data_access_object.Event;
import de.unipassau.fim.projekt40.data_access_layer.repository.EventRepository;
import de.unipassau.fim.projekt40.web_layer.model.EventDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for the Event class that returns the content of the database.
 */
@Service
public class EventService {

    private EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Gets the last added n events.
     *
     * @param n number of Events.
     * @return the list of Events as DTOs.
     */
    public List<EventDto> getLastN(int n) {
        return convertToDtos(getLastN(eventRepository.findAll(), n));
    }

    /**
     * Gets the last added n events in the future.
     *
     * @param n number of Events.
     * @return the list of Events as DTOs.
     */
    public List<EventDto> getLastNInFuture(int n) {
        return convertToDtos(getLastN(eventRepository.findAllInFuture(), n));
    }

    /**
     * Gets the last added n events that match the search in the name or the
     * place.
     *
     * @param entry string that the event should match.
     * @param n number of Events.
     * @return the list of Events as DTOs.
     */
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

    /**
     * Gets the last added n events that match the EventType.
     *
     * @param sort EventType that the event should match.
     * @param n number of Events.
     * @return the list of Events as DTOs.
     */
    public List<EventDto> getLastNByEventType(String sort, int n) {
        if (sort.equals("Alle mit Vergangenheit")) {
            return getLastN(n);
        } else {
            return convertToDtos(getLastN(eventRepository.findByEventType(sort), n));
        }
    }

    /**
     * Gets the event by Id and returns it in a list.
     * If no event matches the id the list returned is empty.
     *
     * @param id id of the event.
     * @return List with that event that matches the id as DTO.
     */
    public List<EventDto> getEventById(long id) {
        Event event = eventRepository.findById(id);
        List<Event> events = new ArrayList<>();
        events.add(event);
        return convertToDtos(events);
    }

    /**
     * Gets the top 3 events of all time.
     *
     * @return List with the events as DTOs.
     */
    public List<EventDto> getTop3() {
        if (eventRepository.findAllSort().size() <= 3) {
            return convertToDtos(eventRepository.findAllSort());
        }
        return convertToDtos(eventRepository.findAllSort().subList(0, 3));
    }

    /**
     * Gets all ids of the events of {@code eventDtos} that are in the future.
     *
     * @param eventDtos events that are filtered to be in the future.
     * @return list of the ids.
     */
    public List<Long> getInFutureIDs(List<EventDto> eventDtos) {
        List<Long> result = new ArrayList<>();
        for (EventDto eventDto: eventDtos) {
            if (EventRepository.checkDateIsInFuture(eventDto.getDatum())) {
                result.add(eventDto.getId());
            }
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

    /**
     * Gets the last n events of {@code events}.
     */
    private List<Event> getLastN(List<Event> events, int n) {
        while (events.size() > n) {
            events.remove(0);
        }
        return events;
    }

    /**
     * Converts a list of events to a list with DTOs.
     *
     * @param events list that should be converted.
     * @return list with the DTOs.
     */
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
