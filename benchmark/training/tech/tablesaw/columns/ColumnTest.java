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
package tech.tablesaw.columns;


import ColumnType.DOUBLE;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;


/**
 * Tests for Column functionality that is common across column types
 */
public class ColumnTest {
    private static final ColumnType[] types = new ColumnType[]{ ColumnType.LOCAL_DATE// date of poll
    , ColumnType.DOUBLE// approval rating (pct)
    , ColumnType.STRING// polling org
     };

    private static final BinaryOperator<Double> sum = ( d1, d2) -> d1 + d2;

    private static final Predicate<Double> isPositiveOrZero = ( d) -> d >= 0;

    private static final Predicate<Double> isNegative = ColumnTest.isPositiveOrZero.negate();

    private static final Function<Double, String> toString = Object::toString;

    private static final Function<Double, Double> negate = ( d) -> -d;

    private static final Function<LocalDateTime, String> toSeason = ( d) -> ColumnTest.getSeason(d.toLocalDate());

    private Table table;

    @Test
    public void testFirst() {
        // test with dates
        DateColumn first = table.dateColumn("date").first(3);
        Assertions.assertEquals(LocalDate.parse("2004-02-04"), first.get(0));
        Assertions.assertEquals(LocalDate.parse("2004-01-21"), first.get(1));
        Assertions.assertEquals(LocalDate.parse("2004-01-07"), first.get(2));
        // test with ints
        DoubleColumn first2 = ((DoubleColumn) (table.numberColumn("approval").first(3)));
        Assertions.assertEquals(53, first2.get(0), 1.0E-4);
        Assertions.assertEquals(53, first2.get(1), 1.0E-4);
        Assertions.assertEquals(58, first2.get(2), 1.0E-4);
        // test with categories
        StringColumn first3 = table.stringColumn("who").first(3);
        Assertions.assertEquals("fox", first3.get(0));
        Assertions.assertEquals("fox", first3.get(1));
        Assertions.assertEquals("fox", first3.get(2));
    }

    @Test
    public void testLast() {
        // test with dates
        DateColumn last = table.dateColumn("date").last(3);
        Assertions.assertEquals(LocalDate.parse("2001-03-27"), last.get(0));
        Assertions.assertEquals(LocalDate.parse("2001-02-27"), last.get(1));
        Assertions.assertEquals(LocalDate.parse("2001-02-09"), last.get(2));
        // test with ints
        DoubleColumn last2 = ((DoubleColumn) (table.numberColumn("approval").last(3)));
        Assertions.assertEquals(52, last2.get(0), 1.0E-4);
        Assertions.assertEquals(53, last2.get(1), 1.0E-4);
        Assertions.assertEquals(57, last2.get(2), 1.0E-4);
        // test with categories
        StringColumn last3 = table.stringColumn("who").last(3);
        Assertions.assertEquals("zogby", last3.get(0));
        Assertions.assertEquals("zogby", last3.get(1));
        Assertions.assertEquals("zogby", last3.get(2));
    }

    @Test
    public void testName() {
        Column<?> c = table.numberColumn("approval");
        Assertions.assertEquals("approval", c.name());
    }

    @Test
    public void testType() {
        Column<?> c = table.numberColumn("approval");
        Assertions.assertEquals(DOUBLE, c.type());
    }

    @Test
    public void testContains() {
        Column<String> c = table.stringColumn("who");
        Assertions.assertTrue(c.contains("fox"));
        Assertions.assertFalse(c.contains("foxes"));
    }

    @Test
    public void testAsList() {
        Column<String> whoColumn = table.stringColumn("who");
        List<String> whos = whoColumn.asList();
        Assertions.assertEquals(whos.size(), whoColumn.size());
    }

    @Test
    public void testMin() {
        double[] d1 = new double[]{ 1, 0, -1 };
        double[] d2 = new double[]{ 2, -4, 3 };
        DoubleColumn dc1 = DoubleColumn.create("t1", d1);
        DoubleColumn dc2 = DoubleColumn.create("t2", d2);
        DoubleColumn dc3 = dc1.min(dc2);
        Assertions.assertTrue(dc3.contains(1.0));
        Assertions.assertTrue(dc3.contains((-4.0)));
        Assertions.assertTrue(dc3.contains((-1.0)));
    }

    @Test
    public void testMax() {
        double[] d1 = new double[]{ 1, 0, -1 };
        double[] d2 = new double[]{ 2, -4, 3 };
        DoubleColumn dc1 = DoubleColumn.create("t1", d1);
        DoubleColumn dc2 = DoubleColumn.create("t2", d2);
        DoubleColumn dc3 = dc1.max(dc2);
        Assertions.assertTrue(dc3.contains(2.0));
        Assertions.assertTrue(dc3.contains(0.0));
        Assertions.assertTrue(dc3.contains(3.0));
    }

    // Functional methods
    @Test
    public void testCountAtLeast() {
        Assertions.assertEquals(2, DoubleColumn.create("t1", new double[]{ 0, 1, 2 }).count(ColumnTest.isPositiveOrZero, 2));
        Assertions.assertEquals(0, DoubleColumn.create("t1", new double[]{ 0, 1, 2 }).count(ColumnTest.isNegative, 2));
    }

