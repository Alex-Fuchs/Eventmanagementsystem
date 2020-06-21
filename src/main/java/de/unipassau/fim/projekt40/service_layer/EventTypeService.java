package de.unipassau.fim.projekt40.service_layer;

import de.unipassau.fim.projekt40.data_access_layer.data_access_object.EventType;

import de.unipassau.fim.projekt40.data_access_layer.repository.EventTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventTypeService {

    private EventTypeRepository eventTypeRepository;

    public EventTypeService(EventTypeRepository eventTypeRepository) {
        this.eventTypeRepository = eventTypeRepository;
    }

    public List<EventType> getEventTypes() {
        return eventTypeRepository.findAll();
    }

    public List<EventType> getEventTypesWithAll() {
        List<EventType> eventTypes = eventTypeRepository.findAll();
        eventTypes.add(0, new EventType("Alle (auch Vergangenheit)"));
        return eventTypes;
    }

    public boolean eventTypeExists(String name) {
        return (eventTypeRepository.findByName(name) != null);
    }
}
