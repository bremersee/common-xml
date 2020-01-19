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
 * @author Christian Bremer
 */
public interface SchemaBuilder {

  SchemaBuilder schemaLanguage(String schemaLanguage);

  SchemaBuilder factoryClassName(String factoryClassName);

  SchemaBuilder classLoader(ClassLoader classLoader);

  SchemaBuilder resourceLoader(ResourceLoader resourceLoader);

  SchemaBuilder resourceResolver(LSResourceResolver resourceResolver);

  SchemaBuilder errorHandler(ErrorHandler errorHandler);

  SchemaBuilder feature(String name, Boolean value);

  SchemaBuilder property(String name, Object value);

  default List<Source> buildSchemaSources(String... locations) {
    if (locations == null || locations.length == 0) {
      return Collections.emptyList();
    }
    return buildSchemaSources(Arrays.asList(locations));
  }

  List<Source> buildSchemaSources(Collection<String> locations);

  default Schema buildSchema(String... locations) {
    if (locations == null || locations.length == 0) {
      return buildSchema((Source) null);
    }
    return buildSchema(buildSchemaSources(locations));
  }

  Schema buildSchema(URL url);

  Schema buildSchema(File file);

  default Schema buildSchema(Source source) {
    if (source == null) {
      return buildSchema((Source[]) null);
    }
    return buildSchema(new Source[]{source});
  }

  Schema buildSchema(Source[] sources);

  default Schema buildSchema(Collection<? extends Source> sources) {
    if (sources == null || sources.isEmpty()) {
      return buildSchema((Source) null);
    }
    return buildSchema(sources.toArray(new Source[0]));
  }

  static SchemaBuilder builder() {
    return new DefaultBuilder();
  }

  class DefaultBuilder implements SchemaBuilder {

    String schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI;

    String factoryClassName;

    ClassLoader classLoader;

    ResourceLoader resourceLoader = new DefaultResourceLoader();

    LSResourceResolver resourceResolver;

    ErrorHandler errorHandler;

    final Map<String, Boolean> features = new LinkedHashMap<>();

    final Map<String, Object> properties = new LinkedHashMap<>();

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
    public SchemaBuilder schemaLanguage(String schemaLanguage) {
      if (StringUtils.hasText(schemaLanguage)) {
        this.schemaLanguage = schemaLanguage;
      }
      return this;
    }

    @Override
    public SchemaBuilder factoryClassName(String factoryClassName) {
      this.factoryClassName = factoryClassName;
      return this;
    }

    @Override
    public SchemaBuilder classLoader(ClassLoader classLoader) {
      this.classLoader = classLoader;
      return this;
    }

    @Override
    public SchemaBuilder resourceLoader(ResourceLoader resourceLoader) {
      if (resourceLoader != null) {
        this.resourceLoader = resourceLoader;
      }
      return this;
    }

    @Override
    public SchemaBuilder resourceResolver(LSResourceResolver resourceResolver) {
      this.resourceResolver = resourceResolver;
      return this;
    }

    @Override
    public SchemaBuilder errorHandler(ErrorHandler errorHandler) {
      this.errorHandler = errorHandler;
      return this;
    }

    @Override
    public SchemaBuilder feature(String name, Boolean value) {
      if (StringUtils.hasText(name)) {
        if (value == null) {
          features.remove(name);
        } else {
          features.put(name, value);
        }
      }
      return this;
    }

    @Override
    public SchemaBuilder property(String name, Object value) {
      if (StringUtils.hasText(name)) {
        if (value == null) {
          properties.remove(name);
        } else {
          properties.put(name, value);
        }
      }
      return this;
    }

    @Override
    public List<Source> buildSchemaSources(Collection<String> locations) {
      if (locations == null || locations.size() == 0) {
        return Collections.emptyList();
      }
      Set<String> locationSet = new LinkedHashSet<>(locations);
      List<Source> sources = new ArrayList<>(locationSet.size());
      for (String location : locationSet) {
        try (InputStream is = resourceLoader.getResource(location).getInputStream()) {
          byte[] bytes = FileCopyUtils.copyToByteArray(is);
          sources.add(new StreamSource(new ByteArrayInputStream(bytes)));
        } catch (IOException e) {
          throw new XmlRuntimeException(e);
        }
      }
      return sources;
    }

    @Override
    public Schema buildSchema(URL url) {
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
    public Schema buildSchema(File file) {
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
    public Schema buildSchema(Source[] sources) {
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
