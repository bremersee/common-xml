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

package org.bremersee.xml.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Month;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;

/**
 * The offset date time xml adapter test.
 *
 * @author Christian Bremer
 */
class OffsetDateTimeXmlAdapterTest {

  /**
   * Convert.
   *
   * @throws Exception the exception
   */
  @Test
  void convert() throws Exception {
    OffsetDateTimeXmlAdapter adapter = new OffsetDateTimeXmlAdapter();

    assertNull(adapter.marshal(null));
    assertNull(adapter.unmarshal(null));

    String expected = "2000-01-16T13:00:00.000+01:00";
    OffsetDateTime date = adapter.unmarshal(expected);
    assertNotNull(date);
    assertEquals(2000, date.getYear());
    assertEquals(Month.JANUARY, date.getMonth());
    assertEquals(16, date.getDayOfMonth());
    assertEquals(13, date.getHour());
    assertEquals(0, date.getMinute());
    assertEquals("+01:00", date.getOffset().getId());

    String actual = adapter.marshal(date);
    assertEquals(expected, actual);
  }
}