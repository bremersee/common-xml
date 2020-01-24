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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
 * The schema builder implementation.
 *
 * @author Christian Bremer
 */
class SchemaBuilderImpl implements SchemaBuilder {

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
  public SchemaBuilderImpl copy() {
    SchemaBuilderImpl copy = new SchemaBuilderImpl();
    copy.schemaLanguage = schemaLanguage;
    copy.factoryClassName = factoryClassName;
    copy.classLoader = classLoader;
    copy.resourceLoader = resourceLoader;
    copy.resourceResolver = resourceResolver;
    copy.errorHandler = errorHandler;
    copy.features.putAll(features);
    copy.properties.putAll(properties);
    return copy;
  }

  @Override
  public SchemaBuilder withSchemaLanguage(final String schemaLanguage) {
    if (StringUtils.hasText(schemaLanguage)) {
      this.schemaLanguage = schemaLanguage;
    }
    return this;
  }

  @Override
  public SchemaBuilder withFactory(final String factoryClassName) {
    this.factoryClassName = factoryClassName;
    return this;
  }

  @Override
  public SchemaBuilder withClassLoader(final ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  @Override
  public SchemaBuilder withResourceLoader(final ResourceLoader resourceLoader) {
    if (resourceLoader != null) {
      this.resourceLoader = resourceLoader;
    }
    return this;
  }

  @Override
  public SchemaBuilder withResourceResolver(final LSResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
    return this;
  }

  @Override
  public SchemaBuilder withErrorHandler(final ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public SchemaBuilder withFeature(final String name, final Boolean value) {
    if (StringUtils.hasText(name)) {
      features.put(name, value);
    }
    return this;
  }

  @Override
  public SchemaBuilder withProperty(final String name, final Object value) {
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