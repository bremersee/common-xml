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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import org.springframework.util.StringUtils;

/**
 * The jaxb context builder creates a {@link JAXBContext} from the provided meta data {@link
 * JaxbContextData}*.
 *
 * @author Christian Bremer
 */
public interface JaxbContextBuilder {

  /**
   * Specify whether the xml output should be formatted or not.
   *
   * @param formattedOutput the formatted output
   * @return the jaxb context builder
   */
  JaxbContextBuilder formattedOutput(boolean formattedOutput);

  /**
   * Specify the class loader.
   *
   * @param classLoader the class loader
   * @return the jaxb context builder
   */
  JaxbContextBuilder contextClassLoader(ClassLoader classLoader);

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
   * Build the marshaller of the jaxb context.
   *
   * @param nameSpaces the name spaces
   * @return the marshaller of the jaxb context
   * @throws JaxbRuntimeException if building fails
   */
  default Marshaller buildMarshaller(final String... nameSpaces) {
    try {
      return buildJaxbContext(nameSpaces).createMarshaller();
    } catch (JaxbRuntimeException e) {
      throw e;
    } catch (Exception e) {
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
    } catch (JaxbRuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new JaxbRuntimeException(e);
    }
  }

  /**
   * Builder jaxb context builder.
   *
   * @return the jaxb context builder
   */
  static JaxbContextBuilder builder() {
    return new Builder();
  }

  /**
   * The builder implementation.
   */
  class Builder implements JaxbContextBuilder {

    /**
     * Key is name space concatenation separated by colon, value is JAXB context.
     */
    private final Map<String, JAXBContext> jaxbContextMap = new ConcurrentHashMap<>();

    /**
     * Key is name space, value is a data set.
     *
     * <p>A data set with more than one entry is only possible when there is no name space present.
     */
    private final Map<String, Set<JaxbContextData>> jaxbContextDataMap = new ConcurrentHashMap<>();

    private boolean formattedOutput = true;

    private ClassLoader classLoader;

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ClassLoader getContextClassLoader() {
      if (classLoader == null) {
        if (System.getSecurityManager() == null) {
          classLoader = Thread.currentThread().getContextClassLoader();
        } else {
          classLoader = (ClassLoader) java.security.AccessController.doPrivileged(
              (PrivilegedAction) () -> Thread.currentThread().getContextClassLoader());
        }
      }
      return classLoader;
    }

    @Override
    public JaxbContextBuilder contextClassLoader(final ClassLoader classLoader) {
      this.classLoader = classLoader;
      return this;
    }

    @Override
    public JaxbContextBuilder formattedOutput(final boolean formattedOutput) {
      this.formattedOutput = formattedOutput;
      return this;
    }

    @Override
    public JaxbContextBuilder add(final JaxbContextData data) {
      if (data != null
          && data.getPackageName() != null
          && data.getPackageName().length() > 0) {
        final String nameSpace = data.getNameSpace() != null ? data.getNameSpace().trim() : "";
        jaxbContextMap.clear();
        if (nameSpace.length() == 0) {
          jaxbContextDataMap
              .computeIfAbsent(data.getNameSpace(), s -> new HashSet<>())
              .add(data);
        } else {
          jaxbContextDataMap.put(data.getNameSpace(), new HashSet<>(Collections.singleton(data)));
        }
      }
      return this;
    }

    @Override
    public JaxbContextBuilder addAll(final Iterable<? extends JaxbContextData> data) {
      if (data != null) {
        return addAll(data.iterator());
      }
      return this;
    }

    @Override
    public JaxbContextBuilder addAll(final Iterator<? extends JaxbContextData> data) {
      if (data != null) {
        while (data.hasNext()) {
          add(data.next());
        }
      }
      return this;
    }

    @Override
    public JaxbContextBuilder process(final JaxbContextDataProvider dataProvider) {
      if (dataProvider != null) {
        addAll(dataProvider.getJaxbContextData());
      }
      return this;
    }

    @Override
    public JaxbContextBuilder processAll(
        final Iterable<? extends JaxbContextDataProvider> dataProviders) {
      if (dataProviders != null) {
        processAll(dataProviders.iterator());
      }
      return this;
    }

    @Override
    public JaxbContextBuilder processAll(
        final Iterator<? extends JaxbContextDataProvider> dataProviders) {
      if (dataProviders != null) {
        while (dataProviders.hasNext()) {
          process(dataProviders.next());
        }
      }
      return this;
    }

    private DataDetails buildDataDetails(final String... nameSpaces) {
      if (nameSpaces == null || nameSpaces.length == 0) {
        return buildDataDetails(new TreeSet<>(jaxbContextDataMap.keySet()));
      }
      return buildDataDetails(new TreeSet<>(Arrays
          .stream(nameSpaces)
          .filter(s -> s != null && jaxbContextDataMap.containsKey(s))
          .collect(Collectors.toList())));
    }

    private DataDetails buildDataDetails(final SortedSet<String> nameSpaces) {
      final Set<JaxbContextData> dataSet = new HashSet<>();
      nameSpaces.forEach(nameSpace -> dataSet.addAll(jaxbContextDataMap.get(nameSpace)));
      final String key = String.join(":", nameSpaces);
      final String contextPath;
      final String schemaLocation;
      final JAXBContext jaxbContext = jaxbContextMap.get(key);
      if (jaxbContext instanceof JaxbContextDetailsAware) {
        final JaxbContextDetailsAware jb = (JaxbContextDetailsAware) jaxbContext;
        contextPath = jb.getContextPath();
        schemaLocation = jb.getSchemaLocation();
      } else {
        contextPath = dataSet
            .stream()
            .map(JaxbContextData::getPackageName)
            .collect(Collectors.joining(":"));
        schemaLocation = dataSet
            .stream()
            .filter(ds -> ds.getNameSpace().length() > 0
                && ds.getSchemaLocation() != null
                && ds.getSchemaLocation().length() > 0)
            .map(ds -> ds.getNameSpace() + " " + ds.getSchemaLocation())
            .collect(Collectors.joining(" "));
      }
      return new DataDetails(key, contextPath, schemaLocation);
    }

