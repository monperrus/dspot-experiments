/**
 * LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.server;


import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test HTTP server access from multiple threads.
 */
public class HTTPServerLoadTest extends HTTPServerTest {
    // we keep these numbers low so the tests stay fast - increase them for serious testing:
    private static final int REPEAT_COUNT = 1;

    private static final int THREAD_COUNT = 2;

    private final AtomicInteger runningTests = new AtomicInteger();

    @Test
    @Override
    public void testHTTPServer() throws Exception {
        long startTime = System.currentTimeMillis();
        HTTPServerConfig config = new HTTPServerConfig(HTTPTools.getDefaultPort(), true);
        HTTPServer server = new HTTPServer(config);
        Assert.assertFalse(server.isRunning());
        try {
            server.run();
            Assert.assertTrue(server.isRunning());
            doTest();
        } finally {
            server.stop();
            Assert.assertFalse(server.isRunning());
            long runtime = (System.currentTimeMillis()) - startTime;
            System.out.println((((("Running with " + (getThreadCount())) + " threads in ") + runtime) + "ms"));
        }
    }

    private class TestRunnable implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < (getRepeatCount()); i++) {
                runningTests.incrementAndGet();
                try {
                    runTestsV2();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    int count = runningTests.decrementAndGet();
                    // System.out.println("Tests currently running: " + count);
                }
            }
        }
    }
}
