package de.unipassau.fim.projekt40;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Projekt40ApplicationTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

	@Test
    void testRestApi() {
        String result = testRestTemplate.getForObject("/events?sizeString=-1", String.class);
        assert result.contains("\"status\":400");
        result = testRestTemplate.getForObject("/events?sizeString=Hallo", String.class);
        assert result.contains("\"status\":400");
    }

    @Test
    void testMainController() {
        String result = testRestTemplate.getForObject("/event?id=Hallo", String.class);
        assert result.contains("\"status\":400");
        result = testRestTemplate.getForObject("/?size=Hallo", String.class);
        assert result.contains("\"status\":400");
        result = testRestTemplate.getForObject("/?size=-1", String.class);
        assert result.contains("\"status\":400");
        result = testRestTemplate.getForObject("/sort?sort=Hallo&size=Hallo", String.class);
        assert result.contains("\"status\":400");
        result = testRestTemplate.getForObject("/sort?sort=Hallo&size=-1", String.class);
        assert result.contains("\"status\":400");
        result = testRestTemplate.getForObject("/search?entry=Hallo&size=Hallo", String.class);
        assert result.contains("\"status\":400");
        result = testRestTemplate.getForObject("/search?entry=Hallo&size=-1", String.class);
        assert result.contains("\"status\":400");
    }
}
