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
import org.bremersee.xml.model6.StandaloneModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The jaxb context details test.
 *
 * @author Christian Bremer
 */
@ExtendWith({SoftAssertionsExtension.class})
class JaxbContextDetailsTest {

  /**
   * Empty.
   *
   * @param softly the soft assertions
   */
  @Test
  void empty(SoftAssertions softly) {
    JaxbContextDetails model = JaxbContextDetails.empty();
    softly.assertThat(model.isEmpty()).isTrue();
    softly.assertThat(model.getSchemaLocation()).isEmpty();
    softly.assertThat(model.getClasses()).isEmpty();
    softly.assertThat(model.getContextPath()).isEmpty();
    softly.assertThat(model.getNameSpaces()).isEmpty();
    softly.assertThat(model.getPackageNames()).isEmpty();
    softly.assertThat(model.getSchemaLocations()).isEmpty();
    softly.assertThat(model.getNameSpacesWithSchemaLocations()).isEmpty();

    softly.assertThat(model)
        .isEqualTo(JaxbContextDetails.builder().build());
    softly.assertThat(model.hashCode())
        .isEqualTo(JaxbContextDetails.builder().build().hashCode());

    softly.assertThat(model.toString()).isNotEmpty();
  }

  /**
   * With context paths.
   *
   * @param softly the soft assertions
   */
  @Test
  void withContextPaths(SoftAssertions softly) {
    JaxbContextData data0 = new JaxbContextData(
        org.bremersee.xml.model2.ObjectFactory.class.getPackage(),
        "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd");
    JaxbContextData data1 = new JaxbContextData(
        org.bremersee.xml.model5.ObjectFactory.class.getPackage());
    JaxbContextDetails model = JaxbContextDetails.builder()
        .add(data0)
        .add(data1)
        .build();

    softly.assertThat(model.isEmpty()).isFalse();
    softly.assertThat(model.getClasses()).isEmpty();
    softly.assertThat(model.getContextPath())
        .isEqualTo(org.bremersee.xml.model2.ObjectFactory.class.getPackage().getName()
            + ":" + org.bremersee.xml.model5.ObjectFactory.class.getPackage().getName());
    softly.assertThat(model.getSchemaLocation())
        .isEqualTo("http://bremersee.org/xmlschemas/common-xml-test-model-2 "
            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd "
            + "http://bremersee.org/xmlschemas/common-xml-test-model-5 "
            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-5.xsd");
    softly.assertThat(model.getSchemaLocations())
        .containsExactly(
            "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd",
            "http://bremersee.github.io/xmlschemas/common-xml-test-model-5.xsd");
    softly.assertThat(model.getNameSpaces())
        .containsExactly(
            "http://bremersee.org/xmlschemas/common-xml-test-model-2",
            "http://bremersee.org/xmlschemas/common-xml-test-model-5");
    softly.assertThat(model.getNameSpacesWithSchemaLocations())
        .containsExactly(
            "http://bremersee.org/xmlschemas/common-xml-test-model-2 "
                + "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd",
            "http://bremersee.org/xmlschemas/common-xml-test-model-5"
                + " http://bremersee.github.io/xmlschemas/common-xml-test-model-5.xsd");

    softly.assertThat(model)
        .isEqualTo(JaxbContextDetails.builder().add(data0).add(data1).build());
    softly.assertThat(model.hashCode())
        .isEqualTo(JaxbContextDetails.builder().add(data0).add(data1).build().hashCode());

    softly.assertThat(JaxbContextDetails.builder().add(data0))
        .isEqualTo(JaxbContextDetails.builder().add(data0));
    softly.assertThat(JaxbContextDetails.builder().add(data0).hashCode())
        .isEqualTo(JaxbContextDetails.builder().add(data0).hashCode());
    softly.assertThat(JaxbContextDetails.builder().add(data0).toString())
        .contains("http://bremersee.org/xmlschemas/common-xml-test-model-2");
  }

  /**
   * With classes.
   *
   * @param softly the soft assertions
   */
  @Test
  void withClasses(SoftAssertions softly) {
    JaxbContextDetails model = JaxbContextDetails.builder()
        .add(StandaloneModel.class)
        .build();
    softly.assertThat(model.getClasses()).containsExactly(StandaloneModel.class);
    softly.assertThat(model.isEmpty()).isFalse();
    softly.assertThat(model.getSchemaLocation()).isEmpty();
    softly.assertThat(model.getContextPath()).isEmpty();
    softly.assertThat(model.getNameSpaces()).isEmpty();
    softly.assertThat(model.getPackageNames()).isEmpty();
    softly.assertThat(model.getSchemaLocations()).isEmpty();
    softly.assertThat(model.getNameSpacesWithSchemaLocations()).isEmpty();

    softly.assertThat(model)
        .isEqualTo(JaxbContextDetails.builder().add(StandaloneModel.class).build());
    softly.assertThat(model.hashCode())
        .isEqualTo(JaxbContextDetails.builder().add(StandaloneModel.class).build().hashCode());

    softly.assertThat(model.toString()).contains(StandaloneModel.class.getName());

    softly.assertThat(JaxbContextDetails.builder().add(StandaloneModel.class))
        .isEqualTo(JaxbContextDetails.builder().add(StandaloneModel.class));
    softly.assertThat(JaxbContextDetails.builder().add(StandaloneModel.class).hashCode())
        .isEqualTo(JaxbContextDetails.builder().add(StandaloneModel.class).hashCode());
    softly.assertThat(JaxbContextDetails.builder().add(StandaloneModel.class).toString())
        .contains("StandaloneModel");
  }

}