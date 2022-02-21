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
import lombok.ToString;
import org.springframework.util.ClassUtils;

/**
 * The jaxb context builder.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("SameNameButDifferent")
@ToString
class JaxbContextBuilderImpl implements JaxbContextBuilder {

  /**
   * Key is package name, value is jaxb data set.
   */
  private final Map<Object, JaxbContextData> jaxbContextDataMap = new ConcurrentHashMap<>();

  private final Map<JaxbContextDetails, Schema> schemaCache = new ConcurrentHashMap<>();

  private final Map<JaxbContextDetails, JAXBContext> jaxbContextCache
      = new ConcurrentHashMap<>();

  private JaxbDependenciesResolver dependenciesResolver = DEFAULT_DEPENDENCIES_RESOLVER;

  private SchemaBuilder schemaBuilder = SchemaBuilder.newInstance();

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

  @Override
  public JaxbContextBuilder copy() {
    JaxbContextBuilderImpl copy = new JaxbContextBuilderImpl();
    copy.dependenciesResolver = dependenciesResolver;
    copy.schemaMode = schemaMode;
    copy.schemaCache.putAll(schemaCache);
    copy.attachmentMarshaller = attachmentMarshaller;
    copy.attachmentUnmarshaller = attachmentUnmarshaller;
    copy.classLoader = classLoader;
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
  public JaxbContextBuilder withSchemaMode(SchemaMode schemaMode) {
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
  public JaxbContextBuilder withDependenciesResolver(JaxbDependenciesResolver resolver) {
    if ((isEmpty(dependenciesResolver) && !isEmpty(resolver))
        || (!isEmpty(dependenciesResolver) && isEmpty(resolver))
        || (!isEmpty(dependenciesResolver) && !ClassUtils.getUserClass(dependenciesResolver)
        .equals(ClassUtils.getUserClass(resolver)))) {
      clearCache();
    }
    this.dependenciesResolver = resolver;
    return this;
  }

  @Override
  public JaxbContextBuilder withContextClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  @Override
  public JaxbContextBuilder withFormattedOutput(boolean formattedOutput) {
    this.formattedOutput = formattedOutput;
    return this;
  }

  @Override
  public JaxbContextBuilder withXmlAdapters(
      Collection<? extends XmlAdapter<?, ?>> xmlAdapters) {

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
      AttachmentMarshaller attachmentMarshaller) {
    this.attachmentMarshaller = attachmentMarshaller;
    return this;
  }

  @Override
  public JaxbContextBuilder withAttachmentUnmarshaller(
      AttachmentUnmarshaller attachmentUnmarshaller) {
    this.attachmentUnmarshaller = attachmentUnmarshaller;
    return this;
  }

  @Override
  public JaxbContextBuilder withValidationEventHandler(
      ValidationEventHandler validationEventHandler) {
    this.validationEventHandler = validationEventHandler;
    return this;
  }

  @Override
  public JaxbContextBuilder add(JaxbContextMember data) {
    return Optional.ofNullable(data)
        .map(JaxbContextData::new)
        .map(d -> {
          clearCache();
          jaxbContextDataMap.put(d.getKey(), d);
          return this;
        })
        .orElse(this);
  }


  @Override
  public Unmarshaller buildUnmarshaller(Class<?>... classes) {
    if (!isEmpty(classes)) {
      Class<?>[] jaxbClasses = isEmpty(dependenciesResolver)
          ? classes
          : dependenciesResolver.resolveClasses(classes);
      Arrays.stream(jaxbClasses)
          .map(JaxbContextData::new)
          .forEach(data -> jaxbContextDataMap.computeIfAbsent(data.getKey(), key -> {
            clearCache();
            return data;
          }));
    }
    JaxbContextWrapper jaxbContext = computeJaxbContext(null);
    SchemaMode mode = jaxbContext.getSchemaMode();
    if (mode == SchemaMode.ALWAYS
        || mode == SchemaMode.UNMARSHAL
        || mode == SchemaMode.EXTERNAL_XSD
        && !isEmpty(jaxbContext.getDetails().getSchemaLocation())) {
      jaxbContext.setSchema(computeSchema(jaxbContext));
    }
    try {
      return jaxbContext.createUnmarshaller();

    } catch (JAXBException e) {
      throw new JaxbRuntimeException(e);
    }
  }

  @Override
  public Marshaller buildMarshaller(Object value) {
    JaxbContextWrapper jaxbContext = computeJaxbContext(value);
    SchemaMode mode = jaxbContext.getSchemaMode();
    if ((mode == SchemaMode.ALWAYS
        || mode == SchemaMode.MARSHAL
        || mode == SchemaMode.EXTERNAL_XSD)
        && !isEmpty(jaxbContext.getDetails().getSchemaLocation())) {
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
  public JaxbContextWrapper buildJaxbContext(Object value) {
    JaxbContextWrapper wrapper = computeJaxbContext(value);
    SchemaMode mode = wrapper.getSchemaMode();
    if ((mode == SchemaMode.ALWAYS
        || mode == SchemaMode.MARSHAL
        || mode == SchemaMode.UNMARSHAL
        || mode == SchemaMode.EXTERNAL_XSD)
        && !isEmpty(wrapper.getDetails().getSchemaLocation())) {
      wrapper.setSchema(computeSchema(wrapper));
    }
    return wrapper;
  }

  @Override
  public Schema buildSchema(Object value) {
    return computeSchema(value);
  }

  private JaxbContextDetails buildDetails() {
    return jaxbContextDataMap.values().stream()
        .collect(JaxbContextDetails.contextDataCollector());
  }

  private JaxbContextDetails buildDetails(Object value) {
    if (isEmpty(value)) {
      return buildDetails();
    }
    if (value instanceof Class<?>) {
      return buildDetails(new Class<?>[]{(Class<?>) value});
    }
    Class<?>[] classes;
    if (value instanceof Class<?>[]) {
      classes = isEmpty(dependenciesResolver)
          ? (Class<?>[]) value
          : dependenciesResolver.resolveClasses(value);
    } else {
      classes = isEmpty(dependenciesResolver)
          ? new Class<?>[]{value.getClass()}
          : dependenciesResolver.resolveClasses(value);
    }
    return Arrays.stream(classes)
        .map(JaxbContextData::new)
        .map(data -> jaxbContextDataMap.computeIfAbsent(data.getKey(), key -> {
          clearCache();
          return data;
        }))
        .collect(JaxbContextDetails.contextDataCollector());
  }

  private JaxbContextWrapper computeJaxbContext(Object value) {
    JaxbContextDetails details = buildDetails(value);
    JAXBContext jaxbContext = jaxbContextCache.computeIfAbsent(details, key -> {
      try {
        return isEmpty(classLoader)
            ? JAXBContext.newInstance(key.getClasses())
            : JAXBContext.newInstance(key.getClasses(classLoader));

      } catch (Exception e) {
        throw new JaxbRuntimeException(
            String.format("Creating jaxb context failed with builder context: %s", key),
            e);
      }
    });
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext, details);
    wrapper.setAttachmentMarshaller(attachmentMarshaller);
    wrapper.setAttachmentUnmarshaller(attachmentUnmarshaller);
    wrapper.setFormattedOutput(formattedOutput);
    wrapper.setValidationEventHandler(validationEventHandler);
    wrapper.setXmlAdapters(xmlAdapters);
    wrapper.setSchemaMode(schemaMode);
    return wrapper;
  }

  private Schema computeSchema(Object value) {
    return computeSchema(computeJaxbContext(value));
  }

  private Schema computeSchema(JaxbContextWrapper jaxbContext) {
    JaxbContextDetails details = jaxbContext.getDetails();
    return schemaCache.computeIfAbsent(details, key -> {
      SchemaSourcesResolver sourcesResolver = new SchemaSourcesResolver();
      try {
        jaxbContext.generateSchema(sourcesResolver);
      } catch (Exception e) {
        throw new JaxbRuntimeException(e);
      }
      List<Source> sources = new ArrayList<>(
          sourcesResolver.toSources(key.getNameSpaces()));
      Set<String> locations = key.getSchemaLocations();
      sources.addAll(schemaBuilder.fetchSchemaSources(locations));
      return schemaBuilder.buildSchema(sources);
    });
  }

}
