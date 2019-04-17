package org.baeldung;


import org.baeldung.boot.Application;
import org.baeldung.boot.domain.GenericEntity;
import org.baeldung.boot.repository.GenericEntityRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SpringBootJPAIntegrationTest {
    @Autowired
    private GenericEntityRepository genericEntityRepository;

    @Test
    public void givenGenericEntityRepository_whenSaveAndRetreiveEntity_thenOK() {
        GenericEntity genericEntity = genericEntityRepository.save(new GenericEntity("test"));
        GenericEntity foundEntity = genericEntityRepository.findById(genericEntity.getId()).orElse(null);
        Assert.assertNotNull(foundEntity);
        Assert.assertEquals(genericEntity.getValue(), foundEntity.getValue());
    }
}
