package de.unipassau.fim.projekt40.web_layer.controller;

import de.unipassau.fim.projekt40.data_access_layer.data_access_object.EventType;
import de.unipassau.fim.projekt40.data_access_layer.repository.EventRepository;
import de.unipassau.fim.projekt40.service_layer.EventService;

import de.unipassau.fim.projekt40.service_layer.EventTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class Add {

    private EventService eventService;
    private EventTypeService eventTypeService;

    @Autowired
    public Add(EventService eventService, EventTypeService eventTypeService) {
        this.eventService = eventService;
        this.eventTypeService = eventTypeService;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "addVer")
    @ResponseBody
    public String getQuery (@RequestParam String name, @RequestParam String place, @RequestParam String description,
                            @RequestParam String eventType, @RequestParam String datum) {
        String checkOfDatum = checkDatum(datum);
        String checkOfName = checkName(name);
        String checkOfEventType = checkEventType(eventType);
        if (checkOfDatum != null) {
            return checkOfDatum;
        } else if (checkOfName != null) {
            return checkOfName;
        } else if (checkOfEventType != null){
            return checkOfEventType;
        } else {
            return insert(name, place, datum, description, eventType);
        }
    }

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

    private String checkEventType(String eventTypeName) {
        if (eventTypeService.eventTypeExists(eventTypeName)) {
            return "\"<script LANGUAGE='JavaScript'>\n" +
                    "    window.alert('Eventtyp ist nicht vorhanden!');\n" +
                    "    window.location.href='/add';\n" +
                    "    </script>\"" +
                    "Eventtyp ist nicht vorhanden!";
        }
        return null;
    }

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
}
