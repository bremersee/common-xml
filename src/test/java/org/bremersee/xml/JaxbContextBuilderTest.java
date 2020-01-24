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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.StringReader;
import java.io.StringWriter;
import java.security.PrivilegedAction;
import java.time.OffsetDateTime;
import java.util.Arrays;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import org.bremersee.xml.adapter.DurationXmlAdapter;
import org.bremersee.xml.adapter.OffsetDateTimeXmlAdapter;
import org.bremersee.xml.model3.Company;
import org.bremersee.xml.model4.Address;
import org.bremersee.xml.model5.StartEnd;
import org.bremersee.xml.model7a.ObjectFactory;
import org.bremersee.xml.model7b.DirtBikeReseller;
import org.bremersee.xml.model7b.MountainBike;
import org.bremersee.xml.model7b.RacingReseller;
import org.bremersee.xml.model7b.SportBikes;
import org.bremersee.xml.model7c.BikeSchmied;
import org.bremersee.xml.model7c.Carrier;
import org.bremersee.xml.model7c.Fastcycle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

/**
 * The jaxb context builder test.
 *
 * @author Christian Bremer
 */
class JaxbContextBuilderTest {

  /**
   * The xml we want to write and read.
   *
   * <pre>
   * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   * <ns2:MountainBike xsi:schemaLocation="http://bremersee.org/xmlschemas/common-xml-test-model-7a http://bremersee.github.io/xmlschemas/common-xml-test-model-7a.xsd http://bremersee.org/xmlschemas/common-xml-test-model-7b http://bremersee.github.io/xmlschemas/common-xml-test-model-7b.xsd" xmlns="http://bremersee.org/xmlschemas/common-xml-test-model-7a" xmlns:ns2="http://bremersee.org/xmlschemas/common-xml-test-model-7b" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   *     <producer xsi:type="ns2:sportBikesType">
   *         <name>Sport Bikes</name>
   *         <ns2:Reseller xsi:type="ns2:dirtBikeResellerType">
   *             <name>Dirt Bikes</name>
   *         </ns2:Reseller>
   *         <ns2:Reseller xsi:type="ns2:racingResellerType">
   *             <name>Racing Fun</name>
   *         </ns2:Reseller>
   *     </producer>
   *     <color>Red</color>
   *     <extraParts>
   *         <ns2:Carrier xsi:schemaLocation="http://bremersee.org/xmlschemas/common-xml-test-model-7a http://bremersee.github.io/xmlschemas/common-xml-test-model-7a.xsd http://bremersee.org/xmlschemas/common-xml-test-model-7c http://bremersee.github.io/xmlschemas/common-xml-test-model-7c.xsd" xmlns:ns2="http://bremersee.org/xmlschemas/common-xml-test-model-7c">
   *             <partNumber>123456789</partNumber>
   *             <ns2:capacity>15 kg</ns2:capacity>
   *         </ns2:Carrier>
   *     </extraParts>
   *     <ns2:seatHeight>60</ns2:seatHeight>
   * </ns2:MountainBike>
   * </pre>
   */
  private static final String XML1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
      + "<ns2:MountainBike "
      + "xsi:schemaLocation=\"http://bremersee.org/xmlschemas/common-xml-test-model-7a "
      + "http://bremersee.github.io/xmlschemas/common-xml-test-model-7a.xsd "
      + "http://bremersee.org/xmlschemas/common-xml-test-model-7b "
      + "http://bremersee.github.io/xmlschemas/common-xml-test-model-7b.xsd\" "
      + "xmlns=\"http://bremersee.org/xmlschemas/common-xml-test-model-7a\" "
      + "xmlns:ns2=\"http://bremersee.org/xmlschemas/common-xml-test-model-7b\" "
      + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
      + "<producer xsi:type=\"ns2:sportBikesType\">"
      + "<name>Sport Bikes</name>"
      + "<ns2:Reseller xsi:type=\"ns2:dirtBikeResellerType\">"
      + "<name>Dirt Bikes</name></ns2:Reseller>"
      + "<ns2:Reseller xsi:type=\"ns2:racingResellerType\">"
      + "<name>Racing Fun</name></ns2:Reseller>"
      + "</producer><color>Red</color><extraParts>"
      + "<ns2:Carrier "
      + "xsi:schemaLocation=\"http://bremersee.org/xmlschemas/common-xml-test-model-7a "
      + "http://bremersee.github.io/xmlschemas/common-xml-test-model-7a.xsd "
      + "http://bremersee.org/xmlschemas/common-xml-test-model-7c "
      + "http://bremersee.github.io/xmlschemas/common-xml-test-model-7c.xsd\" "
      + "xmlns:ns2=\"http://bremersee.org/xmlschemas/common-xml-test-model-7c\">"
      + "<partNumber>123456789</partNumber>"
      + "<ns2:capacity>15 kg</ns2:capacity>"
      + "</ns2:Carrier>"
      + "</extraParts>"
      + "<ns2:seatHeight>60</ns2:seatHeight></ns2:MountainBike>";

