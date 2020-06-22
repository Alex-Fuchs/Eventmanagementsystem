package de.unipassau.fim.projekt40.data_access_layer.repository;

import de.unipassau.fim.projekt40.Start;
import de.unipassau.fim.projekt40.data_access_layer.data_access_object.EventType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;


@Repository
public class EventTypeRepository {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public EventTypeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        if (Start.isNewStart()) {
            deleteAll();
            fetchEventTypes();
        } else if (Start.isAddingEventTypes()) {
            fetchEventTypes();
        }
    }

    private void fetchEventTypes() {
        for (EventType eventType: Start.getEventTypes()) {
            insert(eventType);
        }
    }

    class VeranstaltungRowMapper implements RowMapper<EventType> {

        @Override
        public EventType mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new EventType(rs.getString("ver_name"));
        }
    }

    public List <EventType> findAll() {
        return new ArrayList<>(jdbcTemplate.query(
                "select * from EventType", new VeranstaltungRowMapper()));
    }

    public EventType findByName(String name) {
        try {
            return jdbcTemplate.queryForObject("select * from EventType WHERE VER_NAME =?",
                    new Object[] { name }, new VeranstaltungRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private int insert(EventType eventType) {
        if (eventType != null && eventType.getName() != null
            && !eventType.getName().equals("")) {
            eventType.setName(eventType.getName().substring(0, 1).toUpperCase()
                    + eventType.getName().substring(1));
            if (findByName(eventType.getName()) == null) {
                return jdbcTemplate.update("insert into EventType " +
                        "(ver_name) " + "values(?)", eventType.getName());
            }
        }
        return -1;
    }

    private int deleteAll() {
        return jdbcTemplate.update( "delete from EventType");
    }
}

