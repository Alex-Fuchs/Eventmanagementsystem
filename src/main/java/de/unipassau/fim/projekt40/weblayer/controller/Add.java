package de.unipassau.fim.projekt40.weblayer.controller;



import de.unipassau.fim.projekt40.data_access_layer.data_access_object.Event;
import de.unipassau.fim.projekt40.data_access_layer.data_access_object.EventType;

import de.unipassau.fim.projekt40.data_access_layer.repository.EventRepository;
import de.unipassau.fim.projekt40.data_access_layer.repository.EventTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class Add {

    private EventRepository eventRepository;
    private EventTypeRepository eventTypeRepository;

    @Autowired
    public Add(EventRepository eventRepository, EventTypeRepository eventTypeRepository) {
        this.eventRepository = eventRepository;
        this.eventTypeRepository = eventTypeRepository;
    }


    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "addVer")
    @ResponseBody
    public String getQuery (@RequestParam String name, @RequestParam String place, @RequestParam String description,
                            @RequestParam String eventType, @RequestParam String datum) {
        String checkOfName = checkName(name);
        String checkOfEventType = checkEventType(eventType);
        if (checkOfName != null) {
            return checkOfName;
        } else if (checkOfEventType != null){
            return checkOfEventType;
        } else {
            return insert(name, place, datum, description, eventType);
        }
    }

    private String checkName(String name) {
        if (!eventRepository.findByName(name).isEmpty()){
            return  "\"<script LANGUAGE='JavaScript'>\n" +
                    "    window.alert('Name schon vorhanden');\n" +
                    "    window.location.href='/add';\n" +
                    "    </script>\"" +
                    "Der Name ist leider schon vergeben";
        }
        return null;
    }

    private String checkEventType(String eventTypeName) {
        for (EventType eventType: eventTypeRepository.findAll()) {
            if (eventType.getName().equals(eventTypeName)) {
                return null;
            }
        }
        return "\"<script LANGUAGE='JavaScript'>\n" +
                "    window.alert('Eventtyp ist nicht vorhanden!');\n" +
                "    window.location.href='/add';\n" +
                "    </script>\"" +
                "Eventtyp ist nicht vorhanden!";
    }

    private String insert(String name, String place, String datum, String description, String eventType) {
        eventRepository.insert(new Event(name, place, datum, description, eventType));
        return "<script>\n" +
                " window.setTimeout(\"location.href='/';\", 0);\n" +
                "</script>" +
                "<div>\n" +
                "    <a href=\"http://localhost:8080\">Startseite</a> <br><br><br>\n" +
                "</div>" +
                "Die Veranstaltung wurde erfolgreich hinzugefügt";
    }
}
