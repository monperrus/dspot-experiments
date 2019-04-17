package com.airbnb.epoxy;


import com.airbnb.epoxy.integrationtest.AdapterWithFieldAssigned;
import com.airbnb.epoxy.integrationtest.AdapterWithIdChanged;
import com.airbnb.epoxy.integrationtest.BasicAutoModelsAdapter;
import com.airbnb.epoxy.integrationtest.BuildConfig;
import com.airbnb.epoxy.integrationtest.ControllerWithoutImplicityAdding;
import com.airbnb.epoxy.integrationtest.Model_;
import com.airbnb.epoxy.integrationtest.autoaddautomodels.ControllerWithImplicitlyAddedModels;
import com.airbnb.epoxy.integrationtest.autoaddautomodels.ControllerWithImplicitlyAddedModels2;
import com.airbnb.epoxy.integrationtest.autoaddautomodels.ControllerWithImplicitlyAddedModels3;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;


@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class AutoModelIntegrationTest {
    @Test
    public void basicAutoModels() {
        BasicAutoModelsAdapter controller = new BasicAutoModelsAdapter();
        controller.requestModelBuild();
        List<EpoxyModel<?>> models = controller.getAdapter().getCopyOfModels();
        Assert.assertEquals("Models size", 2, models.size());
        Assert.assertEquals("First model", Model_.class, models.get(0).getClass());
        Assert.assertEquals("Second model", Model_.class, models.get(1).getClass());
    }

    @Test(expected = IllegalStateException.class)
    public void assigningValueToFieldFails() {
        AdapterWithFieldAssigned testAdapter = new AdapterWithFieldAssigned();
        testAdapter.requestModelBuild();
    }

    @Test(expected = IllegalStateException.class)
    public void assigningIdToAutoModelFails() {
        AdapterWithIdChanged testAdapter = new AdapterWithIdChanged();
        testAdapter.requestModelBuild();
    }

    @Test
    public void implicitlyAddingAutoModelsDisabledByDefault() {
        ControllerWithoutImplicityAdding controller = new ControllerWithoutImplicityAdding();
        controller.requestModelBuild();
        Assert.assertEquals(new ArrayList(), controller.getAdapter().getCopyOfModels());
    }

    @Test
    public void implicitlyAddingAutoModels() {
        ControllerWithImplicitlyAddedModels controller = new ControllerWithImplicitlyAddedModels();
        controller.requestModelBuild();
        Assert.assertEquals(controller.getExpectedModels(), controller.getAdapter().getCopyOfModels());
    }

    @Test
    public void implicitlyAddingAutoModels2() {
        ControllerWithImplicitlyAddedModels2 controller = new ControllerWithImplicitlyAddedModels2();
        controller.requestModelBuild();
        Assert.assertEquals(controller.getExpectedModels(), controller.getAdapter().getCopyOfModels());
    }

    @Test
    public void implicitlyAddingAutoModels3() {
        ControllerWithImplicitlyAddedModels3 controller = new ControllerWithImplicitlyAddedModels3();
        controller.requestModelBuild();
        Assert.assertEquals(controller.getExpectedModels(), controller.getAdapter().getCopyOfModels());
    }
}
