package de.unipassau.fim.projekt40.web_layer.model;

import java.util.Objects;

public class EventTypeDto {

    private String name;

    public EventTypeDto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventTypeDto that = (EventTypeDto) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
