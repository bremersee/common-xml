/*
 * Copyright 2020-2022 the original author or authors.
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
import org.bremersee.xml.adapter.DateXmlAdapter;
import org.bremersee.xml.model1.ObjectFactory;
import org.bremersee.xml.model1.Person;
import org.bremersee.xml.model3.Company;
import org.bremersee.xml.model6.StandaloneModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The jaxb utils test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class JaxbUtilsTest {

  /**
   * Gets name space of element.
   *
   * @param softly the soft assertions
   */
  @Test
  void getNameSpaceOfElement(SoftAssertions softly) {
    softly.assertThat(JaxbUtils.getNameSpaceOfElement(StandaloneModel.class))
        .hasValue("http://bremersee.org/xmlschemas/common-xml-test-model-6");
    softly.assertThat(JaxbUtils.getNameSpaceOfElement(Person.class))
        .hasValue("http://bremersee.org/xmlschemas/common-xml-test-model-1");
    softly.assertThat(JaxbUtils.getNameSpaceOfElement(Company.class))
        .isEmpty();
    softly.assertThat(JaxbUtils.getNameSpaceOfElement(DateXmlAdapter.class))
        .isEmpty();
  }

  /**
   * Gets name space of type.
   *
   * @param softly the soft assertions
   */
  @Test
  void getNameSpaceOfType(SoftAssertions softly) {
    softly.assertThat(JaxbUtils.getNameSpaceOfType(StandaloneModel.class))
        .hasValue("http://bremersee.org/xmlschemas/common-xml-test-model-6");
    softly.assertThat(JaxbUtils.getNameSpaceOfType(Person.class))
        .hasValue("http://bremersee.org/xmlschemas/common-xml-test-model-1");
    softly.assertThat(JaxbUtils.getNameSpaceOfType(Company.class))
        .isEmpty();
    softly.assertThat(JaxbUtils.getNameSpaceOfType(DateXmlAdapter.class))
        .isEmpty();
  }

  /**
   * Gets name space.
   *
   * @param softly the soft assertions
   */
  @Test
  void getNameSpace(SoftAssertions softly) {
    softly.assertThat(JaxbUtils.getNameSpace(Person.class.getPackage()))
        .hasValue("http://bremersee.org/xmlschemas/common-xml-test-model-1");
    softly.assertThat(JaxbUtils.getNameSpace(Company.class.getPackage()))
        .isEmpty();
  }

  /**
   * Gets schema location.
   *
   * @param softly the soft assertions
   */
  @Test
  void getSchemaLocation(SoftAssertions softly) {
    softly.assertThat(JaxbUtils.getSchemaLocation(Person.class.getPackage()))
        .hasValue("http://bremersee.github.io/xmlschemas/common-xml-test-model-1.xsd");
    softly.assertThat(JaxbUtils.getSchemaLocation(Company.class.getPackage()))
        .isEmpty();
  }

  /**
   * Is in jaxb model package.
   *
   * @param softly the soft assertions
   */
  @Test
  void isInJaxbModelPackage(SoftAssertions softly) {
    softly.assertThat(JaxbUtils.isInJaxbModelPackage(Person.class))
        .isTrue();
    softly.assertThat(JaxbUtils.isInJaxbModelPackage(StandaloneModel.class))
        .isFalse();
  }

  /**
   * Is jaxb model package.
   *
   * @param softly the soft assertions
   */
  @Test
  void isJaxbModelPackage(SoftAssertions softly) {
    softly.assertThat(JaxbUtils.isJaxbModelPackage(Person.class.getPackage()))
        .isTrue();
    softly.assertThat(JaxbUtils.isJaxbModelPackage(StandaloneModel.class.getPackage()))
        .isFalse();
  }

  /**
   * Find jaxb classes.
   *
   * @param softly the soft assertions
   */
  @Test
  void findJaxbClasses(SoftAssertions softly) {
    softly.assertThat(JaxbUtils.findJaxbClasses(ObjectFactory.class.getPackageName()))
        .containsExactly(Person.class);
    softly.assertThat(JaxbUtils.findJaxbClasses(DateXmlAdapter.class.getPackageName()))
        .isEmpty();
  }
}