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

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
   * Xml duration to millis.
   *
   * @param xmlDuration the xml duration
   * @return the long
   */
  public static Long xmlDurationToMillis(javax.xml.datatype.Duration xmlDuration) {
    if (xmlDuration == null) {
      return null;
    }
    Date tmp = new Date(0L);
    xmlDuration.addTo(tmp);
    return tmp.getTime();
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
    return Duration.ofMillis(xmlDurationToMillis(xmlDuration));
  }

  /**
   * Millis to xml duration.
   *
   * @param millis the millis
   * @return the javax . xml . datatype . duration
   */
  public static javax.xml.datatype.Duration millisToXmlDuration(Long millis) {
    if (millis == null || millis < 0L) {
      return null;
    }
    try {
      return DatatypeFactory.newInstance().newDuration(millis);

    } catch (DatatypeConfigurationException e) {
      throw new UnsupportedOperationException("Creating xml duration failed.", e);
    }
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
    return millisToXmlDuration(duration.toMillis());
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
