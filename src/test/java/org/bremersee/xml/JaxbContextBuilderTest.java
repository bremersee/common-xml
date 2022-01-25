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

import static org.mockito.Mockito.mock;

import java.io.StringReader;
import java.io.StringWriter;
import java.security.PrivilegedAction;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
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
import org.bremersee.xml.model3.Company;
import org.bremersee.xml.model4.Address;
import org.bremersee.xml.model5.StartEnd;
import org.bremersee.xml.model6.StandaloneModel;
import org.bremersee.xml.model7a.Fender;
import org.bremersee.xml.model7a.ObjectFactory;
import org.bremersee.xml.model7b.DirtBikeReseller;
import org.bremersee.xml.model7b.MountainBike;
import org.bremersee.xml.model7b.RacingReseller;
import org.bremersee.xml.model7b.SportBikes;
import org.bremersee.xml.model7c.Carrier;
import org.bremersee.xml.model8.AnyElementList;
import org.junit.jupiter.api.BeforeEach;
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

  private JaxbContextBuilder builder;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
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
        .withSchemaBuilder(SchemaBuilder.builder())
        .addAll(Arrays.asList(
            new JaxbContextData(ObjectFactory.class.getPackage()),
            new JaxbContextData(org.bremersee.xml.model7b.ObjectFactory.class.getPackage()),
            new JaxbContextData(org.bremersee.xml.model7c.ObjectFactory.class.getPackage())))
        .initJaxbContext();
  }

  /**
   * Write and read with context path.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void writeAndReadWithContextPath(SoftAssertions softly) throws Exception {

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

    Element carrierElement = XmlDocumentBuilder.builder()
        .buildDocument(carrier, builder.buildMarshaller(carrier))
        .getDocumentElement();

    MountainBike model = new MountainBike();
    model.setSeatHeight(60);
    model.setColor("Red");
    model.setProducer(sportBikes);
    model.getExtraParts().add(carrierElement);

    StringWriter sw = new StringWriter();
    builder.buildMarshaller(model).marshal(model, sw);

    String actualXml = sw.toString();
    softly.assertThat(actualXml)
        .isEqualTo(XML1);

    MountainBike actualModel = (MountainBike) builder.buildUnmarshaller(MountainBike.class)
        .unmarshal(new StringReader(XML1));
    softly.assertThat(actualModel)
        .isEqualTo(model);
    softly.assertThat(actualModel.getExtraParts())
        .map(elem -> builder.buildUnmarshaller().unmarshal(elem))
        .containsExactly(carrier);
  }

  /**
   * Write and read with classes.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void writeAndReadWithClasses(SoftAssertions softly) throws Exception {
    StartEnd startEnd = new StartEnd();
    startEnd.setStart(OffsetDateTime
        .parse("2000-01-16T12:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    startEnd.setStart(OffsetDateTime
        .parse("2000-01-20T12:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

    Address model = new Address();
    model.setStreet("Casparstreet");
    model.setStreetNumber("1234");
    model.setStartEnd(startEnd);

    softly.assertThat(builder.canMarshal(Address.class))
        .isTrue();
    softly.assertThat(builder.canMarshalWithoutExtending(Address.class))
        .isFalse();
    softly.assertThat(builder.canUnmarshal(Address.class))
        .isTrue();
    softly.assertThat(builder.canUnmarshalWithoutExtending(Address.class))
        .isFalse();

    StringWriter sw = new StringWriter();
    builder.buildMarshaller(new Class[]{Address.class, Company.class}).marshal(model, sw);
    String xml = sw.toString();

    Address actual = (Address) builder.buildUnmarshaller(new Class[]{Address.class, Company.class})
        .unmarshal(new StringReader(xml));
    softly.assertThat(actual)
        .isEqualTo(model);
  }

  /**
   * Write and read with class.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void writeAndReadWithClass(SoftAssertions softly) throws Exception {
    StartEnd startEnd = new StartEnd();
    startEnd.setStart(OffsetDateTime
        .parse("2000-01-16T12:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    startEnd.setStart(OffsetDateTime
        .parse("2000-01-20T12:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

    Address model = new Address();
    model.setStreet("Casparstreet");
    model.setStreetNumber("1234");
    model.setStartEnd(startEnd);

    StringWriter sw = new StringWriter();
    builder.buildMarshaller(model).marshal(model, sw);
    String xml = sw.toString();

    Address actual = (Address) builder.buildUnmarshaller(Address.class)
        .unmarshal(new StringReader(xml));
    softly.assertThat(actual)
        .isEqualTo(model);

    JaxbContextBuilder jaxbContextBuilder = builder
        .withXmlAdapters(null)
        .withDependenciesResolver(null);
    sw = new StringWriter();
    jaxbContextBuilder.buildMarshaller(model).marshal(model, sw);
    actual = (Address) builder.buildUnmarshaller(Address.class)
        .unmarshal(new StringReader(sw.toString()));
    softly.assertThat(actual)
        .isEqualTo(model);
  }

  /**
   * Can write and read with class.
   *
   * @param softly the soft assertions
   */
  @Test
  void canWriteAndReadWithClass(SoftAssertions softly) {
    JaxbContextBuilder jaxbContextBuilder = builder;

    softly.assertThat(jaxbContextBuilder.canMarshalWithoutExtending(Address.class))
        .isFalse();
    softly.assertThat(jaxbContextBuilder.canUnmarshalWithoutExtending(Address.class))
        .isFalse();

    softly.assertThat(jaxbContextBuilder.canMarshalWithoutExtending(MountainBike.class))
        .isTrue();
    softly.assertThat(jaxbContextBuilder.canUnmarshalWithoutExtending(MountainBike.class))
        .isTrue();

    softly.assertThat(jaxbContextBuilder.buildSchema())
        .isNotNull();
  }

  /**
   * Build jaxb context.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void buildJaxbContext(SoftAssertions softly) throws Exception {
    JaxbContextWrapper ctx = builder.copy()
        .withFormattedOutput(true)
        .withSchemaMode(SchemaMode.EXTERNAL_XSD)
        .withAttachmentMarshaller(mock(AttachmentMarshaller.class))
        .withAttachmentUnmarshaller(mock(AttachmentUnmarshaller.class))
        .withValidationEventHandler(mock(ValidationEventHandler.class))
        .withXmlAdapters(Arrays.asList(
            new EpochMilliXmlAdapter(),
            new DateXmlAdapter()))
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
        .withSchemaMode(SchemaMode.EXTERNAL_XSD)
        .add("org.bremersee.xml.model7a:org.bremersee.xml.model7b:org.bremersee.xml.model7c");
    softly.assertThat(builder.buildUnmarshaller(null)).isNotNull();
    softly.assertThat(builder.buildUnmarshaller(new Fender())).isNotNull();
    softly.assertThat(builder.buildUnmarshaller(new StandaloneModel())).isNotNull();
  }

  /**
   * Build marshaller.
   *
   * @param softly the soft assertions
   */
  @Test
  void buildMarshaller(SoftAssertions softly) {
    JaxbContextBuilder builder = JaxbContextBuilder.builder()
        .withSchemaMode(SchemaMode.EXTERNAL_XSD)
        .add("org.bremersee.xml.model7a:org.bremersee.xml.model7b:org.bremersee.xml.model7c");
    softly.assertThat(builder.buildMarshaller(null)).isNotNull();
    softly.assertThat(builder.buildMarshaller(new Fender())).isNotNull();
    softly.assertThat(builder.buildMarshaller(new StandaloneModel())).isNotNull();
  }

  /**
   * Write any elements.
   *
   * @param softly the soft assertions
   * @throws Exception the exception
   */
  @Test
  void writeAnyElements(SoftAssertions softly) throws Exception {
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

    // Empty jaxb context
    JaxbContextBuilder emptyBuilder = JaxbContextBuilder
        .builder();
    softly.assertThat(emptyBuilder.canMarshalWithoutExtending(AnyElementList.class)).isFalse();
    softly.assertThat(emptyBuilder.canMarshalWithoutExtending(Company.class)).isFalse();
    softly.assertThat(emptyBuilder.canMarshalWithoutExtending(Address.class)).isFalse();
    softly.assertThat(emptyBuilder.canMarshalWithoutExtending(StandaloneModel.class)).isFalse();

    // We need a new object because we use soft assertions here.
    JaxbContextBuilder builder = JaxbContextBuilder
        .builder();
    Marshaller marshaller = builder.buildMarshaller(list);
    StringWriter sw = new StringWriter();
    marshaller.marshal(list, sw);

    // Empty jaxb context was extended:
    softly.assertThat(builder.canMarshalWithoutExtending(AnyElementList.class)).isTrue();
    softly.assertThat(builder.canMarshalWithoutExtending(Company.class)).isTrue();
    softly.assertThat(builder.canMarshalWithoutExtending(Address.class)).isTrue();

    // It's still false, because it has no package with jaxb meta-data.
    softly.assertThat(emptyBuilder.canMarshalWithoutExtending(StandaloneModel.class)).isFalse();

    String xml = sw.toString();

    AnyElementList actual = (AnyElementList) builder
        .buildUnmarshaller().unmarshal(new StringReader(xml));
    softly.assertThat(actual)
        .isNotEqualTo(list); // unmarshalling of StandaloneModel is not possible
    softly.assertThat(actual.getContent())
        .hasSize(3)
        .contains(
            new Company("UnitTest"),
            new Address("Surefire", "123", new StartEnd(start, null, null)))
        .anyMatch(entry -> entry instanceof Element);
    // StandaloneModel was unmarshalled as org.w3c.dom.Element

    Element element = (Element) actual.getContent().get(2);
    StandaloneModel actualStandaloneModel = (StandaloneModel) builder
        .buildUnmarshaller(StandaloneModel.class)
        .unmarshal(element);
    softly.assertThat(actualStandaloneModel)
        .isEqualTo(new StandaloneModel("assert"));
  }

}
