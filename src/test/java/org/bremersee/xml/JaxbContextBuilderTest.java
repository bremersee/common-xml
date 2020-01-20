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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.ServiceLoader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.bremersee.xml.model1.Person;
import org.bremersee.xml.model2.Vehicle;
import org.bremersee.xml.model3.Company;
import org.bremersee.xml.model4.Address;
import org.bremersee.xml.provider.ExampleJaxbContextDataProvider;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

/**
 * The jaxb context builder test.
 *
 * @author Christian Bremer
 */
class JaxbContextBuilderTest {

  /**
   * Test jaxb context builder.
   *
   * @throws Exception the exception
   */
  @Test
  void testJaxbContextBuilder() throws Exception {

    JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder
        .builder()
        .processAll(ServiceLoader.load(JaxbContextDataProvider.class));

    String contextPath = jaxbContextBuilder.buildContextPath();
    assertNotNull(contextPath);
    assertTrue(
        contextPath.contains(org.bremersee.xml.model1.ObjectFactory.class.getPackage().getName()));
    assertTrue(
        contextPath.contains(org.bremersee.xml.model2.ObjectFactory.class.getPackage().getName()));
    assertTrue(
        contextPath.contains(org.bremersee.xml.model3.ObjectFactory.class.getPackage().getName()));
    assertTrue(
        contextPath.contains(org.bremersee.xml.model4.ObjectFactory.class.getPackage().getName()));

    contextPath = jaxbContextBuilder.buildContextPath("");
    assertFalse(
        contextPath.contains(org.bremersee.xml.model1.ObjectFactory.class.getPackage().getName()));
    assertFalse(
        contextPath.contains(org.bremersee.xml.model2.ObjectFactory.class.getPackage().getName()));
    assertTrue(
        contextPath.contains(org.bremersee.xml.model3.ObjectFactory.class.getPackage().getName()));
    assertTrue(
        contextPath.contains(org.bremersee.xml.model4.ObjectFactory.class.getPackage().getName()));

    contextPath = jaxbContextBuilder
        .buildContextPath("http://bremersee.org/xmlschemas/common-xml-test-model-1");
    assertTrue(
        contextPath.contains(org.bremersee.xml.model1.ObjectFactory.class.getPackage().getName()));
    assertFalse(
        contextPath.contains(org.bremersee.xml.model2.ObjectFactory.class.getPackage().getName()));
    assertFalse(
        contextPath.contains(org.bremersee.xml.model3.ObjectFactory.class.getPackage().getName()));
    assertFalse(
        contextPath.contains(org.bremersee.xml.model4.ObjectFactory.class.getPackage().getName()));

    jaxbContextBuilder = jaxbContextBuilder
        .addAll(new ExampleJaxbContextDataProvider().getJaxbContextData());

    String schemaLocation = jaxbContextBuilder.buildSchemaLocation();
    assertNotNull(schemaLocation);
    assertTrue(
        schemaLocation.contains("http://bremersee.org/xmlschemas/common-xml-test-model-1 "
            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-1.xsd"));
    assertTrue(
        schemaLocation.contains("http://bremersee.org/xmlschemas/common-xml-test-model-2 "
            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-2.xsd"));

    schemaLocation = jaxbContextBuilder
        .buildSchemaLocation("http://bremersee.org/xmlschemas/common-xml-test-model-2");
    assertFalse(
        schemaLocation.contains("http://bremersee.org/xmlschemas/common-xml-test-model-1 "
            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-1.xsd"));
    assertTrue(
        schemaLocation.contains("http://bremersee.org/xmlschemas/common-xml-test-model-2 "
            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-2.xsd"));

    assertTrue(jaxbContextBuilder.supports(Person.class));
    assertTrue(jaxbContextBuilder.supports(Vehicle.class));
    assertTrue(jaxbContextBuilder.supports(Company.class));
    assertTrue(jaxbContextBuilder.supports(Address.class));

    assertFalse(jaxbContextBuilder.supports(JaxbContextData.class));

    assertFalse(jaxbContextBuilder.supports(
        Person.class,
        "http://bremersee.org/xmlschemas/common-xml-test-model-2"));

    JAXBContext jaxbContext = jaxbContextBuilder.buildJaxbContext();
    assertNotNull(jaxbContext);
    assertTrue(jaxbContext instanceof SchemaLocationAwareJaxbContext);
    SchemaLocationAwareJaxbContext ctx = (SchemaLocationAwareJaxbContext) jaxbContext;
    assertNotNull(ctx.createJAXBIntrospector());
    assertNotNull(ctx.createMarshaller());
    assertNotNull(ctx.createUnmarshaller());
    assertNotNull(ctx.getSchemaLocation());

    assertTrue(ctx.isFormattedOutput());
    assertEquals(jaxbContextBuilder.buildContextPath(), ctx.getContextPath());

    assertNotNull(ctx.createBinder());
    assertThrows(UnsupportedOperationException.class, () -> ctx.createBinder(null));
    assertThrows(UnsupportedOperationException.class, ctx::createValidator);

    final BufferSchemaOutputResolver res = new BufferSchemaOutputResolver();
    jaxbContextBuilder.buildJaxbContext().generateSchema(res);
    assertTrue(StringUtils.hasText(res.toString()));
  }

  /**
   * Write and read xml.
   *
   * @throws Exception the exception
   */
  @Test
  void writeAndReadXml() throws Exception {

    Vehicle vehicle = new Vehicle();
    vehicle.setBrand("A brand");
    vehicle.setModel("A model");

    JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder
        .builder()
        .formattedOutput(true)
        .process(new ExampleJaxbContextDataProvider());

    StringWriter sw = new StringWriter();
    jaxbContextBuilder
        .buildMarshaller("http://bremersee.org/xmlschemas/common-xml-test-model-2")
        .marshal(vehicle, sw);

    String xml = sw.toString();
    Vehicle readVehicle = (Vehicle) jaxbContextBuilder
        .buildUnmarshaller()
        .unmarshal(new StringReader(xml));

    assertEquals(vehicle, readVehicle);
  }

  /**
   * Write xml but read fails.
   */
  @Test
  void writeXmlButReadFails() {
    assertThrows(JAXBException.class, () -> {
      Company company = new Company();
      company.setName("XML Generator Service");

      JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder
          .builder()
          .formattedOutput(true)
          .add(new JaxbContextData(org.bremersee.xml.model1.ObjectFactory.class.getPackage()))
          .add(new JaxbContextData(org.bremersee.xml.model3.ObjectFactory.class.getPackage()
              .getName()));

      StringWriter sw = new StringWriter();
      jaxbContextBuilder
          .buildMarshaller()
          .marshal(company, sw);

      String xml = sw.toString();
      jaxbContextBuilder
          .buildUnmarshaller("http://bremersee.org/xmlschemas/common-xml-test-model-1")
          .unmarshal(new StringReader(xml));
    });
  }

  /**
   * Build marshaller properties.
   */
  @Test
  void buildMarshallerProperties() {
    ClassLoader classLoader;
    if (System.getSecurityManager() == null) {
      classLoader = Thread.currentThread().getContextClassLoader();
    } else {
      //noinspection unchecked,rawtypes
      classLoader = (ClassLoader) java.security.AccessController.doPrivileged(
          (PrivilegedAction) () -> Thread.currentThread().getContextClassLoader());
    }
    Map<String, ?> properties = JaxbContextBuilder.builder()
        .contextClassLoader(classLoader)
        .processAll(ServiceLoader.load(JaxbContextDataProvider.class).iterator())
        .buildMarshallerProperties();
    assertNotNull(properties);
    assertEquals("UTF-8", properties.get("jaxb.encoding"));
  }

  /**
   * Build schema.
   *
   * @throws Exception the exception
   */
  @Test
  void buildSchema() throws Exception {

    JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder
        .builder()
        .processAll(ServiceLoader.load(JaxbContextDataProvider.class));

    JAXBContext jaxbContext = jaxbContextBuilder.buildJaxbContext();

    Schema schema = jaxbContextBuilder.buildSchema();
    assertNotNull(schema);

    schema = jaxbContextBuilder.buildSchema(SchemaBuilder.builder());
    assertNotNull(schema);

    Person person = new Person();
    person.setFirstName("Anna Livia");
    person.setLastName("Plurabelle");
    StringWriter out = new StringWriter();
    jaxbContext.createMarshaller().marshal(person, out);
    String xml = out.toString();
    schema.newValidator().validate(new StreamSource(new StringReader(xml)));
  }

}
