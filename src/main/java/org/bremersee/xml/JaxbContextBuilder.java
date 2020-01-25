/*
 * Copyright 2018-2020 the original author or authors.
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.validation.Schema;
import org.springframework.util.StringUtils;

/**
 * The jaxb context builder.
 *
 * @author Christian Bremer
 */
public interface JaxbContextBuilder {

  /**
   * The constant DEFAULT_DEPENDENCIES_RESOLVER.
   */
  JaxbDependenciesResolver DEFAULT_DEPENDENCIES_RESOLVER = new JaxbDependenciesResolverImpl();

  /**
   * The can marshal all function.
   */
  BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> CAN_MARSHAL_ALL
      = (aClass, predefinedData) -> aClass != null
      && aClass.isAnnotationPresent(XmlRootElement.class);

  /**
   * The can unmarshal all function.
   */
  BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> CAN_UNMARSHAL_ALL
      = (aClass, predefinedData) -> aClass != null
      && aClass.isAnnotationPresent(XmlRootElement.class);

  /**
   * The can marshal only predefined data function.
   */
  BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> CAN_MARSHAL_ONLY_PREDEFINED_DATA
      = (aClass, predefinedData) -> aClass != null
      && aClass.isAnnotationPresent(XmlRootElement.class)
      && predefinedData.containsKey(aClass.getPackage().getName());

  /**
   * The can unmarshal only predefined data function.
   */
  BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> CAN_UNMARSHAL_ONLY_PREDEFINED_DATA
      = (aClass, predefinedData) -> aClass != null
      && aClass.isAnnotationPresent(XmlRootElement.class)
      && predefinedData.containsKey(aClass.getPackage().getName());


  /**
   * Returns a jaxb context builder.
   *
   * @return the jaxb context builder
   */
  static JaxbContextBuilder builder() {
    return new JaxbContextBuilderImpl();
  }


  /**
   * Copy jaxb context builder.
   *
   * @return the jaxb context builder
   */
  JaxbContextBuilder copy();

  /**
   * With can marshal function.
   *
   * @param function the function
   * @return the jaxb context builder
   */
  JaxbContextBuilder withCanMarshal(
      BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> function);

  /**
   * With can unmarshal function.
   *
   * @param function the function
   * @return the jaxb context builder
   */
  JaxbContextBuilder withCanUnmarshal(
      BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> function);

  /**
   * With schema mode.
   *
   * @param schemaMode the schema mode
   * @return the jaxb context builder
   */
  JaxbContextBuilder withSchemaMode(SchemaMode schemaMode);

  /**
   * With dependencies resolver.
   *
   * @param resolver the resolver
   * @return the jaxb context builder
   */
  JaxbContextBuilder withDependenciesResolver(JaxbDependenciesResolver resolver);

  /**
   * Specify the class loader.
   *
   * @param classLoader the class loader
   * @return the jaxb context builder
   */
  JaxbContextBuilder withContextClassLoader(ClassLoader classLoader);

  /**
   * Specify whether the xml output should be formatted or not.
   *
   * @param formattedOutput the formatted output
   * @return the jaxb context builder
   */
  JaxbContextBuilder withFormattedOutput(boolean formattedOutput);

  /**
   * Sets xml adapters to marshaller and unmarshaller.
   *
   * @param xmlAdapters the xml adapters
   * @return the jaxb context builder
   */
  JaxbContextBuilder withXmlAdapters(Collection<? extends XmlAdapter<?, ?>> xmlAdapters);

  /**
   * Set attachment marshaller.
   *
   * @param attachmentMarshaller the attachment marshaller
   * @return the jaxb context builder
   */
  JaxbContextBuilder withAttachmentMarshaller(AttachmentMarshaller attachmentMarshaller);

  /**
   * Set attachment unmarshaller.
   *
   * @param attachmentUnmarshaller the attachment unmarshaller
   * @return the jaxb context builder
   */
  JaxbContextBuilder withAttachmentUnmarshaller(AttachmentUnmarshaller attachmentUnmarshaller);

  /**
   * Set validation event handler on marshaller and unmarshaller.
   *
   * @param validationEventHandler the validation event handler
   * @return the jaxb context builder
   */
  JaxbContextBuilder withValidationEventHandler(ValidationEventHandler validationEventHandler);


