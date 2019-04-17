package com.baeldung.java9.rangedates;


import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Test;


public class DatesCollectionIterationUnitTest {
    private Collection<LocalDate> localDates = datesUntil(LocalDate.now().plus(10L, ChronoUnit.DAYS)).collect(Collectors.toList());

    private Collection<Date> dates = localDates.stream().map(( localDate) -> Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())).collect(Collectors.toList());

    @Test
    public void givenIteratingListOfDatesJava7_WhenStartTodayAndEnding10DaysAhead() {
        DatesCollectionIteration iterateInColleciton = new DatesCollectionIteration();
        Calendar today = Calendar.getInstance();
        Calendar next10Ahead = ((Calendar) (today.clone()));
        next10Ahead.add(Calendar.DATE, 10);
        iterateInColleciton.iteratingRangeOfDatesJava7(createRangeDates(today.getTime(), next10Ahead.getTime()));
    }

    @Test
    public void givenIteratingListOfDatesJava8_WhenStartTodayAndEnd10DaysAhead() {
        DatesCollectionIteration iterateInColleciton = new DatesCollectionIteration();
        iterateInColleciton.iteratingRangeOfDatesJava8(dates);
    }
}
