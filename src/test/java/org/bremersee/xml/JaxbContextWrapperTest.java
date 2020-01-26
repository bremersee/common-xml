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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.MarshalException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.validation.Schema;
import org.bremersee.xml.adapter.OffsetDateTimeXmlAdapter;
import org.bremersee.xml.model2.Vehicle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.w3c.dom.Node;

/**
 * The jaxb context wrapper test.
 *
 * @author Christian Bremer
 */
class JaxbContextWrapperTest {

  private static JAXBContext jaxbContext;

  private static Map<String, JaxbContextData> map;

  /**
   * Sets up.
   *
   * @throws Exception the exception
   */
  @BeforeAll
  static void setUp() throws Exception {
    jaxbContext = JAXBContext.newInstance(
        org.bremersee.xml.model2.ObjectFactory.class.getPackage().getName()
            + ":"
            + org.bremersee.xml.model5.ObjectFactory.class.getPackage().getName());

    map = new HashMap<>();
    map.put(org.bremersee.xml.model2.ObjectFactory.class.getPackage().getName(),
        new JaxbContextData(org.bremersee.xml.model2.ObjectFactory.class.getPackage(),
            "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd"));
    map.put(org.bremersee.xml.model5.ObjectFactory.class.getPackage().getName(),
        new JaxbContextData(org.bremersee.xml.model5.ObjectFactory.class.getPackage()));
  }

  /**
   * Gets details.
   */
  @Test
  void getDetails() {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    assertNotNull(wrapper.getDetails());

    assertEquals(wrapper, wrapper);
    assertEquals(wrapper, new JaxbContextWrapper(jaxbContext));
    assertEquals(wrapper.hashCode(), new JaxbContextWrapper(jaxbContext).hashCode());
    assertEquals(wrapper.toString(), new JaxbContextWrapper(jaxbContext).toString());
    assertNotEquals(wrapper, null);
    assertNotEquals(wrapper, new Object());

    JaxbContextBuilderDetails details = new JaxbContextBuilderDetailsImpl(null, map);
    wrapper = new JaxbContextWrapper(jaxbContext, details);
    assertNotNull(wrapper.getDetails());
    assertEquals(details, wrapper.getDetails());

    assertEquals(wrapper, wrapper);
    assertEquals(wrapper, new JaxbContextWrapper(jaxbContext, details));
    assertEquals(wrapper.hashCode(), new JaxbContextWrapper(jaxbContext, details).hashCode());
    assertEquals(wrapper.toString(), new JaxbContextWrapper(jaxbContext, details).toString());
  }

  /**
   * Is formatted output.
   */
  @Test
  void isFormattedOutput() {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    wrapper.setFormattedOutput(false);
    assertFalse(wrapper.isFormattedOutput());
    assertTrue(wrapper.toString().contains("false"));

    wrapper.setFormattedOutput(true);
    assertTrue(wrapper.isFormattedOutput());
    assertTrue(wrapper.toString().contains("true"));
  }

  /**
   * Gets xml adapters.
   */
  @Test
  void getXmlAdapters() {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    assertNull(wrapper.getXmlAdapters());

    OffsetDateTimeXmlAdapter adapter = new OffsetDateTimeXmlAdapter();
    wrapper.setXmlAdapters(Collections.singletonList(adapter));
    assertEquals(Collections.singletonList(adapter), wrapper.getXmlAdapters());
  }

  /**
   * Gets attachment marshaller.
   */
  @Test
  void getAttachmentMarshaller() {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    assertNull(wrapper.getAttachmentMarshaller());

    AttachmentMarshaller marshaller = Mockito.mock(AttachmentMarshaller.class);
    wrapper.setAttachmentMarshaller(marshaller);
    assertNotNull(wrapper.getAttachmentMarshaller());
  }

  /**
   * Gets attachment unmarshaller.
   */
  @Test
  void getAttachmentUnmarshaller() {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    assertNull(wrapper.getAttachmentUnmarshaller());

    AttachmentUnmarshaller unmarshaller = Mockito.mock(AttachmentUnmarshaller.class);
    wrapper.setAttachmentUnmarshaller(unmarshaller);
    assertNotNull(wrapper.getAttachmentUnmarshaller());
  }

  /**
   * Use schema.
   *
   * @throws Exception the exception
   */
  @Test
  void useSchema() throws Exception {
    JaxbContextBuilderDetails details = new JaxbContextBuilderDetailsImpl(null, map);
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext, details);
    assertNull(wrapper.getSchema());

