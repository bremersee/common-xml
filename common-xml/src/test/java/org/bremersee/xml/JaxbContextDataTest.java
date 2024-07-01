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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.xml.adapter.DateXmlAdapter;
import org.bremersee.xml.model1.ObjectFactory;
import org.bremersee.xml.model1.Person;
import org.bremersee.xml.model6.StandaloneModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The jaxb context data test.
 *
 * @author Christian Bremer
 */
@ExtendWith({SoftAssertionsExtension.class})
class JaxbContextDataTest {

  /**
   * With class.
   *
   * @param softly the soft assertions
   */
  @Test
  void withClass(SoftAssertions softly) {
    JaxbContextData actual = new JaxbContextData(
        StandaloneModel.class,
        "http://localhost/standalone.xsd",
        "http://localhost/standalone.xsd");
    softly.assertThat(actual.getJaxbClasses())
        .containsExactly(StandaloneModel.class);
    softly.assertThat(actual.getNameSpacesWithSchemaLocations())
        .containsExactly(new SchemaLocation(
            "http://bremersee.org/xmlschemas/common-xml-test-model-6",
            "http://localhost/standalone.xsd"));
    softly.assertThat(actual.getKey())
        .isEqualTo(StandaloneModel.class);
    softly.assertThat(actual.toString())
        .contains(StandaloneModel.class.getName());
    softly.assertThat(actual.compareTo(new JaxbContextData(StandaloneModel.class)))
        .isEqualTo(0);
    softly.assertThat(actual)
        .isNotEqualTo(new JaxbContextData(StandaloneModel.class, ""));
  }

  /**
   * With class as package.
   *
   * @param softly the soft assertions
   */
  @Test
  void withClassAsPackage(SoftAssertions softly) {
    JaxbContextData actual = new JaxbContextData(
        ObjectFactory.class,
        "http://bremersee.github.io/xmlschemas/common-xml-test-model-1.xsd");
    softly.assertThat(actual.getJaxbClasses())
        .containsExactly(Person.class);
    softly.assertThat(actual.getNameSpacesWithSchemaLocations())
        .containsExactly(new SchemaLocation(
            "http://bremersee.org/xmlschemas/common-xml-test-model-1",
            "http://bremersee.github.io/xmlschemas/common-xml-test-model-1.xsd"));
  }

  /**
   * With illegal class.
   */
  @Test
  void withIllegalClass() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new JaxbContextData(DateXmlAdapter.class));
  }

  /**
   * With illegal class as package.
   */
  @Test
  void withIllegalClassAsPackage() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new JaxbContextData(DateXmlAdapter.class, ""));
  }

  /**
   * With illegal package.
   */
  @Test
  void withIllegalPackage() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new JaxbContextData(DateXmlAdapter.class.getPackage()));
  }

  /**
   * With package.
   *
   * @param softly the soft assertions
   */
  @Test
  void withPackage(SoftAssertions softly) {
    JaxbContextData actual = new JaxbContextData(
        ObjectFactory.class.getPackage(),
        "http://bremersee.github.io/xmlschemas/common-xml-test-model-1.xsd");
    softly.assertThat(actual.getKey())
        .isEqualTo(ObjectFactory.class.getPackage());
    softly.assertThat(actual.getJaxbClasses())
        .containsExactly(Person.class);
    softly.assertThat(actual.getNameSpacesWithSchemaLocations())
        .containsExactly(new SchemaLocation(
            "http://bremersee.org/xmlschemas/common-xml-test-model-1",
            "http://bremersee.github.io/xmlschemas/common-xml-test-model-1.xsd"));
    softly.assertThat(actual)
        .isEqualTo(new JaxbContextData(ObjectFactory.class));
    softly.assertThat(actual)
        .isEqualTo(new JaxbContextData(ObjectFactory.class,
            "http://bremersee.github.io/xmlschemas/common-xml-test-model-1.xsd"));
    softly.assertThat(actual)
        .isEqualTo(new JaxbContextData(ObjectFactory.class.getPackage()));
  }

}