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
import static org.mockito.Mockito.mock;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.xml.adapter.DateXmlAdapter;
import org.bremersee.xml.adapter.DurationXmlAdapter;
import org.bremersee.xml.adapter.EpochMilliXmlAdapter;
import org.bremersee.xml.adapter.OffsetDateTimeXmlAdapter;
import org.bremersee.xml.model1.Person;
import org.bremersee.xml.model2.Vehicle;
import org.bremersee.xml.model3.Company;
import org.bremersee.xml.model4.Address;
import org.bremersee.xml.model5.StartEnd;
import org.bremersee.xml.model6.StandaloneModel;
import org.bremersee.xml.model7a.Fender;
import org.bremersee.xml.model7a.ObjectFactory;
import org.bremersee.xml.model7b.DirtBikeReseller;
import org.bremersee.xml.model7b.MountainBike;
import org.bremersee.xml.model7b.RacingBike;
import org.bremersee.xml.model7b.RacingReseller;
import org.bremersee.xml.model7b.SportBikes;
import org.bremersee.xml.model7c.Carrier;
import org.bremersee.xml.model8.AnyElementList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.w3c.dom.Element;

/**
 * The jaxb context builder test.
 *
 * @author Christian Bremer
 */
@ExtendWith({SoftAssertionsExtension.class})
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

  /**
   * Read and write element.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void readAndWriteElement(SoftAssertions softly) throws Exception {

    Carrier carrier = new Carrier();
    carrier.setPartNumber("123456789");
    carrier.setCapacity("15 kg");

    DirtBikeReseller r0 = new DirtBikeReseller();
    r0.setName("Dirt Bikes");
    RacingReseller r1 = new RacingReseller();
    r1.setName("Racing Fun");

    SportBikes sportBikes = new SportBikes();
    sportBikes.setName("Sport Bikes");
    sportBikes.getChain().add(r0);
    sportBikes.getChain().add(r1);

    JaxbContextBuilder builder = JaxbContextBuilder
        .builder()
        .withDependenciesResolver(new JaxbDependenciesResolverImpl())
        .withXmlAdapters(Arrays.asList(new OffsetDateTimeXmlAdapter(), new DurationXmlAdapter()))
        .copy()
        .withFormattedOutput(false)
        .withSchemaMode(SchemaMode.MARSHAL)
        .withSchemaBuilder(SchemaBuilder.builder())
        .withContextClassLoader(Thread.currentThread().getContextClassLoader())
        .addAll(Arrays.asList(
            new JaxbContextData(ObjectFactory.class.getPackage()),
            new JaxbContextData(org.bremersee.xml.model7b.ObjectFactory.class.getPackage()),
            new JaxbContextData(org.bremersee.xml.model7c.ObjectFactory.class.getPackage())))
        .process(() -> List.of(new JaxbContextData(StandaloneModel.class)))
        .processAll(ServiceLoader.load(JaxbContextDataProvider.class));

    MountainBike model = new MountainBike();
    model.setSeatHeight(60);
    model.setColor("Red");
    model.setProducer(sportBikes);

    Element carrierElement = XmlDocumentBuilder.builder()
        .buildDocument(carrier, builder.buildMarshaller(carrier))
        .getDocumentElement();
    model.getExtraParts().add(carrierElement);

    MountainBike actualModel = (MountainBike) builder
        .copy()
        .withDependenciesResolver(null)
        .buildUnmarshaller(MountainBike.class)
        .unmarshal(new StringReader(XML1));
    softly.assertThat(actualModel)
        .isEqualTo(model);
    softly.assertThat(actualModel.getExtraParts())
        .map(elem -> builder
            .buildUnmarshaller().unmarshal(elem))
        .containsExactly(carrier);

    StringWriter sw = new StringWriter();
    builder.buildMarshaller(model).marshal(model, sw);
    String actualXml = sw.toString();
    softly.assertThat(actualXml)
        .isEqualTo(XML1);
  }

  /**
   * Write and read any elements.
   *
   * @throws Exception the exception
   */
  @Test
  void writeAndReadAnyElements() throws Exception {
    OffsetDateTime start = OffsetDateTime
        .parse("2000-01-16T12:00:00.000Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    AnyElementList list = new AnyElementList(List.of(

        // Company has no namespace.
        new Company("UnitTest"),

        // Address has no namespace, but StartEnd has one.
        new Address("Surefire", "123", new StartEnd(start, null, null)),

        // StandaloneModel has a namespace, too.
        new StandaloneModel("assert")
    ));

    JaxbContextBuilder builder = JaxbContextBuilder
        .builder();
    Marshaller marshaller = builder.buildMarshaller(list);
    StringWriter sw = new StringWriter();
    marshaller.marshal(list, sw);

    String xml = sw.toString();

    AnyElementList actual = (AnyElementList) builder
        .buildUnmarshaller().unmarshal(new StringReader(xml));
    assertThat(actual)
        .isEqualTo(list);
  }

  /**
   * Write and read date time.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void writeAndReadDateTime(SoftAssertions softly) throws Exception {

    JaxbContextBuilder builder = JaxbContextBuilder
        .builder()
        .initJaxbContext()
        .withFormattedOutput(true)
        .withSchemaMode(SchemaMode.ALWAYS)
        .addAll(List.of(new JaxbContextData(Address.class)).iterator())
        .initJaxbContext();

    softly.assertThat(builder.canMarshal(Address.class))
        .isTrue();
    softly.assertThat(builder.canUnmarshal(Address.class))
        .isTrue();

    StartEnd startEnd = new StartEnd();
    startEnd.setStart(OffsetDateTime
        .parse("2000-01-16T12:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    startEnd.setEnd(OffsetDateTime
        .parse("2000-01-20T12:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    startEnd.setDuration(Duration.ofDays(4));

    Address model = new Address();
    model.setStreet("Casparstreet");
    model.setStreetNumber("1234");
    model.setStartEnd(startEnd);

    StringWriter sw = new StringWriter();
    builder.buildMarshaller().marshal(model, sw);
    String xml = sw.toString();

    Address actual = (Address) builder.buildUnmarshaller(Address.class, Company.class)
        .unmarshal(new StringReader(xml));
    softly.assertThat(actual)
        .isEqualTo(model);
  }

  /**
   * Write and read date time with empty context.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void writeAndReadDateTimeWithEmptyContext(SoftAssertions softly) throws Exception {

    StartEnd startEnd = new StartEnd();
    startEnd.setStart(OffsetDateTime
        .parse("2000-01-16T12:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    startEnd.setEnd(OffsetDateTime
        .parse("2000-01-20T12:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

    Address model = new Address();
    model.setStreet("Casparstreet");
    model.setStreetNumber("1234");
    model.setStartEnd(startEnd);

    JaxbContextBuilder builder = JaxbContextBuilder
        .builder()
        .withSchemaMode(SchemaMode.UNMARSHAL);

    StringWriter sw = new StringWriter();
    builder.buildMarshaller(new Class[]{Address.class, Company.class}).marshal(model, sw);
    String xml = sw.toString();

    Address actual = (Address) builder.buildUnmarshaller(Address.class)
        .unmarshal(new StringReader(xml));
    softly.assertThat(actual)
        .isEqualTo(model);

    JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder.builder()
        .withXmlAdapters(null)
        .withDependenciesResolver(null);
    sw = new StringWriter();
    jaxbContextBuilder.buildMarshaller(model).marshal(model, sw);
    xml = sw.toString();

    actual = (Address) builder.buildUnmarshaller(Address.class)
        .unmarshal(new StringReader(xml));
    softly.assertThat(actual)
        .isEqualTo(model);
  }

  /**
   * Build jaxb context.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void buildJaxbContext(SoftAssertions softly) throws Exception {
    JaxbContextWrapper ctx = JaxbContextBuilder.builder().copy()
        .withFormattedOutput(true)
        .withSchemaMode(SchemaMode.EXTERNAL_XSD)
        .withAttachmentMarshaller(mock(AttachmentMarshaller.class))
        .withAttachmentUnmarshaller(mock(AttachmentUnmarshaller.class))
        .withValidationEventHandler(mock(ValidationEventHandler.class))
        .withXmlAdapters(Arrays.asList(
            new EpochMilliXmlAdapter(),
            new DateXmlAdapter()))
        .add(new JaxbContextData(RacingBike.class))
        .buildJaxbContext();

    softly.assertThat(ctx)
        .extracting(JaxbContextWrapper::createJAXBIntrospector)
        .isNotNull();
    softly.assertThat(ctx)
        .extracting(JaxbContextWrapper::getAttachmentMarshaller)
        .isNotNull();
    softly.assertThat(ctx)
        .extracting(JaxbContextWrapper::getAttachmentUnmarshaller)
        .isNotNull();
    softly.assertThat(ctx)
        .extracting(JaxbContextWrapper::getXmlAdapters)
        .isNotNull();
    softly.assertThat(ctx)
        .extracting(JaxbContextWrapper::getSchema)
        .isNotNull();
    softly.assertThat(ctx)
        .extracting(JaxbContextWrapper::getDetails)
        .isNotNull();
    softly.assertThat(ctx)
        .extracting(JaxbContextWrapper::getValidationEventHandler)
        .isNotNull();
    softly.assertThat(ctx)
        .extracting(JaxbContextWrapper::isFormattedOutput, InstanceOfAssertFactories.BOOLEAN)
        .isTrue();
    softly.assertThat(ctx)
        .extracting(JaxbContextWrapper::getSchemaMode)
        .isEqualTo(SchemaMode.EXTERNAL_XSD);

    softly.assertThat(ctx.createMarshaller())
        .isNotNull();
    softly.assertThat(ctx.createUnmarshaller())
        .isNotNull();
  }

  /**
   * Build unmarshaller.
   *
   * @param softly the soft assertions
   */
  @Test
  void buildUnmarshaller(SoftAssertions softly) {
    JaxbContextBuilder builder = JaxbContextBuilder.builder()
        .withSchemaMode(SchemaMode.EXTERNAL_XSD);
    softly.assertThat(builder.buildUnmarshaller(Fender.class)).isNotNull();
    softly.assertThat(builder.buildUnmarshaller(StandaloneModel.class)).isNotNull();
  }

  /**
   * Build marshaller.
   *
   * @param softly the soft assertions
   */
  @Test
  void buildMarshaller(SoftAssertions softly) {
    JaxbContextBuilder builder = JaxbContextBuilder.builder()
        .withSchemaMode(SchemaMode.EXTERNAL_XSD);
    softly.assertThat(builder.buildMarshaller(new Fender())).isNotNull();
    softly.assertThat(builder.buildMarshaller(StandaloneModel.class)).isNotNull();
    softly.assertThat(builder
            .copy()
            .withDependenciesResolver(null)
            .buildMarshaller(new Class<?>[]{Person.class, Vehicle.class}))
        .isNotNull();
  }

  /**
   * Build schema.
   *
   * @param softly the soft assertions
   */
  @Test
  void buildSchema(SoftAssertions softly) {
    JaxbContextBuilder builder = JaxbContextBuilder.builder()
        .withSchemaMode(SchemaMode.EXTERNAL_XSD)
        .add(new JaxbContextData(Person.class));
    softly.assertThat(builder.buildSchema()).isNotNull();
    softly.assertThat(builder.buildSchema(new Fender())).isNotNull();
  }

}