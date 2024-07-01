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
  }

  /**
   * Xml format.
   *
   * @param softly the soft assertions
   */
  @Test
  void xmlFormat(SoftAssertions softly) {
    softly.assertThat(dateTimeZ.toString()).isEqualTo(xmlCalZ.toXMLFormat());
    softly.assertThat(dateTimeNewYork.toString()).isEqualTo(xmlCalNewYork.toXMLFormat());
  }

  /**
   * Duration test.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void duration(SoftAssertions softly) throws Exception {
    // 2 years, 6 months, 5 days, 12 hours, 35 minutes, 30 seconds and 200 millis
    String expected = "P2Y6M5DT12H35M30.200S";
    Duration xmlDuration = DatatypeFactory.newInstance().newDuration(expected);
    java.time.Duration duration = ConverterUtils.xmlDurationToDuration(xmlDuration);
    xmlDuration = ConverterUtils.durationToXmlDuration(duration);
    softly.assertThat(xmlDuration.toString())
        .isEqualTo(expected);

    // negative
    expected = "-P2Y6M5DT12H35M30.800S";
    xmlDuration = DatatypeFactory.newInstance().newDuration(expected);
    duration = ConverterUtils.xmlDurationToDuration(xmlDuration);
    xmlDuration = ConverterUtils.durationToXmlDuration(duration);
    softly.assertThat(xmlDuration.toString())
        .isEqualTo(expected);

    // 1 day, 2 hours
    expected = "P0Y0M1DT2H0M0.000S";
    xmlDuration = DatatypeFactory.newInstance().newDuration("P1DT2H");
    duration = ConverterUtils.xmlDurationToDuration(xmlDuration);
    xmlDuration = ConverterUtils.durationToXmlDuration(duration);
    softly.assertThat(xmlDuration.toString())
        .isEqualTo(expected);

    // 20 months (the number of months can be more than 12)
    expected = "P1Y8M0DT0H0M0.000S";
    xmlDuration = DatatypeFactory.newInstance().newDuration("P20M");
    duration = ConverterUtils.xmlDurationToDuration(xmlDuration);
    xmlDuration = ConverterUtils.durationToXmlDuration(duration);
    softly.assertThat(xmlDuration.toString())
        .isEqualTo(expected);

    // 20 minutes
    expected = "P0Y0M0DT0H20M0.000S";
    xmlDuration = DatatypeFactory.newInstance().newDuration("PT20M");
    duration = ConverterUtils.xmlDurationToDuration(xmlDuration);
    xmlDuration = ConverterUtils.durationToXmlDuration(duration);
    softly.assertThat(xmlDuration.toString())
        .isEqualTo(expected);

    // 0 years
    expected = "P0Y0M0DT0H0M0.000S";
    xmlDuration = DatatypeFactory.newInstance().newDuration("P0Y");
    duration = ConverterUtils.xmlDurationToDuration(xmlDuration);
    xmlDuration = ConverterUtils.durationToXmlDuration(duration);
    softly.assertThat(xmlDuration.toString())
        .isEqualTo(expected);
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
    softly.assertThat(cal)
        .isNotNull()
        .extracting(XMLGregorianCalendar::toXMLFormat)
        .isEqualTo(xmlCalStrZ);
    softly.assertThat(cal)
        .isNotNull()
        .extracting(XMLGregorianCalendar::toGregorianCalendar)
        .extracting(GregorianCalendar::getTimeInMillis)
        .isEqualTo(millis);

    millis = ConverterUtils.xmlCalendarToMillis(xmlCalNewYork);
    softly.assertThat(millis)
        .isNotNull()
        .isEqualTo(dateTimeNewYork.toInstant().toEpochMilli());

    cal = ConverterUtils.millisToXmlCalendar(millis, TimeZone.getTimeZone("America/New_York"));
    softly.assertThat(cal)
        .isNotNull()
        .extracting(XMLGregorianCalendar::toXMLFormat)
        .isEqualTo(xmlCalStrNewYork);
    softly.assertThat(cal)
        .isNotNull()
        .extracting(XMLGregorianCalendar::toGregorianCalendar)
        .extracting(GregorianCalendar::getTimeInMillis)
        .isEqualTo(millis);

    softly.assertThat(ConverterUtils.xmlCalendarToMillis(null)).isNull();
    softly.assertThat(ConverterUtils.millisToXmlCalendar(null)).isNull();
  }

}