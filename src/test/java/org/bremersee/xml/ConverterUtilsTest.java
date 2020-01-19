/*
 * Copyright 2018-2020 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The converter utilities test.
 *
 * @author Christian Bremer
 */
class ConverterUtilsTest {

  private static String xmlCalStrZ = "2019-03-27T17:41:31.687Z";

  private static String xmlCalStrNewYork = "2019-03-27T13:45:44.157-04:00";

  private static XMLGregorianCalendar xmlCalZ;

  private static XMLGregorianCalendar xmlCalNewYork;

  private static OffsetDateTime dateTimeZ;

  private static OffsetDateTime dateTimeNewYork;

  /**
   * Setup test.
   */
  @BeforeAll
  static void setup() {
    try {
      xmlCalZ = DatatypeFactory.newInstance().newXMLGregorianCalendar(xmlCalStrZ);
      xmlCalNewYork = DatatypeFactory.newInstance().newXMLGregorianCalendar(xmlCalStrNewYork);

    } catch (DatatypeConfigurationException e) {
      throw new RuntimeException("Creating XMLGregorianCalendar failed.", e);
    }
    dateTimeZ = OffsetDateTime.parse(xmlCalStrZ, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    dateTimeNewYork = OffsetDateTime
        .parse(xmlCalStrNewYork, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

    assertEquals(xmlCalZ.toXMLFormat(), dateTimeZ.toString());
    assertEquals(xmlCalNewYork.toXMLFormat(), dateTimeNewYork.toString());
  }

  /**
   * Duration test.
   */
  @Test
  void duration() {
    Duration xmlDuration = ConverterUtils.millisToXmlDuration(1234L);
    assertNotNull(xmlDuration);
    Long millis = ConverterUtils.xmlDurationToMillis(xmlDuration);
    assertNotNull(millis);
    assertEquals(Long.valueOf(1234L), millis);

    xmlDuration = ConverterUtils.durationToXmlDuration(java.time.Duration.ofMillis(987655432L));
    assertNotNull(xmlDuration);
    java.time.Duration duration = ConverterUtils.xmlDurationToDuration(xmlDuration);
    assertNotNull(duration);
    assertEquals(987655432L, duration.toMillis());

    assertNull(ConverterUtils.millisToXmlDuration(null));
    assertNull(ConverterUtils.durationToXmlDuration(null));
    assertNull(ConverterUtils.xmlDurationToDuration(null));
    assertNull(ConverterUtils.xmlDurationToMillis(null));
  }

  /**
   * Calendar test.
   */
  @Test
  void calendar() {
    GregorianCalendar cal = ConverterUtils.xmlCalendarToCalendar(xmlCalZ);
    assertNotNull(cal);
    assertEquals(xmlCalStrZ, ConverterUtils.calendarToXmlCalendar(cal).toXMLFormat());

    cal = ConverterUtils.xmlCalendarToCalendar(xmlCalNewYork);
    assertNotNull(cal);
    assertEquals(xmlCalStrNewYork, ConverterUtils.calendarToXmlCalendar(cal).toXMLFormat());

    assertNull(ConverterUtils.xmlCalendarToCalendar(null));
    assertNull(ConverterUtils.calendarToXmlCalendar(null));
  }

  /**
   * Date time test.
   */
  @Test
  void dateTime() {
    OffsetDateTime dateTime = ConverterUtils.xmlCalendarToOffsetDateTime(xmlCalZ);
    assertNotNull(dateTime);
    assertEquals(
        xmlCalStrZ,
        ConverterUtils.offsetDateTimeToXmlCalendar(dateTime).toXMLFormat());

    dateTime = ConverterUtils.xmlCalendarToOffsetDateTime(xmlCalNewYork);
    assertNotNull(dateTime);
    assertEquals(
        xmlCalStrNewYork,
        ConverterUtils.offsetDateTimeToXmlCalendar(dateTime).toXMLFormat());

    assertNull(ConverterUtils.xmlCalendarToOffsetDateTime(null));
    assertNull(ConverterUtils.offsetDateTimeToXmlCalendar(null));
  }

  /**
   * Date time UTC test.
   */
  @Test
  void dateTimeUtc() {
    OffsetDateTime dateTime = ConverterUtils.xmlCalendarToOffsetDateTimeUtc(xmlCalNewYork);
    assertNotNull(dateTime);
    assertEquals(
        dateTime.toString(),
        ConverterUtils.offsetDateTimeToXmlCalendarUtc(dateTime).toXMLFormat());
    assertTrue(dateTime.toString().endsWith("Z"));

    assertNull(ConverterUtils.xmlCalendarToOffsetDateTimeUtc(null));
    assertNull(ConverterUtils.offsetDateTimeToXmlCalendarUtc(null));
  }

  /**
   * Date test.
   */
  @Test
  void date() {
    Date date = ConverterUtils.xmlCalendarToDate(xmlCalZ);
    assertNotNull(date);
    assertEquals(Date.from(dateTimeZ.toInstant()), date);

    XMLGregorianCalendar cal = ConverterUtils.dateToXmlCalendar(date, TimeZone.getTimeZone("GMT"));
    assertNotNull(cal);
    assertEquals(date, cal.toGregorianCalendar().getTime());
    assertEquals(xmlCalStrZ, cal.toXMLFormat());

    date = ConverterUtils.xmlCalendarToDate(xmlCalNewYork);
    assertNotNull(date);
    assertEquals(Date.from(dateTimeNewYork.toInstant()), date);

    cal = ConverterUtils.dateToXmlCalendar(date, TimeZone.getTimeZone("America/New_York"));
    assertNotNull(cal);
    assertEquals(date, cal.toGregorianCalendar().getTime());
    assertEquals(xmlCalStrNewYork, cal.toXMLFormat());

    assertNull(ConverterUtils.xmlCalendarToDate(null));
    assertNull(ConverterUtils.dateToXmlCalendar(null));
  }

  /**
   * Instant test.
   */
  @Test
  void instant() {
    Instant instant = ConverterUtils.xmlCalendarToInstant(xmlCalZ);
    assertNotNull(instant);
    assertEquals(xmlCalZ.toGregorianCalendar().getTime().toInstant(), instant);

    XMLGregorianCalendar cal = ConverterUtils.instantToXmlCalendar(instant, ZoneOffset.UTC);
    assertNotNull(cal);
    assertEquals(instant, cal.toGregorianCalendar().getTime().toInstant());
    assertEquals(xmlCalStrZ, cal.toXMLFormat());

    instant = ConverterUtils.xmlCalendarToInstant(xmlCalNewYork);
    assertNotNull(instant);
    assertEquals(xmlCalNewYork.toGregorianCalendar().getTime().toInstant(), instant);

    cal = ConverterUtils.instantToXmlCalendar(
        instant, TimeZone.getTimeZone("America/New_York").toZoneId());
    assertNotNull(cal);
    assertEquals(instant, cal.toGregorianCalendar().getTime().toInstant());
    assertEquals(xmlCalStrNewYork, cal.toXMLFormat());

    assertNull(ConverterUtils.xmlCalendarToInstant(null));
    assertNull(ConverterUtils.instantToXmlCalendar(null));
  }

  /**
   * Millis test.
   */
  @Test
  void millis() {
    Long millis = ConverterUtils.xmlCalendarToMillis(xmlCalZ);
    assertNotNull(millis);
    assertEquals(xmlCalZ.toGregorianCalendar().getTime().getTime(), (long) millis);

    XMLGregorianCalendar cal = ConverterUtils.millisToXmlCalendar(
        millis, TimeZone.getTimeZone("GMT"));
    assertNotNull(cal);
    assertEquals((long) millis, cal.toGregorianCalendar().getTime().getTime());
    assertEquals(xmlCalStrZ, cal.toXMLFormat());

    millis = ConverterUtils.xmlCalendarToMillis(xmlCalNewYork);
    assertNotNull(millis);
    assertEquals(Date.from(dateTimeNewYork.toInstant()), new Date(millis));

    cal = ConverterUtils.millisToXmlCalendar(millis, TimeZone.getTimeZone("America/New_York"));
    assertNotNull(cal);
    assertEquals((long) millis, cal.toGregorianCalendar().getTime().getTime());
    assertEquals(xmlCalStrNewYork, cal.toXMLFormat());

    assertNull(ConverterUtils.xmlCalendarToMillis(null));
    assertNull(ConverterUtils.millisToXmlCalendar(null));
  }

}