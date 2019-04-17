package com.baeldung.mongodb;


import com.baeldung.mongodb.daos.UserRepository;
import com.baeldung.mongodb.models.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class MongoDbAutoGeneratedFieldIntegrationTest {
    @Autowired
    UserRepository userRepository;

    @Test
    public void givenUserObject_whenSave_thenCreateNewUser() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        userRepository.save(user);
        assertThat(userRepository.findAll().size()).isGreaterThan(0);
    }
}
