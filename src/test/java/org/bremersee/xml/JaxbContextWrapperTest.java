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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.stream.Stream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.validation.Schema;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.xml.adapter.OffsetDateTimeXmlAdapter;
import org.bremersee.xml.model2.Vehicle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.w3c.dom.Node;

/**
 * The jaxb context wrapper test.
 *
 * @author Christian Bremer
 */
@ExtendWith({SoftAssertionsExtension.class})
class JaxbContextWrapperTest {

  private static JAXBContext jaxbContext;

  private static JaxbContextDetails detailsOfJaxbContext;

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
    JaxbContextData data0 = new JaxbContextData(
        org.bremersee.xml.model2.ObjectFactory.class.getPackage(),
        "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd");
    JaxbContextData data1 = new JaxbContextData(
        org.bremersee.xml.model5.ObjectFactory.class.getPackage());
    detailsOfJaxbContext = Stream.of(data0, data1)
        .collect(JaxbContextDetails.contextDataCollector());
  }

  /**
   * Gets details.
   *
   * @param softly the soft assertions
   */
  @Test
  void getDetails(SoftAssertions softly) {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    softly.assertThat(wrapper)
        .isEqualTo(wrapper);
    softly.assertThat(wrapper)
        .extracting(JaxbContextWrapper::getDetails)
        .isNotNull();

    JaxbContextWrapper actual = new JaxbContextWrapper(jaxbContext);
    softly.assertThat(actual)
        .isEqualTo(wrapper);
    softly.assertThat(actual.hashCode())
        .isEqualTo(wrapper.hashCode());
    softly.assertThat(actual.toString())
        .isEqualTo(wrapper.toString());
    softly.assertThat(actual)
        .isNotEqualTo(null);
    softly.assertThat(actual)
        .isNotEqualTo(new Object());

    wrapper = new JaxbContextWrapper(jaxbContext, detailsOfJaxbContext);
    softly.assertThat(wrapper)
        .extracting(JaxbContextWrapper::getDetails)
        .isEqualTo(detailsOfJaxbContext);
  }

  /**
   * Gets details with jaxb context data stream.
   *
   * @throws JAXBException the jaxb exception
   */
  @Test
  void getDetailsWithJaxbContextDataStream() throws JAXBException {
    JaxbContextData data0 = new JaxbContextData(
        org.bremersee.xml.model2.ObjectFactory.class.getPackage(),
        "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd");
    JaxbContextData data1 = new JaxbContextData(
        org.bremersee.xml.model5.ObjectFactory.class.getPackage());
    assertThat(new JaxbContextWrapper(Stream.of(data0, data1)))
        .extracting(JaxbContextWrapper::getDetails)
        .isEqualTo(detailsOfJaxbContext);
  }

  /**
   * Is formatted output.
   *
   * @param softly the soft assertions
   */
  @Test
  void isFormattedOutput(SoftAssertions softly) {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    wrapper.setFormattedOutput(false);
    softly.assertThat(wrapper.isFormattedOutput())
        .isFalse();
    softly.assertThat(wrapper.toString())
        .contains("false");

    wrapper.setFormattedOutput(true);
    softly.assertThat(wrapper.isFormattedOutput())
        .isTrue();
    softly.assertThat(wrapper.toString())
        .contains("true");
  }

  /**
   * Gets xml adapters.
   *
   * @param softly the soft assertions
   */
  @Test
  void getXmlAdapters(SoftAssertions softly) {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    softly.assertThat(wrapper.getXmlAdapters())
        .isNull();

    OffsetDateTimeXmlAdapter adapter = new OffsetDateTimeXmlAdapter();
    wrapper.setXmlAdapters(Collections.singletonList(adapter));
    softly.assertThat(wrapper.getXmlAdapters())
        .containsExactly(adapter);
  }

  /**
   * Gets attachment marshaller.
   *
   * @param softly the soft assertions
   */
  @Test
  void getAttachmentMarshaller(SoftAssertions softly) {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    softly.assertThat(wrapper.getAttachmentMarshaller())
        .isNull();

    AttachmentMarshaller marshaller = mock(AttachmentMarshaller.class);
    wrapper.setAttachmentMarshaller(marshaller);
    softly.assertThat(wrapper.getAttachmentMarshaller())
        .isNotNull();
  }

  /**
   * Gets attachment unmarshaller.
   *
   * @param softly the soft assertions
   */
  @Test
  void getAttachmentUnmarshaller(SoftAssertions softly) {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    assertNull(wrapper.getAttachmentUnmarshaller());
    softly.assertThat(wrapper.getAttachmentUnmarshaller())
        .isNull();

    AttachmentUnmarshaller unmarshaller = mock(AttachmentUnmarshaller.class);
    wrapper.setAttachmentUnmarshaller(unmarshaller);
    softly.assertThat(wrapper.getAttachmentUnmarshaller())
        .isNotNull();
  }

  /**
   * Use schema.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void useSchema(SoftAssertions softly) throws Exception {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext, detailsOfJaxbContext);
    softly.assertThat(wrapper.getSchema())
        .isNull();

    wrapper.setFormattedOutput(false);
    wrapper.setSchemaMode(SchemaMode.EXTERNAL_XSD);

    Schema schema = SchemaBuilder.newInstance().buildSchema(
        "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd");
    wrapper.setSchema(schema);
    softly.assertThat(schema)
        .isEqualTo(wrapper.getSchema());

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
    softly.assertThat(actual)
        .isEqualTo(expected);

    wrapper.setSchemaMode(SchemaMode.UNMARSHAL);
    Vehicle actualModel = (Vehicle) wrapper.createUnmarshaller()
        .unmarshal(new StringReader(actual));
    softly.assertThat(actualModel)
        .isEqualTo(model);
  }

  /**
   * Use schema and expect validation fails.
   *
   * @param softly the soft assertions
   */
  @Test
  void useSchemaAndExpectValidationFails(SoftAssertions softly) {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext, detailsOfJaxbContext);
    softly.assertThat(wrapper.getSchema())
        .isNull();

    wrapper.setFormattedOutput(false);
    wrapper.setSchemaMode(SchemaMode.MARSHAL);

    Schema schema = SchemaBuilder.newInstance().buildSchema(
        "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd");
    wrapper.setSchema(schema);
    softly.assertThat(schema)
        .isEqualTo(wrapper.getSchema());

    Vehicle model = new Vehicle();
    model.setModel("Diabolo3");

    softly
        .assertThatThrownBy(() -> wrapper.createMarshaller()
            .marshal(model, new StringWriter()))
        .extracting(Object::getClass)
        .isEqualTo(MarshalException.class);

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
    softly
        .assertThatThrownBy(() -> wrapper.createUnmarshaller()
            .unmarshal(new StringReader(invalid)))
        .extracting(Object::getClass)
        .isEqualTo(UnmarshalException.class);
  }

  /**
   * Gets validation event handler.
   *
   * @param softly the soft assertions
   */
  @Test
  void getValidationEventHandler(SoftAssertions softly) {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    softly.assertThat(wrapper.getValidationEventHandler())
        .isNull();

    ValidationEventHandler handler = mock(ValidationEventHandler.class);
    wrapper.setValidationEventHandler(handler);
    softly.assertThat(wrapper.getValidationEventHandler())
        .isNotNull();
  }

  /**
   * Gets schema mode.
   *
   * @param softly the soft assertions
   */
  @Test
  void getSchemaMode(SoftAssertions softly) {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    softly.assertThat(wrapper.getSchemaMode())
        .isEqualTo(SchemaMode.NEVER);

    wrapper.setSchemaMode(SchemaMode.EXTERNAL_XSD);
    softly.assertThat(wrapper.getSchemaMode())
        .isEqualTo(SchemaMode.EXTERNAL_XSD);
  }

  /**
   * Create validator.
   */
  @Test
  void createValidator() {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(wrapper::createValidator);
  }

  /**
   * Create binder.
   */
  @Test
  void createBinder() {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    assertThat(wrapper.createBinder(Node.class))
        .isNotNull();
  }

  /**
   * Create jaxb introspector.
   */
  @Test
  void createJaxbIntrospector() {
    JaxbContextWrapper wrapper = new JaxbContextWrapper(jaxbContext);
    assertThat(wrapper.createJAXBIntrospector())
        .isNotNull();
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
    assertThat(resolver.toString())
        .isNotNull();
  }
}