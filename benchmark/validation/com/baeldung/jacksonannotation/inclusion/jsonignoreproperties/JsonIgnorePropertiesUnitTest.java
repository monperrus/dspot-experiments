package com.baeldung.jacksonannotation.inclusion.jsonignoreproperties;


import Course.Medium.ONLINE;
import com.baeldung.jacksonannotation.domain.Author;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.path.json.JsonPath;
import org.junit.Test;


/**
 * Source code github.com/eugenp/tutorials
 *
 * @author Alex Theedom www.baeldung.com
 * @version 1.0
 */
public class JsonIgnorePropertiesUnitTest {
    @Test
    public void whenSerializingUsingJsonIgnoreProperties_thenCorrect() throws JsonProcessingException {
        // arrange
        Course course = new Course("Spring Security", new Author("Eugen", "Paraschiv"));
        course.setMedium(ONLINE);
        // act
        String result = new ObjectMapper().writeValueAsString(course);
        // assert
        assertThat(JsonPath.from(result).getString("medium")).isNull();
        /* {
        "id": "ef0c8d2b-b088-409e-905c-95ac88dc0ed0",
        "title": "Spring Security",
        "authors": [
        {
        "id": "47a4f498-b0f3-4daf-909f-d2c35a0fe3c2",
        "firstName": "Eugen",
        "lastName": "Paraschiv",
        "items": []
        }
        ],
        "price": 0,
        "duration": 0,
        "level": null,
        "prerequisite": null
        }
         */
    }
}
