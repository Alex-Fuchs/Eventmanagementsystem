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

@Repository
public class EventRepository {

    private JdbcTemplate jdbcTemplate;

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

    public List<Event> findAll() {
        return new ArrayList<>(jdbcTemplate.query(
                "select * from Event", new VeranstaltungRowMapper()));
    }

    public List<Event> findAllInFuture() {
        List<Event> events = new ArrayList<>();
        for (Event event: findAll()) {
            if (checkDateIsInFuture(event.getDatum())) {
                events.add(event);
            }
        }
        return events;
    }

    public List<Event> findByEventType(String eventType) {
        List<Event> old = jdbcTemplate.query("SELECT * FROM Event WHERE eventType =? ",
                new Object[] { eventType }, new VeranstaltungRowMapper());
        return old;
    }

    public List<Event> findAllSort() {
        List<Event> old = new ArrayList<>(jdbcTemplate.query(
                "select * from Event", new VeranstaltungRowMapper()));
        old.sort(Comparator.comparing(Event::getRankInt).reversed());
        return old;
    }

    public Event findById(long id) {
        try {
            return jdbcTemplate.queryForObject("select * from Event where id=?",
                    new Object[]{id}, new VeranstaltungRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Event findByName(String name) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM Event WHERE VER_NAME =? ",
                    new Object[] { name }, new VeranstaltungRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

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

    private int deleteAll() {
        return jdbcTemplate.update( "delete from Event");
    }

    public int vote(long id, int vote) {
        Event event = findById(id);
        int rank = event.getRankInt() + vote;

        return jdbcTemplate.update("UPDATE Event\n" +
                "        SET rank = ?" +
                "        WHERE id = ?", rank, id);
    }

    private int updateWeather(Long id , String data) {
        return jdbcTemplate.update("UPDATE Event\n" +
                "        SET weather = ?\n" +
                "        WHERE id = ?", data, id);
    }

    private void fetchEvents() {
        for (Event event: Start.getEvents()) {
            insert(event);
        }
    }

    private void setWeatherTask() {
        Date date = new Date();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                System.out.println("UpdateWeatherTask is running at " + date
                        + " with intervall of 1 minute");
                updateWeather();
            }
        }, date, 60 * 1000);
    }

    private void updateWeather() {
        List <Event> events = findAllSort();
        for (Event event : events) {
            updateWeatherOfEvent(event);
        }
    }

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

    private boolean checkAttributes(Event vera) {
        return vera != null && vera.getVer_name() != null && !vera.getVer_name().equals("")
                && vera.getPlace() != null && !vera.getPlace().equals("")
                && vera.getDescription() != null && !vera.getDescription().equals("")
                && checkDateIsInFuture(vera.getDatum())
                && findByName(vera.getVer_name().substring(0, 1).toUpperCase()
                + vera.getVer_name().substring(1)) == null;
    }
}
