package org.bremersee.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ServiceLoader;
import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.bremersee.xml.model1.ObjectFactory;
import org.bremersee.xml.model1.Person;
import org.junit.jupiter.api.Test;

class SchemaBuilderTest {

  @Test
  void buildSchemaWithJaxbContext() throws Exception {

    JaxbContextBuilder jaxbContextBuilder = JaxbContextBuilder
        .builder()
        // .add(new JaxbContextData(ObjectFactory.class.getPackage()));
        .processAll(ServiceLoader.load(JaxbContextDataProvider.class));

    JAXBContext jaxbContext = jaxbContextBuilder.buildJaxbContext(); // "http://bremersee.org/xmlschemas/common-xml-test-model-1"

    Schema schema = jaxbContextBuilder.buildSchema(SchemaBuilder.builder());

    assertNotNull(schema);

    BufferSchemaOutputResolver r = new BufferSchemaOutputResolver();
    jaxbContext.generateSchema(r);

    Person person = new Person();
    person.setFirstName("Anna Livia");
    person.setLastName("Plurabelle");
    StringWriter out = new StringWriter();
    jaxbContext.createMarshaller().marshal(person, out);
    String xml = out.toString();
    System.out.println(xml);
    schema.newValidator().validate(new StreamSource(new StringReader(xml)));
  }
}