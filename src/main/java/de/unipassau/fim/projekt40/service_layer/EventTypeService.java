package de.unipassau.fim.projekt40.service_layer;

import de.unipassau.fim.projekt40.data_access_layer.data_access_object.EventType;
import de.unipassau.fim.projekt40.web_layer.model.EventTypeDto;
import de.unipassau.fim.projekt40.data_access_layer.repository.EventTypeRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventTypeService {

    private EventTypeRepository eventTypeRepository;

    public EventTypeService(EventTypeRepository eventTypeRepository) {
        this.eventTypeRepository = eventTypeRepository;
    }

    public List<EventTypeDto> getEventTypes() {
        return convertToDtos(eventTypeRepository.findAll());
    }

    public List<EventTypeDto> getEventTypesWithAll() {
        List<EventType> eventTypes = eventTypeRepository.findAll();
        eventTypes.add(0, new EventType("Alle (auch Vergangenheit)"));
        return convertToDtos(eventTypes);
    }

    public boolean eventTypeExists(String name) {
        return (eventTypeRepository.findByName(name) != null);
    }

    private List<EventTypeDto> convertToDtos(List<EventType> eventTypes) {
        List<EventTypeDto> eventTypeDtos = new ArrayList<>();
        for (EventType eventType: eventTypes) {
            eventTypeDtos.add(new EventTypeDto(eventType.getName()));
        }
        return eventTypeDtos;
    }
}
