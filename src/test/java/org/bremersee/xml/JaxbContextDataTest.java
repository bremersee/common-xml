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

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.xml.model1.ObjectFactory;
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
   * Gets name space.
   *
   * @param softly the soft assertions
   */
  @Test
  void getNameSpace(SoftAssertions softly) {
    JaxbContextData model = new JaxbContextData(ObjectFactory.class.getPackage());
    softly.assertThat(model.getNameSpace())
        .isEqualTo("http://bremersee.org/xmlschemas/common-xml-test-model-1");
    softly.assertThat(model)
        .isEqualTo(model);
    softly.assertThat(model)
        .isNotEqualTo(null);
    softly.assertThat(model)
        .isNotEqualTo(new Object());

    softly.assertThat(model)
        .isEqualTo(new JaxbContextData(ObjectFactory.class.getPackage()));
    softly.assertThat(model.hashCode())
        .isEqualTo(new JaxbContextData(ObjectFactory.class.getPackage()).hashCode());
    softly.assertThat(model.toString())
        .isEqualTo(new JaxbContextData(ObjectFactory.class.getPackage()).toString());
    softly.assertThat(model.toString())
        .contains("http://bremersee.org/xmlschemas/common-xml-test-model-1");
  }

  /**
   * Gets schema location.
   */
  @Test
  void getSchemaLocation() {
    JaxbContextData model = new JaxbContextData(
        org.bremersee.xml.model2.ObjectFactory.class.getPackage(),
        "http://bremersee.github.io/xmlschemas/common-xml-test-model-2.xsd");
    assertThat(model.getNameSpace())
        .isEqualTo("http://bremersee.org/xmlschemas/common-xml-test-model-2");
  }

  /**
   * Gets package name.
   *
   * @param softly the soft assertions
   */
  @Test
  void getPackageName(SoftAssertions softly) {
    JaxbContextData model = new JaxbContextData(
        "org.bremersee.xml",
        "http://namespace",
        "http://example.org/namespace.xsd");
    softly.assertThat(model.getPackageName())
        .isEqualTo("org.bremersee.xml");

    model = new JaxbContextData("org.bremersee.xml");
    softly.assertThat(model.getPackageName())
        .isEqualTo("org.bremersee.xml");
  }
}