    public boolean supports(final Class<?> clazz, final String... nameSpaces) {
      return (clazz != null
          && (clazz.isAnnotationPresent(XmlRootElement.class)
          || clazz.isAnnotationPresent(XmlType.class))
          && contextPathContains(clazz, nameSpaces));
    }

    private boolean contextPathContains(final Class<?> clazz, final String... nameSpaces) {
      if (clazz == null) {
        return false;
      }
      final String packageName = clazz.getPackage().getName();
      final String contextPath = buildContextPath(nameSpaces);
      final StringTokenizer st = new StringTokenizer(contextPath, ":");
      while (st.hasMoreTokens()) {
        final String token = st.nextToken().trim();
        if (packageName.equals(token)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public String buildContextPath(final String... nameSpaces) {
      return buildDataDetails(nameSpaces).getContextPath();
    }

    @Override
    public String buildSchemaLocation(final String... nameSpaces) {
      return buildDataDetails(nameSpaces).getSchemaLocation();
    }

    @Override
    public Schema buildSchema(final SchemaBuilder schemaBuilder, final String... nameSpaces) {
      final SchemaBuilder sb = schemaBuilder != null ? schemaBuilder : SchemaBuilder.builder();
      final DataDetails dataDetails = buildDataDetails(nameSpaces);
      final Set<String> locations = dataDetails.getSchemaLocations();
      final List<Source> sources = new ArrayList<>(sb.fetchSchemaSources(locations));
      final JAXBContext jaxbContext = jaxbContextMap
          .computeIfAbsent(
              dataDetails.getKey(),
              s -> computeJaxbContext(dataDetails));
      final SchemaSourcesResolver resolver = new SchemaSourcesResolver();
      try {
        jaxbContext.generateSchema(resolver);
      } catch (IOException e) {
        throw new JaxbRuntimeException(e);
      }
      sources.addAll(resolver.toSources());
      return sb.buildSchema(sources);
    }

    @Override
    public Map<String, ?> buildMarshallerProperties(final String... nameSpaces) {
      final Map<String, Object> properties = new HashMap<>();
      properties.put(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
      properties.put(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
      final String schemaLocation = buildSchemaLocation(nameSpaces);
      if (schemaLocation != null && schemaLocation.trim().length() > 0) {
        properties.put(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation);
      }
      return properties;
    }

    @Override
    public JAXBContext buildJaxbContext(final String... nameSpaces) {
      final DataDetails dataDetails = buildDataDetails(nameSpaces);
      return jaxbContextMap
          .computeIfAbsent(
              dataDetails.getKey(),
              s -> computeJaxbContext(dataDetails));
    }

    private JAXBContext computeJaxbContext(final DataDetails dataDetails) {
      try {
        final JAXBContext jaxbContext = JAXBContext.newInstance(
            dataDetails.getContextPath(),
            getContextClassLoader());
        return new SchemaLocationAwareJaxbContext(
            jaxbContext,
            dataDetails.getContextPath(),
            dataDetails.getSchemaLocation(),
            formattedOutput);

      } catch (final Exception e) {
        throw new JaxbRuntimeException(e);
      }
    }

    private static class DataDetails {

      private final String key;

      private final String contextPath;

      private final String schemaLocation;

      /**
       * Instantiates data details.
       *
       * @param key the key, a concatenation of name spaces separated by colon
       * @param contextPath the context path, normally package names separated by colon
       * @param schemaLocation the schema location
       */
      DataDetails(String key, String contextPath, String schemaLocation) {
        this.key = key;
        this.contextPath = contextPath;
        this.schemaLocation = schemaLocation;
      }

      /**
       * Gets key, the key is a concatenation of name spaces separated by colon.
       *
       * @return the key
       */
      String getKey() {
        return key;
      }

      /**
       * Gets context path of JAXB, normally package names separated by colon.
       *
       * @return the context path
       */
      String getContextPath() {
        return contextPath;
      }

      /**
       * Builds schema location as it appears in the generated xml file. Name space and location
       * (url) are separated by space. The pairs of name space and location is also separated by
       * space.
       * <pre>
       * http://example.org/namesspace1 http://example.org/ns1.xsd
       * </pre>
       * In the xml file it looks like:
       * <pre>
       * xsi:schemaLocation="http://example.org/namesspace1 http://example.org/ns1.xsd"
       * </pre>
       *
       * @return the schema location as it appears in the generated xml file
       */
      String getSchemaLocation() {
        return schemaLocation;
      }

      /**
       * Gets a set of schema locations, normally as URL.
       * <pre>
       * http://example.org/model.xsd, http://example.org/another-model.xsd
       * </pre>
       *
       * @return the schema locations
       */
      Set<String> getSchemaLocations() {
        String[] parts = StringUtils.delimitedListToStringArray(schemaLocation, " ");
        if (parts.length == 0) {
          return Collections.emptySet();
        }
        Set<String> locations = new LinkedHashSet<>();
        for (int i = 1; i < parts.length; i = i + 2) {
          locations.add(parts[i]);
        }
        return locations;
      }
    }

  }

}