  private static JaxbContextBuilder builder;

  /**
   * Sets up.
   */
  @BeforeAll
  static void setUp() {
    ClassLoader classLoader;
    if (System.getSecurityManager() == null) {
      classLoader = Thread.currentThread().getContextClassLoader();
    } else {
      //noinspection unchecked,rawtypes
      classLoader = (ClassLoader) java.security.AccessController.doPrivileged(
          (PrivilegedAction) () -> Thread.currentThread().getContextClassLoader());
    }
    builder = JaxbContextBuilder
        .builder()
        .withContextClassLoader(classLoader)
        .withDependenciesResolver(new JaxbDependenciesResolverImpl())
        .withXmlAdapters(Arrays.asList(new OffsetDateTimeXmlAdapter(), new DurationXmlAdapter()))
        .withFormattedOutput(false)
        .withSchemaMode(SchemaMode.ALWAYS)
        .addAll(Arrays.asList(
            new JaxbContextData(ObjectFactory.class.getPackage()),
            new JaxbContextData(org.bremersee.xml.model7b.ObjectFactory.class.getPackage()),
            new JaxbContextData(org.bremersee.xml.model7c.ObjectFactory.class.getPackage())));
  }

  /**
   * Write and read with context path.
   *
   * @throws Exception the exception
   */
  @Test
  void writeAndReadWithContextPath() throws Exception {

    BikeSchmied producer = new BikeSchmied();
    producer.setAddress("Somewhere");
    producer.setName("Smith");

    Carrier carrier = new Carrier();
    carrier.setPartNumber("123456789");
    carrier.setCapacity("15 kg");
    Element carrierElement = XmlDocumentBuilder.builder()
        .buildDocument(carrier, builder.copy().buildMarshaller(carrier))
        .getDocumentElement();

    DirtBikeReseller r0 = new DirtBikeReseller();
    r0.setName("Dirt Bikes");
    RacingReseller r1 = new RacingReseller();
    r1.setName("Racing Fun");

    Fastcycle r2 = new Fastcycle();
    r2.setHref("http://fast.org");

    SportBikes sportBikes = new SportBikes();
    sportBikes.setName("Sport Bikes");
    sportBikes.getChain().add(r0);
    sportBikes.getChain().add(r1);

    MountainBike model = new MountainBike();
    model.setSeatHeight(60);
    model.setColor("Red");
    model.setProducer(sportBikes);
    model.getExtraParts().add(carrierElement);

    StringWriter sw = new StringWriter();
    builder.buildMarshaller(model).marshal(model, sw);

    String actualXml = sw.toString();
    assertEquals(XML1, actualXml);

    MountainBike actualModel = (MountainBike) builder.buildUnmarshaller(MountainBike.class)
        .unmarshal(new StringReader(XML1));

    assertNotNull(actualModel);
    assertEquals(model.getSeatHeight(), actualModel.getSeatHeight());
    assertEquals(model.getColor(), actualModel.getColor());
    assertEquals(model.getProducer(), actualModel.getProducer());

    assertNotNull(actualModel.getExtraParts());
    assertFalse(actualModel.getExtraParts().isEmpty());
    Element actualCarrierElement = actualModel.getExtraParts().get(0);
    assertNotNull(actualCarrierElement);
    Carrier actualCarrier = (Carrier) builder.buildUnmarshaller().unmarshal(actualCarrierElement);
    assertNotNull(actualCarrier);
    assertEquals(carrier, actualCarrier);
  }