  /**
   * Add.
   *
   * @param contextPath the context path
   * @return the jaxb context builder
   */
  default JaxbContextBuilder add(final String contextPath) {
    if (StringUtils.hasText(contextPath)) {
      final String[] packages = StringUtils.delimitedListToStringArray(contextPath, ":");
      for (String pakkage : packages) {
        add(new JaxbContextData(pakkage));
      }
    }
    return this;
  }

  /**
   * Add jaxb context meta data to this builder.
   *
   * @param data the data
   * @return the jaxb context builder
   */
  JaxbContextBuilder add(JaxbContextData data);

  /**
   * Add all jaxb context meta data to this builder.
   *
   * @param data the data
   * @return the jaxb context builder
   */
  default JaxbContextBuilder addAll(final Iterable<? extends JaxbContextData> data) {
    return data == null ? this : addAll(data.iterator());
  }

  /**
   * Add all jaxb context meta data to this builder.
   *
   * @param data the data
   * @return the jaxb context builder
   */
  default JaxbContextBuilder addAll(final Iterator<? extends JaxbContextData> data) {
    if (data != null) {
      while (data.hasNext()) {
        add(data.next());
      }
    }
    return this;
  }

  /**
   * Process the jaxb context meta data provider and add it's data to this builder.
   *
   * @param dataProvider the data provider
   * @return the jaxb context builder
   */
  default JaxbContextBuilder process(final JaxbContextDataProvider dataProvider) {
    return dataProvider == null ? this : addAll(dataProvider.getJaxbContextData());
  }

  /**
   * Process the jaxb context meta data providers and add their data to this builder.
   *
   * @param dataProviders the data providers
   * @return the jaxb context builder
   */
  default JaxbContextBuilder processAll(
      final Iterable<? extends JaxbContextDataProvider> dataProviders) {
    return dataProviders == null ? this : processAll(dataProviders.iterator());
  }

  /**
   * Process the jaxb context meta data providers and add their data to this builder.
   *
   * @param dataProviders the data providers
   * @return the jaxb context builder
   */
  default JaxbContextBuilder processAll(
      final Iterator<? extends JaxbContextDataProvider> dataProviders) {
    if (dataProviders != null) {
      while (dataProviders.hasNext()) {
        process(dataProviders.next());
      }
    }
    return this;
  }

  /**
   * Determines whether the unmarshaller can decode xml into an object of the given class
   *
   * @param clazz the class
   * @return {@code true} if the unmarshaller can decode xml into an object of the given class,
   *     otherwise {@code false}
   */
  boolean canUnmarshal(Class<?> clazz);

  /**
   * Determines whether the marshaller can encode an object of the given class into xml
   *
   * @param clazz the class
   * @return {@code true} if the marshaller can decode an object of the given class into xml,
   *     otherwise {@code false}
   */
  boolean canMarshal(Class<?> clazz);

  /**
   * Build unmarshaller unmarshaller.
   *
   * @return the unmarshaller
   */
  default Unmarshaller buildUnmarshaller() {
    return buildUnmarshaller(null);
  }

  /**
   * Build unmarshaller unmarshaller.
   *
   * @param value the value
   * @return the unmarshaller
   */
  Unmarshaller buildUnmarshaller(Object value);

  /**
   * Build marshaller marshaller.
   *
   * @return the marshaller
   */
  default Marshaller buildMarshaller() {
    return buildMarshaller(null);
  }

  /**
   * Build marshaller marshaller.
   *
   * @param value the value
   * @return the marshaller
   */
  Marshaller buildMarshaller(Object value);

  /**
   * Init jaxb context.
   *
   * @return the jaxb context builder
   */
  JaxbContextBuilder initJaxbContext();

  /**
   * Build jaxb context jaxb context wrapper.
   *
   * @return the jaxb context wrapper
   */
  default JaxbContextWrapper buildJaxbContext() {
    return buildJaxbContext(null);
  }

  /**
   * Build jaxb context jaxb context.
   *
   * @param value the value
   * @return the jaxb context
   */
  JaxbContextWrapper buildJaxbContext(Object value);

  /**
   * Build schema schema.
   *
   * @return the schema
   */
  default Schema buildSchema() {
    return buildSchema(null);
  }

  /**
   * Build schema schema.
   *
   * @param value the value
   * @return the schema
   */
  Schema buildSchema(Object value);

}
