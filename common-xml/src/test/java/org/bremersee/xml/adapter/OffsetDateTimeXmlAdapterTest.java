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

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The offset date time xml adapter test.
 *
 * @author Christian Bremer
 */
@ExtendWith({SoftAssertionsExtension.class})
class OffsetDateTimeXmlAdapterTest {

  /**
   * Marshal.
   *
   * @param softly the soft assertions
   */
  @Test
  void marshal(SoftAssertions softly) {
    OffsetDateTimeXmlAdapter adapter = new OffsetDateTimeXmlAdapter();

    softly.assertThat(adapter.marshal(null))
        .as("OffsetDateTimeXmlAdapter marshal null is null")
        .isNull();

    OffsetDateTime date = OffsetDateTime
        .parse("2000-01-16T12:00:00.000Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    String actual = adapter.marshal(date);
    softly.assertThat(actual)
        .as("InstantXmlAdapter marshal date")
        .isEqualTo("2000-01-16T12:00:00.000Z");

    date = OffsetDateTime
        .parse("2000-01-16T13:00:00.000+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    actual = adapter.marshal(date);
    softly.assertThat(actual)
        .as("OffsetDateTimeXmlAdapter marshal date with time zone")
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
    OffsetDateTimeXmlAdapter adapter = new OffsetDateTimeXmlAdapter();

    softly.assertThat(adapter.unmarshal(null))
        .as("OffsetDateTimeXmlAdapter unmarshal null is null")
        .isNull();

    OffsetDateTime expected = OffsetDateTime
        .parse("2000-01-16T12:00:00.000Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    OffsetDateTime actual = adapter.unmarshal("2000-01-16T12:00:00Z");
    softly.assertThat(actual)
        .as("OffsetDateTimeXmlAdapter unmarshal xml value")
        .isEqualTo(expected);

    expected = OffsetDateTime
        .parse("2000-01-16T13:00:00.000+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    actual = adapter.unmarshal("2000-01-16T13:00:00.000+01:00");
    softly.assertThat(actual)
        .as("OffsetDateTimeXmlAdapter unmarshal xml value with time zone")
        .isEqualTo(expected);
  }
}