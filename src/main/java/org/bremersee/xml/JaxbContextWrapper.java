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
import org.springframework.util.StringUtils;

/**
 * This {@link JAXBContext} will be returned by the {@link JaxbContextBuilder}.
 *
 * @author Christian Bremer
 */
@SuppressWarnings({"unused", "deprecation"})
public class JaxbContextWrapper extends JAXBContext {

  private final JAXBContext jaxbContext;

  private final JaxbContextBuilderDetails details;

  private boolean formattedOutput;

  private List<XmlAdapter<?, ?>> xmlAdapters;

  private AttachmentMarshaller attachmentMarshaller;

  private AttachmentUnmarshaller attachmentUnmarshaller;

  private ValidationEventHandler validationEventHandler;

  private Schema schema;

  private SchemaMode schemaMode = SchemaMode.NEVER;

  public JaxbContextWrapper(
      final JAXBContext jaxbContext) {
    this(jaxbContext, null);
  }

  public JaxbContextWrapper(
      final JAXBContext jaxbContext,
      final JaxbContextBuilderDetails details) {
    this.jaxbContext = jaxbContext;
    this.details = details != null ? details : new JaxbContextBuilderDetailsImpl();
  }

  public JaxbContextBuilderDetails getDetails() {
    return details;
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
  public JaxbContextWrapper withFormattedOutput(boolean formattedOutput) {
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
  public JaxbContextWrapper withXmlAdapters(
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
  public JaxbContextWrapper withAttachmentMarshaller(AttachmentMarshaller attachmentMarshaller) {
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
  public JaxbContextWrapper withAttachmentUnmarshaller(
      AttachmentUnmarshaller attachmentUnmarshaller) {
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
  public JaxbContextWrapper withSchema(Schema schema) {
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
  public JaxbContextWrapper withValidationEventHandler(
      ValidationEventHandler validationEventHandler) {
    this.validationEventHandler = validationEventHandler;
    return this;
  }

  public SchemaMode getSchemaMode() {
    return schemaMode;
  }

  public JaxbContextWrapper withSchemaMode(SchemaMode schemaMode) {
    if (schemaMode != null) {
      this.schemaMode = schemaMode;
    }
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
    if (schema != null && (schemaMode == SchemaMode.ALWAYS
        || schemaMode == SchemaMode.UNMARSHAL
        || (schemaMode == SchemaMode.EXTERNAL_XSD
        && StringUtils.hasText(details.getSchemaLocation())))) {
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
    if (StringUtils.hasText(details.getSchemaLocation())) {
      marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, details.getSchemaLocation());
    }
    if (xmlAdapters != null) {
      xmlAdapters.forEach(marshaller::setAdapter);
    }
    if (attachmentMarshaller != null) {
      marshaller.setAttachmentMarshaller(attachmentMarshaller);
    }
    if (schema != null && (schemaMode == SchemaMode.ALWAYS
        || schemaMode == SchemaMode.MARSHAL
        || (schemaMode == SchemaMode.EXTERNAL_XSD
        && StringUtils.hasText(details.getSchemaLocation())))) {
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
