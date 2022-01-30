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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.Validator;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.validation.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.Assert;

/**
 * This {@link JAXBContext} will be returned by the {@link JaxbContextBuilder}.
 *
 * @author Christian Bremer
 */
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"jaxbContext"})
public class JaxbContextWrapper extends JAXBContext {

  private final JAXBContext jaxbContext;

  @Getter
  private final JaxbContextDetails details;

  @Getter
  @Setter
  private boolean formattedOutput;

  @Getter
  @Setter
  private List<XmlAdapter<?, ?>> xmlAdapters;

  @Getter
  @Setter
  private AttachmentMarshaller attachmentMarshaller;

  @Getter
  @Setter
  private AttachmentUnmarshaller attachmentUnmarshaller;

  @Getter
  @Setter
  private ValidationEventHandler validationEventHandler;

  @Getter
  @Setter
  private Schema schema;

  @Getter
  @Setter
  @NonNull
  private SchemaMode schemaMode = SchemaMode.NEVER;

  /**
   * Instantiates a new jaxb context wrapper.
   *
   * @param jaxbContext the jaxb context
   */
  public JaxbContextWrapper(
      JAXBContext jaxbContext) {
    this(jaxbContext, null);
  }

  /**
   * Instantiates a new jaxb context wrapper.
   *
   * @param data the data
   * @param classLoaders the class loaders
   * @throws JAXBException the jaxb exception
   */
  public JaxbContextWrapper(Stream<JaxbContextData> data, ClassLoader... classLoaders)
      throws JAXBException {
    Assert.notNull(data, "Stream of jaxb context data must be present.");
    this.details = data.collect(JaxbContextDetails.contextDataCollector());
    Assert.isTrue(!details.isEmpty(), "There is no jaxb model.");
    this.jaxbContext = JAXBContext.newInstance(this.details.getClasses(classLoaders));
  }

  /**
   * Instantiates a new jaxb context wrapper.
   *
   * @param jaxbContext the jaxb context
   * @param details the details
   */
  JaxbContextWrapper(
      JAXBContext jaxbContext,
      JaxbContextDetails details) {
    Assert.notNull(jaxbContext, "Jaxb context must be present.");
    this.jaxbContext = jaxbContext;
    this.details = Optional.ofNullable(details)
        .orElseGet(JaxbContextDetails::empty);
  }

  @Override
  public Unmarshaller createUnmarshaller() throws JAXBException {
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    if (!isEmpty(xmlAdapters)) {
      xmlAdapters.forEach(unmarshaller::setAdapter);
    }
    if (!isEmpty(attachmentUnmarshaller)) {
      unmarshaller.setAttachmentUnmarshaller(attachmentUnmarshaller);
    }
    if (!isEmpty(schema) && (schemaMode == SchemaMode.ALWAYS
        || schemaMode == SchemaMode.UNMARSHAL
        || schemaMode == SchemaMode.EXTERNAL_XSD
        && !isEmpty(details.getSchemaLocation()))) {
      unmarshaller.setSchema(schema);
    }
    if (!isEmpty(validationEventHandler)) {
      unmarshaller.setEventHandler(validationEventHandler);
    }
    return unmarshaller;
  }

  @Override
  public Marshaller createMarshaller() throws JAXBException {
    Marshaller marshaller = jaxbContext.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
    if (!isEmpty(details.getSchemaLocation())) {
      marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, details.getSchemaLocation());
    }
    if (!isEmpty(xmlAdapters)) {
      xmlAdapters.forEach(marshaller::setAdapter);
    }
    if (!isEmpty(attachmentMarshaller)) {
      marshaller.setAttachmentMarshaller(attachmentMarshaller);
    }
    if (!isEmpty(schema) && (schemaMode == SchemaMode.ALWAYS
        || schemaMode == SchemaMode.MARSHAL
        || schemaMode == SchemaMode.EXTERNAL_XSD
        && !isEmpty(details.getSchemaLocation()))) {
      marshaller.setSchema(schema);
    }
    if (!isEmpty(validationEventHandler)) {
      marshaller.setEventHandler(validationEventHandler);
    }
    return marshaller;
  }

  @Override
  public Validator createValidator() throws JAXBException {
    return jaxbContext.createValidator();
  }

  @Override
  public <T> Binder<T> createBinder(Class<T> domType) {
    return jaxbContext.createBinder(domType);
  }

  @Override
  public JAXBIntrospector createJAXBIntrospector() {
    return jaxbContext.createJAXBIntrospector();
  }

  @Override
  public void generateSchema(SchemaOutputResolver outputResolver) throws IOException {
    jaxbContext.generateSchema(outputResolver);
  }

}
