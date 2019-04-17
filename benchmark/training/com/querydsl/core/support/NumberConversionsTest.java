package com.querydsl.core.support;


import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QTuple;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import org.junit.Assert;
import org.junit.Test;


public class NumberConversionsTest {
    public enum Color {

        GREEN,
        BLUE,
        RED,
        YELLOW,
        BLACK,
        WHITE;}

    @Test
    public void name() {
        EnumPath<NumberConversionsTest.Color> color = Expressions.enumPath(NumberConversionsTest.Color.class, "path");
        QTuple qTuple = Projections.tuple(color);
        NumberConversions<Tuple> conversions = new NumberConversions<Tuple>(qTuple);
        Assert.assertEquals(NumberConversionsTest.Color.BLUE, conversions.newInstance("BLUE").get(color));
    }

    @Test
    public void ordinal() {
        EnumPath<NumberConversionsTest.Color> color = Expressions.enumPath(NumberConversionsTest.Color.class, "path");
        QTuple qTuple = Projections.tuple(color);
        NumberConversions<Tuple> conversions = new NumberConversions<Tuple>(qTuple);
        Assert.assertEquals(NumberConversionsTest.Color.RED, conversions.newInstance(2).get(color));
    }

    @Test
    public void safe_number_conversion() {
        StringPath strPath = Expressions.stringPath("strPath");
        NumberPath<Integer> intPath = Expressions.numberPath(Integer.class, "intPath");
        QTuple qTuple = Projections.tuple(strPath, intPath);
        NumberConversions<Tuple> conversions = new NumberConversions<Tuple>(qTuple);
        Assert.assertNotNull(conversions.newInstance(1, 2));
    }

    @Test
    public void number_conversion() {
        StringPath strPath = Expressions.stringPath("strPath");
        NumberPath<Integer> intPath = Expressions.numberPath(Integer.class, "intPath");
        QTuple qTuple = Projections.tuple(strPath, intPath);
        NumberConversions<Tuple> conversions = new NumberConversions<Tuple>(qTuple);
        Tuple tuple = conversions.newInstance("a", Long.valueOf(3));
        Assert.assertEquals("a", tuple.get(strPath));
        Assert.assertEquals(Integer.valueOf(3), tuple.get(intPath));
    }
}
