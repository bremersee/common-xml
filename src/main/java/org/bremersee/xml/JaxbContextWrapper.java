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
import java.util.List;
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

/**
 * This {@link JAXBContext} will be returned by the {@link JaxbContextBuilder}.
 *
 * @author Christian Bremer
 */
@SuppressWarnings({"unused", "deprecation"})
class JaxbContextWrapper extends JAXBContext implements JaxbContextDetailsAware {

  private final JAXBContext jaxbContext;

  private final String contextPath;

  private final String schemaLocation;

  private boolean formattedOutput;

  private List<XmlAdapter<?, ?>> xmlAdapters;

  private AttachmentMarshaller attachmentMarshaller;

  private AttachmentUnmarshaller attachmentUnmarshaller;

  private Schema schema;

  private ValidationEventHandler validationEventHandler;

  /**
   * Instantiates a new jaxb context wrapper.
   *
   * @param jaxbContext the jaxb context
   * @param contextPath the context path
   * @param schemaLocation the schema location
   */
  JaxbContextWrapper(
      final JAXBContext jaxbContext,
      final String contextPath,
      final String schemaLocation) {
    this.jaxbContext = jaxbContext;
    this.contextPath = contextPath;
    this.schemaLocation = schemaLocation;
  }

  @Override
  public String getContextPath() {
    return contextPath;
  }

  @Override
  public String getSchemaLocation() {
    return schemaLocation;
  }

  /**
   * Return {@code true} if the output is formatted, otherwise {@code false}.
   *
   * @return {@code true} if the output is formatted, otherwise {@code false}
   */
  public boolean isFormattedOutput() {
    return formattedOutput;
  }

  /**
   * Sets formatted output.
   *
   * @param formattedOutput the formatted output
   */
  JaxbContextWrapper withFormattedOutput(boolean formattedOutput) {
    this.formattedOutput = formattedOutput;
    return this;
  }

  /**
   * Gets xml adapters.
   *
   * @return the xml adapters
   */
  public List<XmlAdapter<?, ?>> getXmlAdapters() {
    return xmlAdapters;
  }

  /**
   * Sets xml adapters.
   *
   * @param xmlAdapters the xml adapters
   */
  JaxbContextWrapper withXmlAdapters(
      List<XmlAdapter<?, ?>> xmlAdapters) {
    this.xmlAdapters = xmlAdapters;
    return this;
  }

  /**
   * Gets attachment marshaller.
   *
   * @return the attachment marshaller
   */
  public AttachmentMarshaller getAttachmentMarshaller() {
    return attachmentMarshaller;
  }

  /**
   * Sets attachment marshaller.
   *
   * @param attachmentMarshaller the attachment marshaller
   */
  JaxbContextWrapper withAttachmentMarshaller(AttachmentMarshaller attachmentMarshaller) {
    this.attachmentMarshaller = attachmentMarshaller;
    return this;
  }

  /**
   * Gets attachment unmarshaller.
   *
   * @return the attachment unmarshaller
   */
  public AttachmentUnmarshaller getAttachmentUnmarshaller() {
    return attachmentUnmarshaller;
  }

  /**
   * Sets attachment unmarshaller.
   *
   * @param attachmentUnmarshaller the attachment unmarshaller
   */
  JaxbContextWrapper withAttachmentUnmarshaller(AttachmentUnmarshaller attachmentUnmarshaller) {
    this.attachmentUnmarshaller = attachmentUnmarshaller;
    return this;
  }

  /**
   * Gets schema.
   *
   * @return the schema
   */
  public Schema getSchema() {
    return schema;
  }

  /**
   * Sets schema.
   *
   * @param schema the schema
   */
  JaxbContextWrapper withSchema(Schema schema) {
    this.schema = schema;
    return this;
  }

  /**
   * Gets validation event handler.
   *
   * @return the validation event handler
   */
  public ValidationEventHandler getValidationEventHandler() {
    return validationEventHandler;
  }

  /**
   * Sets validation event handler.
   *
   * @param validationEventHandler the validation event handler
   */
  JaxbContextWrapper withValidationEventHandler(ValidationEventHandler validationEventHandler) {
    this.validationEventHandler = validationEventHandler;
    return this;
  }

  @Override
  public Unmarshaller createUnmarshaller() throws JAXBException {
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    if (xmlAdapters != null) {
      xmlAdapters.forEach(unmarshaller::setAdapter);
    }
    if (attachmentUnmarshaller != null) {
      unmarshaller.setAttachmentUnmarshaller(attachmentUnmarshaller);
    }
    if (schema != null) {
      unmarshaller.setSchema(schema);
    }
    if (validationEventHandler != null) {
      unmarshaller.setEventHandler(validationEventHandler);
    }
    return unmarshaller;
  }

  @Override
  public Marshaller createMarshaller() throws JAXBException {
    final Marshaller marshaller = jaxbContext.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
    if (schemaLocation != null && schemaLocation.trim().length() > 0) {
      marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation);
    }
    if (xmlAdapters != null) {
      xmlAdapters.forEach(marshaller::setAdapter);
    }
    if (attachmentMarshaller != null) {
      marshaller.setAttachmentMarshaller(attachmentMarshaller);
    }
    if (schema != null) {
      marshaller.setSchema(schema);
    }
    if (validationEventHandler != null) {
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
