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

import java.util.stream.Stream;
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
    softly.assertThat(model.getNameSpaces()).isEmpty();
    softly.assertThat(model.getSchemaLocations()).isEmpty();
    softly.assertThat(model.getNameSpacesWithSchemaLocations()).isEmpty();

    softly.assertThat(model)
        .isEqualTo(JaxbContextDetails.empty());
    softly.assertThat(model.hashCode())
        .isEqualTo(JaxbContextDetails.empty().hashCode());

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
    JaxbContextDetails model = Stream.of(data0, data1)
        .collect(JaxbContextDetails.contextDataCollector());

    softly.assertThat(model.isEmpty()).isFalse();
    softly.assertThat(model.getClasses()).isNotEmpty();
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
  }

  /**
   * With classes.
   *
   * @param softly the soft assertions
   */
  @Test
  void withClasses(SoftAssertions softly) {
    JaxbContextDetails model = Stream.of(new JaxbContextData(StandaloneModel.class))
        .collect(JaxbContextDetails.contextDataCollector());
    softly.assertThat(model.getClasses()).containsExactly(StandaloneModel.class);
    softly.assertThat(model.isEmpty()).isFalse();
    softly.assertThat(model.getSchemaLocation()).isEmpty();
    softly.assertThat(model.getNameSpaces()).isEmpty();
    softly.assertThat(model.getSchemaLocations()).isEmpty();
    softly.assertThat(model.getNameSpacesWithSchemaLocations()).isEmpty();
  }

}