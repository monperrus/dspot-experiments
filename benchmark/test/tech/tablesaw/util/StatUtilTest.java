/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.tablesaw.util;


import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.DoubleColumn;


/**
 *
 */
public class StatUtilTest {
    @Test
    public void testSum() {
        Random random = new Random();
        double sum = 0.0F;
        DoubleColumn column = DoubleColumn.create("c1");
        for (int i = 0; i < 100; i++) {
            double f = random.nextDouble();
            column.append(f);
            sum += f;
        }
        Assertions.assertEquals(sum, column.sum(), 0.01F);
    }

    @Test
    public void testMin() {
        Random random = new Random();
        double min = Double.MAX_VALUE;
        DoubleColumn column = DoubleColumn.create("c1");
        for (int i = 0; i < 100; i++) {
            double f = random.nextDouble();
            column.append(f);
            if (min > f) {
                min = f;
            }
        }
        Assertions.assertEquals(min, column.min(), 0.01F);
    }

    @Test
    public void testMax() {
        Random random = new Random();
        double max = Double.MIN_VALUE;
        DoubleColumn column = DoubleColumn.create("c1");
        for (int i = 0; i < 100; i++) {
            double f = random.nextDouble();
            column.append(f);
            if (max < f) {
                max = f;
            }
        }
        Assertions.assertEquals(max, column.max(), 0.01F);
    }
}
