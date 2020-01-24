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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.sun.org.apache.xerces.internal.jaxp.JAXPConstants;
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.UnmarshalException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.bremersee.xml.model1.Person;
import org.bremersee.xml.model3.Company;
import org.bremersee.xml.model3.ObjectFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

/**
 * The xml document builder test.
 *
 * @author Christian Bremer
 */
class XmlDocumentBuilderTest {

  private JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder
      .builder()
      .processAll(ServiceLoader.load(JaxbContextDataProvider.class));

  /**
   * Test with namespaces.
   *
   * @throws Exception the exception
   */
  @Test
  void testWithNamespaces() throws Exception {
    Person expected = new Person();
    expected.setFirstName("Anna Livia");
    expected.setLastName("Plurabelle");
    XmlDocumentBuilder builder = XmlDocumentBuilder.builder();
    Document document = builder.buildDocument(expected, jaxbContextBuilder.buildJaxbContext());

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource domSource = new DOMSource(document);
    StringWriter sw = new StringWriter();
    StreamResult streamResult = new StreamResult(sw);
    transformer.transform(domSource, streamResult);

    Person actual = (Person) jaxbContextBuilder.buildUnmarshaller().unmarshal(document);
    assertEquals(expected, actual);

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
    assertEquals(expected, actual);
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
      XmlDocumentBuilder builder = XmlDocumentBuilder.builder();
      Document document = builder.buildDocument(expected, jaxbContextBuilder.buildJaxbContext());

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource domSource = new DOMSource(document);
      StringWriter sw = new StringWriter();
      StreamResult streamResult = new StreamResult(sw);
      transformer.transform(domSource, streamResult);

      Person actual = (Person) jaxbContextBuilder.buildUnmarshaller().unmarshal(document);
      assertEquals(expected, actual);

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

      actual = (Person) jaxbContextBuilder.buildUnmarshaller().unmarshal(document);
      assertEquals(expected, actual);
    });
  }

  /**
   * Test without namespaces.
   *
   * @throws Exception the exception
   */
  @Test
  void testWithoutNamespaces() throws Exception {
    JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());

    Company expected = new Company();
    expected.setName("bremersee.org");
    XmlDocumentBuilder builder = XmlDocumentBuilder.builder()
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
    assertTrue(StringUtils.hasText(sw.toString()));

    Company actual = (Company) jaxbContextBuilder.buildUnmarshaller().unmarshal(document);
    assertEquals(expected, actual);
  }

  /**
   * Build document builder.
   */
  @Test
  void buildDocumentBuilder() {

    DocumentBuilder builder = XmlDocumentBuilder.builder()
        .configureFactoryAttribute(JAXPConstants.JAXP_SCHEMA_LANGUAGE, JAXPConstants.W3C_XML_SCHEMA)
        .configureFactoryFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false)
        .configureFactorySchema(null)
        .configureEntityResolver(mock(EntityResolver.class))
        .configureErrorHandler(mock(ErrorHandler.class))
        .buildDocumentBuilder();
    assertNotNull(builder);
  }

  /**
   * Build document.
   */
  @Test
  void buildDocument() {
    Document document = XmlDocumentBuilder.builder()
        .buildDocument();
    assertNotNull(document);
  }

  /**
   * Build document with input stream.
   *
   * @throws Exception the exception
   */
  @Test
  void buildDocumentWithInputStream() throws Exception {
    Document document = XmlDocumentBuilder.builder()
        .configureFactorySchema(SchemaBuilder.builder()
            .buildSchema("classpath:common-xml-test-model-1.xsd"))
        .buildDocument(new DefaultResourceLoader()
            .getResource("classpath:person.xml").getInputStream());
    assertNotNull(document);

    document = XmlDocumentBuilder.builder()
        .configureFactorySchema(SchemaBuilder.builder()
            .buildSchema("classpath:common-xml-test-model-1.xsd"))
        .buildDocument(new DefaultResourceLoader()
            .getResource("classpath:person.xml").getInputStream(), "systemId");
    assertNotNull(document);
  }

  /**
   * Build document from input source.
   *
   * @throws Exception the exception
   */
  @Test
  void buildDocumentFromInputSource() throws Exception {
    Document document = XmlDocumentBuilder.builder()
        .buildDocument(new InputSource(new DefaultResourceLoader()
            .getResource("classpath:person.xml").getInputStream()));
    assertNotNull(document);
  }

  /**
   * Build document from uri.
   */
  @Test
  void buildDocumentFromUri() {
    Document document = XmlDocumentBuilder.builder()
        .buildDocument("http://bremersee.github.io/xmlschemas/common-xml-test-model-2.xsd");
    assertNotNull(document);
  }

  /**
   * Build document from illegal uri.
   */
  @Test
  void buildDocumentFromIllegalUri() {
    assertThrows(XmlRuntimeException.class, () -> XmlDocumentBuilder.builder()
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
    Document document = XmlDocumentBuilder.builder()
        .buildDocument(file);
    assertNotNull(document);
  }

  /**
   * Build document from file and expect exception.
   */
  @Test
  void buildDocumentFromFileAndExpectException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> XmlDocumentBuilder.builder().buildDocument((File) null));
  }

  /**
   * Build document from illegal file and expect exception.
   *
   * @throws IOException the io exception
   */
  @Test
  void buildDocumentFromIllegalFileAndExpectException() throws IOException {
    final File file = File.createTempFile("junit", ".test",
        new File(System.getProperty("java.io.tmpdir")));
    file.deleteOnExit();

    assertThrows(XmlRuntimeException.class, () -> XmlDocumentBuilder.builder().buildDocument(file));

    try (InputStream in = new FileInputStream(file)) {
      assertThrows(XmlRuntimeException.class, () -> XmlDocumentBuilder.builder()
          .buildDocument(in));
    }

    try (InputStream in = new FileInputStream(file)) {
      assertThrows(XmlRuntimeException.class, () -> XmlDocumentBuilder.builder()
          .buildDocument(in, "system-id"));
    }

    try (InputStream in = new FileInputStream(file)) {
      assertThrows(XmlRuntimeException.class, () -> XmlDocumentBuilder.builder()
          .buildDocument(new InputSource(in)));
    }
  }

  /**
   * Build document with marshaller.
   */
  @Test
  void buildDocumentWithMarshaller() {
    Person person = new Person();
    person.setFirstName("Anna Livia");
    person.setLastName("Plurabelle");

    JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder
        .builder()
        .processAll(ServiceLoader.load(JaxbContextDataProvider.class));

    Document document = XmlDocumentBuilder.builder()
        .buildDocument(person, jaxbContextBuilder.buildMarshaller());
    assertNotNull(document);

    assertNull(XmlDocumentBuilder.builder()
        .buildDocument(null, jaxbContextBuilder.buildMarshaller()));

    assertThrows(JaxbRuntimeException.class, () -> XmlDocumentBuilder.builder()
        .buildDocument("", jaxbContextBuilder.buildMarshaller()));
  }

  /**
   * Build document with jaxb context.
   */
  @Test
  void buildDocumentWithJaxbContext() {
    Person person = new Person();
    person.setFirstName("Anna Livia");
    person.setLastName("Plurabelle");

    List<JaxbContextData> ctxData = new ArrayList<>();
    ctxData.add(new JaxbContextData(
        org.bremersee.xml.model1.ObjectFactory.class.getPackage().getName()));
    ctxData.add(new JaxbContextData(
        org.bremersee.xml.model2.ObjectFactory.class.getPackage().getName()));

    JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder
        .builder()
        .addAll(ctxData.iterator());

    Document document = XmlDocumentBuilder.builder()
        .buildDocument(person, jaxbContextBuilder.buildJaxbContext());
    assertNotNull(document);

    assertNull(XmlDocumentBuilder.builder()
        .buildDocument(null, jaxbContextBuilder.buildJaxbContext()));

    assertThrows(JaxbRuntimeException.class, () -> XmlDocumentBuilder.builder()
        .buildDocument("", jaxbContextBuilder.buildJaxbContext()));
  }

}
