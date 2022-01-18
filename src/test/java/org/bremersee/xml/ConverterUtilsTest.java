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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The converter utilities test.
 *
 * @author Christian Bremer
 */
@ExtendWith({SoftAssertionsExtension.class})
class ConverterUtilsTest {

  private static final String xmlCalStrZ = "2019-03-27T17:41:31.687Z";

  private static final String xmlCalStrNewYork = "2019-03-27T13:45:44.157-04:00";

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
   *
   * @param softly the soft assertions
   */
  @Test
  void duration(SoftAssertions softly) {
    Duration xmlDuration = ConverterUtils.millisToXmlDuration(1234L);
    assertNotNull(xmlDuration);
    Long millis = ConverterUtils.xmlDurationToMillis(xmlDuration);
    softly.assertThat(millis)
        .isEqualTo(Long.valueOf(1234L));

    xmlDuration = ConverterUtils.durationToXmlDuration(java.time.Duration.ofMillis(987655432L));
    assertNotNull(xmlDuration);
    java.time.Duration duration = ConverterUtils.xmlDurationToDuration(xmlDuration);
    softly.assertThat(duration.toMillis())
        .isEqualTo(987655432L);

    softly.assertThat(ConverterUtils.millisToXmlDuration(null)).isNull();
    softly.assertThat(ConverterUtils.durationToXmlDuration(null)).isNull();
    softly.assertThat(ConverterUtils.xmlDurationToDuration(null)).isNull();
    softly.assertThat(ConverterUtils.xmlDurationToMillis(null)).isNull();
  }

  /**
   * Calendar test.
   *
   * @param softly the soft assertions
   */
  @Test
  void calendar(SoftAssertions softly) {
    GregorianCalendar cal = ConverterUtils.xmlCalendarToCalendar(xmlCalZ);
    softly.assertThat(cal).isNotNull();
    softly.assertThat(ConverterUtils.calendarToXmlCalendar(cal).toXMLFormat())
        .isEqualTo(xmlCalStrZ);

    cal = ConverterUtils.xmlCalendarToCalendar(xmlCalNewYork);
    softly.assertThat(cal).isNotNull();
    softly.assertThat(ConverterUtils.calendarToXmlCalendar(cal).toXMLFormat())
        .isEqualTo(xmlCalStrNewYork);

    softly.assertThat(ConverterUtils.xmlCalendarToCalendar(null)).isNull();
    softly.assertThat(ConverterUtils.calendarToXmlCalendar(null)).isNull();
  }

  /**
   * Date time test.
   *
   * @param softly the soft assertions
   */
  @Test
  void dateTime(SoftAssertions softly) {
    OffsetDateTime dateTime = ConverterUtils.xmlCalendarToOffsetDateTime(xmlCalZ);
    softly.assertThat(dateTime)
        .isNotNull();
    softly.assertThat(ConverterUtils.offsetDateTimeToXmlCalendar(dateTime).toXMLFormat())
        .isEqualTo(xmlCalStrZ);

    dateTime = ConverterUtils.xmlCalendarToOffsetDateTime(xmlCalNewYork);
    softly.assertThat(dateTime)
        .isNotNull();
    softly.assertThat(ConverterUtils.offsetDateTimeToXmlCalendar(dateTime).toXMLFormat())
        .isEqualTo(xmlCalStrNewYork);

    softly.assertThat(ConverterUtils.xmlCalendarToOffsetDateTime(null)).isNull();
    softly.assertThat(ConverterUtils.offsetDateTimeToXmlCalendar(null)).isNull();
  }

  /**
   * Date time UTC test.
   *
   * @param softly the soft assertions
   */
  @Test
  void dateTimeUtc(SoftAssertions softly) {
    OffsetDateTime dateTime = ConverterUtils.xmlCalendarToOffsetDateTimeUtc(xmlCalNewYork);
    softly.assertThat(dateTime)
        .isNotNull();
    softly.assertThat(ConverterUtils.offsetDateTimeToXmlCalendarUtc(dateTime).toXMLFormat())
        .isEqualTo(dateTime.toString());
    softly.assertThat(dateTime.toString().endsWith("Z"))
        .isTrue();

    softly.assertThat(ConverterUtils.xmlCalendarToOffsetDateTimeUtc(null)).isNull();
    softly.assertThat(ConverterUtils.offsetDateTimeToXmlCalendarUtc(null)).isNull();
  }

