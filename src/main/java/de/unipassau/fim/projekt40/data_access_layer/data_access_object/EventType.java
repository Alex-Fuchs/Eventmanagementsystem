package de.unipassau.fim.projekt40.data_access_layer.data_access_object;

import java.util.Objects;

public class EventType {

    private String name;

    public EventType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventType eventType = (EventType) o;
        return Objects.equals(name, eventType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

