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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bremersee.xml.model4.Address;
import org.bremersee.xml.model6.StandaloneModel;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

/**
 * The jaxb context builder details test.
 *
 * @author Christian Bremer
 */
class JaxbContextBuilderDetailsTest {

  /**
   * Empty.
   */
  @Test
  void empty() {
    JaxbContextBuilderDetailsImpl model = new JaxbContextBuilderDetailsImpl();

    assertFalse(model.isBuildWithContextPath());
    assertNull(model.getContextPath());
    assertNull(model.getSchemaLocation());
    assertNotNull(model.getClasses());
    assertEquals(0, model.getClasses().length);
    assertTrue(model.getNameSpacesWithLocation().isEmpty());
    assertTrue(model.getSchemaLocations().isEmpty());

    assertEquals(model, model);
    assertEquals(model, new JaxbContextBuilderDetailsImpl());
    assertEquals(model.hashCode(), new JaxbContextBuilderDetailsImpl().hashCode());
    assertNotEquals(model, null);
    assertNotEquals(model, new Object());

    assertTrue(StringUtils.hasText(model.toString()));
  }

  /**
   * With classes.
   */
  @Test
  void withClasses() {
    JaxbContextBuilderDetailsImpl model = new JaxbContextBuilderDetailsImpl(
        Address.class,
        StandaloneModel.class);

    assertFalse(model.isBuildWithContextPath());
    assertNull(model.getContextPath());
    assertNull(model.getSchemaLocation());
    assertNotNull(model.getClasses());
    assertEquals(2, model.getClasses().length);
    assertTrue(model.getNameSpacesWithLocation().isEmpty());
    assertTrue(model.getSchemaLocations().isEmpty());

    assertEquals(model, model);
    assertEquals(
        model,
        new JaxbContextBuilderDetailsImpl(Address.class, StandaloneModel.class));
    assertEquals(
        model.hashCode(),
        new JaxbContextBuilderDetailsImpl(Address.class, StandaloneModel.class).hashCode());

    assertTrue(StringUtils.hasText(model.toString()));
  }

  /**
   * With map but no packages.
   */
  @Test
  void withMapButNoPackages() {
    Map<String, JaxbContextData> map = new HashMap<>();
    map.put(org.bremersee.xml.model7a.ObjectFactory.class.getPackage().getName(),
        new JaxbContextData(org.bremersee.xml.model7a.ObjectFactory.class.getPackage()));
    map.put(org.bremersee.xml.model7b.ObjectFactory.class.getPackage().getName(),
        new JaxbContextData(org.bremersee.xml.model7b.ObjectFactory.class.getPackage()));
    map.put(org.bremersee.xml.model2.ObjectFactory.class.getPackage().getName(),
        new JaxbContextData(org.bremersee.xml.model2.ObjectFactory.class.getPackage()));

    JaxbContextBuilderDetailsImpl model = new JaxbContextBuilderDetailsImpl(null, map);

    assertTrue(model.isBuildWithContextPath());
    assertEquals(
        "org.bremersee.xml.model2:org.bremersee.xml.model7a:org.bremersee.xml.model7b",
        model.getContextPath());
    assertEquals("http://bremersee.org/xmlschemas/common-xml-test-model-7a "
            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-7a.xsd "
            + "http://bremersee.org/xmlschemas/common-xml-test-model-7b "
            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-7b.xsd",
        model.getSchemaLocation());
    assertNull(model.getClasses());
    assertEquals(2, model.getNameSpacesWithLocation().size());
    assertEquals(2, model.getSchemaLocations().size());

    assertEquals(model, model);
    assertEquals(
        model,
        new JaxbContextBuilderDetailsImpl(null, map));
    assertEquals(
        model.hashCode(),
        new JaxbContextBuilderDetailsImpl(null, map).hashCode());

    assertTrue(model.toString().contains("org.bremersee.xml.model7a"));
  }

  /**
   * With map.
   */
  @Test
  void withMap() {
    Set<String> packages = Collections.singleton("org.bremersee.xml.model7a");

    Map<String, JaxbContextData> map = new HashMap<>();
    map.put(org.bremersee.xml.model7a.ObjectFactory.class.getPackage().getName(),
        new JaxbContextData(org.bremersee.xml.model7a.ObjectFactory.class.getPackage()));
    map.put(org.bremersee.xml.model7b.ObjectFactory.class.getPackage().getName(),
        new JaxbContextData(org.bremersee.xml.model7b.ObjectFactory.class.getPackage()));
    map.put(org.bremersee.xml.model2.ObjectFactory.class.getPackage().getName(),
        new JaxbContextData(org.bremersee.xml.model2.ObjectFactory.class.getPackage()));

    JaxbContextBuilderDetailsImpl model = new JaxbContextBuilderDetailsImpl(packages, map);

    assertTrue(model.isBuildWithContextPath());
    assertEquals(
        "org.bremersee.xml.model7a",
        model.getContextPath());
    assertEquals("http://bremersee.org/xmlschemas/common-xml-test-model-7a "
            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-7a.xsd",
        model.getSchemaLocation());
    assertNull(model.getClasses());
    assertEquals(1, model.getNameSpacesWithLocation().size());
    assertEquals(1, model.getSchemaLocations().size());

    assertEquals(model, model);
    assertEquals(
        model,
        new JaxbContextBuilderDetailsImpl(packages, map));
    assertEquals(
        model.hashCode(),
        new JaxbContextBuilderDetailsImpl(packages, map).hashCode());

    assertTrue(model.toString().contains("org.bremersee.xml.model7a"));
  }

}