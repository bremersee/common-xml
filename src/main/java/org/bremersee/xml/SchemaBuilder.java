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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * The schema builder interface.
 *
 * @author Christian Bremer
 */
public interface SchemaBuilder {

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
  SchemaBuilder schemaLanguage(String schemaLanguage);

  /**
   * Specifies the factory class to use (see {@link SchemaFactory#newInstance(String, String,
   * ClassLoader)}*).
   *
   * @param factoryClassName the factory class name
   * @return the schema builder
   */
  SchemaBuilder factoryClassName(String factoryClassName);

  /**
   * Specifies the class loader to use (see {@link SchemaFactory#newInstance(String, String,
   * ClassLoader)}*).
   *
   * @param classLoader the class loader
   * @return the schema builder
   */
  SchemaBuilder classLoader(ClassLoader classLoader);

  /**
   * Specifies the resource loader to use. The resource loader is used to retrieve the xsd files
   * that are specified in the schema location (see {@link #fetchSchemaSources(String...)}).
   *
   * @param resourceLoader the resource loader
   * @return the schema builder
   */
  SchemaBuilder resourceLoader(ResourceLoader resourceLoader);

  /**
   * Specifies the resource resolver to use.
   *
   * @param resourceResolver the resource resolver
   * @return the schema builder
   */
  SchemaBuilder resourceResolver(LSResourceResolver resourceResolver);

  /**
   * Specifies the error handler to use (see {@link SchemaFactory#setErrorHandler(ErrorHandler)}).
   *
   * @param errorHandler the error handler
   * @return the schema builder
   */
  SchemaBuilder errorHandler(ErrorHandler errorHandler);

  /**
   * Adds a feature schema factory (see {@link SchemaFactory#setFeature(String, boolean)}).
   *
   * @param name the name
   * @param value the value
   * @return the schema builder
   */
  SchemaBuilder feature(String name, Boolean value);

  /**
   * Adds a property to the schema factory (see {@link SchemaFactory#setProperty(String, Object)}).
   *
   * @param name the name
   * @param value the value
   * @return the schema builder
   */
  SchemaBuilder property(String name, Object value);

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
   * #buildSchema(Collection)}*).
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
    return new DefaultBuilder();
  }

  /**
   * The default builder implementation.
   */
  class DefaultBuilder implements SchemaBuilder {

    /**
     * The schema language.
     */
    String schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI;

    /**
     * The factory class name.
     */
    String factoryClassName;

    /**
     * The class loader.
     */
    ClassLoader classLoader;

    /**
     * The resource loader.
     */
    ResourceLoader resourceLoader = new DefaultResourceLoader();

    /**
     * The resource resolver.
     */
    LSResourceResolver resourceResolver;

    /**
     * The error handler.
     */
    ErrorHandler errorHandler;

    /**
     * The features.
     */
    final Map<String, Boolean> features = new LinkedHashMap<>();

    /**
     * The properties.
     */
    final Map<String, Object> properties = new LinkedHashMap<>();

    /**
     * Creates a new schema factory.
     *
     * @return the schema factory
     */
    SchemaFactory createSchemaFactory() {
      final SchemaFactory schemaFactory;
      if (factoryClassName != null) {
        if (classLoader == null) {
          if (System.getSecurityManager() == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
          } else {
            //noinspection unchecked,rawtypes
            classLoader = (ClassLoader) java.security.AccessController.doPrivileged(
                (PrivilegedAction) () -> Thread.currentThread().getContextClassLoader());
          }
        }
        schemaFactory = SchemaFactory.newInstance(schemaLanguage, factoryClassName, classLoader);
      } else {
        schemaFactory = SchemaFactory.newInstance(schemaLanguage);
      }
      if (resourceResolver != null) {
        schemaFactory.setResourceResolver(resourceResolver);
      }
      if (errorHandler != null) {
        schemaFactory.setErrorHandler(errorHandler);
      }
      try {
        for (Map.Entry<String, Boolean> feature : features.entrySet()) {
          schemaFactory.setFeature(feature.getKey(), feature.getValue());
        }
        for (Map.Entry<String, Object> property : properties.entrySet()) {
          schemaFactory.setProperty(property.getKey(), property.getValue());
        }

      } catch (SAXNotSupportedException | SAXNotRecognizedException e) {
        throw new XmlRuntimeException(e);
      }
      return schemaFactory;
    }

    @Override
    public SchemaBuilder schemaLanguage(final String schemaLanguage) {
      if (StringUtils.hasText(schemaLanguage)) {
        this.schemaLanguage = schemaLanguage;
      }
      return this;
    }

    @Override
    public SchemaBuilder factoryClassName(final String factoryClassName) {
      this.factoryClassName = factoryClassName;
      return this;
    }

    @Override
    public SchemaBuilder classLoader(final ClassLoader classLoader) {
      this.classLoader = classLoader;
      return this;
    }

    @Override
    public SchemaBuilder resourceLoader(final ResourceLoader resourceLoader) {
      if (resourceLoader != null) {
        this.resourceLoader = resourceLoader;
      }
      return this;
    }

    @Override
    public SchemaBuilder resourceResolver(final LSResourceResolver resourceResolver) {
      this.resourceResolver = resourceResolver;
      return this;
    }

    @Override
    public SchemaBuilder errorHandler(final ErrorHandler errorHandler) {
      this.errorHandler = errorHandler;
      return this;
    }

    @Override
    public SchemaBuilder feature(final String name, final Boolean value) {
      if (StringUtils.hasText(name)) {
        features.put(name, value);
      }
      return this;
    }

    @Override
    public SchemaBuilder property(final String name, final Object value) {
      if (StringUtils.hasText(name)) {
        properties.put(name, value);
      }
      return this;
    }

    @Override
    public List<Source> fetchSchemaSources(final Collection<String> locations) {
      if (locations == null || locations.size() == 0) {
        return Collections.emptyList();
      }
      final Set<String> locationSet = new LinkedHashSet<>(locations);
      final List<Source> sources = new ArrayList<>(locationSet.size());
      for (final String location : locationSet) {
        try (final InputStream is = resourceLoader.getResource(location).getInputStream()) {
          final byte[] bytes = FileCopyUtils.copyToByteArray(is);
          sources.add(new StreamSource(new ByteArrayInputStream(bytes)));
        } catch (IOException e) {
          throw new XmlRuntimeException(e);
        }
      }
      return sources;
    }

    @Override
    public Schema buildSchema(final URL url) {
      try {
        if (url == null) {
          return createSchemaFactory().newSchema();
        }
        return createSchemaFactory().newSchema(url);
      } catch (SAXException e) {
        throw new XmlRuntimeException(e);
      }
    }

    @Override
    public Schema buildSchema(final File file) {
      try {
        if (file == null) {
          return createSchemaFactory().newSchema();
        }
        return createSchemaFactory().newSchema(file);
      } catch (SAXException e) {
        throw new XmlRuntimeException(e);
      }
    }

    @Override
    public Schema buildSchema(final Source[] sources) {
      try {
        if (sources == null || sources.length == 0) {
          return createSchemaFactory().newSchema();
        }
        return createSchemaFactory().newSchema(sources);
      } catch (SAXException e) {
        throw new XmlRuntimeException(e);
      }
    }

  }

}
