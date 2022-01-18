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
   * The default dependencies resolver implementation.
   *
   * @see #withDependenciesResolver(JaxbDependenciesResolver)
   */
  JaxbDependenciesResolver DEFAULT_DEPENDENCIES_RESOLVER = new JaxbDependenciesResolverImpl();

  /**
   * The can marshal all function. If this function is set, the builder can marshal all objects
   * which are annotated with {@code XmlRootElement}. This is the default behaviour.
   *
   * @see #withCanMarshal(BiFunction)
   */
  BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> CAN_MARSHAL_ALL
      = (aClass, predefinedData) -> aClass != null
      && aClass.isAnnotationPresent(XmlRootElement.class);

  /**
   * The can unmarshal all function. If this function is set, the builder can unmarshal all objects
   * which are annotated with {@code XmlRootElement}. This is the default behaviour.
   *
   * @see #withCanUnmarshal(BiFunction)
   */
  BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> CAN_UNMARSHAL_ALL
      = (aClass, predefinedData) -> aClass != null
      && aClass.isAnnotationPresent(XmlRootElement.class);

  /**
   * The can marshal only predefined data function. If this function is set, the builder can marshal
   * only objects which were added previously to the builder.
   *
   * @see #withCanMarshal(BiFunction)
   */
  BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> CAN_MARSHAL_ONLY_PREDEFINED_DATA
      = (aClass, predefinedData) -> aClass != null
      && aClass.isAnnotationPresent(XmlRootElement.class)
      && predefinedData.containsKey(aClass.getPackage().getName());

  /**
   * The can unmarshal only predefined data function. If this function is set, the builder can
   * unmarshal only objects which were added previously to the builder.
   *
   * @see #withCanUnmarshal(BiFunction)
   */
  BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> CAN_UNMARSHAL_ONLY_PREDEFINED_DATA
      = (aClass, predefinedData) -> aClass != null
      && aClass.isAnnotationPresent(XmlRootElement.class)
      && predefinedData.containsKey(aClass.getPackage().getName());


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
   * Sets a function of the builder to determine whether the builder is responsible for the given
   * class. The first parameter of the function is the class, the second is a map with meta data,
   * which were previously added to the builder). The key of the map is a package name.
   *
   * @param function the function
   * @return the jaxb context builder
   * @see #canMarshal(Class)
   */
  JaxbContextBuilder withCanMarshal(
      BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> function);

  /**
   * Sets a function of the builder to determine whether the builder is responsible for the given
   * class. The first parameter of the function is the class, the second is a map with meta data,
   * which were previously added to the builder). The key of the map is a package name.
   *
   * @param function the function
   * @return the jaxb context builder
   * @see #canUnmarshal(Class)
   */
  JaxbContextBuilder withCanUnmarshal(
      BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> function);

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
   * Specifies the dependencies resolver to use. The default jaxb context builder will use a default
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
   * Add the given context path (package names which are separated by colon) to the jaxb context
   * builder. This is the same as {@link javax.xml.bind.JAXBContext#newInstance(String)}.
   *
   * @param contextPath the context path (package names which are separated by colon)
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
   * Add jaxb context meta data to the jaxb context builder.
   *
   * @param data the data
   * @return the jaxb context builder
   */
  JaxbContextBuilder add(JaxbContextData data);

  /**
   * Add all jaxb context meta data to the jaxb context builder.
   *
   * @param data the data
   * @return the jaxb context builder
   */
  default JaxbContextBuilder addAll(final Iterable<? extends JaxbContextData> data) {
    return data == null ? this : addAll(data.iterator());
  }

  /**
   * Add all jaxb context meta data to the jaxb context builder.
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
   * Process the jaxb context meta data provider and add it's data to the jaxb context builder.
   *
   * @param dataProvider the data provider
   * @return the jaxb context builder
   */
  default JaxbContextBuilder process(final JaxbContextDataProvider dataProvider) {
    return dataProvider == null ? this : addAll(dataProvider.getJaxbContextData());
  }

  /**
   * Process the jaxb context meta data providers and add their data to the jaxb context builder.
   *
   * @param dataProviders the data providers
   * @return the jaxb context builder
   */
  default JaxbContextBuilder processAll(
      final Iterable<? extends JaxbContextDataProvider> dataProviders) {
    return dataProviders == null ? this : processAll(dataProviders.iterator());
  }

  /**
   * Process the jaxb context meta data providers and add their data to the jaxb context builder.
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
   * Determines whether the unmarshaller can decode xml into an object of the given class. The
   * function that is set by {@link #withCanUnmarshal(BiFunction)} will be used.
   *
   * @param clazz the class
   * @return {@code true} if the unmarshaller can decode xml into an object of the given class,
   *     otherwise {@code false}
   */
  boolean canUnmarshal(Class<?> clazz);

  /**
   * Determines whether the marshaller can encode an object of the given class into xml. The
   * function that is set by {@link #withCanMarshal(BiFunction)} will be used.
   *
   * @param clazz the class
   * @return {@code true} if the marshaller can decode an object of the given class into xml,
   *     otherwise {@code false}
   */
  boolean canMarshal(Class<?> clazz);

  /**
   * Build unmarshaller with the context which is defined by the added meta data.
   *
   * @return the unmarshaller
   */
  default Unmarshaller buildUnmarshaller() {
    return buildUnmarshaller(null);
  }

  /**
   * Build unmarshaller for the given object (POJO) or for the given class or array of classes with
   * the specified dependencies resolver. If dependency resolving is turned off, an unmarshaller of
   * the default context (defined by the added meta data) will be returned or one that is created
   * with {@link javax.xml.bind.JAXBContext#newInstance(Class[])}.
   *
   * @param value the value (POJO) that should be processed by the unmarshaller or a single
   *     class or an array of classes
   * @return the unmarshaller
   * @see JaxbDependenciesResolver
   */
  Unmarshaller buildUnmarshaller(Object value);

  /**
   * Build marshaller with the context which is defined by the added meta data.
   *
   * @return the marshaller
   */
  default Marshaller buildMarshaller() {
    return buildMarshaller(null);
  }

  /**
   * Build marshaller for the given object (POJO) or for the given class or array of classes with
   * the specified dependencies resolver. If dependency resolving is turned off, a marshaller of the
   * default context (defined by the added meta data) will be returned or one that is created with
   * {@link javax.xml.bind.JAXBContext#newInstance(Class[])}.
   *
   * @param value the value (POJO) that should be processed by the marshaller or a single class
   *     or an array of classes
   * @return the marshaller
   * @see JaxbDependenciesResolver
   */
  Marshaller buildMarshaller(Object value);

  /**
   * Inits default jaxb context. Otherwise the jaxb context will be created at first usage.
   *
   * @return the jaxb context builder
   */
  JaxbContextBuilder initJaxbContext();

  /**
   * Build default jaxb context that is defined by the added meta data.
   *
   * @return the jaxb context wrapper
   */
  default JaxbContextWrapper buildJaxbContext() {
    return buildJaxbContext(null);
  }

  /**
   * Build jaxb context for the given object (POJO) or for the given class or array of classes with
   * the specified dependencies resolver. If dependency resolving is turned off, the default jaxb
   * context (defined by the added meta data) will be returned or a jaxb context will be created
   * with {@link javax.xml.bind.JAXBContext#newInstance(Class[])}.
   *
   * @param value the value (POJO) that should be processed by the jaxb context or a single
   *     class or an array of classes
   * @return the jaxb context
   */
  JaxbContextWrapper buildJaxbContext(Object value);

  /**
   * Build schema of the default jaxb context (defined by the added meta data).
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
