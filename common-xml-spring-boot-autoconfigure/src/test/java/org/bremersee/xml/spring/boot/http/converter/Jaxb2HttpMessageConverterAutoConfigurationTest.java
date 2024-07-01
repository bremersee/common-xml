/*
 * Copyright 2022 the original author or authors.
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

package org.bremersee.xml.spring.boot.http.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.bremersee.xml.JaxbContextBuilder;
import org.bremersee.xml.http.converter.Jaxb2HttpMessageConverter;
import org.bremersee.xml.spring.boot.http.JaxbReadWriteConfigurer;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.ObjectProvider;

/**
 * The Jaxb 2 http message converter autoconfiguration test.
 *
 * @author Christian Bremer
 */
class Jaxb2HttpMessageConverterAutoConfigurationTest {

  private static final Jaxb2HttpMessageConverterAutoConfiguration target
      = new Jaxb2HttpMessageConverterAutoConfiguration();

  /**
   * Init.
   */
  @Test
  void init() {
    target.init();
  }

  /**
   * Jaxb http message converter.
   */
  @Test
  void jaxb2HttpMessageConverter() {
    List<JaxbReadWriteConfigurer> configurers = List.of(
        new JaxbReadWriteConfigurer() {
          @Override
          public Set<Class<?>> getIgnoreReadingClasses() {
            return Set.of(IllegalStateException.class);
          }

          @Override
          public Set<Class<?>> getIgnoreWritingClasses() {
            return Set.of(IllegalStateException.class);
          }
        },
        new JaxbReadWriteConfigurer() {
          @Override
          public Set<Class<?>> getIgnoreReadingClasses() {
            return Set.of(BigDecimal.class);
          }

          @Override
          public Set<Class<?>> getIgnoreWritingClasses() {
            return Set.of(BigDecimal.class);
          }
        }
    );
    //noinspection unchecked
    ObjectProvider<JaxbReadWriteConfigurer> provider = mock(ObjectProvider.class);
    when(provider.stream())
        .then((Answer<Stream<JaxbReadWriteConfigurer>>) invocationOnMock -> configurers.stream());
    Jaxb2HttpMessageConverter actual = target.jaxb2HttpMessageConverter(
        JaxbContextBuilder.newInstance(), provider);
    assertThat(actual).isNotNull();
  }
}