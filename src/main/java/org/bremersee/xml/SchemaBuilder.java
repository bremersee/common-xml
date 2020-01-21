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
 * The schema builder interface.
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
   * Specifies the schema language (see {@link SchemaFactory#newInstance(String)}).
   *
   * <p>Default is {@code javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI}
   * ("http://www.w3.org/2001/XMLSchema").
   *
   * @param schemaLanguage specifies the schema language which the used schema factory will
   *     understand
   * @return the schema builder
   */
  SchemaBuilder withSchemaLanguage(String schemaLanguage);

  /**
   * Specifies the factory to use (see {@link SchemaFactory#newInstance(String, String,
   * ClassLoader)}**).
   *
   * @param factoryClassName the factory class name
   * @return the schema builder
   */
  SchemaBuilder withFactory(String factoryClassName);

  /**
   * Specifies the class loader to use (see
   * {@link SchemaFactory#newInstance(String, String, ClassLoader)}).
   *
   * @param classLoader the class loader
   * @return the schema builder
   */
  SchemaBuilder withClassLoader(ClassLoader classLoader);

  /**
   * Specifies the resource loader to use. The resource loader is used to retrieve the xsd files
   * that are specified in the schema location (see {@link #fetchSchemaSources(String...)}).
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
   */
  SchemaBuilder withResourceResolver(LSResourceResolver resourceResolver);

  /**
   * Specifies the error handler to use (see {@link SchemaFactory#setErrorHandler(ErrorHandler)}).
   *
   * @param errorHandler the error handler
   * @return the schema builder
   */
  SchemaBuilder withErrorHandler(ErrorHandler errorHandler);

  /**
   * Adds a feature schema factory (see {@link SchemaFactory#setFeature(String, boolean)}).
   *
   * @param name the name
   * @param value the value
   * @return the schema builder
   */
  SchemaBuilder withFeature(String name, Boolean value);

  /**
   * Adds a property to the schema factory (see {@link SchemaFactory#setProperty(String, Object)}).
   *
   * @param name the name
   * @param value the value
   * @return the schema builder
   */
  SchemaBuilder withProperty(String name, Object value);

  /**
   * Retrieves the schema files with the specified locations.
   *
   * @param locations the locations
   * @return the list
   */
  default List<Source> fetchSchemaSources(String... locations) {
    return Optional.ofNullable(locations)
        .map(a -> fetchSchemaSources(Arrays.asList(a)))
        .orElseGet(Collections::emptyList);
  }

  /**
   * Retrieves the schema files with the specified locations.
   *
   * @param locations the locations
   * @return the schema files as source list
   */
  List<Source> fetchSchemaSources(Collection<String> locations);

  /**
   * Retrieves the schema files with the specified locations and builds the schema (see {@link
   * #buildSchema(Collection)}**).
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
   * Build schema (see {@link SchemaFactory#newSchema(URL)}).
   *
   * @param url the url
   * @return the schema
   */
  Schema buildSchema(URL url);

  /**
   * Build schema (see {@link SchemaFactory#newSchema(File)}).
   *
   * @param file the file
   * @return the schema
   */
  Schema buildSchema(File file);

  /**
   * Build schema (see {@link SchemaFactory#newSchema(Source)}).
   *
   * @param source the source
   * @return the schema
   */
  default Schema buildSchema(Source source) {
    return Optional.ofNullable(source)
        .map(s -> buildSchema(new Source[]{s}))
        .orElseGet(() -> buildSchema((Source[]) null));
  }

  /**
   * Build schema (see {@link SchemaFactory#newSchema(Source[])}).
   *
   * @param sources the sources
   * @return the schema
   */
  Schema buildSchema(Source[] sources);

  /**
   * Build schema (see {@link SchemaFactory#newSchema(Source[])}).
   *
   * @param sources the sources
   * @return the schema
   */
  default Schema buildSchema(Collection<? extends Source> sources) {
    return Optional.ofNullable(sources)
        .map(c -> buildSchema(c.toArray(new Source[0])))
        .orElseGet(() -> buildSchema((Source[]) null));
  }

  /**
   * Builder schema builder.
   *
   * @return the schema builder
   */
  static SchemaBuilder builder() {
    return new SchemaBuilderImpl();
  }

}
