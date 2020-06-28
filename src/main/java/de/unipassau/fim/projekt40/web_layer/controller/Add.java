package de.unipassau.fim.projekt40.web_layer.controller;

import de.unipassau.fim.projekt40.data_access_layer.repository.EventRepository;
import de.unipassau.fim.projekt40.service_layer.EventService;
import de.unipassau.fim.projekt40.service_layer.EventTypeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for adding a event per Post Request.
 */
@Controller
class Add {

    private EventService eventService;
    private EventTypeService eventTypeService;

    @Autowired
    public Add(EventService eventService, EventTypeService eventTypeService) {
        this.eventService = eventService;
        this.eventTypeService = eventTypeService;
    }

    /**
     * Adds the event if all parameters are valid.
     *
     * @param name name of the Event.
     * @param place place of the Event.
     * @param description description of the Event.
     * @param eventType eventType of the Event.
     * @param datum datum of the Event.
     */
    @PostMapping("addVer")
    @ResponseBody
    public String getQuery (@RequestParam String name, @RequestParam String place, @RequestParam String description,
                            @RequestParam String eventType, @RequestParam String datum) {
        String checkNothingIsEmpty = checkNothingIsEmpty(name, place, description, eventType, datum);
        if (checkNothingIsEmpty == null) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            place = place.substring(0, 1).toUpperCase() + place.substring(1);
            description = description.substring(0, 1).toUpperCase() + description.substring(1);

            String checkOfDatum = checkDatum(datum);
            String checkOfName = checkName(name);
            String checkOfEventType = checkEventType(eventType);

            if (checkOfDatum != null) {
                return checkOfDatum;
            } else if (checkOfName != null) {
                return checkOfName;
            } else if (checkOfEventType != null) {
                return checkOfEventType;
            } else {
                return insert(name, place, datum, description, eventType);
            }
        } else {
            return checkNothingIsEmpty;
        }
    }

    /**
     * Inserts the event. All parameters should be checked before.
     *
     * @param name name of the Event.
     * @param place place of the Event.
     * @param description description of the Event.
     * @param eventType eventType of the Event.
     * @param datum datum of the Event.
     */
    private String insert(String name, String place, String datum, String description, String eventType) {
        eventService.insert(name, place, datum, description, eventType);
        return "<script>\n" +
                " window.setTimeout(\"location.href='/';\", 0);\n" +
                "</script>" +
                "<div>\n" +
                "    <a href=\"http://localhost:8080\">Startseite</a> <br><br><br>\n" +
                "</div>" +
                "Die Veranstaltung wurde erfolgreich hinzugefügt";
    }

    /**
     * Checks if the date is valid and in the future.
     *
     * @param datum datum of the event.
     * @return Alert that is shown.
     */
    private String checkDatum(String datum) {
        try {
            if (!EventRepository.checkDateIsInFuture(datum)) {
                return  "\"<script LANGUAGE='JavaScript'>\n" +
                        "    window.alert('Das Datum liegt in der Vergangenheit');\n" +
                        "    window.location.href='/add';\n" +
                        "    </script>\"" +
                        "Das Datum liegt in der Vergangenheit";
            }
        } catch (IllegalArgumentException e) {
            return  "\"<script LANGUAGE='JavaScript'>\n" +
                    "    window.alert('Das Datum ist illegal!');\n" +
                    "    window.location.href='/add';\n" +
                    "    </script>\"" +
                    "Das Datum ist illegal!";
        }
        return null;
    }

    /**
     * Checks if the name is already used.
     *
     * @param name name of the event.
     * @return Alert that is shown.
     */
    private String checkName(String name) {
        if (eventService.isEventNameAlreadyUsed(name)) {
            return  "\"<script LANGUAGE='JavaScript'>\n" +
                    "    window.alert('Name schon vorhanden');\n" +
                    "    window.location.href='/add';\n" +
                    "    </script>\"" +
                    "Der Name ist leider schon vergeben";
        }
        return null;
    }

    /**
     * Checks if the eventType exists.
     *
     * @param eventTypeName eventType of the event.
     * @return Alert that is shown.
     */
    private String checkEventType(String eventTypeName) {
        if (!eventTypeService.eventTypeExists(eventTypeName)) {
            return "\"<script LANGUAGE='JavaScript'>\n" +
                    "    window.alert('Eventtyp ist nicht vorhanden!');\n" +
                    "    window.location.href='/add';\n" +
                    "    </script>\"" +
                    "Eventtyp ist nicht vorhanden!";
        }
        return null;
    }

    /**
     * Checks if all parameters are not empty.
     *
     * @param name name of the Event.
     * @param place place of the Event.
     * @param description description of the Event.
     * @param eventType eventType of the Event.
     * @param datum datum of the Event.
     * @return Alert that is shown.
     */
    private String checkNothingIsEmpty(String name, String place, String description, String eventType, String datum) {
        if (name.isEmpty() || place.isEmpty() || description.isEmpty()
                || eventType.isEmpty() || datum.isEmpty()) {
            return "\"<script LANGUAGE='JavaScript'>\n" +
                    "    window.alert('Manche Felder sind leer!');\n" +
                    "    window.location.href='/add';\n" +
                    "    </script>\"" +
                    "Manche Felder sind leer!";
        }
        return null;
    }
}
