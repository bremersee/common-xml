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

import org.junit.jupiter.api.Test;

/**
 * The xml runtime exception test.
 *
 * @author Christian Bremer
 */
class XmlRuntimeExceptionTest {

  /**
   * Test constructors.
   */
  @Test
  void testConstructors() {
    Exception cause = new Exception("Something went wrong");
    assertEquals(cause, new XmlRuntimeException(cause).getCause());
    assertEquals(cause, new XmlRuntimeException("Message", cause).getCause());
    assertEquals("Message", new XmlRuntimeException("Message", cause).getMessage());
  }

}