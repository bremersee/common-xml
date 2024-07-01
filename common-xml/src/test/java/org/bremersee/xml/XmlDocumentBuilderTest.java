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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;
import javax.xml.XMLConstants;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.UnmarshalException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.xml.model1.Person;
import org.bremersee.xml.model3.Company;
import org.bremersee.xml.model3.ObjectFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

/**
 * The xml document builder test.
 *
 * @author Christian Bremer
 */
@ExtendWith({SoftAssertionsExtension.class})
class XmlDocumentBuilderTest {

  private final JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder
      .newInstance()
      .processAll(ServiceLoader.load(JaxbContextDataProvider.class));

  /**
   * Test with namespaces.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void testWithNamespaces(SoftAssertions softly) throws Exception {
    Person expected = new Person();
    expected.setFirstName("Anna Livia");
    expected.setLastName("Plurabelle");
    XmlDocumentBuilder builder = XmlDocumentBuilder.newInstance();
    Document document = builder.buildDocument(expected, jaxbContextBuilder.buildJaxbContext());

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource domSource = new DOMSource(document);
    StringWriter sw = new StringWriter();
    StreamResult streamResult = new StreamResult(sw);
    transformer.transform(domSource, streamResult);

    Person actual = (Person) jaxbContextBuilder.buildUnmarshaller().unmarshal(document);
    softly.assertThat(actual)
        .isEqualTo(expected);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    jaxbContextBuilder.buildMarshaller().marshal(expected, out);

    builder = builder.configureFactory(
        null,
        null,
        false,
        false,
        true,
        false,
        null);
    document = builder.buildDocument(new ByteArrayInputStream(out.toByteArray()));

    actual = (Person) jaxbContextBuilder.buildUnmarshaller().unmarshal(document);
    softly.assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Test without namespaces and expect error.
   */
  @Test
  void testWithoutNamespacesAndExpectError() {
    assertThrows(UnmarshalException.class, () -> {
      Person expected = new Person();
      expected.setFirstName("Anna Livia");
      expected.setLastName("Plurabelle");
      XmlDocumentBuilder builder = XmlDocumentBuilder.newInstance();
      Document document = builder.buildDocument(expected, jaxbContextBuilder.buildJaxbContext());

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource domSource = new DOMSource(document);
      StringWriter sw = new StringWriter();
      StreamResult streamResult = new StreamResult(sw);
      transformer.transform(domSource, streamResult);

      jaxbContextBuilder.buildUnmarshaller().unmarshal(document);

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      jaxbContextBuilder.buildMarshaller().marshal(expected, out);

      builder = builder.configureFactory(
          null,
          null,
          false,
          false,
          false,
          false,
          null);
      document = builder.buildDocument(new ByteArrayInputStream(out.toByteArray()));

      jaxbContextBuilder.buildUnmarshaller().unmarshal(document);
    });
  }

