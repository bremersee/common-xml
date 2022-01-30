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

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.validation.Schema;

/**
 * The jaxb context builder.
 *
 * @author Christian Bremer
 */
public interface JaxbContextBuilder {

  /**
   * The default dependencies resolver implementation.
   */
  JaxbDependenciesResolver DEFAULT_DEPENDENCIES_RESOLVER = new JaxbDependenciesResolverImpl();


  /**
   * Creates a new jaxb context builder.
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
   * Specifies whether to add a schema to the marshaller or unmarshaller. The default is to add
   * never a schema to the marshaller or unmarshaller.
   *
   * @param schemaMode the schema mode
   * @return the jaxb context builder
   * @see SchemaMode#NEVER
   * @see SchemaMode#ALWAYS
   * @see SchemaMode#MARSHAL
   * @see SchemaMode#UNMARSHAL
   * @see SchemaMode#EXTERNAL_XSD
   */
  JaxbContextBuilder withSchemaMode(SchemaMode schemaMode);

  /**
   * Specifies the schema builder to generate the schema. The default is the default schema builder
   * implementation (see {@link SchemaBuilder#builder()}).
   *
   * @param schemaBuilder the schema builder
   * @return the jaxb context builder
   */
  JaxbContextBuilder withSchemaBuilder(SchemaBuilder schemaBuilder);

  /**
   * Specifies the dependencies-resolver to use. The default jaxb context builder will use a default
   * implementation.
   *
   * <p>To turn off dependency resolving set {@code null} here.
   *
   * @param resolver the resolver
   * @return the jaxb context builder
   */
  JaxbContextBuilder withDependenciesResolver(JaxbDependenciesResolver resolver);

  /**
   * Specifies the class loader to use.
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
   * Sets xml adapters of marshaller and unmarshaller.
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
   * Set validation event handler of marshaller and unmarshaller.
   *
   * @param validationEventHandler the validation event handler
   * @return the jaxb context builder
   */
  JaxbContextBuilder withValidationEventHandler(ValidationEventHandler validationEventHandler);


  /**
   * Add jaxb context meta-data to the jaxb context builder.
   *
   * @param data the data
   * @return the jaxb context builder
   */
  JaxbContextBuilder add(JaxbContextData data);

  /**
   * Add all jaxb context meta-data to the jaxb context builder.
   *
   * @param data the data
   * @return the jaxb context builder
   */
  default JaxbContextBuilder addAll(Iterable<? extends JaxbContextData> data) {
    return Optional.ofNullable(data)
        .map(d -> addAll(d.iterator()))
        .orElse(this);
  }