    wrapper.setFormattedOutput(false);
    wrapper.setSchemaMode(SchemaMode.EXTERNAL_XSD);

    Schema schema = SchemaBuilder.builder().buildSchema(
        "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd");
    wrapper.setSchema(schema);
    assertEquals(schema, wrapper.getSchema());

    Vehicle model = new Vehicle();
    model.setModel("Diabolo");

    String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<vehicle xsi:schemaLocation=\"http://bremersee.org/xmlschemas/common-xml-test-model-2 "
        + "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd "
        + "http://bremersee.org/xmlschemas/common-xml-test-model-5 "
        + "http://bremersee.github.io/xmlschemas/common-xml-test-model-5.xsd\" "
        + "xmlns=\"http://bremersee.org/xmlschemas/common-xml-test-model-2\" "
        + "xmlns:ns2=\"http://bremersee.org/xmlschemas/common-xml-test-model-5\" "
        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
        + "<model>Diabolo</model></vehicle>";

    StringWriter sw = new StringWriter();
    wrapper.createMarshaller().marshal(model, sw);
    String actual = sw.toString();
    assertEquals(expected, actual);

    wrapper.setSchemaMode(SchemaMode.UNMARSHAL);
    Vehicle actualModel = (Vehicle) wrapper.createUnmarshaller()
        .unmarshal(new StringReader(actual));
    assertEquals(model, actualModel);
  }

  /**
   * Use schema and expect validation fails.
   */
  @Test
  void useSchemaAndExpectValidationFails() {
    JaxbContextBuilderDetails details = new JaxbContextBuilderDetailsImpl(null, map);
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext, details);
    assertNull(wrapper.getSchema());

    wrapper.setFormattedOutput(false);
    wrapper.setSchemaMode(SchemaMode.MARSHAL);

    Schema schema = SchemaBuilder.builder().buildSchema(
        "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd");
    wrapper.setSchema(schema);
    assertEquals(schema, wrapper.getSchema());

    Vehicle model = new Vehicle();
    model.setModel("Diabolo3");

    assertThrows(
        MarshalException.class,
        () -> wrapper.createMarshaller().marshal(model, new StringWriter()));

    String invalid = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<vehicle xsi:schemaLocation=\"http://bremersee.org/xmlschemas/common-xml-test-model-2 "
        + "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd "
        + "http://bremersee.org/xmlschemas/common-xml-test-model-5 "
        + "http://bremersee.github.io/xmlschemas/common-xml-test-model-5.xsd\" "
        + "xmlns=\"http://bremersee.org/xmlschemas/common-xml-test-model-2\" "
        + "xmlns:ns2=\"http://bremersee.org/xmlschemas/common-xml-test-model-5\" "
        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
        + "<model>Diabolo3</model></vehicle>";

    wrapper.setSchemaMode(SchemaMode.ALWAYS);
    assertThrows(
        UnmarshalException.class,
        () -> wrapper.createUnmarshaller().unmarshal(new StringReader(invalid)));
  }

  /**
   * Gets validation event handler.
   */
  @Test
  void getValidationEventHandler() {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    assertNull(wrapper.getValidationEventHandler());

    ValidationEventHandler handler = Mockito.mock(ValidationEventHandler.class);
    wrapper.setValidationEventHandler(handler);
    assertNotNull(wrapper.getValidationEventHandler());
  }

  /**
   * Gets schema mode.
   */
  @Test
  void getSchemaMode() {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    assertEquals(SchemaMode.NEVER, wrapper.getSchemaMode());

    wrapper.setSchemaMode(SchemaMode.EXTERNAL_XSD);
    assertEquals(SchemaMode.EXTERNAL_XSD, wrapper.getSchemaMode());
  }

  /**
   * Create validator.
   */
  @Test
  void createValidator() {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    assertThrows(UnsupportedOperationException.class, wrapper::createValidator);
  }

  /**
   * Create binder.
   */
  @Test
  void createBinder() {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    assertNotNull(wrapper.createBinder(Node.class));
  }

  /**
   * Create jaxb introspector.
   */
  @Test
  void createJaxbIntrospector() {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    assertNotNull(wrapper.createJAXBIntrospector());
  }

  /**
   * Generate schema.
   *
   * @throws Exception the exception
   */
  @Test
  void generateSchema() throws Exception {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    BufferSchemaOutputResolver resolver = new BufferSchemaOutputResolver();
    wrapper.generateSchema(resolver);
    assertNotNull(resolver.toString());
  }
}