  /**
   * Date test.
   *
   * @param softly the soft assertions
   */
  @Test
  void date(SoftAssertions softly) {
    Date date = ConverterUtils.xmlCalendarToDate(xmlCalZ);
    softly.assertThat(date)
        .isNotNull();
    softly.assertThat(date)
        .isEqualTo(Date.from(dateTimeZ.toInstant()));

    XMLGregorianCalendar cal = ConverterUtils.dateToXmlCalendar(date, TimeZone.getTimeZone("GMT"));
    softly.assertThat(cal)
        .isNotNull();
    softly.assertThat(cal.toGregorianCalendar().getTime())
        .isEqualTo(date);
    softly.assertThat(cal.toXMLFormat())
        .isEqualTo(xmlCalStrZ);

    date = ConverterUtils.xmlCalendarToDate(xmlCalNewYork);
    softly.assertThat(date)
        .isEqualTo(Date.from(dateTimeNewYork.toInstant()));

    cal = ConverterUtils.dateToXmlCalendar(date, TimeZone.getTimeZone("America/New_York"));
    softly.assertThat(cal)
        .isNotNull();
    softly.assertThat(cal.toGregorianCalendar().getTime())
        .isEqualTo(date);
    softly.assertThat(cal.toXMLFormat())
        .isEqualTo(xmlCalStrNewYork);

    softly.assertThat(ConverterUtils.xmlCalendarToDate(null)).isNull();
    softly.assertThat(ConverterUtils.dateToXmlCalendar(null)).isNull();
  }

  /**
   * Instant test.
   *
   * @param softly the soft assertions
   */
  @Test
  void instant(SoftAssertions softly) {
    Instant instant = ConverterUtils.xmlCalendarToInstant(xmlCalZ);
    softly.assertThat(instant)
        .isNotNull();
    softly.assertThat(instant)
        .isEqualTo(xmlCalZ.toGregorianCalendar().getTime().toInstant());

    XMLGregorianCalendar cal = ConverterUtils.instantToXmlCalendar(instant, ZoneOffset.UTC);
    softly.assertThat(cal)
        .isNotNull();
    softly.assertThat(cal.toGregorianCalendar().getTime().toInstant())
        .isEqualTo(instant);
    softly.assertThat(cal.toXMLFormat())
        .isEqualTo(xmlCalStrZ);

    instant = ConverterUtils.xmlCalendarToInstant(xmlCalNewYork);
    softly.assertThat(instant)
        .isEqualTo(xmlCalNewYork.toGregorianCalendar().getTime().toInstant());

    cal = ConverterUtils.instantToXmlCalendar(
        instant, TimeZone.getTimeZone("America/New_York").toZoneId());
    softly.assertThat(cal)
        .isNotNull();
    softly.assertThat(cal.toGregorianCalendar().getTime().toInstant())
        .isEqualTo(instant);
    softly.assertThat(cal.toXMLFormat())
        .isEqualTo(xmlCalStrNewYork);

    softly.assertThat(ConverterUtils.xmlCalendarToInstant(null)).isNull();
    softly.assertThat(ConverterUtils.instantToXmlCalendar(null)).isNull();
  }

  /**
   * Millis test.
   *
   * @param softly the soft assertions
   */
  @Test
  void millis(SoftAssertions softly) {
    Long millis = ConverterUtils.xmlCalendarToMillis(xmlCalZ);
    softly.assertThat(millis)
        .isNotNull();
    softly.assertThat(millis)
        .isEqualTo(xmlCalZ.toGregorianCalendar().getTime().getTime());

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

    softly.assertThat(ConverterUtils.xmlCalendarToMillis(null)).isNull();
    softly.assertThat(ConverterUtils.millisToXmlCalendar(null)).isNull();
  }

}