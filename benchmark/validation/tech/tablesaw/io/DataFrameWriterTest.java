package tech.tablesaw.io;


import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;


public class DataFrameWriterTest {
    private static final String LINE_END = System.lineSeparator();

    private double[] v1 = new double[]{ 1, 2, 3, 4, 5, Double.NaN };

    private double[] v2 = new double[]{ 1, 2, 3, 4, 5, Double.NaN };

    private Table table = Table.create("t", DoubleColumn.create("v", v1), DoubleColumn.create("v2", v2));

    @Test
    public void csv() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        table.write().csv(baos);
        String output = baos.toString();
        Assertions.assertEquals((((((((((((((("v,v2" + (DataFrameWriterTest.LINE_END)) + "1.0,1.0") + (DataFrameWriterTest.LINE_END)) + "2.0,2.0") + (DataFrameWriterTest.LINE_END)) + "3.0,3.0") + (DataFrameWriterTest.LINE_END)) + "4.0,4.0") + (DataFrameWriterTest.LINE_END)) + "5.0,5.0") + (DataFrameWriterTest.LINE_END)) + ",") + (DataFrameWriterTest.LINE_END)) + ""), output);
    }

    @Test
    public void csv2() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(baos);
        table.write().csv(osw);
        String output = baos.toString();
        Assertions.assertEquals((((((((((((((("v,v2" + (DataFrameWriterTest.LINE_END)) + "1.0,1.0") + (DataFrameWriterTest.LINE_END)) + "2.0,2.0") + (DataFrameWriterTest.LINE_END)) + "3.0,3.0") + (DataFrameWriterTest.LINE_END)) + "4.0,4.0") + (DataFrameWriterTest.LINE_END)) + "5.0,5.0") + (DataFrameWriterTest.LINE_END)) + ",") + (DataFrameWriterTest.LINE_END)) + ""), output);
    }

    @Test
    public void html() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        table.write().html(baos);
        String output = baos.toString();
        Assertions.assertEquals(((((((((((((((((((((("<thead>" + (DataFrameWriterTest.LINE_END)) + "<tr><th>v</th><th>v 2</th></tr>") + (DataFrameWriterTest.LINE_END)) + "</thead>") + (DataFrameWriterTest.LINE_END)) + "<tbody>") + (DataFrameWriterTest.LINE_END)) + "<tr><td>1.0</td><td>1.0</td></tr>") + (DataFrameWriterTest.LINE_END)) + "<tr><td>2.0</td><td>2.0</td></tr>") + (DataFrameWriterTest.LINE_END)) + "<tr><td>3.0</td><td>3.0</td></tr>") + (DataFrameWriterTest.LINE_END)) + "<tr><td>4.0</td><td>4.0</td></tr>") + (DataFrameWriterTest.LINE_END)) + "<tr><td>5.0</td><td>5.0</td></tr>") + (DataFrameWriterTest.LINE_END)) + "<tr><td></td><td></td></tr>") + (DataFrameWriterTest.LINE_END)) + "</tbody>") + (DataFrameWriterTest.LINE_END)), output);
    }
}
