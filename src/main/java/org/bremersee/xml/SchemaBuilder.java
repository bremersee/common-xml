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

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.springframework.core.io.ResourceLoader;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;

/**
 * The schema builder wraps the standard {@link SchemaFactory} of Java into a builder. It also
 * offers the ability to load schema using Spring's {@link ResourceLoader}.
 *
 * @author Christian Bremer
 */
public interface SchemaBuilder {

  /**
   * Copy schema builder.
   *
   * @return the schema builder
   */
  SchemaBuilder copy();

  /**
   * Specifies the schema language.
   *
   * <p>Default is {@code javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI}
   * ("http://www.w3.org/2001/XMLSchema").
   *
   * @param schemaLanguage specifies the schema language which the used schema factory will
   *     understand
   * @return the schema builder
   * @see SchemaFactory#newInstance(String)
   */
  SchemaBuilder withSchemaLanguage(String schemaLanguage);

  /**
   * Specifies the factory to use.
   *
   * @param factoryClassName the factory class name
   * @return the schema builder
   * @see SchemaFactory#newInstance(String, String, ClassLoader)
   */
  SchemaBuilder withFactory(String factoryClassName);

  /**
   * Specifies the class loader to use.
   *
   * @param classLoader the class loader
   * @return the schema builder
   * @see SchemaFactory#newInstance(String, String, ClassLoader)
   */
  SchemaBuilder withClassLoader(ClassLoader classLoader);

  /**
   * Specifies the resource loader to use. The resource loader is used to retrieve the xsd files
   * that are specified in the schema location (see {@link #fetchSchemaSources(String...)}).
   *
   * <p>Default is {@link org.springframework.core.io.DefaultResourceLoader}.
   *
   * @param resourceLoader the resource loader
   * @return the schema builder
   */
  SchemaBuilder withResourceLoader(ResourceLoader resourceLoader);

  /**
   * Specifies the resource resolver to use.
   *
   * @param resourceResolver the resource resolver
   * @return the schema builder
   * @see SchemaFactory#setResourceResolver(LSResourceResolver)
   */
  SchemaBuilder withResourceResolver(LSResourceResolver resourceResolver);

  /**
   * Specifies the error handler to use.
   *
   * @param errorHandler the error handler
   * @return the schema builder
   * @see SchemaFactory#setErrorHandler(ErrorHandler)
   */
  SchemaBuilder withErrorHandler(ErrorHandler errorHandler);

  /**
   * Adds a feature to the schema factory.
   *
   * @param name the name
   * @param value the value
   * @return the schema builder
   * @see SchemaFactory#setFeature(String, boolean)
   */
  SchemaBuilder withFeature(String name, Boolean value);

  /**
   * Adds a property to the schema factory.
   *
   * @param name the name
   * @param value the value
   * @return the schema builder
   * @see SchemaFactory#setProperty(String, Object)
   */
  SchemaBuilder withProperty(String name, Object value);

  /**
   * Retrieves the schema files of the specified locations.
   *
   * <p>The location can have any format that is supported by the {@link ResourceLoader}.
   *
   * @param locations the locations
   * @return the list
   * @see #withResourceLoader(ResourceLoader)
   */
  default List<Source> fetchSchemaSources(String... locations) {
    return Optional.ofNullable(locations)
        .map(a -> fetchSchemaSources(Arrays.asList(a)))
        .orElseGet(Collections::emptyList);
  }

  /**
   * Retrieves the schema files of the specified locations.
   *
   * <p>The location can have any format that is supported by the {@link ResourceLoader}.
   *
   * @param locations the locations
   * @return the schema files as source list
   * @see #withResourceLoader(ResourceLoader)
   */
  List<Source> fetchSchemaSources(Collection<String> locations);

  /**
   * Retrieves the schema files of the specified locations and builds the schema.
   *
   * <p>The location can have any format that is supported by the {@link ResourceLoader}.
   *
   * @param locations the locations
   * @return the schema
   */
  default Schema buildSchema(String... locations) {
    return Optional.ofNullable(locations)
        .map(c -> buildSchema(fetchSchemaSources(c)))
        .orElseGet(() -> buildSchema((Source[]) null));
  }

  /**
   * Builds schema.
   *
   * @param url the url
   * @return the schema
   * @see SchemaFactory#newSchema(URL)
   */
  Schema buildSchema(URL url);

  /**
   * Builds schema.
   *
   * @param file the file
   * @return the schema
   * @see SchemaFactory#newSchema(File)
   */
  Schema buildSchema(File file);

  /**
   * Builds schema.
   *
   * @param source the source
   * @return the schema
   * @see SchemaFactory#newSchema(Source)
   */
  default Schema buildSchema(Source source) {
    return Optional.ofNullable(source)
        .map(s -> buildSchema(new Source[]{s}))
        .orElseGet(() -> buildSchema((Source[]) null));
  }

  /**
   * Builds schema.
   *
   * @param sources the sources
   * @return the schema
   * @see SchemaFactory#newSchema(Source[])
   */
  Schema buildSchema(Source[] sources);

  /**
   * Builds schema.
   *
   * @param sources the sources
   * @return the schema
   * @see SchemaFactory#newSchema(Source[])
   */
  default Schema buildSchema(Collection<? extends Source> sources) {
    return Optional.ofNullable(sources)
        .map(c -> buildSchema(c.toArray(new Source[0])))
        .orElseGet(() -> buildSchema((Source[]) null));
  }

  /**
   * Creates a new schema builder.
   *
   * @return the schema builder
   */
  static SchemaBuilder builder() {
    return new SchemaBuilderImpl();
  }

}
