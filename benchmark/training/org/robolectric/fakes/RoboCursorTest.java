package org.robolectric.fakes;


import Cursor.FIELD_TYPE_BLOB;
import Cursor.FIELD_TYPE_FLOAT;
import Cursor.FIELD_TYPE_INTEGER;
import Cursor.FIELD_TYPE_NULL;
import Cursor.FIELD_TYPE_STRING;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class RoboCursorTest {
    private static final String STRING_COLUMN = "stringColumn";

    private static final String LONG_COLUMN = "longColumn";

    private static final String INT_COLUMN = "intColumn";

    private static final String BLOB_COLUMN = "blobColumn";

    private static final String SHORT_COLUMN = "shortColumn";

    private static final String FLOAT_COLUMN = "floatColumn";

    private static final String DOUBLE_COLUMN = "doubleColumn";

    private static final String NULL_COLUMN = "nullColumn";

    private final Uri uri = Uri.parse("http://foo");

    private final RoboCursor cursor = new RoboCursor();

    private ContentResolver contentResolver;

    @Test
    public void query_shouldMakeQueryParamsAvailable() throws Exception {
        contentResolver.query(uri, new String[]{ "projection" }, "selection", new String[]{ "selection" }, "sortOrder");
        assertThat(cursor.uri).isEqualTo(uri);
        assertThat(cursor.projection[0]).isEqualTo("projection");
        assertThat(cursor.selection).isEqualTo("selection");
        assertThat(cursor.selectionArgs[0]).isEqualTo("selection");
        assertThat(cursor.sortOrder).isEqualTo("sortOrder");
    }

    @Test
    public void getColumnCount_whenSetColumnNamesHasntBeenCalled_shouldReturnCountFromData() throws Exception {
        RoboCursor cursor = new RoboCursor();
        cursor.setResults(new Object[][]{ new Object[]{ 1, 2, 3 }, new Object[]{ 1, 2 } });
        assertThat(cursor.getColumnCount()).isEqualTo(3);
        cursor.setColumnNames(Arrays.asList("a", "b", "c", "d"));
        assertThat(cursor.getColumnCount()).isEqualTo(4);
    }

    @Test
    public void getColumnName_shouldReturnColumnName() throws Exception {
        assertThat(cursor.getColumnCount()).isEqualTo(8);
        assertThat(cursor.getColumnName(0)).isEqualTo(RoboCursorTest.STRING_COLUMN);
        assertThat(cursor.getColumnName(1)).isEqualTo(RoboCursorTest.LONG_COLUMN);
    }

    @Test
    public void getType_shouldReturnColumnType() throws Exception {
        cursor.setResults(new Object[][]{ new Object[]{ "aString", 1234L, 42, new byte[]{ 1, 2, 3 }, 255, 1.25F, 2.5, null } });
        assertThat(cursor.getCount()).isEqualTo(1);
        assertThat(cursor.getType(indexOf(RoboCursorTest.STRING_COLUMN))).isEqualTo(FIELD_TYPE_STRING);
        assertThat(cursor.getType(indexOf(RoboCursorTest.LONG_COLUMN))).isEqualTo(FIELD_TYPE_INTEGER);
        assertThat(cursor.getType(indexOf(RoboCursorTest.INT_COLUMN))).isEqualTo(FIELD_TYPE_INTEGER);
        assertThat(cursor.getType(indexOf(RoboCursorTest.BLOB_COLUMN))).isEqualTo(FIELD_TYPE_BLOB);
        assertThat(cursor.getType(indexOf(RoboCursorTest.SHORT_COLUMN))).isEqualTo(FIELD_TYPE_INTEGER);
        assertThat(cursor.getType(indexOf(RoboCursorTest.FLOAT_COLUMN))).isEqualTo(FIELD_TYPE_FLOAT);
        assertThat(cursor.getType(indexOf(RoboCursorTest.DOUBLE_COLUMN))).isEqualTo(FIELD_TYPE_FLOAT);
        assertThat(cursor.getType(indexOf(RoboCursorTest.NULL_COLUMN))).isEqualTo(FIELD_TYPE_NULL);
    }

    @Test
    public void get_shouldReturnColumnValue() throws Exception {
        cursor.setResults(new Object[][]{ new Object[]{ "aString", 1234L, 42, new byte[]{ 1, 2, 3 }, 255, 1.25F, 2.5, null } });
        assertThat(cursor.getCount()).isEqualTo(1);
        assertThat(cursor.moveToNext()).isTrue();
        assertThat(cursor.getString(indexOf(RoboCursorTest.STRING_COLUMN))).isEqualTo("aString");
        assertThat(cursor.getLong(indexOf(RoboCursorTest.LONG_COLUMN))).isEqualTo(1234L);
        assertThat(cursor.getInt(indexOf(RoboCursorTest.INT_COLUMN))).isEqualTo(42);
        assertThat(cursor.getBlob(indexOf(RoboCursorTest.BLOB_COLUMN))).asList().containsExactly(((byte) (1)), ((byte) (2)), ((byte) (3)));
        assertThat(cursor.getShort(indexOf(RoboCursorTest.SHORT_COLUMN))).isEqualTo(((short) (255)));
        assertThat(cursor.getFloat(indexOf(RoboCursorTest.FLOAT_COLUMN))).isEqualTo(1.25F);
        assertThat(cursor.getDouble(indexOf(RoboCursorTest.DOUBLE_COLUMN))).isEqualTo(2.5);
        assertThat(cursor.isNull(indexOf(RoboCursorTest.NULL_COLUMN))).isTrue();
    }

    @Test
    public void get_shouldConvert() throws Exception {
        cursor.setResults(new Object[][]{ new Object[]{ "aString", "1234", "42", new byte[]{ 1, 2, 3 }, 255, "1.25", 2.5, null } });
        assertThat(cursor.getCount()).isEqualTo(1);
        assertThat(cursor.moveToNext()).isTrue();
        assertThat(cursor.getString(indexOf(RoboCursorTest.INT_COLUMN))).isEqualTo("42");
        assertThat(cursor.getInt(indexOf(RoboCursorTest.INT_COLUMN))).isEqualTo(42);
        assertThat(cursor.getFloat(indexOf(RoboCursorTest.FLOAT_COLUMN))).isEqualTo(1.25F);
        assertThat(cursor.isNull(indexOf(RoboCursorTest.INT_COLUMN))).isFalse();
        assertThat(cursor.isNull(indexOf(RoboCursorTest.FLOAT_COLUMN))).isFalse();
    }

    @Test
    public void moveToNext_advancesToNextRow() throws Exception {
        cursor.setResults(new Object[][]{ new Object[]{ "aString", 1234L, 41 }, new Object[]{ "anotherString", 5678L, 42 } });
        assertThat(cursor.getCount()).isEqualTo(2);
        assertThat(cursor.getType(0)).isEqualTo(FIELD_TYPE_STRING);
        assertThat(cursor.getType(1)).isEqualTo(FIELD_TYPE_INTEGER);
        assertThat(cursor.moveToNext()).isTrue();
        assertThat(cursor.moveToNext()).isTrue();
        assertThat(cursor.getColumnName(0)).isEqualTo(RoboCursorTest.STRING_COLUMN);
        assertThat(cursor.getColumnName(1)).isEqualTo(RoboCursorTest.LONG_COLUMN);
        assertThat(cursor.getString(indexOf(RoboCursorTest.STRING_COLUMN))).isEqualTo("anotherString");
        assertThat(cursor.getLong(indexOf(RoboCursorTest.LONG_COLUMN))).isEqualTo(5678L);
        assertThat(cursor.getInt(indexOf(RoboCursorTest.INT_COLUMN))).isEqualTo(42);
    }

    @Test
    public void moveToPosition_movesToAppropriateRow() throws Exception {
        cursor.setResults(new Object[][]{ new Object[]{ "aString", 1234L, 41 }, new Object[]{ "anotherString", 5678L, 42 } });
        assertThat(cursor.moveToPosition(1)).isTrue();
        assertThat(cursor.getString(indexOf(RoboCursorTest.STRING_COLUMN))).isEqualTo("anotherString");
        assertThat(cursor.getLong(indexOf(RoboCursorTest.LONG_COLUMN))).isEqualTo(5678L);
        assertThat(cursor.getInt(indexOf(RoboCursorTest.INT_COLUMN))).isEqualTo(42);
        assertThat(cursor.moveToPosition(0)).isTrue();
        assertThat(cursor.getString(indexOf(RoboCursorTest.STRING_COLUMN))).isEqualTo("aString");
        assertThat(cursor.getLong(indexOf(RoboCursorTest.LONG_COLUMN))).isEqualTo(1234L);
        assertThat(cursor.getInt(indexOf(RoboCursorTest.INT_COLUMN))).isEqualTo(41);
    }

    @Test
    public void moveToPosition_checksBounds() {
        cursor.setResults(new Object[][]{ new Object[]{ "aString", 1234L, 41 }, new Object[]{ "anotherString", 5678L, 42 } });
        assertThat(cursor.moveToPosition(2)).isFalse();
        assertThat(cursor.moveToPosition((-1))).isFalse();
    }

    @Test
    public void getCount_shouldReturnNumberOfRows() {
        cursor.setResults(new Object[][]{ new Object[]{ "aString", 1234L, 41 }, new Object[]{ "anotherString", 5678L, 42 } });
        assertThat(cursor.getCount()).isEqualTo(2);
    }

    @Test
    public void close_isRemembered() throws Exception {
        cursor.close();
        assertThat(cursor.getCloseWasCalled()).isTrue();
    }

    @Test
    public void getColumnIndex_shouldReturnColumnIndex() {
        assertThat(indexOf("invalidColumn")).isEqualTo((-1));
        assertThat(indexOf(RoboCursorTest.STRING_COLUMN)).isEqualTo(0);
        assertThat(cursor.getColumnIndexOrThrow(RoboCursorTest.STRING_COLUMN)).isEqualTo(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getColumnIndexOrThrow_shouldThrowException() {
        cursor.getColumnIndexOrThrow("invalidColumn");
    }

    @Test
    public void isBeforeFirst_shouldReturnTrueForPosition() {
        cursor.setResults(new Object[][]{ new Object[]{ "Foo" } });
        assertThat(cursor.isBeforeFirst()).isTrue();
        cursor.moveToPosition(0);
        assertThat(cursor.isBeforeFirst()).isFalse();
    }

    @Test
    public void isAfterLast_shouldReturnTrueForPosition() {
        cursor.setResults(new Object[][]{ new Object[]{ "Foo" } });
        assertThat(cursor.isAfterLast()).isFalse();
        cursor.moveToPosition(1);
        assertThat(cursor.isAfterLast()).isTrue();
    }

    @Test
    public void isFirst_shouldReturnTrueForPosition() {
        cursor.setResults(new Object[][]{ new Object[]{ "Foo" }, new Object[]{ "Bar" } });
        cursor.moveToPosition(0);
        assertThat(cursor.isFirst()).isTrue();
        cursor.moveToPosition(1);
        assertThat(cursor.isFirst()).isFalse();
    }

    @Test
    public void isLast_shouldReturnTrueForPosition() {
        cursor.setResults(new Object[][]{ new Object[]{ "Foo" }, new Object[]{ "Bar" } });
        cursor.moveToPosition(0);
        assertThat(cursor.isLast()).isFalse();
        cursor.moveToPosition(1);
        assertThat(cursor.isLast()).isTrue();
    }

    @Test
    public void getExtras_shouldReturnExtras() {
        Bundle extras = new Bundle();
        extras.putString("Foo", "Bar");
        cursor.setExtras(extras);
        assertThat(cursor.getExtras()).isEqualTo(extras);
        assertThat(cursor.getExtras().getString("Foo")).isEqualTo("Bar");
    }
}
