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
import static org.mockito.Mockito.mock;

import java.io.StringReader;
import java.io.StringWriter;
import java.security.PrivilegedAction;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import org.bremersee.xml.adapter.DateXmlAdapter;
import org.bremersee.xml.adapter.DurationXmlAdapter;
import org.bremersee.xml.adapter.EpochMilliXmlAdapter;
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
            new JaxbContextData(org.bremersee.xml.model7c.ObjectFactory.class.getPackage())))
        .initJaxbContext();
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
    startEnd.setStart(OffsetDateTime
        .parse("2000-01-16T12:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    startEnd.setStart(OffsetDateTime
        .parse("2000-01-20T12:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

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
    startEnd.setStart(OffsetDateTime
        .parse("2000-01-16T12:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    startEnd.setStart(OffsetDateTime
        .parse("2000-01-20T12:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

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
        .withCanUnmarshal(JaxbContextBuilder.CAN_UNMARSHAL_ONLY_PREDEFINED_DATA);

    assertFalse(jaxbContextBuilder.canMarshal(Address.class));
    assertFalse(jaxbContextBuilder.canUnmarshal(Address.class));

    assertTrue(jaxbContextBuilder.canMarshal(MountainBike.class));
    assertTrue(jaxbContextBuilder.canUnmarshal(MountainBike.class));

    assertNotNull(jaxbContextBuilder.buildSchema());
  }

  /**
   * Add context path.
   */
  @Test
  void addContextPath() {
    // We have no valid xml model package here; the test packages produce illegal argument
    // exceptions, because Package.getPackage(java.langString) doesn't work with mvn test.
    assertThrows(JaxbRuntimeException.class, () -> builder.copy()
        .add("org.bremersee.xml.adapter")
        .buildMarshaller());
  }

  /**
   * Build jaxb context.
   *
   * @throws Exception the exception
   */
  @Test
  void buildJaxbContext() throws Exception {
    JaxbContextWrapper ctx = builder.copy()
        .withFormattedOutput(true)
        .withSchemaMode(SchemaMode.ALWAYS)
        .withAttachmentMarshaller(mock(AttachmentMarshaller.class))
        .withAttachmentUnmarshaller(mock(AttachmentUnmarshaller.class))
        .withValidationEventHandler(mock(ValidationEventHandler.class))
        .withXmlAdapters(Arrays.asList(
            new EpochMilliXmlAdapter(),
            new DateXmlAdapter()))
        .buildJaxbContext();

    assertNotNull(ctx);
    assertNotNull(ctx.createJAXBIntrospector());
    assertNotNull(ctx.createMarshaller());
    assertNotNull(ctx.createUnmarshaller());
    assertNotNull(ctx.getAttachmentMarshaller());
    assertNotNull(ctx.getAttachmentUnmarshaller());
    assertNotNull(ctx.getXmlAdapters());
    assertNotNull(ctx.getSchema());
    assertNotNull(ctx.getDetails());
    assertNotNull(ctx.getValidationEventHandler());
    assertTrue(ctx.isFormattedOutput());
    assertEquals(SchemaMode.ALWAYS, ctx.getSchemaMode());
  }

}
