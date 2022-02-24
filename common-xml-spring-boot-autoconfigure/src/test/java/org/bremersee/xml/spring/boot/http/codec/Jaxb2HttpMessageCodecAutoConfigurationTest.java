/*
 * Copyright 2020 the original author or authors.
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

package org.bremersee.xml.spring.boot.http.codec;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.bremersee.xml.JaxbContextBuilder;
import org.bremersee.xml.spring.boot.http.JaxbReadWriteConfigurer;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.codec.CodecConfigurer.CustomCodecs;
import org.springframework.http.codec.ServerCodecConfigurer;

/**
 * The Jaxb 2 http message codec auto configuration test.
 *
 * @author Christian Bremer
 */
class Jaxb2HttpMessageCodecAutoConfigurationTest {

  /**
   * Init.
   */
  @Test
  void init() {
    Jaxb2HttpMessageCodecAutoConfiguration target = new Jaxb2HttpMessageCodecAutoConfiguration(
        jaxbContextBuilder(JaxbContextBuilder.newInstance()), readWriteConfigurers());
    target.init();
  }

  /**
   * Configure http message codecs.
   */
  @Test
  void configureHttpMessageCodecs() {
    Jaxb2HttpMessageCodecAutoConfiguration target = new Jaxb2HttpMessageCodecAutoConfiguration(
        jaxbContextBuilder(JaxbContextBuilder.newInstance()), readWriteConfigurers());

    ServerCodecConfigurer configurer = mock(ServerCodecConfigurer.class);
    CustomCodecs customCodecs = mock(CustomCodecs.class);
    when(configurer.customCodecs()).thenReturn(customCodecs);

    target.configureHttpMessageCodecs(configurer);
    verify(customCodecs, times(2)).registerWithDefaultConfig(any());
  }

  /**
   * Do not configure http message codecs because of missing jaxb context builder.
   */
  @Test
  void doNotConfigureHttpMessageCodecsBecauseOfMissingJaxbContextBuilder() {
    Jaxb2HttpMessageCodecAutoConfiguration target = new Jaxb2HttpMessageCodecAutoConfiguration(
        jaxbContextBuilder(null), readWriteConfigurers());

    ServerCodecConfigurer configurer = mock(ServerCodecConfigurer.class);

    target.configureHttpMessageCodecs(configurer);
    verify(configurer, never()).customCodecs();
  }

  private static ObjectProvider<JaxbContextBuilder> jaxbContextBuilder(
      JaxbContextBuilder jaxbContextBuilder) {

    //noinspection unchecked
    ObjectProvider<JaxbContextBuilder> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(jaxbContextBuilder);
    return provider;
  }

  private static ObjectProvider<JaxbReadWriteConfigurer> readWriteConfigurers() {
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
    return provider;
  }

}