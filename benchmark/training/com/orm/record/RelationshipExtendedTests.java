package com.orm.record;


import com.orm.SugarRecord;
import com.orm.app.ClientApp;
import com.orm.dsl.BuildConfig;
import com.orm.model.RelationshipExtendedModel;
import com.orm.model.SimpleExtendedModel;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;


@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = 18, constants = BuildConfig.class, application = ClientApp.class, packageName = "com.orm.model", manifest = Config.NONE)
public class RelationshipExtendedTests {
    @Test
    public void emptyDatabaseTest() throws Exception {
        Assert.assertEquals(0L, SugarRecord.count(RelationshipExtendedModel.class));
        Assert.assertEquals(0L, SugarRecord.count(SimpleExtendedModel.class));
    }

    @Test
    public void oneSaveTest() throws Exception {
        SimpleExtendedModel simple = new SimpleExtendedModel();
        SugarRecord.save(simple);
        SugarRecord.save(new RelationshipExtendedModel(simple));
        Assert.assertEquals(1L, SugarRecord.count(SimpleExtendedModel.class));
        Assert.assertEquals(1L, SugarRecord.count(RelationshipExtendedModel.class));
    }

    @Test
    public void twoSameSaveTest() throws Exception {
        SimpleExtendedModel simple = new SimpleExtendedModel();
        SugarRecord.save(simple);
        SugarRecord.save(new RelationshipExtendedModel(simple));
        SugarRecord.save(new RelationshipExtendedModel(simple));
        Assert.assertEquals(1L, SugarRecord.count(SimpleExtendedModel.class));
        Assert.assertEquals(2L, SugarRecord.count(RelationshipExtendedModel.class));
    }

    @Test
    public void twoDifferentSaveTest() throws Exception {
        SimpleExtendedModel simple = new SimpleExtendedModel();
        SugarRecord.save(simple);
        SimpleExtendedModel anotherSimple = new SimpleExtendedModel();
        SugarRecord.save(anotherSimple);
        SugarRecord.save(new RelationshipExtendedModel(simple));
        SugarRecord.save(new RelationshipExtendedModel(anotherSimple));
        Assert.assertEquals(2L, SugarRecord.count(SimpleExtendedModel.class));
        Assert.assertEquals(2L, SugarRecord.count(RelationshipExtendedModel.class));
    }

    @Test
    public void manySameSaveTest() throws Exception {
        SimpleExtendedModel simple = new SimpleExtendedModel();
        SugarRecord.save(simple);
        for (int i = 1; i <= 100; i++) {
            SugarRecord.save(new RelationshipExtendedModel(simple));
        }
        Assert.assertEquals(1L, SugarRecord.count(SimpleExtendedModel.class));
        Assert.assertEquals(100L, SugarRecord.count(RelationshipExtendedModel.class));
    }

    @Test
    public void manyDifferentSaveTest() throws Exception {
        for (int i = 1; i <= 100; i++) {
            SimpleExtendedModel simple = new SimpleExtendedModel();
            SugarRecord.save(simple);
            SugarRecord.save(new RelationshipExtendedModel(simple));
        }
        Assert.assertEquals(100L, SugarRecord.count(SimpleExtendedModel.class));
        Assert.assertEquals(100L, SugarRecord.count(RelationshipExtendedModel.class));
    }

    @Test
    public void listAllSameTest() throws Exception {
        SimpleExtendedModel simple = new SimpleExtendedModel();
        SugarRecord.save(simple);
        for (int i = 1; i <= 100; i++) {
            SugarRecord.save(new RelationshipExtendedModel(simple));
        }
        List<RelationshipExtendedModel> models = SugarRecord.listAll(RelationshipExtendedModel.class);
        Assert.assertEquals(100, models.size());
        for (RelationshipExtendedModel model : models) {
            Assert.assertEquals(getId(), getId());
        }
    }

    @Test
    public void listAllDifferentTest() throws Exception {
        for (int i = 1; i <= 100; i++) {
            SimpleExtendedModel simple = new SimpleExtendedModel();
            SugarRecord.save(simple);
            SugarRecord.save(new RelationshipExtendedModel(simple));
        }
        List<RelationshipExtendedModel> models = SugarRecord.listAll(RelationshipExtendedModel.class);
        Assert.assertEquals(100, models.size());
        for (RelationshipExtendedModel model : models) {
            Assert.assertEquals(getId(), getId());
        }
    }
}
