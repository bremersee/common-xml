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

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import org.bremersee.xml.JaxbContextDetails.JaxbContextDetailsBuilder;
import org.springframework.util.ClassUtils;

/**
 * The jaxb context builder.
 *
 * @author Christian Bremer
 */
class JaxbContextBuilderImpl implements JaxbContextBuilder {

  /**
   * Key is package name, value is jaxb data set.
   */
  private final Map<String, JaxbContextData> jaxbContextDataMap = new ConcurrentHashMap<>();

  private final Map<JaxbContextDetails, Schema> schemaCache = new ConcurrentHashMap<>();

  private final Map<JaxbContextDetails, JAXBContext> jaxbContextCache
      = new ConcurrentHashMap<>();

  private JaxbDependenciesResolver dependenciesResolver = DEFAULT_DEPENDENCIES_RESOLVER;

  private SchemaBuilder schemaBuilder = SchemaBuilder.builder();

  private ClassLoader classLoader;

  private boolean formattedOutput = true;

  private SchemaMode schemaMode = SchemaMode.NEVER;

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
    if (isEmpty(classLoader)) {
      if (isEmpty(System.getSecurityManager())) {
        return Thread.currentThread().getContextClassLoader();
      } else {
        return (ClassLoader) java.security.AccessController.doPrivileged(
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
    copy.classLoader = classLoader;
    copy.attachmentMarshaller = attachmentMarshaller;
    copy.attachmentUnmarshaller = attachmentUnmarshaller;
    copy.formattedOutput = formattedOutput;
    copy.jaxbContextCache.putAll(jaxbContextCache);
    copy.jaxbContextDataMap.putAll(jaxbContextDataMap);
    copy.schemaBuilder = schemaBuilder.copy();
    copy.validationEventHandler = validationEventHandler;
    if (!isEmpty(xmlAdapters)) {
      copy.xmlAdapters = new ArrayList<>(xmlAdapters);
    }
    return copy;
  }

  @Override
  public JaxbContextBuilder withSchemaMode(final SchemaMode schemaMode) {
    if (!isEmpty(schemaMode)) {
      this.schemaMode = schemaMode;
    }
    return this;
  }

  @Override
  public JaxbContextBuilder withSchemaBuilder(SchemaBuilder schemaBuilder) {
    if (!isEmpty(schemaBuilder)) {
      this.schemaBuilder = schemaBuilder;
    }
    return this;
  }

  @Override
  public JaxbContextBuilder withDependenciesResolver(final JaxbDependenciesResolver resolver) {
    if ((isEmpty(dependenciesResolver) && !isEmpty(resolver))
        || (!isEmpty(dependenciesResolver) && isEmpty(resolver))
        || (!isEmpty(dependenciesResolver)
        && !ClassUtils.getUserClass(dependenciesResolver)
        .equals(ClassUtils.getUserClass(resolver)))) {
      clearCache();
    }
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

    if (isEmpty(xmlAdapters)) {
      this.xmlAdapters = null;
    } else {
      this.xmlAdapters = xmlAdapters
          .stream()
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
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
    return Optional.ofNullable(data)
        .map(d -> {
          clearCache();
          jaxbContextDataMap.put(data.getPackageName(), data);
          return this;
        })
        .orElse(this);
  }


  @Override
  public boolean canUnmarshalWithoutExtending(Class<?> clazz) {
    return Optional.ofNullable(clazz)
        .filter(c -> allClassesAreSupported(new Class<?>[]{c}))
        .isPresent();
  }

  @Override
  public boolean canMarshalWithoutExtending(Class<?> clazz) {
    return Optional.ofNullable(clazz)
        .filter(c -> allClassesAreSupported(new Class<?>[]{c}))
        .isPresent();
  }

  @Override
  public Unmarshaller buildUnmarshaller(final Object value) {
    final JaxbContextWrapper jaxbContext;
    if (value instanceof Class<?>[]) {
      if (allClassesAreSupported((Class<?>[]) value)) {
        jaxbContext = computeJaxbContext(null);
      } else {
        jaxbContext = computeJaxbContext(value);
      }
    } else if (value instanceof Class<?>) {
      return buildUnmarshaller(new Class[]{(Class<?>) value});
    } else if (value == null
        || allClassesAreSupported(new Class[]{ClassUtils.getUserClass(value)})) {
      jaxbContext = computeJaxbContext(null);
    } else {
      jaxbContext = computeJaxbContext(value);
    }
    final SchemaMode mode = jaxbContext.getSchemaMode();
    if (mode == SchemaMode.ALWAYS
        || mode == SchemaMode.UNMARSHAL
        || (mode == SchemaMode.EXTERNAL_XSD
        && !isEmpty(jaxbContext.getDetails().getSchemaLocation()))) {
      jaxbContext.setSchema(computeSchema(jaxbContext));
    }
    try {
      return jaxbContext.createUnmarshaller();

    } catch (JAXBException e) {
      throw new JaxbRuntimeException(e);
    }
  }

  @Override
  public Marshaller buildMarshaller(final Object value) {
    final JaxbContextWrapper jaxbContext = computeJaxbContext(value);
    final SchemaMode mode = jaxbContext.getSchemaMode();
    if (mode == SchemaMode.ALWAYS
        || mode == SchemaMode.MARSHAL
        || (mode == SchemaMode.EXTERNAL_XSD
        && !isEmpty(jaxbContext.getDetails().getSchemaLocation()))) {
      jaxbContext.setSchema(computeSchema(jaxbContext));
    }
    try {
      return jaxbContext.createMarshaller();
    } catch (JAXBException e) {
      throw new JaxbRuntimeException(e);
    }
  }

  @Override
  public JaxbContextBuilder initJaxbContext() {
    if (!jaxbContextDataMap.isEmpty()) {
      buildJaxbContext(null);
    }
    return this;
  }

  @Override
  public JaxbContextWrapper buildJaxbContext(final Object value) {
    final JaxbContextWrapper wrapper = computeJaxbContext(value);
    final SchemaMode mode = wrapper.getSchemaMode();
    if (mode == SchemaMode.ALWAYS
        || mode == SchemaMode.MARSHAL
        || mode == SchemaMode.UNMARSHAL
        || (mode == SchemaMode.EXTERNAL_XSD
        && !isEmpty(wrapper.getDetails().getSchemaLocation()))) {
      wrapper.setSchema(computeSchema(wrapper));
    }
    return wrapper;
  }

  @Override
  public Schema buildSchema(final Object value) {
    return computeSchema(value);
  }

  private JaxbContextDetails buildDetails() {
    return jaxbContextDataMap.values().stream()
        .map(data -> JaxbContextDetails.builder().add(data))
        .reduce((a, b) -> a.merge(b.build()))
        .map(JaxbContextDetailsBuilder::build)
        .orElseGet(() -> JaxbContextDetails.builder().build());
  }

  private JaxbContextDetails buildDetails(final Object value) {
    if (isEmpty(value)) {
      return buildDetails();
    }
    if (value instanceof Class<?>) {
      return buildDetails(new Class<?>[]{(Class<?>) value});
    }
    final Class<?>[] classes;
    if (value instanceof Class<?>[]) {
      classes = isEmpty(dependenciesResolver)
          ? (Class<?>[]) value
          : dependenciesResolver.resolveClasses(value);
    } else {
      classes = isEmpty(dependenciesResolver)
          ? new Class<?>[]{value.getClass()}
          : dependenciesResolver.resolveClasses(value);
    }
    return buildDetailsWithClasses(classes);
  }

  private JaxbContextDetails buildDetailsWithClasses(final Class<?>[] classes) {
    JaxbContextDetails details = Arrays.stream(classes)
        .map(clazz -> Optional
            .ofNullable(jaxbContextDataMap.get(clazz.getPackage().getName()))
            .or(() -> JaxbContextData.fromClass(clazz))
            .map(data -> {
              if (!jaxbContextDataMap.containsKey(data.getPackageName())) {
                add(data);
              }
              return JaxbContextDetails.builder().add(data);
            })
            .orElseGet(() -> JaxbContextDetails.builder().add(clazz)))
        .reduce((a, b) -> a.merge(b.build()))
        .map(JaxbContextDetailsBuilder::build)
        .orElseThrow(() -> new JaxbRuntimeException(
            "There are no classes to build the jaxb context details."));
    return isEmpty(details.getClasses()) ? details : JaxbContextDetails.builder()
        .add(classes)
        .addSchemaLocation(details.getSchemaLocation())
        .build();
  }

  private boolean allClassesAreSupported(final Class<?>[] classes) {
    return Arrays.stream(classes)
        .map(clazz -> clazz.getPackage().getName())
        .allMatch(jaxbContextDataMap::containsKey);
  }

  private JaxbContextWrapper computeJaxbContext(final Object value) {
    final JaxbContextDetails details = buildDetails(value);
    final JAXBContext jaxbContext = jaxbContextCache.computeIfAbsent(details, key -> {
      try {
        return isEmpty(key.getClasses())
            ? JAXBContext.newInstance(key.getContextPath(), getContextClassLoader())
            : JAXBContext.newInstance(key.getClasses());

      } catch (final Exception e) {
        throw new JaxbRuntimeException(
            String.format("Creating jaxb context failed with builder context: %s", key),
            e);
      }
    });
    final JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext, details);
    wrapper.setAttachmentMarshaller(attachmentMarshaller);
    wrapper.setAttachmentUnmarshaller(attachmentUnmarshaller);
    wrapper.setFormattedOutput(formattedOutput);
    wrapper.setValidationEventHandler(validationEventHandler);
    wrapper.setXmlAdapters(xmlAdapters);
    wrapper.setSchemaMode(schemaMode);
    return wrapper;
  }

  private Schema computeSchema(final Object value) {
    return computeSchema(computeJaxbContext(value));
  }

  private Schema computeSchema(final JaxbContextWrapper jaxbContext) {
    final JaxbContextDetails details = jaxbContext.getDetails();
    return schemaCache.computeIfAbsent(details, key -> {
      final SchemaSourcesResolver sourcesResolver = new SchemaSourcesResolver();
      try {
        jaxbContext.generateSchema(sourcesResolver);
      } catch (Exception e) {
        throw new JaxbRuntimeException(e);
      }
      final List<Source> sources = new ArrayList<>(
          sourcesResolver.toSources(key.getNameSpaces()));
      final Set<String> locations = key.getSchemaLocations();
      sources.addAll(schemaBuilder.fetchSchemaSources(locations));
      return schemaBuilder.buildSchema(sources);
    });
  }

}
