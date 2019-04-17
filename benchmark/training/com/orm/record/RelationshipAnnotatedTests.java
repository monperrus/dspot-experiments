package com.orm.record;


import com.orm.SugarRecord;
import com.orm.app.ClientApp;
import com.orm.dsl.BuildConfig;
import com.orm.model.RelationshipAnnotatedModel;
import com.orm.model.SimpleAnnotatedModel;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;


@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = 18, constants = BuildConfig.class, application = ClientApp.class, packageName = "com.orm.model", manifest = Config.NONE)
public final class RelationshipAnnotatedTests {
    @Test
    public void emptyDatabaseTest() throws Exception {
        Assert.assertEquals(0L, SugarRecord.count(RelationshipAnnotatedModel.class));
        Assert.assertEquals(0L, SugarRecord.count(SimpleAnnotatedModel.class));
    }

    @Test
    public void oneSaveTest() throws Exception {
        SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
        SugarRecord.save(simple);
        SugarRecord.save(new RelationshipAnnotatedModel(simple));
        Assert.assertEquals(1L, SugarRecord.count(SimpleAnnotatedModel.class));
        Assert.assertEquals(1L, SugarRecord.count(RelationshipAnnotatedModel.class));
    }

    @Test
    public void twoSameSaveTest() throws Exception {
        SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
        SugarRecord.save(simple);
        SugarRecord.save(new RelationshipAnnotatedModel(simple));
        SugarRecord.save(new RelationshipAnnotatedModel(simple));
        Assert.assertEquals(1L, SugarRecord.count(SimpleAnnotatedModel.class));
        Assert.assertEquals(2L, SugarRecord.count(RelationshipAnnotatedModel.class));
    }

    @Test
    public void twoDifferentSaveTest() throws Exception {
        SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
        SugarRecord.save(simple);
        SimpleAnnotatedModel anotherSimple = new SimpleAnnotatedModel();
        SugarRecord.save(anotherSimple);
        SugarRecord.save(new RelationshipAnnotatedModel(simple));
        SugarRecord.save(new RelationshipAnnotatedModel(anotherSimple));
        Assert.assertEquals(2L, SugarRecord.count(SimpleAnnotatedModel.class));
        Assert.assertEquals(2L, SugarRecord.count(RelationshipAnnotatedModel.class));
    }

    @Test
    public void manySameSaveTest() throws Exception {
        SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
        SugarRecord.save(simple);
        for (int i = 1; i <= 100; i++) {
            SugarRecord.save(new RelationshipAnnotatedModel(simple));
        }
        Assert.assertEquals(1L, SugarRecord.count(SimpleAnnotatedModel.class));
        Assert.assertEquals(100L, SugarRecord.count(RelationshipAnnotatedModel.class));
    }

    @Test
    public void manyDifferentSaveTest() throws Exception {
        for (int i = 1; i <= 100; i++) {
            SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
            SugarRecord.save(simple);
            SugarRecord.save(new RelationshipAnnotatedModel(simple));
        }
        Assert.assertEquals(100L, SugarRecord.count(SimpleAnnotatedModel.class));
        Assert.assertEquals(100L, SugarRecord.count(RelationshipAnnotatedModel.class));
    }

    @Test
    public void listAllSameTest() throws Exception {
        SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
        SugarRecord.save(simple);
        for (int i = 1; i <= 100; i++) {
            SugarRecord.save(new RelationshipAnnotatedModel(simple));
        }
        List<RelationshipAnnotatedModel> models = SugarRecord.listAll(RelationshipAnnotatedModel.class);
        Assert.assertEquals(100, models.size());
        for (RelationshipAnnotatedModel model : models) {
            Assert.assertEquals(simple.getId(), model.getSimple().getId());
        }
    }

    @Test
    public void listAllDifferentTest() throws Exception {
        for (int i = 1; i <= 100; i++) {
            SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
            SugarRecord.save(simple);
            SugarRecord.save(new RelationshipAnnotatedModel(simple));
        }
        List<RelationshipAnnotatedModel> models = SugarRecord.listAll(RelationshipAnnotatedModel.class);
        Assert.assertEquals(100, models.size());
        for (RelationshipAnnotatedModel model : models) {
            Assert.assertEquals(model.getId(), model.getSimple().getId());
        }
    }
}
