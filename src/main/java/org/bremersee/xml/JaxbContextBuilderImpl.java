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
import javax.xml.bind.Marshaller;
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
 * @author Christian Bremer
 */
class JaxbContextBuilderImpl implements JaxbContextBuilder {

  /**
   * Key is name space concatenation separated by colon, value is JAXB context.
   */
  private final Map<String, JAXBContext> jaxbContextMap = new ConcurrentHashMap<>();

  /**
   * Key is name space concatenation separated by colon, value is Schema.
   */
  private final Map<String, Schema> schemaMap = new ConcurrentHashMap<>();

  /**
   * Key is name space, value is a data set.
   *
   * <p>A data set with more than one entry is only possible when there is no name space present.
   */
  private final Map<String, Set<JaxbContextData>> jaxbContextDataMap = new ConcurrentHashMap<>();

  private ClassLoader classLoader;

  private boolean formattedOutput = true;

  private List<XmlAdapter<?, ?>> xmlAdapters = null;

  private AttachmentMarshaller attachmentMarshaller;

  private AttachmentUnmarshaller attachmentUnmarshaller;

  private ValidationEventHandler validationEventHandler;

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
  public JaxbContextBuilder copy() {
    JaxbContextBuilderImpl copy = new JaxbContextBuilderImpl();
    copy.jaxbContextMap.putAll(jaxbContextMap);
    copy.schemaMap.putAll(schemaMap);
    copy.jaxbContextDataMap.putAll(jaxbContextDataMap);
    copy.classLoader = classLoader;
    copy.formattedOutput = formattedOutput;
    if (xmlAdapters != null) {
      copy.xmlAdapters = new ArrayList<>(xmlAdapters);
    }
    copy.attachmentUnmarshaller = attachmentUnmarshaller;
    copy.attachmentMarshaller = attachmentMarshaller;

    return copy;
  }