  /**
   * Write and read with classes.
   *
   * @throws Exception the exception
   */
  @Test
  void writeAndReadWithClasses() throws Exception {
    StartEnd startEnd = new StartEnd();
    startEnd.setStart(OffsetDateTime.now());
    startEnd.setStart(OffsetDateTime.now().plusDays(30L));

    Address model = new Address();
    model.setStreet("Casparstreet");
    model.setStreetNumber("1234");
    model.setStartEnd(startEnd);

    assertTrue(builder.canMarshal(Address.class));
    assertTrue(builder.canUnmarshal(Address.class));

    StringWriter sw = new StringWriter();
    builder.buildMarshaller(new Class[]{Address.class, Company.class}).marshal(model, sw);
    String xml = sw.toString();

    Address actual = (Address) builder.buildUnmarshaller(new Class[]{Address.class, Company.class})
        .unmarshal(new StringReader(xml));
    assertEquals(model, actual);
  }

  /**
   * Write and read with class.
   *
   * @throws Exception the exception
   */
  @Test
  void writeAndReadWithClass() throws Exception {
    StartEnd startEnd = new StartEnd();
    startEnd.setStart(OffsetDateTime.now());
    startEnd.setStart(OffsetDateTime.now().plusDays(30L));

    Address model = new Address();
    model.setStreet("Casparstreet");
    model.setStreetNumber("1234");
    model.setStartEnd(startEnd);

    assertTrue(builder.canMarshal(Address.class));
    assertTrue(builder.canUnmarshal(Address.class));

    StringWriter sw = new StringWriter();
    builder.buildMarshaller(model).marshal(model, sw);
    String xml = sw.toString();

    Address actual = (Address) builder.buildUnmarshaller(Address.class)
        .unmarshal(new StringReader(xml));
    assertEquals(model, actual);

    JaxbContextBuilder jaxbContextBuilder = builder.copy()
        .withXmlAdapters(null)
        .withDependenciesResolver(null);
    jaxbContextBuilder.buildMarshaller(model).marshal(model, new StringWriter());
  }

