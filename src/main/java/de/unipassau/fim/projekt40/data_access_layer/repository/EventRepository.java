package de.unipassau.fim.projekt40.data_access_layer.repository;

import de.unipassau.fim.projekt40.Start;
import de.unipassau.fim.projekt40.data_access_layer.data_access_object.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public List <Event> findAllSort() {
        List<Event> old = new ArrayList<>(jdbcTemplate.query(
                "select * from Event", new VeranstaltungRowMapper()));
        old.sort(Comparator.comparing(Event::getRankingInt).reversed());
        return old;
    }

    public List <Event> findByEventType(String eventType) {
        List<Event> old = jdbcTemplate.query("SELECT * FROM Event WHERE eventType =? ",
                new Object[] { eventType }, new VeranstaltungRowMapper());
        old.sort(Comparator.comparing(Event::getRankingInt).reversed());
        return old;
    }

    public List<Event> findByName(String name) {
        List<Event> old = jdbcTemplate.query("SELECT * FROM Event WHERE VER_NAME =? ",
                new Object[] { name }, new VeranstaltungRowMapper());
        old.sort(Comparator.comparing(Event::getRankingInt).reversed());
        return old;
    }

    public Event findById(long id) {
        return jdbcTemplate.queryForObject("select * from Event where id=?",
                new Object[] { id }, new VeranstaltungRowMapper());
    }

    public int insert(Event vera) {
        return jdbcTemplate.update("insert into Event " +
                        "(id, ver_name, place, datum, description, eventType, rank, weather) "
                        + "values(?,  ?, ?, ?, ?,?, ?, ?)",
                vera.getId(), vera.getVer_name(), vera.getPlace(), vera.getDatum(), vera.getDescription(),
                vera.getEventType(), vera.getRank(), vera.getWeather());
    }

    private int deleteAll() {
        return jdbcTemplate.update( "delete from Event");
    }

    public int vote(long id, int vote) {
        Event event = findById(id);
        int rank = event.getRankingInt() + vote;

        return jdbcTemplate.update("UPDATE Event\n" +
                "        SET rank = ?" +
                "        WHERE id = ?", rank, id);
    }

    private void fetchEvents() {
        List<Event> events = Start.getEvents();
        for (Event event: events) {
            insert(event);
        }
    }


}