  @Override
  public JaxbContextBuilder withContextClassLoader(final ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  @Override
  public JaxbContextBuilder withFormattedOutput(final boolean formattedOutput) {
    this.formattedOutput = formattedOutput;
    return this;
  }

  @Override
  public JaxbContextBuilder withXmlAdapters(
      Collection<? extends XmlAdapter<?, ?>> xmlAdapters) {
    if (xmlAdapters != null && !xmlAdapters.isEmpty()) {
      this.xmlAdapters = xmlAdapters
          .stream()
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    } else {
      this.xmlAdapters = null;
    }
    return this;
  }

  @Override
  public JaxbContextBuilder withAttachmentMarshaller(
      final AttachmentMarshaller attachmentMarshaller) {
    this.attachmentMarshaller = attachmentMarshaller;
    return this;
  }

  @Override
  public JaxbContextBuilder withAttachmentUnmarshaller(
      final AttachmentUnmarshaller attachmentUnmarshaller) {
    this.attachmentUnmarshaller = attachmentUnmarshaller;
    return this;
  }

  @Override
  public JaxbContextBuilder withValidationEventHandler(
      final ValidationEventHandler validationEventHandler) {
    this.validationEventHandler = validationEventHandler;
    return this;
  }

  private void clear() {
    jaxbContextMap.clear();
    schemaMap.clear();
  }

  @Override
  public JaxbContextBuilder add(final JaxbContextData data) {
    if (data != null
        && data.getPackageName() != null
        && data.getPackageName().length() > 0) {
      final String nameSpace = data.getNameSpace() != null ? data.getNameSpace().trim() : "";
      clear();
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
    final String contextPath = dataSet
        .stream()
        .map(JaxbContextData::getPackageName)
        .collect(Collectors.joining(":"));
    final String schemaLocation = dataSet
        .stream()
        .filter(ds -> ds.getNameSpace().length() > 0
            && ds.getSchemaLocation() != null
            && ds.getSchemaLocation().length() > 0)
        .map(ds -> ds.getNameSpace() + " " + ds.getSchemaLocation())
        .collect(Collectors.joining(" "));
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
    final DataDetails dataDetails = buildDataDetails(nameSpaces);
    return computeSchema(dataDetails, schemaBuilder);
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
  public JaxbContextWrapper buildJaxbContext(final String... nameSpaces) {
    final DataDetails dataDetails = buildDataDetails(nameSpaces);
    final JAXBContext jaxbContext = computeJaxbContext(dataDetails);
    return newJaxbContextWrapper(jaxbContext, dataDetails, null);
  }

  @Override
  public JaxbContextWrapper buildJaxbContextWithSchema(
      final SchemaBuilder schemaBuilder,
      final String... nameSpaces) {

    final DataDetails dataDetails = buildDataDetails(nameSpaces);
    final JAXBContext jaxbContext = computeJaxbContext(dataDetails);
    return newJaxbContextWrapper(
        jaxbContext,
        dataDetails,
        computeSchema(dataDetails, schemaBuilder));
  }

  private JAXBContext computeJaxbContext(
      final DataDetails dataDetails) {

    return jaxbContextMap.computeIfAbsent(dataDetails.getKey(), key -> {
      try {
        return JAXBContext.newInstance(
            dataDetails.getContextPath(),
            getContextClassLoader());

      } catch (final Exception e) {
        throw new JaxbRuntimeException(e);
      }
    });
  }

  private Schema computeSchema(
      final DataDetails dataDetails,
      final SchemaBuilder schemaBuilder) {

    return schemaMap.computeIfAbsent(dataDetails.getKey(), key -> {
      final JAXBContext ctx = computeJaxbContext(dataDetails);
      final SchemaSourcesResolver resolver = new SchemaSourcesResolver();
      try {
        ctx.generateSchema(resolver);
      } catch (IOException e) {
        throw new JaxbRuntimeException(e);
      }
      final List<Source> sources = new ArrayList<>(
          resolver.toSources(dataDetails.getNameSpaces()));
      final SchemaBuilder sb = schemaBuilder != null ? schemaBuilder : SchemaBuilder.builder();
      final Set<String> locations = dataDetails.getSchemaLocations();
      sources.addAll(sb.fetchSchemaSources(locations));
      return sb.buildSchema(sources);
    });
  }

  private JaxbContextWrapper newJaxbContextWrapper(
      final JAXBContext jaxbContext,
      final DataDetails dataDetails,
      final Schema schema) {
    final JaxbContextWrapper wrapper = new JaxbContextWrapper(
        jaxbContext, dataDetails.getContextPath(), dataDetails.getSchemaLocation());
    return wrapper
        .withAttachmentMarshaller(attachmentMarshaller)
        .withAttachmentUnmarshaller(attachmentUnmarshaller)
        .withFormattedOutput(formattedOutput)
        .withSchema(schema)
        .withValidationEventHandler(validationEventHandler)
        .withXmlAdapters(xmlAdapters);
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
    DataDetails(final String key, final String contextPath, final String schemaLocation) {
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
     * @return the schema location as it appears in the generated xml file
     */
    String getSchemaLocation() {
      return schemaLocation;
    }

    /**
     * Gets the set of schema locations, normally as URL.
     * <pre>
     * http://example.org/model.xsd, http://example.org/another-model.xsd
     * </pre>
     *
     * @return the schema locations
     */
    Set<String> getSchemaLocations() {
      final String[] parts = StringUtils.delimitedListToStringArray(schemaLocation, " ");
      if (parts.length == 0) {
        return Collections.emptySet();
      }
      final Set<String> locations = new LinkedHashSet<>();
      for (int i = 1; i < parts.length; i = i + 2) {
        locations.add(parts[i]);
      }
      return locations;
    }

    /**
     * Gets the set of name spaces where the schema location is present.
     *
     * @return the name spaces
     */
    Set<String> getNameSpaces() {
      final String[] parts = StringUtils.delimitedListToStringArray(schemaLocation, " ");
      if (parts.length == 0) {
        return Collections.emptySet();
      }
      final Set<String> locations = new LinkedHashSet<>();
      for (int i = 0; i < parts.length; i = i + 2) {
        locations.add(parts[i]);
      }
      return locations;
    }
  }

}
