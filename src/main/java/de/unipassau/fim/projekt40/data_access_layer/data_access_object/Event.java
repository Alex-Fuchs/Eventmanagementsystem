package de.unipassau.fim.projekt40.data_access_layer.data_access_object;

import java.util.Objects;

public class Event {

    private Long id;
    private String ver_name;
    private String place;
    private String datum;
    private String description;
    private String eventType;
    private String weather = "unbekannt";
    private String rank = "0";

    public Event(Long id, String ver_name, String place, String datum, String description, String eventType, String weather, String rank) {
        this.id = id;
        this.ver_name = ver_name;
        this.place = place;
        this.datum = datum;
        this.description = description;
        this.eventType = eventType;
        this.weather = weather;
        this.rank = rank;
    }

    public Event(String ver_name, String place, String datum, String description, String eventType) {
        this.ver_name = ver_name;
        this.place = place;
        this.datum = datum;
        this.description = description;
        this.eventType = eventType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVer_name() {
        return ver_name;
    }

    public void setVer_name(String ver_name) {
        this.ver_name = ver_name;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public int getRankInt() {
        return Integer.parseInt(getRank());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id) &&
                Objects.equals(ver_name, event.ver_name) &&
                Objects.equals(place, event.place) &&
                Objects.equals(datum, event.datum) &&
                Objects.equals(description, event.description) &&
                Objects.equals(eventType, event.eventType) &&
                Objects.equals(weather, event.weather) &&
                Objects.equals(rank, event.rank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ver_name, place, datum, description, eventType, weather, rank);
    }
}