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

import java.util.AbstractCollection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.xml.JaxbContextBuilder;
import org.bremersee.xml.http.codec.ReactiveJaxbDecoder;
import org.bremersee.xml.http.codec.ReactiveJaxbEncoder;
import org.bremersee.xml.spring.boot.JaxbContextBuilderAutoConfiguration;
import org.bremersee.xml.spring.boot.http.JaxbReadWriteConfigurer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * The Jaxb 2 http message codec autoconfiguration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnClass({WebFluxConfigurer.class, JaxbContextBuilder.class})
@ConditionalOnBean(JaxbContextBuilder.class)
@AutoConfigureAfter(JaxbContextBuilderAutoConfiguration.class)
@AutoConfiguration
@Slf4j
public class Jaxb2HttpMessageCodecAutoConfiguration implements WebFluxConfigurer {

  private final JaxbContextBuilder jaxbContextBuilder;

  private final Set<Class<?>> ignoreReadingClasses;

  private final Set<Class<?>> ignoreWritingClasses;

  /**
   * Instantiates a new Jaxb 2 http message codec autoconfiguration.
   *
   * @param jaxbContextBuilder the jaxb context builder
   * @param readWriteConfigurers the read write configurers
   */
  public Jaxb2HttpMessageCodecAutoConfiguration(
      ObjectProvider<JaxbContextBuilder> jaxbContextBuilder,
      ObjectProvider<JaxbReadWriteConfigurer> readWriteConfigurers) {
    this.jaxbContextBuilder = jaxbContextBuilder.getIfAvailable();
    this.ignoreReadingClasses = readWriteConfigurers
        .stream()
        .collect(
            HashSet::new,
            (a, b) -> a.addAll(b.getIgnoreReadingClasses()),
            AbstractCollection::addAll);
    this.ignoreWritingClasses = readWriteConfigurers
        .stream()
        .collect(
            HashSet::new,
            (a, b) -> a.addAll(b.getIgnoreWritingClasses()),
            AbstractCollection::addAll);
  }

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("""

            *********************************************************************************
            * {}
            *********************************************************************************
            * jaxbContextBuilder = {}
            * ignoreReadingClasses = {}
            * ignoreWritingClasses = {}
            *********************************************************************************""",
        ClassUtils.getUserClass(getClass()).getSimpleName(),
        jaxbContextBuilder, ignoreReadingClasses, ignoreWritingClasses);
  }

  @Override
  public void configureHttpMessageCodecs(@NonNull ServerCodecConfigurer configurer) {
    if (Objects.nonNull(jaxbContextBuilder)) {
      log.info("Registering jaxb encoder and decoder.");
      configurer
          .customCodecs()
          .registerWithDefaultConfig(new ReactiveJaxbEncoder(
              jaxbContextBuilder, ignoreWritingClasses));
      configurer
          .customCodecs()
          .registerWithDefaultConfig(new ReactiveJaxbDecoder(
              jaxbContextBuilder, ignoreReadingClasses));
    }
  }

}
