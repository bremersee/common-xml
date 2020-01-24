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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bremersee.xml.model1.ObjectFactory;
import org.junit.jupiter.api.Test;

/**
 * The jaxb context data test.
 *
 * @author Christian Bremer
 */
class JaxbContextDataTest {

  /**
   * Gets name space.
   */
  @Test
  void getNameSpace() {
    JaxbContextData model = new JaxbContextData(ObjectFactory.class.getPackage());
    assertEquals(
        "http://bremersee.org/xmlschemas/common-xml-test-model-1",
        model.getNameSpace());

    assertEquals(model, model);
    assertEquals(model, new JaxbContextData(ObjectFactory.class.getPackage()));
    assertEquals(
        model.hashCode(),
        new JaxbContextData(ObjectFactory.class.getPackage()).hashCode());
    assertEquals(
        model.toString(),
        new JaxbContextData(ObjectFactory.class.getPackage()).toString());

    assertNotEquals(model, null);
    assertNotEquals(model, new Object());

    assertTrue(
        model.toString().contains("http://bremersee.org/xmlschemas/common-xml-test-model-1"));
  }

  /**
   * Gets schema location.
   */
  @Test
  void getSchemaLocation() {
    JaxbContextData model = new JaxbContextData(
        org.bremersee.xml.model2.ObjectFactory.class.getPackage(),
        "http://bremersee.github.io/xmlschemas/common-xml-test-model-2.xsd");
    assertEquals(
        "http://bremersee.github.io/xmlschemas/common-xml-test-model-2.xsd",
        model.getSchemaLocation());
  }

  /**
   * Gets package name.
   */
  @Test
  void getPackageName() {
    JaxbContextData model = new JaxbContextData(
        "org.bremersee.xml.model1",
        "http://bremersee.org/xmlschemas/common-xml-test-model-1",
        "http://bremersee.github.io/xmlschemas/common-xml-test-model-1.xsd");
    assertEquals("org.bremersee.xml.model1", model.getPackageName());

    model = new JaxbContextData("org.bremersee.xml.model2");
    assertEquals("org.bremersee.xml.model2", model.getPackageName());
  }
}