    @Test
    public void testCount() {
        Assertions.assertEquals(3, DoubleColumn.create("t1", new double[]{ 0, 1, 2 }).count(ColumnTest.isPositiveOrZero));
        Assertions.assertEquals(0, DoubleColumn.create("t1", new double[]{ 0, 1, 2 }).count(ColumnTest.isNegative));
    }

    @Test
    public void testAllMatch() {
        Assertions.assertTrue(DoubleColumn.create("t1", new double[]{ 0, 1, 2 }).allMatch(ColumnTest.isPositiveOrZero));
        Assertions.assertFalse(DoubleColumn.create("t1", new double[]{ -1, 0, 1 }).allMatch(ColumnTest.isPositiveOrZero));
        Assertions.assertFalse(DoubleColumn.create("t1", new double[]{ 1, 0, -1 }).allMatch(ColumnTest.isPositiveOrZero));
    }

    @Test
    public void testAnyMatch() {
        Assertions.assertTrue(DoubleColumn.create("t1", new double[]{ 0, 1, 2 }).anyMatch(ColumnTest.isPositiveOrZero));
        Assertions.assertTrue(DoubleColumn.create("t1", new double[]{ -1, 0, -1 }).anyMatch(ColumnTest.isPositiveOrZero));
        Assertions.assertFalse(DoubleColumn.create("t1", new double[]{ 0, 1, 2 }).anyMatch(ColumnTest.isNegative));
    }

    @Test
    public void noneMatch() {
        Assertions.assertTrue(DoubleColumn.create("t1", new double[]{ 0, 1, 2 }).noneMatch(ColumnTest.isNegative));
        Assertions.assertFalse(DoubleColumn.create("t1", new double[]{ -1, 0, 1 }).noneMatch(ColumnTest.isNegative));
        Assertions.assertFalse(DoubleColumn.create("t1", new double[]{ 1, 0, -1 }).noneMatch(ColumnTest.isNegative));
    }

    @Test
    public void testFilter() {
        Column<Double> filtered = DoubleColumn.create("t1", new double[]{ -1, 0, 1 }).filter(ColumnTest.isPositiveOrZero);
        assertContentEquals(filtered, 0.0, 1.0);
    }

    @Test
    public void testMapInto() {
        String[] strings = new String[]{ "-1.0", "0.0", "1.0" };
        DoubleColumn doubleColumn = DoubleColumn.create("t1", new double[]{ -1, 0, 1 });
        StringColumn stringColumn1 = ((StringColumn) (doubleColumn.mapInto(ColumnTest.toString, StringColumn.create("T", doubleColumn.size()))));
        assertContentEquals(stringColumn1, strings);
    }

    @Test
    public void testMapIntoSeason() {
        String[] strings = new String[]{ "WINTER", "SPRING", "SUMMER" };
        DateTimeColumn dateColumn = DateTimeColumn.create("Date", new LocalDateTime[]{ LocalDateTime.of(2018, 1, 26, 12, 15), LocalDateTime.of(2018, 5, 31, 10, 38), LocalDateTime.of(2018, 9, 2, 21, 42) });
        StringColumn stringColumn1 = ((StringColumn) (dateColumn.mapInto(ColumnTest.toSeason, StringColumn.create("Season", dateColumn.size()))));
        assertContentEquals(stringColumn1, strings);
    }

    @Test
    public void testMap() {
        assertContentEquals(DoubleColumn.create("t1", new double[]{ -1, 0, 1 }).map(ColumnTest.negate), 1.0, (-0.0), (-1.0));
    }

    @Test
    public void testMaxComparator() {
        Assertions.assertEquals(Double.valueOf(1.0), DoubleColumn.create("t1", new double[]{ -1, 0, 1 }).max(Double::compare).get());
        Assertions.assertFalse(DoubleColumn.create("t1").max(( d1, d2) -> ((int) (d1 - d2))).isPresent());
    }

    @Test
    public void testMinComparator() {
        Assertions.assertEquals(Double.valueOf((-1.0)), DoubleColumn.create("t1", new double[]{ -1, 0, 1 }).min(Double::compare).get());
        Assertions.assertFalse(DoubleColumn.create("t1").min(( d1, d2) -> ((int) (d1 - d2))).isPresent());
    }

    @Test
    public void testReduceTBinaryOperator() {
        Assertions.assertEquals(Double.valueOf(1.0), DoubleColumn.create("t1", new double[]{ -1, 0, 1 }).reduce(1.0, ColumnTest.sum));
    }

    @Test
    public void testReduceBinaryOperator() {
        Assertions.assertEquals(Double.valueOf(0.0), DoubleColumn.create("t1", new double[]{ -1, 0, 1 }).reduce(ColumnTest.sum).get());
        Assertions.assertFalse(DoubleColumn.create("t1", new double[]{  }).reduce(ColumnTest.sum).isPresent());
    }

    @Test
    public void sorted() {
        assertContentEquals(DoubleColumn.create("t1", new double[]{ 1, -1, 0 }).sorted(Double::compare), (-1.0), 0.0, 1.0);
    }
}
