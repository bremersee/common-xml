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

import static org.springframework.util.ObjectUtils.isEmpty;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.reflections.util.ClasspathHelper;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;
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
@SuppressWarnings("SameNameButDifferent")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
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
    SchemaFactory schemaFactory;
    if (!isEmpty(factoryClassName)) {
      if (isEmpty(classLoader)) {
        schemaFactory = SchemaFactory
            .newInstance(schemaLanguage, factoryClassName, ClasspathHelper.contextClassLoader());
      } else {
        schemaFactory = SchemaFactory.newInstance(schemaLanguage, factoryClassName, classLoader);
      }
    } else {
      schemaFactory = SchemaFactory.newInstance(schemaLanguage);
    }
    if (!isEmpty(resourceResolver)) {
      schemaFactory.setResourceResolver(resourceResolver);
    }
    if (!isEmpty(errorHandler)) {
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
  public SchemaBuilder withSchemaLanguage(String schemaLanguage) {
    if (isEmpty(schemaLanguage)) {
      this.schemaLanguage = schemaLanguage;
    }
    return this;
  }

  @Override
  public SchemaBuilder withFactory(String factoryClassName) {
    this.factoryClassName = factoryClassName;
    return this;
  }

  @Override
  public SchemaBuilder withClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  @Override
  public SchemaBuilder withResourceLoader(ResourceLoader resourceLoader) {
    if (!isEmpty(resourceLoader)) {
      this.resourceLoader = resourceLoader;
    }
    return this;
  }

  @Override
  public SchemaBuilder withResourceResolver(LSResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
    return this;
  }

  @Override
  public SchemaBuilder withErrorHandler(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public SchemaBuilder withFeature(String name, Boolean value) {
    if (!isEmpty(name)) {
      features.put(name, value);
    }
    return this;
  }

  @Override
  public SchemaBuilder withProperty(String name, Object value) {
    if (!isEmpty(name)) {
      properties.put(name, value);
    }
    return this;
  }

  @Override
  public List<Source> fetchSchemaSources(Collection<String> locations) {
    List<Source> sources = new ArrayList<>();
    if (!isEmpty(locations)) {
      Set<String> locationSet = new LinkedHashSet<>(locations);
      for (String location : locationSet) {
        try (InputStream is = resourceLoader.getResource(location).getInputStream()) {
          byte[] bytes = FileCopyUtils.copyToByteArray(is);
          sources.add(new StreamSource(new ByteArrayInputStream(bytes)));
        } catch (IOException e) {
          throw new XmlRuntimeException(e);
        }
      }
    }
    return Collections.unmodifiableList(sources);
  }

  @Override
  public Schema buildSchema(URL url) {
    try {
      if (isEmpty(url)) {
        return createSchemaFactory().newSchema();
      }
      return createSchemaFactory().newSchema(url);
    } catch (SAXException e) {
      throw new XmlRuntimeException(e);
    }
  }

  @Override
  public Schema buildSchema(File file) {
    try {
      if (isEmpty(file)) {
        return createSchemaFactory().newSchema();
      }
      return createSchemaFactory().newSchema(file);
    } catch (SAXException e) {
      throw new XmlRuntimeException(e);
    }
  }

  @Override
  public Schema buildSchema(Source[] sources) {
    try {
      if (isEmpty(sources)) {
        return createSchemaFactory().newSchema();
      }
      return createSchemaFactory().newSchema(sources);
    } catch (SAXException e) {
      throw new XmlRuntimeException(e);
    }
  }

}