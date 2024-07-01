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

package org.bremersee.xml.adapter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The instant xml adapter test.
 *
 * @author Christian Bremer
 */
@ExtendWith({SoftAssertionsExtension.class})
class InstantXmlAdapterTest {

  /**
   * Marshal.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void marshal(SoftAssertions softly) throws Exception {
    InstantXmlAdapter adapter = new InstantXmlAdapter();

    softly.assertThat(adapter.marshal(null))
        .as("InstantXmlAdapter marshal null is null")
        .isNull();

    Instant date = OffsetDateTime
        .parse("2000-01-16T12:00:00.000Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        .toInstant();
    String actual = adapter.marshal(date);
    softly.assertThat(actual)
        .as("InstantXmlAdapter marshal date instant")
        .isEqualTo("2000-01-16T12:00:00.000Z");

    adapter = new InstantXmlAdapter(
        TimeZone.getTimeZone("Europe/Berlin").toZoneId(), Locale.GERMANY);
    actual = adapter.marshal(date);
    softly.assertThat(actual)
        .as("InstantXmlAdapter marshal date instant with time zone")
        .isEqualTo("2000-01-16T13:00:00.000+01:00");
  }

  /**
   * Unmarshal.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void unmarshal(SoftAssertions softly) throws Exception {
    InstantXmlAdapter adapter = new InstantXmlAdapter();

    softly.assertThat(adapter.unmarshal(null))
        .as("InstantXmlAdapter unmarshal null is null")
        .isNull();

    Instant expected = OffsetDateTime
        .parse("2000-01-16T12:00:00.000Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        .toInstant();
    Instant actual = adapter.unmarshal("2000-01-16T12:00:00Z");
    softly.assertThat(actual)
        .as("InstantXmlAdapter unmarshal xml value")
        .isEqualTo(expected);

    actual = adapter.unmarshal("2000-01-16T13:00:00.000+01:00");
    softly.assertThat(actual)
        .as("InstantXmlAdapter unmarshal xml value with time zone")
        .isEqualTo(expected);
  }
}