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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.FileCopyUtils;

/**
 * The schema builder test.
 */
class SchemaBuilderTest {

  /**
   * Build schema with location.
   */
  @Test
  void buildSchemaWithLocation() {

    ClassLoader classLoader;
    if (System.getSecurityManager() == null) {
      classLoader = Thread.currentThread().getContextClassLoader();
    } else {
      //noinspection unchecked,rawtypes
      classLoader = (ClassLoader) java.security.AccessController.doPrivileged(
          (PrivilegedAction) () -> Thread.currentThread().getContextClassLoader());
    }

    SchemaBuilder builder = SchemaBuilder.builder()
        .withSchemaLanguage(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        .withFactory(null)
        .withClassLoader(classLoader)
        .withResourceLoader(new DefaultResourceLoader())
        .withResourceResolver(new TestResourceResolver())
        .withErrorHandler(new TestErrorHandler())
        .withFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false)
        .withProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "false");
    assertNotNull(builder);

    SchemaBuilder copy = builder.copy();
    assertNotNull(copy);

    Schema schema = copy
        .buildSchema("classpath:common-xml-test-model-1.xsd");
    assertNotNull(schema);
  }

  /**
   * Build schema with url.
   *
   * @throws Exception the exception
   */
  @Test
  void buildSchemaWithUrl() throws Exception {

    Schema schema = SchemaBuilder.builder()
        .withSchemaLanguage(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        .withClassLoader(null)
        .withResourceLoader(null)
        .withResourceResolver(null)
        .withErrorHandler(null)
        .withFeature(null, false)
        .withProperty(null, "false")
        .buildSchema(
            new URL("http://bremersee.github.io/xmlschemas/common-xml-test-model-1.xsd"));
    assertNotNull(schema);
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

    Schema schema = SchemaBuilder.builder().buildSchema(file);
    assertNotNull(schema);
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
    Schema schema = SchemaBuilder.builder()
        .buildSchema(source);
    assertNotNull(schema);
  }

  /**
   * Fetch schema sources.
   */
  @Test
  void fetchSchemaSources() {
    List<Source> sources = SchemaBuilder.builder()
        .fetchSchemaSources(Arrays.asList(
            "classpath:common-xml-test-model-1.xsd",
            "http://bremersee.github.io/xmlschemas/common-xml-test-model-2.xsd"));
    assertNotNull(sources);
    assertEquals(2, sources.size());
  }

}