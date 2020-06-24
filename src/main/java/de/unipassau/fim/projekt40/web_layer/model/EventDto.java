package de.unipassau.fim.projekt40.web_layer.model;

public class EventDto {

    private Long id;
    private String ver_name;
    private String place;
    private String datum;
    private String description;
    private String eventType;
    private String weather;
    private String rank;

    public EventDto(Long id, String ver_name, String place, String datum, String description, String eventType, String weather, String rank) {
        this.id = id;
        this.ver_name = ver_name;
        this.place = place;
        this.datum = datum;
        this.description = description;
        this.eventType = eventType;
        this.weather = weather;
        this.rank = rank;
    }

    public Long getId() {
        return id;
    }

    public String getVer_name() {
        return ver_name;
    }

    public String getPlace() {
        return place;
    }

    public String getDatum() {
        return datum;
    }

    public String getDescription() {
        return description;
    }

    public String getEventType() {
        return eventType;
    }

    public String getWeather() {
        return weather;
    }

    public String getRank() {
        return rank;
    }
}