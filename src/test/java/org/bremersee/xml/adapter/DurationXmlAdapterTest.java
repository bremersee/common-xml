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

import java.time.Duration;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The duration xml adapter test.
 *
 * @author Christian Bremer
 */
@ExtendWith({SoftAssertionsExtension.class})
class DurationXmlAdapterTest {

  /**
   * Marshal.
   *
   * @param softly the soft assertions
   */
  @Test
  void marshal(SoftAssertions softly) {
    DurationXmlAdapter adapter = new DurationXmlAdapter();

    softly.assertThat(adapter.marshal(null))
        .as("DurationXmlAdapter marshal null is null")
        .isNull();

    String expected = "P5Y2M10DT15H0M0.000S";
    Duration duration = Duration.ofSeconds(163782000L);
    String actual = adapter.marshal(duration);
    softly.assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Unmarshal.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void unmarshal(SoftAssertions softly) throws Exception {
    DurationXmlAdapter adapter = new DurationXmlAdapter();

    softly.assertThat(adapter.unmarshal(null))
        .as("DurationXmlAdapter unmarshal null is null")
        .isNull();

    String xmlValue = "P5Y2M10DT15H0M0.000S";
    Duration actual = adapter.unmarshal(xmlValue);
    softly.assertThat(actual)
        .isEqualTo(Duration.ofSeconds(163782000L));
  }
}