  /**
   * Can write and read with class.
   */
  @Test
  void canWriteAndReadWithClass() {
    JaxbContextBuilder jaxbContextBuilder = builder.copy()
        .withCanMarshal(JaxbContextBuilder.CAN_MARSHAL_ONLY_PREDEFINED_DATA)
        .withCanUnmarshal(JaxbContextBuilder.CAN_UNMARSHAL_ONLY_PREDEFINED_DATA)
        .withAttachmentMarshaller(mock(AttachmentMarshaller.class))
        .withAttachmentUnmarshaller(mock(AttachmentUnmarshaller.class))
        .withValidationEventHandler(mock(ValidationEventHandler.class));

    assertFalse(jaxbContextBuilder.canMarshal(Address.class));
    assertFalse(jaxbContextBuilder.canUnmarshal(Address.class));

    assertTrue(jaxbContextBuilder.canMarshal(MountainBike.class));
    assertTrue(jaxbContextBuilder.canUnmarshal(MountainBike.class));

    assertNotNull(jaxbContextBuilder.buildSchema());
  }

//  /**
//   * Supports.
//   */
//  @Test
//  void supports() {
//    assertTrue(builder.supports(Person.class));
//    assertTrue(builder.supports(Vehicle.class));
//    assertTrue(builder.supports(Company.class));
//    assertTrue(builder.supports(Address.class));
//
//    assertFalse(builder.supports(JaxbContextData.class));
//    assertFalse(builder.supports(
//        Person.class,
//        "http://bremersee.org/xmlschemas/common-xml-test-model-2"));
//  }
//
//  /**
//   * Build context path.
//   */
//  @Test
//  void buildContextPath() {
//    String contextPath = builder.buildContextPath();
//    assertNotNull(contextPath);
//    assertTrue(
//        contextPath.contains(org.bremersee.xml.model1.ObjectFactory.class.getPackage().getName()));
//    assertTrue(
//        contextPath.contains(org.bremersee.xml.model2.ObjectFactory.class.getPackage().getName()));
//    assertTrue(
//        contextPath.contains(org.bremersee.xml.model3.ObjectFactory.class.getPackage().getName()));
//    assertTrue(
//        contextPath.contains(org.bremersee.xml.model4.ObjectFactory.class.getPackage().getName()));
//
//    contextPath = builder.buildContextPath("");
//    assertFalse(
//        contextPath.contains(org.bremersee.xml.model1.ObjectFactory.class.getPackage().getName()));
//    assertFalse(
//        contextPath.contains(org.bremersee.xml.model2.ObjectFactory.class.getPackage().getName()));
//    assertTrue(
//        contextPath.contains(org.bremersee.xml.model3.ObjectFactory.class.getPackage().getName()));
//    assertTrue(
//        contextPath.contains(org.bremersee.xml.model4.ObjectFactory.class.getPackage().getName()));
//
//    contextPath = builder
//        .buildContextPath("http://bremersee.org/xmlschemas/common-xml-test-model-1");
//    assertTrue(
//        contextPath.contains(org.bremersee.xml.model1.ObjectFactory.class.getPackage().getName()));
//    assertFalse(
//        contextPath.contains(org.bremersee.xml.model2.ObjectFactory.class.getPackage().getName()));
//    assertFalse(
//        contextPath.contains(org.bremersee.xml.model3.ObjectFactory.class.getPackage().getName()));
//    assertFalse(
//        contextPath.contains(org.bremersee.xml.model4.ObjectFactory.class.getPackage().getName()));
//  }
//
//  /**
//   * Build schema location.
//   */
//  @Test
//  void buildSchemaLocation() {
//    final JaxbContextBuilder tmpBuilder = builder.copy()
//        .addAll(new ExampleJaxbContextDataProvider().getJaxbContextData());
//    String schemaLocation = tmpBuilder.buildSchemaLocation();
//    assertNotNull(schemaLocation);
//    assertTrue(
//        schemaLocation.contains("http://bremersee.org/xmlschemas/common-xml-test-model-1 "
//            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-1.xsd"));
//    assertTrue(
//        schemaLocation.contains("http://bremersee.org/xmlschemas/common-xml-test-model-2 "
//            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-2.xsd"));
//
//    schemaLocation = tmpBuilder
//        .buildSchemaLocation("http://bremersee.org/xmlschemas/common-xml-test-model-2");
//    assertFalse(
//        schemaLocation.contains("http://bremersee.org/xmlschemas/common-xml-test-model-1 "
//            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-1.xsd"));
//    assertTrue(
//        schemaLocation.contains("http://bremersee.org/xmlschemas/common-xml-test-model-2 "
//            + "http://bremersee.github.io/xmlschemas/common-xml-test-model-2.xsd"));
//  }
//
//  /**
//   * Build marshaller properties.
//   */
//  @Test
//  void buildMarshallerProperties() {
//    Map<String, ?> properties = JaxbContextBuilder.builder()
//        .processAll(ServiceLoader.load(JaxbContextDataProvider.class).iterator())
//        .buildMarshallerProperties();
//    assertNotNull(properties);
//    assertEquals("UTF-8", properties.get("jaxb.encoding"));
//  }
//
//  /**
//   * Build jaxb context.
//   *
//   * @throws Exception the exception
//   */
//  @Test
//  void buildJaxbContext() throws Exception {
//    JAXBContext jaxbContext = builder.buildJaxbContext();
//    assertNotNull(jaxbContext);
//    assertTrue(jaxbContext instanceof JaxbContextWrapper);
//
//    JaxbContextWrapper ctx = (JaxbContextWrapper) jaxbContext;
//
//    assertNotNull(ctx.createJAXBIntrospector());
//    assertNotNull(ctx.createMarshaller());
//    assertNotNull(ctx.createUnmarshaller());
//    assertNotNull(ctx.getSchemaLocation());
//    assertTrue(ctx.isFormattedOutput());
//    assertEquals(builder.buildContextPath(), ctx.getContextPath());
//    assertNull(ctx.getAttachmentMarshaller());
//    assertNull(ctx.getAttachmentUnmarshaller());
//    assertTrue(ctx.getXmlAdapters() == null || ctx.getXmlAdapters().isEmpty());
//    assertNull(ctx.getSchema());
//    assertNotNull(ctx.createBinder());
//    assertThrows(UnsupportedOperationException.class, () -> ctx.createBinder(null));
//    assertThrows(UnsupportedOperationException.class, ctx::createValidator);
//
//    final BufferSchemaOutputResolver res = new BufferSchemaOutputResolver();
//    builder
//        .buildJaxbContext("http://bremersee.org/xmlschemas/common-xml-test-model-2")
//        .generateSchema(res);
//    assertTrue(StringUtils.hasText(res.toString()));
//  }
//
//  /**
//   * Write and read xml.
//   *
//   * @throws Exception the exception
//   */
//  @Test
//  void writeAndReadXml() throws Exception {
//
//    Vehicle vehicle = new Vehicle();
//    vehicle.setBrand("A brand");
//    vehicle.setModel("A model");
//
//    JaxbContextBuilder jaxbContextBuilder = builder
//        .copy()
//        .withFormattedOutput(false)
//        .process(new ExampleJaxbContextDataProvider());
//
//    StringWriter sw = new StringWriter();
//    jaxbContextBuilder
//        .buildMarshaller("http://bremersee.org/xmlschemas/common-xml-test-model-2")
//        .marshal(vehicle, sw);
//
//    String xml = sw.toString();
//    Vehicle readVehicle = (Vehicle) jaxbContextBuilder
//        .buildUnmarshaller()
//        .unmarshal(new StringReader(xml));
//
//    assertEquals(vehicle, readVehicle);
//  }
//
//  /**
//   * Write xml but read fails.
//   */
//  @Test
//  void writeXmlButReadFails() {
//    assertThrows(JAXBException.class, () -> {
//      Company company = new Company();
//      company.setName("XML Generator Service");
//
//      JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder
//          .builder()
//          .withFormattedOutput(true)
//          .add(new JaxbContextData(org.bremersee.xml.model1.ObjectFactory.class.getPackage()))
//          .add(new JaxbContextData(org.bremersee.xml.model3.ObjectFactory.class.getPackage()
//              .getName()));
//
//      StringWriter sw = new StringWriter();
//      jaxbContextBuilder
//          .buildMarshaller()
//          .marshal(company, sw);
//
//      String xml = sw.toString();
//      jaxbContextBuilder
//          .buildUnmarshaller("http://bremersee.org/xmlschemas/common-xml-test-model-1")
//          .unmarshal(new StringReader(xml));
//    });
//  }
//
//  /**
//   * Build schema.
//   *
//   * @throws Exception the exception
//   */
//  @Test
//  void buildSchema() throws Exception {
//
//    Schema schema = builder
//        .copy()
//        .processAll(ServiceLoader.load(JaxbContextDataProvider.class))
//        .buildSchema();
//    assertNotNull(schema);
//
//    schema = builder.buildSchema(SchemaBuilder.builder());
//    assertNotNull(schema);
//
//    Person person = new Person();
//    person.setFirstName("Anna Livia");
//    person.setLastName("Plurabelle");
//    StringWriter out = new StringWriter();
//    builder.buildMarshaller().marshal(person, out);
//    String xml = out.toString();
//    schema.newValidator().validate(new StreamSource(new StringReader(xml)));
//    Person actual = (Person) builder
//        .buildUnmarshallerWithSchema(SchemaBuilder.builder())
//        .unmarshal(new StringReader(xml));
//    assertEquals(person, actual);
//  }
//
//  /**
//   * Build schema with pattern.
//   *
//   * @throws Exception the exception
//   */
//  @Test
//  void buildSchemaWithPattern() throws Exception {
//    List<JaxbContextData> ctxData = Arrays.asList(
//        new JaxbContextData(ObjectFactory.class.getPackage()),
//        new JaxbContextData(
//            org.bremersee.xml.model2.ObjectFactory.class.getPackage(),
//            "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd")
//    );
//
//    JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder.builder()
//        .addAll(ctxData);
//
//    JAXBContext jaxbContext = jaxbContextBuilder.buildJaxbContext();
//
//    Person person = new Person();
//    person.setLastName("Joyce");
//    person.setFirstName("James");
//
//    StringWriter personWriter = new StringWriter();
//    jaxbContext.createMarshaller().marshal(person, new StreamResult(personWriter));
//
//    Vehicle vehicle = new Vehicle();
//    vehicle.setBrand("VW");
//    vehicle.setModel("Golf");
//
//    StringWriter vehicleWriter = new StringWriter();
//    jaxbContext.createMarshaller().marshal(vehicle, new StreamResult(vehicleWriter));
//
//    Schema schema = jaxbContextBuilder.buildSchema();
//    Validator validator = schema.newValidator();
//    validator.validate(new StreamSource(new StringReader(personWriter.toString())));
//    validator.validate(new StreamSource(new StringReader(vehicleWriter.toString())));
//  }
//
//  /**
//   * Build schema with pattern and expect validation fails.
//   *
//   * @throws Exception the exception
//   */
//  @Test
//  void buildSchemaWithPatternAndExpectValidationFails() throws Exception {
//    List<JaxbContextData> ctxData = Arrays.asList(
//        new JaxbContextData(ObjectFactory.class.getPackage()),
//        new JaxbContextData(
//            org.bremersee.xml.model2.ObjectFactory.class.getPackage(),
//            "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd")
//    );
//
//    JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder.builder()
//        .addAll(ctxData);
//
//    JAXBContext jaxbContext = jaxbContextBuilder.buildJaxbContext();
//
//    Person person = new Person();
//    person.setLastName("Joyce");
//    person.setFirstName("James");
//
//    StringWriter personWriter = new StringWriter();
//    jaxbContext.createMarshaller().marshal(person, new StreamResult(personWriter));
//
//    Vehicle vehicle = new Vehicle();
//    vehicle.setBrand("VW");
//    vehicle.setModel("Golf1"); // is not allowed
//
//    StringWriter vehicleWriter = new StringWriter();
//    jaxbContext.createMarshaller().marshal(vehicle, new StreamResult(vehicleWriter));
//    String vehicleXml = vehicleWriter.toString();
//
//    Schema schema = jaxbContextBuilder.buildSchema();
//    Validator validator = schema.newValidator();
//    validator.validate(new StreamSource(new StringReader(personWriter.toString())));
//
//    assertThrows(
//        SAXParseException.class,
//        () -> validator.validate(new StreamSource(new StringReader(vehicleXml))));
//  }
//
//  /**
//   * Build jaxb context with schema.
//   *
//   * @throws Exception the exception
//   */
//  @Test
//  void buildJaxbContextWithSchema() throws Exception {
//    List<JaxbContextData> ctxData = Arrays.asList(
//        new JaxbContextData(ObjectFactory.class.getPackage()),
//        new JaxbContextData(
//            org.bremersee.xml.model2.ObjectFactory.class.getPackage(),
//            "http://bremersee.github.io/xmlschemas/common-xml-test-model-2-with-pattern.xsd")
//    );
//
//    JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder.builder()
//        .addAll(ctxData);
//
//    JAXBContext jaxbContext = jaxbContextBuilder
//        .buildJaxbContextWithSchema(SchemaBuilder.builder());
//
//    Person person = new Person();
//    person.setLastName("Joyce");
//    person.setFirstName("James");
//
//    StringWriter personWriter = new StringWriter();
//    jaxbContext.createMarshaller().marshal(person, personWriter);
//
//    Vehicle vehicle = new Vehicle();
//    vehicle.setBrand("VW");
//    vehicle.setModel("Golf"); // is allowed
//
//    StringWriter vehicleWriter = new StringWriter();
//    jaxbContext.createMarshaller().marshal(vehicle, vehicleWriter);
//
//    vehicle.setModel("Golf1"); // is not allowed
//    assertThrows(
//        MarshalException.class,
//        () -> jaxbContext.createMarshaller()
//            .marshal(vehicle, new StreamResult(new StringWriter())));
//
//    assertThrows(
//        XmlRuntimeException.class,
//        () -> jaxbContextBuilder.withValidationEventHandler(
//            event -> {
//              throw new XmlRuntimeException(event.getLinkedException());
//            })
//            .buildMarshallerWithSchema(null)
//            .marshal(vehicle, new StreamResult(new StringWriter())));
//  }
//
//  /**
//   * With xml adapters.
//   *
//   * @throws Exception the exception
//   */
//  @Test
//  void withXmlAdapters() throws Exception {
//    JaxbContextBuilder builder = JaxbContextBuilder.builder()
//        .withXmlAdapters(Arrays.asList(
//            new OffsetDateTimeXmlAdapter(),
//            new DurationXmlAdapter()))
//        .add(new JaxbContextData(org.bremersee.xml.model5.ObjectFactory.class.getPackage()));
//
//    StartEnd startEnd = new StartEnd();
//    startEnd.setStart(OffsetDateTime.now());
//    startEnd.setEnd(OffsetDateTime.now().plusDays(3L));
//    startEnd.setDuration(Duration.of(3L, ChronoUnit.DAYS));
//
//    StringWriter writer = new StringWriter();
//    Marshaller marshaller = builder
//        .buildMarshallerWithSchema(SchemaBuilder.builder());
//    assertNotNull(marshaller.getAdapter(DurationXmlAdapter.class));
//    marshaller.marshal(startEnd, writer);
//    String xml = writer.toString();
//    Unmarshaller unmarshaller = builder
//        .buildUnmarshallerWithSchema(SchemaBuilder.builder());
//    assertNotNull(unmarshaller.getAdapter(OffsetDateTimeXmlAdapter.class));
//    StartEnd actual = (StartEnd) unmarshaller.unmarshal(new StringReader(xml));
//    assertEquals(startEnd, actual);
//  }

}
