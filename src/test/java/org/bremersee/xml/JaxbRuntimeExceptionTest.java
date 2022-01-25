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

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The jaxb runtime exception test.
 *
 * @author Christian Bremer
 */
@ExtendWith({SoftAssertionsExtension.class})
class JaxbRuntimeExceptionTest {

  /**
   * Test constructors.
   *
   * @param softly the soft assertions
   */
  @Test
  void testConstructors(SoftAssertions softly) {
    Exception cause = new Exception("Something went wrong");
    softly.assertThat(cause)
        .isEqualTo(new JaxbRuntimeException(cause).getCause());
    softly.assertThat(cause)
        .isEqualTo(new JaxbRuntimeException("Message", cause).getCause());
    softly.assertThat(new JaxbRuntimeException("Message", cause).getMessage())
        .isEqualTo("Message");
    softly.assertThat(new JaxbRuntimeException("Some Text"))
        .extracting(JaxbRuntimeException::getMessage)
        .isEqualTo("Some Text");
  }

}