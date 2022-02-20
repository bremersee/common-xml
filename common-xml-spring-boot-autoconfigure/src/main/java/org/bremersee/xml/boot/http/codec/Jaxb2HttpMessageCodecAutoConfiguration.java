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

package org.bremersee.xml.boot.http.codec;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.xml.JaxbContextBuilder;
import org.bremersee.xml.boot.JaxbContextBuilderAutoConfiguration;
import org.bremersee.xml.http.codec.ReactiveJaxbDecoder;
import org.bremersee.xml.http.codec.ReactiveJaxbEncoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * The http message jaxb codecs autoconfiguration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnClass(JaxbContextBuilder.class)
@ConditionalOnBean(JaxbContextBuilder.class)
@AutoConfigureAfter(JaxbContextBuilderAutoConfiguration.class)
@Configuration
@Slf4j
public class Jaxb2HttpMessageCodecAutoConfiguration implements WebFluxConfigurer {

  private final JaxbContextBuilder jaxbContextBuilder;

  /**
   * Instantiates a new Jaxb 2 http message codec auto configuration.
   *
   * @param jaxbContextBuilder the jaxb context builder
   */
  public Jaxb2HttpMessageCodecAutoConfiguration(
      ObjectProvider<JaxbContextBuilder> jaxbContextBuilder) {
    this.jaxbContextBuilder = jaxbContextBuilder.getIfAvailable();
  }

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************\n"
            + "* jaxbContextBuilder = {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName(), jaxbContextBuilder);
  }

  @Override
  public void configureHttpMessageCodecs(@NonNull ServerCodecConfigurer configurer) {
    if (Objects.nonNull(jaxbContextBuilder)) {
      log.info("Registering jaxb encoder and decoder.");
      configurer
          .customCodecs()
          .registerWithDefaultConfig(new ReactiveJaxbEncoder(jaxbContextBuilder));
      configurer
          .customCodecs()
          .registerWithDefaultConfig(new ReactiveJaxbDecoder(jaxbContextBuilder));
    }
  }

}
