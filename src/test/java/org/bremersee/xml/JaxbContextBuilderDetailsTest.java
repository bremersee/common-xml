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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.xml.model4.Address;
import org.bremersee.xml.model6.StandaloneModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The jaxb context builder details test.
 *
 * @author Christian Bremer
 */
@ExtendWith({SoftAssertionsExtension.class})
class JaxbContextBuilderDetailsTest {

  /**
   * Empty.
   *
   * @param softly the soft assertions
   */
  @Test
  void empty(SoftAssertions softly) {
    JaxbContextBuilderDetailsImpl model = new JaxbContextBuilderDetailsImpl();

    softly.assertThat(model.isBuildWithContextPath()).isFalse();
    softly.assertThat(model.getContextPath()).isNull();
    softly.assertThat(model.getSchemaLocation()).isNull();
    softly.assertThat(model.getClasses()).isEmpty();
    softly.assertThat(model.getNameSpacesWithLocation()).isEmpty();
    softly.assertThat(model.getSchemaLocations()).isEmpty();

    softly.assertThat(model).isEqualTo(new JaxbContextBuilderDetailsImpl());
    softly.assertThat(model.hashCode()).isEqualTo(new JaxbContextBuilderDetailsImpl().hashCode());

    softly.assertThat(model.toString()).isNotEmpty();
  }

  /**
   * With classes.
   *
   * @param softly the soft assertions
   */
  @Test
  void withClasses(SoftAssertions softly) {
    JaxbContextBuilderDetailsImpl model = new JaxbContextBuilderDetailsImpl(
        Address.class,
        StandaloneModel.class);

    softly.assertThat(model.isBuildWithContextPath()).isFalse();
    softly.assertThat(model.getContextPath()).isNull();
    softly.assertThat(model.getSchemaLocation()).isNull();
    softly.assertThat(model.getClasses()).hasSize(2);
    softly.assertThat(model.getNameSpacesWithLocation()).isEmpty();
    softly.assertThat(model.getSchemaLocations()).isEmpty();

    softly.assertThat(model)
        .isEqualTo(new JaxbContextBuilderDetailsImpl(Address.class, StandaloneModel.class));
    softly.assertThat(model.hashCode())
        .isEqualTo(new JaxbContextBuilderDetailsImpl(Address.class, StandaloneModel.class)
            .hashCode());
    softly.assertThat(model)
        .isNotEqualTo(null);
    softly.assertThat(model)
        .isNotEqualTo(new Object());

    softly.assertThat(model.toString()).isNotEmpty();
  }

  /**
   * With map but no packages.
   *
   * @param softly the soft assertions
   */
  @Test
  void withMapButNoPackages(SoftAssertions softly) {
    Map<String, JaxbContextData> map = new HashMap<>();
    map.put(org.bremersee.xml.model7a.ObjectFactory.class.getPackage().getName(),
        new JaxbContextData(org.bremersee.xml.model7a.ObjectFactory.class.getPackage()));
    map.put(org.bremersee.xml.model7b.ObjectFactory.class.getPackage().getName(),
        new JaxbContextData(org.bremersee.xml.model7b.ObjectFactory.class.getPackage()));
    map.put(org.bremersee.xml.model2.ObjectFactory.class.getPackage().getName(),
        new JaxbContextData(org.bremersee.xml.model2.ObjectFactory.class.getPackage()));

    JaxbContextBuilderDetailsImpl model = new JaxbContextBuilderDetailsImpl(null, map);

    softly.assertThat(model.isBuildWithContextPath()).isTrue();
    softly.assertThat(model.getContextPath())
        .isEqualTo("org.bremersee.xml.model2:org.bremersee.xml.model7a:org.bremersee.xml.model7b");
    softly.assertThat(model.getSchemaLocation())
        .isEqualTo("http://bremersee.org/xmlschemas/common-xml-test-model-7a "
            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-7a.xsd "
            + "http://bremersee.org/xmlschemas/common-xml-test-model-7b "
            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-7b.xsd");
    softly.assertThat(model.getClasses())
        .isNull();
    softly.assertThat(model.getNameSpacesWithLocation())
        .hasSize(2);
    softly.assertThat(model.getSchemaLocations())
        .hasSize(2);

    softly.assertThat(model)
        .isEqualTo(new JaxbContextBuilderDetailsImpl(null, map));
    softly.assertThat(model.hashCode())
        .isEqualTo(new JaxbContextBuilderDetailsImpl(null, map).hashCode());

    softly.assertThat(model.toString())
        .contains("org.bremersee.xml.model7a");
  }

  /**
   * With map.
   *
   * @param softly the soft assertions
   */
  @Test
  void withMap(SoftAssertions softly) {
    Set<String> packages = Collections.singleton("org.bremersee.xml.model7a");

    Map<String, JaxbContextData> map = new HashMap<>();
    map.put(org.bremersee.xml.model7a.ObjectFactory.class.getPackage().getName(),
        new JaxbContextData(org.bremersee.xml.model7a.ObjectFactory.class.getPackage()));
    map.put(org.bremersee.xml.model7b.ObjectFactory.class.getPackage().getName(),
        new JaxbContextData(org.bremersee.xml.model7b.ObjectFactory.class.getPackage()));
    map.put(org.bremersee.xml.model2.ObjectFactory.class.getPackage().getName(),
        new JaxbContextData(org.bremersee.xml.model2.ObjectFactory.class.getPackage()));

    JaxbContextBuilderDetailsImpl model = new JaxbContextBuilderDetailsImpl(packages, map);

    softly.assertThat(model.isBuildWithContextPath())
        .isTrue();
    softly.assertThat(model.getContextPath())
        .isEqualTo("org.bremersee.xml.model7a");
    softly.assertThat(model.getSchemaLocation())
        .isEqualTo("http://bremersee.org/xmlschemas/common-xml-test-model-7a "
            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-7a.xsd");
    softly.assertThat(model.getClasses())
        .isNull();
    softly.assertThat(model.getNameSpacesWithLocation())
        .hasSize(1);
    softly.assertThat(model.getSchemaLocations())
        .hasSize(1);

    softly.assertThat(model)
        .isEqualTo(new JaxbContextBuilderDetailsImpl(packages, map));
    softly.assertThat(model.hashCode())
        .isEqualTo(new JaxbContextBuilderDetailsImpl(packages, map).hashCode());

    softly.assertThat(model.toString())
        .contains("org.bremersee.xml.model7a");
  }

}