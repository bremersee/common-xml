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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import org.springframework.util.StringUtils;

/**
 * The jaxb context builder creates a {@link JAXBContext} from the provided meta data {@link
 * JaxbContextData}*****.
 *
 * @author Christian Bremer
 */
public interface JaxbContextBuilder {

  JaxbContextBuilder copy();

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
  JaxbContextBuilder addAll(Iterable<? extends JaxbContextData> data);

  /**
   * Add all jaxb context meta data to this builder.
   *
   * @param data the data
   * @return the jaxb context builder
   */
  JaxbContextBuilder addAll(Iterator<? extends JaxbContextData> data);

  /**
   * Process the jaxb context meta data provider and add it's data to this builder.
   *
   * @param dataProvider the data provider
   * @return the jaxb context builder
   */
  JaxbContextBuilder process(JaxbContextDataProvider dataProvider);

  /**
   * Process the jaxb context meta data providers and add their data to this builder.
   *
   * @param dataProviders the data providers
   * @return the jaxb context builder
   */
  JaxbContextBuilder processAll(Iterable<? extends JaxbContextDataProvider> dataProviders);

  /**
   * Process the jaxb context meta data providers and add their data to this builder.
   *
   * @param dataProviders the data providers
   * @return the jaxb context builder
   */
  JaxbContextBuilder processAll(Iterator<? extends JaxbContextDataProvider> dataProviders);

  /**
   * Determine whether the given class is supported or not.
   *
   * @param clazz the clazz
   * @param nameSpaces the name spaces
   * @return {@code true} if the given class is supported, otherwise {@code false}
   */
  boolean supports(Class<?> clazz, String... nameSpaces);

  /**
   * Build context path, normally the package names of the model separated by colon.
   *
   * @param nameSpaces the name spaces
   * @return the context path (the package names of the model separated by colon)
   */
  String buildContextPath(String... nameSpaces);

  /**
   * Builds schema location as it appears in the generated xml file. Name space and location (url)
   * are separated by space. The pairs of name space and location is also separated by space.
   * <pre>
   * http://example.org/namesspace1 http://example.org/ns1.xsd
   * </pre>
   * In the xml file it looks like:
   * <pre>
   * xsi:schemaLocation="http://example.org/namesspace1 http://example.org/ns1.xsd"
   * </pre>
   *
   * @param nameSpaces the name spaces
   * @return the schema location as it appears in the generated xml file
   */
  String buildSchemaLocation(String... nameSpaces);

  /**
   * Build schema with a default {@link SchemaBuilder}.
   *
   * @param nameSpaces the name spaces
   * @return the schema
   */
  default Schema buildSchema(String... nameSpaces) {
    return buildSchema(null, nameSpaces);
  }

  /**
   * Build schema. The schema is generated from the present schema locations (xsd files) and the
   * jaxb context.
   *
   * @param schemaBuilder the schema builder
   * @param nameSpaces the name spaces
   * @return the schema
   */
  Schema buildSchema(SchemaBuilder schemaBuilder, String... nameSpaces);

  /**
   * Build marshaller properties.
   *
   * @param nameSpaces the name spaces
   * @return the marshaller properties
   */
  Map<String, ?> buildMarshallerProperties(String... nameSpaces);

  /**
   * Build the jaxb context.
   *
   * @param nameSpaces the name spaces
   * @return the jaxb context
   * @throws JaxbRuntimeException if building fails
   */
  JAXBContext buildJaxbContext(String... nameSpaces);

  /**
   * Build jaxb context with schema.
   *
   * @param schemaBuilder the schema builder
   * @param nameSpaces the name spaces
   * @return the jaxb context
   */
  JAXBContext buildJaxbContextWithSchema(SchemaBuilder schemaBuilder, String... nameSpaces);

  /**
   * Build the marshaller of the jaxb context.
   *
   * @param nameSpaces the name spaces
   * @return the marshaller of the jaxb context
   * @throws JaxbRuntimeException if building fails
   */
  default Marshaller buildMarshaller(final String... nameSpaces) {
    try {
      return buildJaxbContext(nameSpaces).createMarshaller();
    } catch (JAXBException e) {
      throw new JaxbRuntimeException(e);
    }
  }

  /**
   * Build marshaller with schema.
   *
   * @param schemaBuilder the schema builder
   * @param nameSpaces the name spaces
   * @return the marshaller
   */
  default Marshaller buildMarshallerWithSchema(
      final SchemaBuilder schemaBuilder,
      final String... nameSpaces) {
    try {
      return buildJaxbContextWithSchema(schemaBuilder, nameSpaces).createMarshaller();
    } catch (JAXBException e) {
      throw new JaxbRuntimeException(e);
    }
  }

  /**
   * Build the unmarshaller of the jaxb context.
   *
   * @param nameSpaces the name spaces
   * @return the unmarshaller of the jaxb context
   * @throws JaxbRuntimeException if building fails
   */
  default Unmarshaller buildUnmarshaller(final String... nameSpaces) {
    try {
      return buildJaxbContext(nameSpaces).createUnmarshaller();
    } catch (JAXBException e) {
      throw new JaxbRuntimeException(e);
    }
  }

  /**
   * Build unmarshaller with schema.
   *
   * @param schemaBuilder the schema builder
   * @param nameSpaces the name spaces
   * @return the unmarshaller
   */
  default Unmarshaller buildUnmarshallerWithSchema(
      final SchemaBuilder schemaBuilder,
      final String... nameSpaces) {
    try {
      return buildJaxbContextWithSchema(schemaBuilder, nameSpaces).createUnmarshaller();
    } catch (JAXBException e) {
      throw new JaxbRuntimeException(e);
    }
  }

  /**
   * Builder jaxb context builder.
   *
   * @return the jaxb context builder
   */
  static JaxbContextBuilder builder() {
    return new JaxbContextBuilderImpl();
  }

  /**
   * The builder implementation.
   */
}
