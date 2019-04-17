/**
 * The MIT License
 * Copyright (c) 2014-2016 Ilkka Sepp?l?
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.iluwatar.flux.store;


import MenuItem.PRODUCTS;
import com.iluwatar.flux.action.Content;
import com.iluwatar.flux.action.MenuItem;
import com.iluwatar.flux.view.View;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


/**
 * Date: 12/12/15 - 10:18 PM
 *
 * @author Jeroen Meulemeester
 */
public class MenuStoreTest {
    @Test
    public void testOnAction() throws Exception {
        final MenuStore menuStore = new MenuStore();
        final View view = Mockito.mock(View.class);
        menuStore.registerView(view);
        Mockito.verifyZeroInteractions(view);
        // Menu should not react on content action ...
        menuStore.onAction(new com.iluwatar.flux.action.ContentAction(Content.COMPANY));
        Mockito.verifyZeroInteractions(view);
        // ... but it should react on a menu action
        menuStore.onAction(new com.iluwatar.flux.action.MenuAction(MenuItem.PRODUCTS));
        Mockito.verify(view, Mockito.times(1)).storeChanged(ArgumentMatchers.eq(menuStore));
        Mockito.verifyNoMoreInteractions(view);
        Assertions.assertEquals(PRODUCTS, menuStore.getSelected());
    }
}
