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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;

/**
 * The instant xml adapter test.
 *
 * @author Christian Bremer
 */
class InstantXmlAdapterTest {

  /**
   * Convert.
   *
   * @throws Exception the exception
   */
  @Test
  void convert() throws Exception {
    InstantXmlAdapter adapter = new InstantXmlAdapter();

    assertNull(adapter.marshal(null));
    assertNull(adapter.unmarshal(null));

    String expected = "2000-01-16T12:00:00.000Z";
    Instant date = adapter.unmarshal("2000-01-16T12:00:00Z");
    assertNotNull(date);
    String actual = adapter.marshal(date);
    assertEquals(expected, actual);

    adapter = new InstantXmlAdapter(
        TimeZone.getTimeZone("Europe/Berlin").toZoneId(),
        Locale.GERMANY);
    actual = adapter.marshal(date);
    assertEquals("2000-01-16T13:00:00.000+01:00", actual);
  }
}