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
import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

/**
 * This {@link JAXBContext} will be returned by the {@link JaxbContextBuilder}.
 *
 * @author Christian Bremer
 */
@SuppressWarnings({"unused", "deprecation"})
class SchemaLocationAwareJaxbContext extends JAXBContext implements JaxbContextDetailsAware {

  private final JAXBContext jaxbContext;

  private final String contextPath;

  private final String schemaLocation;

  private boolean formattedOutput;

  /**
   * Instantiates a new schema location aware jaxb context.
   *
   * @param jaxbContext the jaxb context
   * @param contextPath the context path
   * @param schemaLocation the schema location
   * @param formattedOutput the formatted output
   */
  SchemaLocationAwareJaxbContext(
      final JAXBContext jaxbContext,
      final String contextPath,
      final String schemaLocation,
      final boolean formattedOutput) {
    this.jaxbContext = jaxbContext;
    this.contextPath = contextPath;
    this.schemaLocation = schemaLocation;
    this.formattedOutput = formattedOutput;
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
  public void setFormattedOutput(boolean formattedOutput) {
    this.formattedOutput = formattedOutput;
  }

  @Override
  public Unmarshaller createUnmarshaller() throws JAXBException {
    return jaxbContext.createUnmarshaller();
  }

  @Override
  public Marshaller createMarshaller() throws JAXBException {
    final Marshaller marshaller = jaxbContext.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
    if (schemaLocation != null && schemaLocation.trim().length() > 0) {
      marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation);
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