  /**
   * Add all jaxb context meta-data to the jaxb context builder.
   *
   * @param data the data
   * @return the jaxb context builder
   */
  default JaxbContextBuilder addAll(Iterator<? extends JaxbContextData> data) {
    return Optional.ofNullable(data)
        .map(iter -> Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED))
        .stream()
        .flatMap(split -> StreamSupport.stream(split, false))
        .map(this::add)
        .reduce((first, second) -> second)
        .orElse(this);
  }

  /**
   * Process the jaxb context meta-data provider and add its data to the jaxb context builder.
   *
   * @param dataProvider the data provider
   * @return the jaxb context builder
   */
  default JaxbContextBuilder process(JaxbContextDataProvider dataProvider) {
    return Optional.ofNullable(dataProvider)
        .map(provider -> addAll(provider.getJaxbContextData()))
        .orElse(this);
  }

  /**
   * Process the jaxb context meta-data providers and add their data to the jaxb context builder.
   *
   * @param dataProviders the data providers
   * @return the jaxb context builder
   */
  default JaxbContextBuilder processAll(
      Iterable<? extends JaxbContextDataProvider> dataProviders) {
    return Optional.ofNullable(dataProviders)
        .map(providers -> processAll(providers.iterator()))
        .orElse(this);
  }

  /**
   * Process the jaxb context meta-data providers and add their data to the jaxb context builder.
   *
   * @param dataProviders the data providers
   * @return the jaxb context builder
   */
  default JaxbContextBuilder processAll(
      Iterator<? extends JaxbContextDataProvider> dataProviders) {
    return Optional.ofNullable(dataProviders)
        .map(iter -> Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED))
        .stream()
        .flatMap(split -> StreamSupport.stream(split, false))
        .map(this::process)
        .reduce((first, second) -> second)
        .orElse(this);
  }

  /**
   * Determines whether the unmarshaller can decode xml into an object of the given class.
   *
   * @param clazz the class
   * @return {@code true} if the unmarshaller can decode xml into an object of the given class,
   *     otherwise {@code false}
   */
  default boolean canUnmarshal(Class<?> clazz) {
    return Optional.ofNullable(clazz)
        .filter(c -> c.isAnnotationPresent(XmlRootElement.class)
            || c.isAnnotationPresent(XmlType.class))
        .isPresent();
  }

  /**
   * Determines whether the marshaller can encode an object of the given class into xml.
   *
   * @param clazz the class
   * @return {@code true} if the marshaller can decode an object of the given class into xml,
   *     otherwise {@code false}
   */
  default boolean canMarshal(Class<?> clazz) {
    return Optional.ofNullable(clazz)
        .filter(c -> c.isAnnotationPresent(XmlRootElement.class))
        .isPresent();
  }

  /**
   * Build unmarshaller for the given classes with the specified dependencies-resolver. If
   * dependency resolving is turned off, an unmarshaller of the default context (defined by the
   * added meta-data) will be returned or one that is created with {@link
   * javax.xml.bind.JAXBContext#newInstance(Class[])}*.
   *
   * @param classes the classes that should be processed by the unmarshaller
   * @return the unmarshaller
   * @see JaxbDependenciesResolver
   */
  Unmarshaller buildUnmarshaller(Class<?>... classes);

  /**
   * Build marshaller with the context which is defined by the added meta-data.
   *
   * @return the marshaller
   */
  default Marshaller buildMarshaller() {
    return buildMarshaller(null);
  }

  /**
   * Build marshaller for the given object (POJO) or for the given class or array of classes with
   * the specified dependencies-resolver. If dependency resolving is turned off, a marshaller of the
   * default context (defined by the added meta-data) will be returned or one that is created with
   * {@link javax.xml.bind.JAXBContext#newInstance(Class[])}.
   *
   * @param value the value (POJO) that should be processed by the marshaller or a single class
   *     or an array of classes
   * @return the marshaller
   * @see JaxbDependenciesResolver
   */
  Marshaller buildMarshaller(Object value);

  /**
   * Inits default jaxb context. Otherwise, the jaxb context will be created at first usage.
   *
   * @return the jaxb context builder
   */
  JaxbContextBuilder initJaxbContext();

  /**
   * Build default jaxb context that is defined by the added meta-data.
   *
   * @return the jaxb context wrapper
   */
  default JaxbContextWrapper buildJaxbContext() {
    return buildJaxbContext(null);
  }

  /**
   * Build jaxb context for the given object (POJO) or for the given class or array of classes with
   * the specified dependency resolver. If dependency resolving is turned off, the default jaxb
   * context (defined by the added meta-data) will be returned or a jaxb context will be created
   * with {@link javax.xml.bind.JAXBContext#newInstance(Class[])}.
   *
   * @param value the value (POJO) that should be processed by the jaxb context or a single
   *     class or an array of classes
   * @return the jaxb context
   */
  JaxbContextWrapper buildJaxbContext(Object value);

  /**
   * Build schema of the default jaxb context (defined by the added meta-data).
   *
   * @return the schema
   */
  default Schema buildSchema() {
    return buildSchema(null);
  }

  /**
   * Build schema of the specified value (POJO), a single class or an array of classes.
   *
   * @param value the value (POJO), a single class or an array of classes for which the schema
   *     should be created
   * @return the schema
   */
  Schema buildSchema(Object value);

}
