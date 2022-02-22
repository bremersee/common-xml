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

import lombok.extern.slf4j.Slf4j;
import org.bremersee.xml.JaxbContextBuilder;
import org.bremersee.xml.spring.boot.JaxbContextBuilderAutoConfiguration;
import org.bremersee.xml.http.converter.Jaxb2HttpMessageConverter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.ClassUtils;

/**
 * The Jaxb 2 http message converter autoconfiguration.
 *
 * @author Christian Bremer
 */
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass(JaxbContextBuilder.class)
@AutoConfigureAfter(JaxbContextBuilderAutoConfiguration.class)
@Configuration
@Slf4j
public class Jaxb2HttpMessageConverterAutoConfiguration {

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName());
  }

  /**
   * Creates jaxb http message converter bean.
   *
   * @param jaxbContextBuilder the jaxb context builder
   * @return the jaxb http message converter
   */
  @ConditionalOnBean(JaxbContextBuilder.class)
  @ConditionalOnMissingBean(Jaxb2HttpMessageConverter.class)
  @Bean
  public Jaxb2HttpMessageConverter jaxb2HttpMessageConverter(
      JaxbContextBuilder jaxbContextBuilder) {
    log.info("Creating bean {}", Jaxb2HttpMessageConverter.class.getSimpleName());
    return new Jaxb2HttpMessageConverter(jaxbContextBuilder);
  }

}
