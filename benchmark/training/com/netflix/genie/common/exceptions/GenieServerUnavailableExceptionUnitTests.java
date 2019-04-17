/**
 * Copyright 2015 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.netflix.genie.common.exceptions;


import com.netflix.genie.test.categories.UnitTest;
import java.io.IOException;
import java.net.HttpURLConnection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;


/**
 * Test the constructors of the GenieException.
 *
 * @author tgianos
 * @since 2.0.0
 */
@Category(UnitTest.class)
public class GenieServerUnavailableExceptionUnitTests extends Exception {
    private static final String ERROR_MESSAGE = "Cannot Run Jobs";

    private static final IOException IOE = new IOException("IOException");

    /**
     * Test the constructor.
     *
     * @throws GenieServerUnavailableException
     * 		When the server is unavailable
     */
    @Test(expected = GenieServerUnavailableException.class)
    public void testTwoArgConstructor() throws GenieServerUnavailableException {
        final GenieServerUnavailableException ge = new GenieServerUnavailableException(GenieServerUnavailableExceptionUnitTests.ERROR_MESSAGE, GenieServerUnavailableExceptionUnitTests.IOE);
        Assert.assertEquals(HttpURLConnection.HTTP_UNAVAILABLE, ge.getErrorCode());
        Assert.assertEquals(GenieServerUnavailableExceptionUnitTests.ERROR_MESSAGE, ge.getMessage());
        Assert.assertEquals(GenieServerUnavailableExceptionUnitTests.IOE, ge.getCause());
        throw ge;
    }

    /**
     * Test the constructor.
     *
     * @throws GenieServerUnavailableException
     * 		When the server is unavailable
     */
    @Test(expected = GenieServerUnavailableException.class)
    public void testMessageArgConstructor() throws GenieServerUnavailableException {
        final GenieServerUnavailableException ge = new GenieServerUnavailableException(GenieServerUnavailableExceptionUnitTests.ERROR_MESSAGE);
        Assert.assertEquals(HttpURLConnection.HTTP_UNAVAILABLE, ge.getErrorCode());
        Assert.assertEquals(GenieServerUnavailableExceptionUnitTests.ERROR_MESSAGE, ge.getMessage());
        Assert.assertNull(ge.getCause());
        throw ge;
    }
}
