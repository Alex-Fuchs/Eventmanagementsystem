package de.unipassau.fim.projekt40.data_access_layer.data_access_object;

import javax.persistence.Entity;

@Entity
public class EventType {

    private String name;

    public EventType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

