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
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;

/**
 * The schema builder test.
 *
 * @author Christian Bremer
 */
@ExtendWith({SoftAssertionsExtension.class})
class SchemaBuilderTest {

  /**
   * Build schema with location.
   *
   * @param softly the soft assertions
   */
  @Test
  void buildSchemaWithLocation(SoftAssertions softly) {

    ClassLoader classLoader;
    if (System.getSecurityManager() == null) {
      classLoader = Thread.currentThread().getContextClassLoader();
    } else {
      //noinspection unchecked,rawtypes
      classLoader = (ClassLoader) java.security.AccessController.doPrivileged(
          (PrivilegedAction) () -> Thread.currentThread().getContextClassLoader());
    }

    SchemaBuilder builder = SchemaBuilder.newInstance()
        .withSchemaLanguage(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        .withFactory(null)
        .withClassLoader(classLoader)
        .withResourceLoader(new DefaultResourceLoader())
        .withResourceResolver(mock(LSResourceResolver.class))
        .withErrorHandler(mock(ErrorHandler.class))
        .withFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false)
        .withProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "false");
    softly.assertThat(builder)
        .isNotNull();

    SchemaBuilder copy = builder.copy();
    softly.assertThat(copy)
        .isNotNull();

    Schema schema = copy
        .buildSchema("classpath:common-xml-test-model-1.xsd");
    softly.assertThat(schema)
        .isNotNull();
  }

  /**
   * Build schema with url.
   *
   * @throws Exception the exception
   */
  @Test
  void buildSchemaWithUrl() throws Exception {

    Schema schema = SchemaBuilder.newInstance()
        .withSchemaLanguage(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        .withFactory("com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory")
        .withClassLoader(null)
        .withResourceLoader(null)
        .withResourceResolver(null)
        .withErrorHandler(null)
        .withFeature(null, false)
        .withProperty(null, "false")
        .buildSchema(
            new URL("http://bremersee.github.io/xmlschemas/common-xml-test-model-1.xsd"));
    assertThat(schema)
        .isNotNull();
  }

  /**
   * Build schema with file.
   */
  @Test
  void buildSchemaWithFile() {

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

    Schema schema = SchemaBuilder.newInstance().buildSchema(file);
    assertThat(schema)
        .isNotNull();
  }

  /**
   * Build schema with source.
   *
   * @throws Exception the exception
   */
  @Test
  void buildSchemaWithSource() throws Exception {
    Source source = new StreamSource(new DefaultResourceLoader()
        .getResource("classpath:common-xml-test-model-1.xsd").getInputStream());
    Schema schema = SchemaBuilder.newInstance()
        .buildSchema(source);
    assertThat(schema)
        .isNotNull();
  }

  /**
   * Fetch schema sources.
   */
  @Test
  void fetchSchemaSources() {
    List<Source> sources = SchemaBuilder.newInstance()
        .fetchSchemaSources(Arrays.asList(
            "classpath:common-xml-test-model-1.xsd",
            "http://bremersee.github.io/xmlschemas/common-xml-test-model-2.xsd"));
    assertThat(sources)
        .hasSize(2);
  }

  /**
   * Create schema factory wth illegal property.
   */
  @Test
  void createSchemaFactoryWthIllegalProperty() {
    assertThatExceptionOfType(XmlRuntimeException.class)
        .isThrownBy(() -> SchemaBuilder.newInstance()
            .withProperty("foo", "bar")
            .buildSchema());
  }

  /**
   * Fetch schema sources that does not exist.
   */
  @Test
  void fetchSchemaSourcesThatDoesNotExist() {
    assertThatExceptionOfType(XmlRuntimeException.class)
        .isThrownBy(() -> SchemaBuilder.newInstance()
            .fetchSchemaSources("classpath:/nothing.xsd"));
  }

  /**
   * Build schema with illegal url.
   */
  @Test
  void buildSchemaWithIllegalUrl() {
    assertThatExceptionOfType(XmlRuntimeException.class)
        .isThrownBy(() -> SchemaBuilder.newInstance()
            .buildSchema(new URL("http://localhost/" + UUID.randomUUID() + ".xsd")));
  }

  /**
   * Build schema with illegal file.
   *
   * @throws IOException the io exception
   */
  @Test
  void buildSchemaWithIllegalFile() throws IOException {
    File file = File.createTempFile("junit", ".test",
        new File(System.getProperty("java.io.tmpdir")));
    file.deleteOnExit();
    assertThatExceptionOfType(XmlRuntimeException.class)
        .isThrownBy(() -> SchemaBuilder.newInstance()
            .buildSchema(file));
  }

  /**
   * Build schema with illegal stream source.
   *
   * @throws IOException the io exception
   */
  @Test
  void buildSchemaWithIllegalStreamSource() throws IOException {
    File file = File.createTempFile("junit", ".test",
        new File(System.getProperty("java.io.tmpdir")));
    file.deleteOnExit();
    assertThatExceptionOfType(XmlRuntimeException.class)
        .isThrownBy(() -> SchemaBuilder.newInstance()
            .buildSchema(new StreamSource(file)));
  }

  /**
   * Build schema with null.
   *
   * @param softly the soft assertions
   */
  @Test
  void buildSchemaWithNull(SoftAssertions softly) {
    softly.assertThat(SchemaBuilder.newInstance().buildSchema((URL) null))
        .isNotNull();
    softly.assertThat(SchemaBuilder.newInstance().buildSchema((File) null))
        .isNotNull();
    softly.assertThat(SchemaBuilder.newInstance().buildSchema((Source) null))
        .isNotNull();
  }

}