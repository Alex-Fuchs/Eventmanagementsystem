package de.unipassau.fim.projekt40;

import de.unipassau.fim.projekt40.data_access_layer.data_access_object.Event;
import de.unipassau.fim.projekt40.data_access_layer.data_access_object.EventType;
import de.unipassau.fim.projekt40.data_access_layer.repository.EventRepository;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class Start {

    private static Set<EventType> eventTypes = new HashSet<>();
    private static Set<Event> events = new HashSet<>();
    private static boolean newStart;
    private static boolean newEventTypes;

    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader
                = new BufferedReader(new InputStreamReader(System.in));
        initialising(bufferedReader);
        if (newStart) {
            setEventTypes(bufferedReader);
            setInitialisingEvents(bufferedReader);
        } else if (newEventTypes) {
            setEventTypes(bufferedReader);
        }
        SpringApplication.run(Start.class, args);
    }

    public Start() { }

    public static Set<EventType> getEventTypes() {
        return eventTypes;
    }

    public static Set<Event> getEvents() {
        return events;
    }

    public static boolean isNewStart() {
        return newStart;
    }

    public static boolean isAddingEventTypes() {
        return newEventTypes;
    }

    private static void initialising(BufferedReader input) throws IOException {
        String answer;
        System.out.println("Soll die Datenbank neu intitalisiert werden (1) oder "
                + "sollen neue EventTypen hinzugefügt werden (2) " +
                "oder nichts verändert werden (3) -> (1/2/3)?");
        if ((answer = input.readLine()) != null) {
            switch (answer) {
            case "1":
                newStart = true;
                break;
            case "2":
                newEventTypes = true;
                break;
            case "3":
                break;
            default:
                System.out.println("Eine Antwort ist nötig!");
                initialising(input);
            }
        }
    }

    private static void setEventTypes(BufferedReader input) throws IOException {
        String eventType;
        System.out.println("Geben Sie mind. einen gewünschten EventTypen" +
                " für die Veranstaltungen an.");
        System.out.println("Falls nur EventTypen hinzugefügt werden und nicht die DB" +
                "neu initialisiert wird, werden nur neue Typen hinzugefügt.");
        System.out.println("Bestätigen Sie jeweils mit Enter und geben Sie" +
                " \"quit\" ein um zu vollenden");
        while ((eventType = input.readLine()) != null
                && !eventType.equals("quit")) {
            if (!eventType.equals("")) {
                eventType = eventType.substring(0, 1).toUpperCase() + eventType.substring(1);
                EventType tmp = new EventType(eventType);
                eventTypes.add(tmp);
            } else {
                System.out.println("EventTyp hat keinen Namen!");
            }
        }

        if (eventTypes.size() == 0) {
            System.out.println("Es wird mind. ein EventTyp benötigt!");
            setEventTypes(input);
        }
    }

    private static void setInitialisingEvents(BufferedReader input) throws IOException {
        String event;
        System.out.println("Geben Sie die gewünschten vorinitalisierten Events ein");
        System.out.println("Geben sie das Event wie folgt ein: <Name> <Ort>" +
                " <YYYY-MM-DD> <Beschreibung> <EventTyp>");
        System.out.println("Bestätigen Sie jeweils mit Enter und geben Sie" +
                " \"quit\" ein um zu vollenden");
        while ((event = input.readLine()) != null
                && !event.equals("quit")){
            String[] attributes = event.split("\\s+");
            if (checkAttributes(attributes)) {
                Event tmp = new Event(attributes[0], attributes[1],
                        attributes[2], attributes[3], attributes[4]);
                events.add(tmp);
            } else {
                System.out.println("Die Parameter des Events passen nicht," +
                        " evtl liegt das Datum nicht in der Zukunft!");
            }
        }
    }

    private static boolean checkAttributes(String[] attributes) {
        if (attributes.length == 5 && checkNothingIsEmpty(attributes)) {
            formatAttributes(attributes);
            boolean isDateLegalAndInFuture;
            try {
                isDateLegalAndInFuture = EventRepository.checkDateIsInFuture(attributes[2]);
            } catch (IllegalArgumentException e) {
                isDateLegalAndInFuture = false;
            }
            return (checkNameIsUnique(attributes[0]) && isDateLegalAndInFuture
                    && checkEventTypeExists(attributes[4]));
        }
        return false;
    }

    private static void formatAttributes(String[] attributes) {
        attributes[0] = attributes[0].substring(0, 1).toUpperCase()
                + attributes[0].substring(1);
        attributes[1] = attributes[1].substring(0, 1).toUpperCase()
                + attributes[1].substring(1);
        attributes[3] = attributes[3].substring(0, 1).toUpperCase()
                + attributes[3].substring(1);
        attributes[4] = attributes[4].substring(0, 1).toUpperCase()
                + attributes[4].substring(1);
    }

    private static boolean checkNameIsUnique(String name) {
        for (Event event: events) {
            if (event.getVer_name().equals(name)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkEventTypeExists(String eventType) {
        for (EventType tmp: eventTypes) {
            if (tmp.getName().equals(eventType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkNothingIsEmpty(String[] attributes) {
        for (String string: attributes) {
            if (string.equals("")) {
                return false;
            }
        }
        return true;
    }
}