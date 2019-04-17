/**
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.litho;


import Color.BLUE;
import Color.CYAN;
import Color.RED;
import Color.WHITE;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.collection.LongSparseArray;
import com.facebook.litho.drawable.ComparableDrawable;
import com.facebook.litho.testing.TestComponent;
import com.facebook.litho.testing.TestDrawableComponent;
import com.facebook.litho.testing.TestViewComponent;
import com.facebook.litho.testing.Whitebox;
import com.facebook.litho.testing.helper.ComponentTestHelper;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import com.facebook.litho.widget.EditText;
import com.facebook.litho.widget.Text;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

import static MeasureSpec.makeMeasureSpec;


@RunWith(ComponentsTestRunner.class)
public class MountStateRemountTest {
    private ComponentContext mContext;

    @Test
    public void testRemountSameLayoutState() {
        final TestComponent component1 = TestDrawableComponent.create(mContext).build();
        final TestComponent component2 = TestDrawableComponent.create(mContext).build();
        final TestComponent component3 = TestDrawableComponent.create(mContext).build();
        final TestComponent component4 = TestDrawableComponent.create(mContext).build();
        final LithoView lithoView = mountComponent(mContext, Column.create(mContext).child(component1).child(component2).build());
        assertThat(component1.isMounted()).isTrue();
        assertThat(component2.isMounted()).isTrue();
        mountComponent(mContext, lithoView, Column.create(mContext).child(component3).child(component4).build());
        assertThat(component1.isMounted()).isTrue();
        assertThat(component2.isMounted()).isTrue();
        assertThat(component3.isMounted()).isFalse();
        assertThat(component4.isMounted()).isFalse();
        final MountState mountState = Whitebox.getInternalState(lithoView, "mMountState");
        final LongSparseArray<MountItem> indexToItemMap = Whitebox.getInternalState(mountState, "mIndexToItemMap");
        final List<Component> components = new ArrayList<>();
        for (int i = 0; i < (indexToItemMap.size()); i++) {
            components.add(indexToItemMap.valueAt(i).getComponent());
        }
        assertThat(containsRef(components, component1)).isFalse();
        assertThat(containsRef(components, component2)).isFalse();
        assertThat(containsRef(components, component3)).isTrue();
        assertThat(containsRef(components, component4)).isTrue();
    }

    /**
     * There was a crash when mounting a drawing in place of a view. This test is here to make sure
     * this does not regress. To reproduce this crash the pools needed to be in a specific state
     * as view layout outputs and mount items were being re-used for drawables.
     */
    @Test
    public void testRemountDifferentMountType() throws IllegalAccessException, NoSuchFieldException {
        final LithoView lithoView = ComponentTestHelper.mountComponent(mContext, TestViewComponent.create(mContext).build());
        ComponentTestHelper.mountComponent(mContext, lithoView, TestDrawableComponent.create(mContext).build());
    }

    @Test
    public void testRemountNewLayoutState() {
        final TestComponent component1 = TestDrawableComponent.create(mContext).color(RED).build();
        final TestComponent component2 = TestDrawableComponent.create(mContext).color(BLUE).build();
        final TestComponent component3 = TestDrawableComponent.create(mContext).unique().build();
        final TestComponent component4 = TestDrawableComponent.create(mContext).unique().build();
        final LithoView lithoView = mountComponent(mContext, Column.create(mContext).child(component1).child(component2).build());
        assertThat(component1.isMounted()).isTrue();
        assertThat(component2.isMounted()).isTrue();
        mountComponent(mContext, lithoView, Column.create(mContext).child(component3).child(component4).build());
        assertThat(component1.isMounted()).isFalse();
        assertThat(component2.isMounted()).isFalse();
        assertThat(component3.isMounted()).isTrue();
        assertThat(component4.isMounted()).isTrue();
    }

    @Test
    public void testRemountAfterSettingNewRootTwice() {
        final TestComponent component1 = TestDrawableComponent.create(mContext).color(RED).returnSelfInMakeShallowCopy().build();
        final TestComponent component2 = TestDrawableComponent.create(mContext).returnSelfInMakeShallowCopy().color(BLUE).build();
        final LithoView lithoView = new LithoView(mContext);
        final ComponentTree componentTree = ComponentTree.create(mContext, Column.create(mContext).child(component1).build()).build();
        mountComponent(lithoView, componentTree, makeMeasureSpec(100, MeasureSpec.EXACTLY), makeMeasureSpec(100, MeasureSpec.EXACTLY));
        assertThat(component1.isMounted()).isTrue();
        componentTree.setRootAndSizeSpec(Column.create(mContext).child(component2).build(), makeMeasureSpec(50, MeasureSpec.EXACTLY), makeMeasureSpec(50, MeasureSpec.EXACTLY));
        componentTree.setSizeSpec(makeMeasureSpec(100, MeasureSpec.EXACTLY), makeMeasureSpec(100, MeasureSpec.EXACTLY));
        assertThat(component2.isMounted()).isTrue();
    }

    @Test
    public void testRemountPartiallyDifferentLayoutState() {
        final TestComponent component1 = TestDrawableComponent.create(mContext).build();
        final TestComponent component2 = TestDrawableComponent.create(mContext).build();
        final TestComponent component3 = TestDrawableComponent.create(mContext).build();
        final TestComponent component4 = TestDrawableComponent.create(mContext).build();
        final LithoView lithoView = mountComponent(mContext, Column.create(mContext).child(component1).child(component2).build());
        assertThat(component1.isMounted()).isTrue();
        assertThat(component2.isMounted()).isTrue();
        mountComponent(mContext, lithoView, Column.create(mContext).child(component3).child(Column.create(mContext).wrapInView().child(component4)).build());
        assertThat(component1.isMounted()).isTrue();
        assertThat(component2.isMounted()).isFalse();
        assertThat(component3.isMounted()).isFalse();
        assertThat(component4.isMounted()).isTrue();
    }

    @Test
    public void testRemountOnNoLayoutChanges() {
        final Component oldComponent = Column.create(mContext).backgroundColor(WHITE).child(EditText.create(mContext).backgroundColor(RED).foregroundColor(CYAN).text("Hello World").viewTag("Alpha").contentDescription("some description")).build();
        final LithoView lithoView = new LithoView(mContext);
        final ComponentTree componentTree = ComponentTree.create(mContext, oldComponent).incrementalMount(false).layoutDiffing(true).build();
        mountComponent(lithoView, componentTree, makeMeasureSpec(400, MeasureSpec.EXACTLY), makeMeasureSpec(400, MeasureSpec.EXACTLY));
        final View oldView = lithoView.getChildAt(0);
        final Object oldTag = oldView.getTag();
        final String oldContentDescription = oldView.getContentDescription().toString();
        final Drawable oldBackground = oldView.getBackground();
        final Component newComponent = Column.create(mContext).backgroundColor(WHITE).child(EditText.create(mContext).backgroundColor(RED).foregroundColor(CYAN).text("Hello World").viewTag("Alpha").contentDescription("some description")).build();
        componentTree.setRootAndSizeSpec(newComponent, makeMeasureSpec(400, MeasureSpec.EXACTLY), makeMeasureSpec(400, MeasureSpec.EXACTLY));
        componentTree.setSizeSpec(makeMeasureSpec(400, MeasureSpec.EXACTLY), makeMeasureSpec(400, MeasureSpec.EXACTLY));
        View newView = lithoView.getChildAt(0);
        assertThat(newView).isSameAs(oldView);
        final Object newTag = newView.getTag();
        final String newContentDescription = newView.getContentDescription().toString();
        final Drawable newBackground = newView.getBackground();
        // Check that props were not set again
        assertThat(newTag).isSameAs(oldTag);
        assertThat(newContentDescription).isSameAs(oldContentDescription);
        assertThat(oldBackground).isSameAs(newBackground);
    }

    @Test
    public void testRemountOnNodeInfoLayoutChanges() {
        final Component oldComponent = Column.create(mContext).backgroundColor(WHITE).child(Text.create(mContext).textSizeSp(12).text("label:")).child(EditText.create(mContext).text("Hello World").textSizeSp(12).viewTag("Alpha").enabled(true)).build();
        final LithoView lithoView = new LithoView(mContext);
        final ComponentTree componentTree = ComponentTree.create(mContext, oldComponent).incrementalMount(false).layoutDiffing(true).build();
        mountComponent(lithoView, componentTree, makeMeasureSpec(400, MeasureSpec.EXACTLY), makeMeasureSpec(400, MeasureSpec.EXACTLY));
        final View oldView = lithoView.getChildAt(0);
        final Object oldTag = oldView.getTag();
        final boolean oldIsEnabled = oldView.isEnabled();
        final Component newComponent = Column.create(mContext).backgroundColor(WHITE).child(Text.create(mContext).textSizeSp(12).text("label:")).child(EditText.create(mContext).text("Hello World").textSizeSp(12).viewTag("Beta").enabled(false)).build();
        componentTree.setRootAndSizeSpec(newComponent, makeMeasureSpec(400, MeasureSpec.EXACTLY), makeMeasureSpec(400, MeasureSpec.EXACTLY));
        componentTree.setSizeSpec(makeMeasureSpec(400, MeasureSpec.EXACTLY), makeMeasureSpec(400, MeasureSpec.EXACTLY));
        final View newView = lithoView.getChildAt(0);
        assertThat(newView).isSameAs(oldView);
        final Object newTag = newView.getTag();
        final boolean newIsEnabled = newView.isEnabled();
        assertThat(newTag).isNotEqualTo(oldTag);
        assertThat(newIsEnabled).isNotEqualTo(oldIsEnabled);
    }

    @Test
    public void testRemountOnViewNodeInfoLayoutChanges() {
        final Component oldComponent = Column.create(mContext).backgroundColor(WHITE).child(Text.create(mContext).textSizeSp(12).text("label:")).child(EditText.create(mContext).text("Hello World").textSizeSp(12).backgroundColor(RED)).build();
        final LithoView lithoView = new LithoView(mContext);
        final ComponentTree componentTree = ComponentTree.create(mContext, oldComponent).incrementalMount(false).layoutDiffing(true).build();
        mountComponent(lithoView, componentTree, makeMeasureSpec(400, MeasureSpec.EXACTLY), makeMeasureSpec(400, MeasureSpec.EXACTLY));
        final View oldView = lithoView.getChildAt(0);
        final ComparableDrawable oldDrawable = ((ComparableDrawable) (oldView.getBackground()));
        final Component newComponent = Column.create(mContext).backgroundColor(WHITE).child(Text.create(mContext).textSizeSp(12).text("label:")).child(EditText.create(mContext).text("Hello World").textSizeSp(12).backgroundColor(CYAN)).build();
        componentTree.setRootAndSizeSpec(newComponent, makeMeasureSpec(400, MeasureSpec.EXACTLY), makeMeasureSpec(400, MeasureSpec.EXACTLY));
        componentTree.setSizeSpec(makeMeasureSpec(400, MeasureSpec.EXACTLY), makeMeasureSpec(400, MeasureSpec.EXACTLY));
        final View newView = lithoView.getChildAt(0);
        assertThat(newView).isSameAs(oldView);
        final ComparableDrawable newDrawable = ((ComparableDrawable) (newView.getBackground()));
        assertThat(oldDrawable.isEquivalentTo(newDrawable)).isFalse();
    }
}
