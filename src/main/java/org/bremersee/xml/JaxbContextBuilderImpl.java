/*
 * Copyright 2019 the original author or authors.
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

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import org.springframework.util.StringUtils;

/**
 * The type Jaxb context builder.
 *
 * @author Christian Bremer
 */
class JaxbContextBuilderImpl implements JaxbContextBuilder {

  /**
   * Key is package name, value is a data set.
   */
  private final Map<String, JaxbContextData> jaxbContextDataMap = new ConcurrentHashMap<>();

  private final Map<JaxbContextBuilderDetails, Schema> schemaCache = new ConcurrentHashMap<>();

  private final Map<JaxbContextBuilderDetails, JAXBContext> jaxbContextCache
      = new ConcurrentHashMap<>();

  private JaxbDependenciesResolver dependenciesResolver = DEFAULT_DEPENDENCIES_RESOLVER;

  private SchemaBuilder schemaBuilder = SchemaBuilder.builder();

  private ClassLoader classLoader;

  private boolean formattedOutput = true;

  private SchemaMode schemaMode = SchemaMode.NEVER;

  private BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> canMarshal = CAN_MARSHAL_ALL;

  private BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> canUnmarshal
      = CAN_UNMARSHAL_ALL;

  private List<XmlAdapter<?, ?>> xmlAdapters;

  private AttachmentMarshaller attachmentMarshaller;

  private AttachmentUnmarshaller attachmentUnmarshaller;

  private ValidationEventHandler validationEventHandler;

  /**
   * Instantiates a new jaxb context builder.
   */
  JaxbContextBuilderImpl() {
  }

