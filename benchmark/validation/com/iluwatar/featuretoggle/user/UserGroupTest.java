/**
 * The MIT License
 * Copyright (c) 2014-2016 Ilkka Sepp?l?
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.iluwatar.featuretoggle.user;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;


/**
 * Test User Group specific feature
 */
public class UserGroupTest {
    @Test
    public void testAddUserToFreeGroup() throws Exception {
        User user = new User("Free User");
        UserGroup.addUserToFreeGroup(user);
        Assertions.assertFalse(UserGroup.isPaid(user));
    }

    @Test
    public void testAddUserToPaidGroup() throws Exception {
        User user = new User("Paid User");
        UserGroup.addUserToPaidGroup(user);
        Assertions.assertTrue(UserGroup.isPaid(user));
    }

    @Test
    public void testAddUserToPaidWhenOnFree() throws Exception {
        User user = new User("Paid User");
        UserGroup.addUserToFreeGroup(user);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            UserGroup.addUserToPaidGroup(user);
        });
    }

    @Test
    public void testAddUserToFreeWhenOnPaid() throws Exception {
        User user = new User("Free User");
        UserGroup.addUserToPaidGroup(user);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            UserGroup.addUserToFreeGroup(user);
        });
    }
}
