/*
 * Copyright 2020-2022  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.xml;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * The converter utilities.
 *
 * @author Christian Bremer
 */
public abstract class ConverterUtils {

  private ConverterUtils() {
  }

  /**
   * Xml duration to duration.
   *
   * @param xmlDuration the xml duration
   * @return the duration
   */
  public static Duration xmlDurationToDuration(javax.xml.datatype.Duration xmlDuration) {
    if (xmlDuration == null) {
      return null;
    }

    GregorianCalendar cal = new GregorianCalendar();
    cal.set(Calendar.MILLISECOND, 0);
    long millis = BigDecimal.valueOf(xmlDuration.getTimeInMillis(cal))
        .abs(MathContext.DECIMAL128)
        .remainder(BigDecimal.valueOf(1000L), MathContext.DECIMAL128)
        .setScale(0, RoundingMode.HALF_UP)
        .longValue();
    LocalDateTime start = LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0, 0, 0);
    LocalDateTime end = start.plusYears(xmlDuration.getYears())
        .plusMonths(xmlDuration.getMonths())
        .plusDays(xmlDuration.getDays())
        .plusHours(xmlDuration.getHours())
        .plusMinutes(xmlDuration.getMinutes())
        .plusSeconds(xmlDuration.getSeconds())
        .plus(millis, ChronoUnit.MILLIS);
    return xmlDuration.getSign() < 0
        ? Duration.between(end, start)
        : Duration.between(start, end);
  }

  /**
   * Duration to xml duration.
   *
   * @param duration the duration
   * @return the javax . xml . datatype . duration
   */
  public static javax.xml.datatype.Duration durationToXmlDuration(Duration duration) {
    if (duration == null) {
      return null;
    }
    LocalDateTime start = LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0, 0, 0);
    LocalDateTime end = start.plus(duration.abs());
    long years = Math.abs(start.until(end, ChronoUnit.YEARS));
    end = end.minusYears(years);
    long months = Math.abs(start.until(end, ChronoUnit.MONTHS));
    end = end.minusMonths(months);
    long days = Math.abs(start.until(end, ChronoUnit.DAYS));
    end = end.minusDays(days);
    long hours = Math.abs(start.until(end, ChronoUnit.HOURS));
    end = end.minusHours(hours);
    long minutes = Math.abs(start.until(end, ChronoUnit.MINUTES));
    end = end.minusMinutes(minutes);
    long seconds = Math.abs(start.until(end, ChronoUnit.SECONDS));
    end = end.minusSeconds(seconds);
    long millis = Math.abs(start.until(end, ChronoUnit.MILLIS));
    try {
      return DatatypeFactory.newInstance().newDuration(
          !duration.isNegative(),
          BigInteger.valueOf(years),
          BigInteger.valueOf(months),
          BigInteger.valueOf(days),
          BigInteger.valueOf(hours),
          BigInteger.valueOf(minutes),
          BigDecimal.valueOf(seconds)
              .add(
                  BigDecimal.valueOf(millis)
                      .divide(BigDecimal.valueOf(1000L), MathContext.DECIMAL128),
                  MathContext.DECIMAL128)
              .setScale(3, RoundingMode.HALF_UP));

    } catch (DatatypeConfigurationException e) {
      throw new UnsupportedOperationException("Creating xml duration failed.", e);
    }
  }

  /**
   * Xml calendar to gregorian calendar.
   *
   * @param xmlGregorianCalendar the xml gregorian calendar
   * @return the gregorian calendar
   */
  public static GregorianCalendar xmlCalendarToCalendar(XMLGregorianCalendar xmlGregorianCalendar) {
    if (xmlGregorianCalendar == null) {
      return null;
    }
    return xmlGregorianCalendar.toGregorianCalendar();
  }

  /**
   * Xml calendar to date.
   *
   * @param xmlGregorianCalendar the xml gregorian calendar
   * @return the date
   */
  public static Date xmlCalendarToDate(XMLGregorianCalendar xmlGregorianCalendar) {
    if (xmlGregorianCalendar == null) {
      return null;
    }
    return xmlGregorianCalendar.toGregorianCalendar().getTime();
  }

  /**
   * Xml calendar to millis.
   *
   * @param xmlGregorianCalendar the xml gregorian calendar
   * @return the long
   */
  public static Long xmlCalendarToMillis(XMLGregorianCalendar xmlGregorianCalendar) {
    if (xmlGregorianCalendar == null) {
      return null;
    }
    return xmlCalendarToDate(xmlGregorianCalendar).getTime();
  }

  /**
   * Xml calendar to instant.
   *
   * @param xmlGregorianCalendar the xml gregorian calendar
   * @return the instant
   */
  public static Instant xmlCalendarToInstant(XMLGregorianCalendar xmlGregorianCalendar) {
    if (xmlGregorianCalendar == null) {
      return null;
    }
    return xmlCalendarToDate(xmlGregorianCalendar).toInstant();
  }

  /**
   * Xml calendar to offset date time.
   *
   * @param xmlGregorianCalendar the xml gregorian calendar
   * @return the offset date time
   */
  public static OffsetDateTime xmlCalendarToOffsetDateTime(
      XMLGregorianCalendar xmlGregorianCalendar) {
    if (xmlGregorianCalendar == null) {
      return null;
    }
    TimeZone tz = xmlGregorianCalendar.getTimeZone(0);
    return OffsetDateTime.ofInstant(xmlCalendarToInstant(xmlGregorianCalendar), tz.toZoneId());
  }

  /**
   * Xml calendar to offset date time utc.
   *
   * @param xmlGregorianCalendar the xml gregorian calendar
   * @return the offset date time
   */
  public static OffsetDateTime xmlCalendarToOffsetDateTimeUtc(
      XMLGregorianCalendar xmlGregorianCalendar) {
    if (xmlGregorianCalendar == null) {
      return null;
    }
    return OffsetDateTime.ofInstant(xmlCalendarToInstant(xmlGregorianCalendar), ZoneOffset.UTC);
  }


  /**
   * Calendar to xml calendar.
   *
   * @param calendar the calendar
   * @return the xml gregorian calendar
   */
  public static XMLGregorianCalendar calendarToXmlCalendar(GregorianCalendar calendar) {
    if (calendar == null) {
      return null;
    }
    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);

    } catch (DatatypeConfigurationException e) {
      throw new UnsupportedOperationException("Creating XMLGregorianCalendar failed.", e);
    }
  }

  /**
   * Date to xml calendar.
   *
   * @param date the date
   * @return the xml gregorian calendar
   */
  public static XMLGregorianCalendar dateToXmlCalendar(Date date) {
    return dateToXmlCalendar(date, null);
  }

  /**
   * Date to xml calendar.
   *
   * @param date the date
   * @param zone the zone
   * @return the xml gregorian calendar
   */
  public static XMLGregorianCalendar dateToXmlCalendar(Date date, TimeZone zone) {
    return dateToXmlCalendar(date, zone, null);
  }

  /**
   * Date to xml calendar.
   *
   * @param date the date
   * @param zone the zone
   * @param locale the locale
   * @return the xml gregorian calendar
   */
  public static XMLGregorianCalendar dateToXmlCalendar(Date date, TimeZone zone, Locale locale) {
    if (date == null) {
      return null;
    }
    TimeZone tz = zone != null ? zone : TimeZone.getTimeZone("GMT");
    GregorianCalendar calendar;
    if (locale != null) {
      calendar = new GregorianCalendar(tz, locale);
    } else {
      calendar = new GregorianCalendar(tz);
    }
    calendar.setTime(date);
    return calendarToXmlCalendar(calendar);
  }

  /**
   * Millis to xml calendar.
   *
   * @param millis the millis
   * @return the xml gregorian calendar
   */
  public static XMLGregorianCalendar millisToXmlCalendar(Long millis) {
    return millisToXmlCalendar(millis, null);
  }

  /**
   * Millis to xml calendar.
   *
   * @param millis the millis
   * @param zone the zone
   * @return the xml gregorian calendar
   */
  public static XMLGregorianCalendar millisToXmlCalendar(Long millis, TimeZone zone) {
    return millisToXmlCalendar(millis, zone, null);
  }

  /**
   * Millis to xml calendar.
   *
   * @param millis the millis
   * @param zone the zone
   * @param locale the locale
   * @return the xml gregorian calendar
   */
  public static XMLGregorianCalendar millisToXmlCalendar(
      Long millis,
      TimeZone zone,
      Locale locale) {

    if (millis == null) {
      return null;
    }
    return dateToXmlCalendar(new Date(millis), zone, locale);
  }


  /**
   * Instant to xml calendar.
   *
   * @param instant the instant
   * @return the xml gregorian calendar
   */
  public static XMLGregorianCalendar instantToXmlCalendar(Instant instant) {
    return instantToXmlCalendar(instant, null);
  }

  /**
   * Instant to xml calendar.
   *
   * @param instant the instant
   * @param zoneId the zone id
   * @return the xml gregorian calendar
   */
  public static XMLGregorianCalendar instantToXmlCalendar(Instant instant, ZoneId zoneId) {
    return instantToXmlCalendar(instant, zoneId, null);
  }

  /**
   * Instant to xml calendar.
   *
   * @param instant the instant
   * @param zoneId the zone id
   * @param locale the locale
   * @return the xml gregorian calendar
   */
  public static XMLGregorianCalendar instantToXmlCalendar(
      Instant instant,
      ZoneId zoneId,
      Locale locale) {

    if (instant == null) {
      return null;
    }
    TimeZone tz = zoneId != null ? TimeZone.getTimeZone(zoneId) : TimeZone.getTimeZone("GMT");
    return dateToXmlCalendar(Date.from(instant), tz, locale);
  }

  /**
   * Offset date time to xml calendar.
   *
   * @param dateTime the date time
   * @return the xml gregorian calendar
   */
  public static XMLGregorianCalendar offsetDateTimeToXmlCalendar(OffsetDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return instantToXmlCalendar(dateTime.toInstant(), dateTime.getOffset());
  }

  /**
   * Offset date time to xml calendar utc.
   *
   * @param dateTime the date time
   * @return the xml gregorian calendar
   */
  public static XMLGregorianCalendar offsetDateTimeToXmlCalendarUtc(OffsetDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return instantToXmlCalendar(dateTime.toInstant(), ZoneOffset.UTC);
  }
}
