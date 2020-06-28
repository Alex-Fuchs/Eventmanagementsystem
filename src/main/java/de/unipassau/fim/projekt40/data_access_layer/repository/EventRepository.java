package de.unipassau.fim.projekt40.data_access_layer.repository;

import de.unipassau.fim.projekt40.Start;
import de.unipassau.fim.projekt40.data_access_layer.JsonWeatherAPI;
import de.unipassau.fim.projekt40.data_access_layer.data_access_object.Event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * allows you to connect and edit the database table Event
 */
@Repository
public class EventRepository {

    private JdbcTemplate jdbcTemplate;

    /**
     * At a new Start all events are deleted and the new ones are fetched.
     * In addition, the weather is updated in the background
     */
    @Autowired
    public EventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        if (Start.isNewStart()) {
            deleteAll();
            fetchEvents();
        }
        setWeatherTask();
    }

    class VeranstaltungRowMapper implements RowMapper<Event> {

        @Override
        public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Event(rs.getLong("id"), rs.getString("ver_name"),
                    rs.getString("place"), rs.getString("datum"),
                    rs.getString("description"), rs.getString("eventType"),
                    rs.getString("weather"), rs.getString("rank"));
        }
    }

    /**
     * returns all events in the database
     *
     * @return list of all events
     */
    public List<Event> findAll() {
        return new ArrayList<>(jdbcTemplate.query(
                "select * from Event", new VeranstaltungRowMapper()));
    }

    /**
     * returns a list of all events being in the future
     *
     * @return list of events in future
     */
    public List<Event> findAllInFuture() {
        List<Event> events = new ArrayList<>();
        for (Event event: findAll()) {
            if (checkDateIsInFuture(event.getDatum())) {
                events.add(event);
            }
        }
        return events;
    }

    /**
     * returns a list of all events of an event type
     *
     * @param eventType a Eventype of a list
     * @return list of all Events by a type
     */
    public List<Event> findByEventType(String eventType) {
        List<Event> old = jdbcTemplate.query("SELECT * FROM Event WHERE eventType =? ",
                new Object[] { eventType }, new VeranstaltungRowMapper());
        return old;
    }

    /**
     * Sorted all events by ranking id
     *
     * @return list sorted by ranking
     */
    public List<Event> findAllSort() {
        List<Event> old = new ArrayList<>(jdbcTemplate.query(
                "select * from Event", new VeranstaltungRowMapper()));
        old.sort(Comparator.comparing(Event::getRankInt).reversed());
        return old;
    }

    /**
     * return the Event by the ID
     *
     * @param id ID of a event
     * @return the event
     */
    public Event findById(long id) {
        try {
            return jdbcTemplate.queryForObject("select * from Event where id=?",
                    new Object[]{id}, new VeranstaltungRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * returns an event with the name
     *
     * @param name name of a event
     * @return the Event
     */
    public Event findByName(String name) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM Event WHERE VER_NAME =? ",
                    new Object[] { name }, new VeranstaltungRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * added a event to the DB
     *
     * @param vera Event to be added
     * @return status of SQL-Command
     */
    public int insert(Event vera) {
        if (checkAttributes(vera)) {
            formatAttributes(vera);
            int code = jdbcTemplate.update("insert into Event " +
                            "(id, ver_name, place, datum, description, eventType, rank, weather) "
                            + "values(?,  ?, ?, ?, ?,?, ?, ?)",
                    vera.getId(), vera.getVer_name(), vera.getPlace(), vera.getDatum(), vera.getDescription(),
                    vera.getEventType(), vera.getRank(), vera.getWeather());
            updateWeatherOfEvent(findByName(vera.getVer_name()));
            return code;
        }
        return -1;
    }

    /**
     * delete all Events in the DB
     *
     * @return status of SQL-Command
     */
    private int deleteAll() {
        return jdbcTemplate.update( "delete from Event");
    }

    /**
     * updates the evaluation status and enters it into the database
     * @param id ID of a Event
     * @param vote evaluation status
     * @return
     */
    public int vote(long id, int vote) {
        Event event = findById(id);
        int rank = event.getRankInt() + vote;

        return jdbcTemplate.update("UPDATE Event\n" +
                "        SET rank = ?" +
                "        WHERE id = ?", rank, id);
    }

    /**
     * Enters a new value for weather into the database
     *
     * @param id ID of a Event
     * @param data the new Value of the weater
     * @return
     */
    private int updateWeather(Long id , String data) {
        return jdbcTemplate.update("UPDATE Event\n" +
                "        SET weather = ?\n" +
                "        WHERE id = ?", data, id);
    }

    /**
     * Inserts a list of events into the database
     */
    private void fetchEvents() {
        for (Event event: Start.getEvents()) {
            insert(event);
        }
    }

    /**
     * Makes sure that the weather is updated every 10 minutes in the background
     */
    private void setWeatherTask() {
        Date date = new Date();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                System.out.println("UpdateWeatherTask is running at " + date
                        + " with intervall of 10 minutes");
                updateWeather();
            }
        }, date, 60 * 10 * 1000);
    }

    /**
     * Iterates over all events and updates the weather
     */
    private void updateWeather() {
        List <Event> events = findAllSort();
        for (Event event : events) {
            updateWeatherOfEvent(event);
        }
    }

    /**
     * Updates the weather for an event
     * @param event a Event in the DB
     */
    private void updateWeatherOfEvent(Event event) {
        if (datumIsInOneWeek(event.getDatum())) {
            String id = JsonWeatherAPI.getWoeid(event.getPlace());
            String weather;
            if (id != null && (weather = JsonWeatherAPI.getWeather(id, event.getDatum())) != null) {
                event.setWeather(weather);
                updateWeather(event.getId(), weather);
            } else {
                event.setWeather("Ort nicht vorhanden");
                updateWeather(event.getId(), "Ort nicht vorhanden");
            }
        } else {
            event.setWeather("Zu weit entfernt");
            updateWeather(event.getId(), "Zu weit entfernt");
        }
    }

    /**
     * Checks if the date is within the next week
     *
     * @param datum date of Event
     * @return if the date is within the next week
     */
    private boolean datumIsInOneWeek(String datum) {
        Date dateInOneWeek = new Date(new Date().getTime()
                + 1000 * 60 * 60 * 24 * 7);
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(datum);
            return (dateInOneWeek.getTime() - date.getTime() > 0);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Datum ist illegal!");
        }
    }

    /**
     * Checks if the date is in the future
     *
     * @param datum Date of a event
     * @return if the date is in the future
     */
    public static boolean checkDateIsInFuture(String datum) {
        if (datum != null && datum.matches("^2[0-9]{3}-(0[1-9]||1[0-2])-(0[1-9]||[1-2][0-9]||3[0-1])$")) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(datum);
                Date todayWithZeroTime = formatter.parse(formatter.format(new Date()));
                return (todayWithZeroTime.before(date) || todayWithZeroTime.equals(date));
            } catch (ParseException e) {
                throw new IllegalArgumentException("Parsing nicht erfolgreich!");
            }
        } else {
            throw new IllegalArgumentException("Datum ist illegal!");
        }
    }

    /**
     * Make sure that the first letter is capitalized
     * @param vera Event to be added
     */
    private void formatAttributes(Event vera) {
        vera.setVer_name(vera.getVer_name().substring(0, 1).toUpperCase()
                + vera.getVer_name().substring(1));
        vera.setPlace(vera.getPlace().substring(0, 1).toUpperCase()
                + vera.getPlace().substring(1));
        vera.setDescription(vera.getDescription().substring(0, 1).toUpperCase()
                + vera.getDescription().substring(1));
        vera.setEventType(vera.getEventType().substring(0, 1).toUpperCase()
                + vera.getEventType().substring(1));
    }

    /**
     * Checks if all values of an event are filled in
     *
     * @param vera Event to be added
     * @return whether all entries are filled out
     */
    private boolean checkAttributes(Event vera) {
        return vera != null && vera.getVer_name() != null && !vera.getVer_name().equals("")
                && vera.getPlace() != null && !vera.getPlace().equals("")
                && vera.getDescription() != null && !vera.getDescription().equals("")
                && checkDateIsInFuture(vera.getDatum())
                && findByName(vera.getVer_name().substring(0, 1).toUpperCase()
                + vera.getVer_name().substring(1)) == null;
    }
}
