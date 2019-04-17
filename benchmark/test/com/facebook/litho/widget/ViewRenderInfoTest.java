/**
 * Copyright 2018-present Facebook, Inc.
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
package com.facebook.litho.widget;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import com.facebook.litho.viewcompat.SimpleViewBinder;
import com.facebook.litho.viewcompat.ViewCreator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;


@RunWith(ComponentsTestRunner.class)
public class ViewRenderInfoTest {
    private static final ViewCreator VIEW_CREATOR_1 = new ViewCreator() {
        @Override
        public View createView(Context c, ViewGroup parent) {
            return Mockito.mock(View.class);
        }
    };

    @Test(expected = UnsupportedOperationException.class)
    public void testThrowWhenUsingIsFullSpan() {
        ViewRenderInfo viewRenderInfo = /* actual value does not matter */
        ViewRenderInfo.create().viewBinder(new SimpleViewBinder()).viewCreator(ViewRenderInfoTest.VIEW_CREATOR_1).isFullSpan(true).build();
    }
}
