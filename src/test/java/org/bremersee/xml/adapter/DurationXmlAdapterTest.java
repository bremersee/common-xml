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

import java.time.Duration;
import org.junit.jupiter.api.Test;

/**
 * The duration xml adapter test.
 *
 * @author Christian Bremer
 */
class DurationXmlAdapterTest {

  /**
   * Convert.
   *
   * @throws Exception the exception
   */
  @Test
  void convert() throws Exception {
    DurationXmlAdapter adapter = new DurationXmlAdapter();
    String expected = "P5Y2M10DT15H0M0.000S";
    Duration duration = adapter.unmarshal(expected);
    assertNotNull(duration);
    String actual = adapter.marshal(duration);
    assertEquals(expected, actual);
  }
}