  /**
   * Test without namespaces.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void testWithoutNamespaces(SoftAssertions softly) throws Exception {
    JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());

    Company expected = new Company();
    expected.setName("bremersee.org");
    XmlDocumentBuilder builder = XmlDocumentBuilder.newInstance()
        .configureFactory(factory -> {
          factory.setNamespaceAware(false);
          factory.setCoalescing(false);
          factory.setExpandEntityReferences(true);
          factory.setIgnoringComments(false);
          factory.setIgnoringElementContentWhitespace(false);
          factory.setValidating(false);
          factory.setXIncludeAware(false);
        });
    Document document = builder.buildDocument(expected, jaxbContext);

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource domSource = new DOMSource(document);
    StringWriter sw = new StringWriter();
    StreamResult streamResult = new StreamResult(sw);
    transformer.transform(domSource, streamResult);
    softly.assertThat(sw.toString())
        .isNotEmpty();

    Company actual = (Company) jaxbContextBuilder.buildUnmarshaller().unmarshal(document);
    softly.assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Create document builder.
   */
  @Test
  void createDocumentBuilder() {

    DocumentBuilder builder = XmlDocumentBuilder.newInstance()
        .configureFactoryFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false)
        .configureFactorySchema(null)
        .configureEntityResolver(mock(EntityResolver.class))
        .configureErrorHandler(mock(ErrorHandler.class))
        .buildDocumentBuilder();
    assertThat(builder)
        .isNotNull();
  }

  /**
   * Build document.
   */
  @Test
  void buildDocument() {
    Document document = XmlDocumentBuilder.newInstance()
        .buildDocument();
    assertThat(document)
        .isNotNull();
  }

  /**
   * Build document with input stream.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void buildDocumentWithInputStream(SoftAssertions softly) throws Exception {
    Document document = XmlDocumentBuilder.newInstance()
        .configureFactorySchema(SchemaBuilder.newInstance()
            .buildSchema("classpath:common-xml-test-model-1.xsd"))
        .buildDocument(new DefaultResourceLoader()
            .getResource("classpath:person.xml").getInputStream());
    softly.assertThat(document)
        .isNotNull();

    document = XmlDocumentBuilder.newInstance()
        .configureFactorySchema(SchemaBuilder.newInstance()
            .buildSchema("classpath:common-xml-test-model-1.xsd"))
        .buildDocument(new DefaultResourceLoader()
            .getResource("classpath:person.xml").getInputStream(), "systemId");
    softly.assertThat(document)
        .isNotNull();
  }

  /**
   * Build document from input source.
   *
   * @throws Exception the exception
   */
  @Test
  void buildDocumentFromInputSource() throws Exception {
    Document document = XmlDocumentBuilder.newInstance()
        .buildDocument(new InputSource(new DefaultResourceLoader()
            .getResource("classpath:person.xml").getInputStream()));
    assertThat(document)
        .isNotNull();
  }

  /**
   * Build document from uri.
   */
  @Test
  void buildDocumentFromUri() {
    Document document = XmlDocumentBuilder.newInstance()
        .buildDocument("http://bremersee.github.io/xmlschemas/common-xml-test-model-2.xsd");
    assertThat(document)
        .isNotNull();
  }

  /**
   * Build document from illegal uri.
   */
  @Test
  void buildDocumentFromIllegalUri() {
    assertThatExceptionOfType(XmlRuntimeException.class)
        .isThrownBy(() -> XmlDocumentBuilder.newInstance()
            .buildDocument("http://localhost/" + UUID.randomUUID() + ".xml"));
  }

  /**
   * Build document from file.
   */
  @Test
  void buildDocumentFromFile() {
    File file;
    try {
      file = File.createTempFile("test", ".xsd", new File(System.getProperty("java.io.tmpdir")));
      file.deleteOnExit();
      FileCopyUtils.copy(
          new DefaultResourceLoader().getResource("classpath:common-xml-test-model-1.xsd")
              .getInputStream(),
          new FileOutputStream(file));

    } catch (Exception ignored) {
      return;
    }
    Document document = XmlDocumentBuilder.newInstance()
        .buildDocument(file);
    assertThat(document)
        .isNotNull();
  }

  /**
   * Build document from file and expect exception.
   */
  @Test
  void buildDocumentFromFileAndExpectException() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> XmlDocumentBuilder.newInstance().buildDocument((File) null));
  }

  /**
   * Build document from illegal file and expect exception.
   *
   * @param softly the soft assertions
   * @throws IOException the io exception
   */
  @Test
  void buildDocumentFromIllegalFileAndExpectException(SoftAssertions softly)
      throws IOException {

    File file = File.createTempFile("junit", ".test",
        new File(System.getProperty("java.io.tmpdir")));
    file.deleteOnExit();

    softly.assertThatThrownBy(() -> XmlDocumentBuilder.newInstance()
            .buildDocument(file))
        .extracting(Object::getClass)
        .isEqualTo(XmlRuntimeException.class);

    try (InputStream in = new FileInputStream(file)) {
      softly.assertThatThrownBy(() -> XmlDocumentBuilder.newInstance()
              .buildDocument(in))
          .extracting(Object::getClass)
          .isEqualTo(XmlRuntimeException.class);
    }

    try (InputStream in = new FileInputStream(file)) {
      softly.assertThatThrownBy(() -> XmlDocumentBuilder.newInstance()
              .buildDocument(in, "system-id"))
          .extracting(Object::getClass)
          .isEqualTo(XmlRuntimeException.class);
    }

    try (InputStream in = new FileInputStream(file)) {
      softly.assertThatThrownBy(() -> XmlDocumentBuilder.newInstance()
              .buildDocument(new InputSource(in)))
          .extracting(Object::getClass)
          .isEqualTo(XmlRuntimeException.class);
    }
  }

  /**
   * Build document with marshaller.
   *
   * @param softly the soft assertions
   */
  @Test
  void buildDocumentWithMarshaller(SoftAssertions softly) {
    Person person = new Person();
    person.setFirstName("Anna Livia");
    person.setLastName("Plurabelle");

    JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder
        .newInstance()
        .processAll(ServiceLoader.load(JaxbContextDataProvider.class));

    Document document = XmlDocumentBuilder.newInstance()
        .buildDocument(person, jaxbContextBuilder.buildMarshaller());
    softly.assertThat(document)
        .isNotNull();

    softly.assertThat(XmlDocumentBuilder.newInstance()
            .buildDocument(null, jaxbContextBuilder.buildMarshaller()))
        .isNull();

    softly.assertThatThrownBy(() -> XmlDocumentBuilder.newInstance()
            .buildDocument("", jaxbContextBuilder.buildMarshaller()))
        .extracting(Object::getClass)
        .isEqualTo(JaxbRuntimeException.class);
  }

  /**
   * Build document with jaxb context.
   *
   * @param softly the soft assertions
   */
  @Test
  void buildDocumentWithJaxbContext(SoftAssertions softly) {
    Person person = new Person();
    person.setFirstName("Anna Livia");
    person.setLastName("Plurabelle");

    List<JaxbContextData> ctxData = new ArrayList<>();
    ctxData.add(new JaxbContextData(
        org.bremersee.xml.model1.ObjectFactory.class.getPackage()));
    ctxData.add(new JaxbContextData(
        org.bremersee.xml.model2.ObjectFactory.class.getPackage()));

    JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder
        .newInstance()
        .addAll(ctxData.iterator());

    Document document = XmlDocumentBuilder.newInstance()
        .buildDocument(person, jaxbContextBuilder.buildJaxbContext());
    softly.assertThat(document)
        .isNotNull();

    softly.assertThat(XmlDocumentBuilder.newInstance()
            .buildDocument(null, jaxbContextBuilder.buildJaxbContext()))
        .isNull();

    softly.assertThatThrownBy(() -> XmlDocumentBuilder.newInstance()
            .buildDocument("no-xml", jaxbContextBuilder.buildJaxbContext()))
        .extracting(Object::getClass)
        .isEqualTo(JaxbRuntimeException.class);
  }

  /**
   * Configure factory attribute.
   */
  @Test
  void configureFactoryAttribute() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> XmlDocumentBuilder.newInstance()
            .configureFactoryAttribute("don't know", new Object())
            .buildDocumentBuilder());
  }

}