  private void clearCache() {
    schemaCache.clear();
    jaxbContextCache.clear();
  }

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
    final JaxbContextBuilderImpl copy = new JaxbContextBuilderImpl();
    copy.dependenciesResolver = dependenciesResolver;
    copy.schemaMode = schemaMode;
    copy.schemaCache.putAll(schemaCache);
    copy.canUnmarshal = canUnmarshal;
    copy.canMarshal = canMarshal;
    copy.classLoader = classLoader;
    copy.attachmentMarshaller = attachmentMarshaller;
    copy.attachmentUnmarshaller = attachmentUnmarshaller;
    copy.formattedOutput = formattedOutput;
    copy.jaxbContextCache.putAll(jaxbContextCache);
    copy.jaxbContextDataMap.putAll(jaxbContextDataMap);
    copy.schemaBuilder = schemaBuilder.copy();
    copy.validationEventHandler = validationEventHandler;
    if (xmlAdapters != null) {
      copy.xmlAdapters = new ArrayList<>(xmlAdapters);
    }
    return copy;
  }

  @Override
  public JaxbContextBuilder withCanMarshal(
      final BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> function) {

    if (function != null) {
      this.canMarshal = function;
    }
    return this;
  }

  @Override
  public JaxbContextBuilder withCanUnmarshal(
      final BiFunction<Class<?>, Map<String, JaxbContextData>, Boolean> function) {

    if (function != null) {
      this.canUnmarshal = function;
    }
    return this;
  }

  @Override
  public JaxbContextBuilder withSchemaMode(final SchemaMode schemaMode) {
    if (schemaMode != null) {
      this.schemaMode = schemaMode;
    }
    return this;
  }

  @Override
  public JaxbContextBuilder withDependenciesResolver(final JaxbDependenciesResolver resolver) {
    this.dependenciesResolver = resolver;
    return this;
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
      final Collection<? extends XmlAdapter<?, ?>> xmlAdapters) {

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

  @Override
  public JaxbContextBuilder add(final JaxbContextData data) {
    if (data != null && StringUtils.hasText(data.getPackageName())) {
      clearCache();
      jaxbContextDataMap.put(data.getPackageName(), data);
    }
    return this;
  }


  @Override
  public boolean canUnmarshal(final Class<?> clazz) { // decode
    return canUnmarshal.apply(clazz, jaxbContextDataMap);
  }

  @Override
  public boolean canMarshal(final Class<?> clazz) { // encode
    return canMarshal.apply(clazz, jaxbContextDataMap);
  }

  @Override
  public Unmarshaller buildUnmarshaller(final Object value) {
    JaxbContextWrapper jaxbContext;
    if (value instanceof Class<?>[] && areAllClassesAreSupported((Class<?>[]) value)) {
      jaxbContext = computeJaxbContext(null);
    } else if (value instanceof Class<?>) {
      return buildUnmarshaller(new Class[]{(Class<?>) value});
    } else if (value == null || areAllClassesAreSupported(new Class[]{value.getClass()})) {
      jaxbContext = computeJaxbContext(null);
    } else {
      jaxbContext = computeJaxbContext(value);
    }
    final SchemaMode mode = jaxbContext.getSchemaMode();
    if (mode == SchemaMode.ALWAYS
        || mode == SchemaMode.UNMARSHAL
        || (mode == SchemaMode.EXTERNAL_XSD
        && StringUtils.hasText(jaxbContext.getDetails().getSchemaLocation()))) {
      jaxbContext = jaxbContext.withSchema(computeSchema(jaxbContext));
    }
    try {
      return jaxbContext.createUnmarshaller();

    } catch (JAXBException e) {
      throw new JaxbRuntimeException(e);
    }
  }

  @Override
  public Marshaller buildMarshaller(final Object value) {
    JaxbContextWrapper jaxbContext = computeJaxbContext(value);
    final SchemaMode mode = jaxbContext.getSchemaMode();
    if (mode == SchemaMode.ALWAYS
        || mode == SchemaMode.MARSHAL
        || (mode == SchemaMode.EXTERNAL_XSD
        && StringUtils.hasText(jaxbContext.getDetails().getSchemaLocation()))) {
      jaxbContext = jaxbContext.withSchema(computeSchema(jaxbContext));
    }
    try {
      return jaxbContext.createMarshaller();
    } catch (JAXBException e) {
      throw new JaxbRuntimeException(e);
    }
  }

  @Override
  public JaxbContextWrapper buildJaxbContext(final Object value) {
    final JaxbContextWrapper wrapper = computeJaxbContext(value);
    final SchemaMode mode = wrapper.getSchemaMode();
    if (mode == SchemaMode.ALWAYS
        || mode == SchemaMode.MARSHAL
        || mode == SchemaMode.UNMARSHAL
        || (mode == SchemaMode.EXTERNAL_XSD
        && StringUtils.hasText(wrapper.getDetails().getSchemaLocation()))) {
      return wrapper.withSchema(computeSchema(wrapper));
    } else {
      return wrapper;
    }
  }

  @Override
  public Schema buildSchema(final Object value) {
    return computeSchema(value);
  }

  private JaxbContextBuilderDetails buildDetails(final Object value) {
    if (value == null) {
      return JaxbContextBuilderDetails.buildWith(null, jaxbContextDataMap);
    }
    if (value instanceof Class<?>) {
      return buildDetails(new Class<?>[]{(Class<?>) value});
    }
    if (value instanceof Class<?>[]) {
      final Class<?>[] classes = (Class<?>[]) value;
      if (areAllClassesAreSupported(classes)) {
        final Set<String> packages = Arrays.stream(classes)
            .map(clazz -> clazz.getPackage().getName())
            .collect(Collectors.toSet());
        return JaxbContextBuilderDetails.buildWith(packages, jaxbContextDataMap);
      } else {
        return JaxbContextBuilderDetails.buildWith(classes);
      }
    }
    if (jaxbContextDataMap.containsKey(value.getClass().getPackage().getName())) {
      final Set<String> packages = dependenciesResolver != null
          ? dependenciesResolver.resolvePackages(value)
          : null;
      return JaxbContextBuilderDetails.buildWith(packages, jaxbContextDataMap);
    } else {
      return buildDetails(value.getClass());
    }
  }

  private boolean areAllClassesAreSupported(final Class<?>[] classes) {
    return Arrays.stream(classes)
        .map(clazz -> clazz.getPackage().getName())
        .allMatch(jaxbContextDataMap::containsKey);
  }

  private JaxbContextWrapper computeJaxbContext(final Object value) {
    final JaxbContextBuilderDetails details = buildDetails(value);
    final JAXBContext jaxbContext = jaxbContextCache.computeIfAbsent(details, key -> {
      try {
        return key.isBuildWithContextPath()
            ? JAXBContext.newInstance(details.getContextPath(), getContextClassLoader())
            : JAXBContext.newInstance(key.getClasses());

      } catch (final Exception e) {
        throw new JaxbRuntimeException(e);
      }
    });
    return new JaxbContextWrapper(jaxbContext, details)
        .withAttachmentMarshaller(attachmentMarshaller)
        .withAttachmentUnmarshaller(attachmentUnmarshaller)
        .withFormattedOutput(formattedOutput)
        .withValidationEventHandler(validationEventHandler)
        .withXmlAdapters(xmlAdapters)
        .withSchemaMode(schemaMode);
  }

  private Schema computeSchema(final Object value) {
    return computeSchema(computeJaxbContext(value));
  }

  private Schema computeSchema(final JaxbContextWrapper jaxbContext) {
    final JaxbContextBuilderDetails details = jaxbContext.getDetails();
    return schemaCache.computeIfAbsent(details, key -> {
      final SchemaSourcesResolver sourcesResolver = new SchemaSourcesResolver();
      try {
        jaxbContext.generateSchema(sourcesResolver);
      } catch (Exception e) {
        throw new JaxbRuntimeException(e);
      }
      final List<Source> sources = new ArrayList<>(
          sourcesResolver.toSources(details.getNameSpacesWithLocation()));
      final Set<String> locations = details.getSchemaLocations();
      sources.addAll(schemaBuilder.fetchSchemaSources(locations));
      return schemaBuilder.buildSchema(sources);
    });
  }

}
