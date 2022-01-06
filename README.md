# Common XML Processing

[![codecov](https://codecov.io/gh/bremersee/common-xml/branch/master/graph/badge.svg)](https://codecov.io/gh/bremersee/common-xml)

This library contains

- some xml adapters (for java.util.Date, java.time.Duration, java.time.Instant, java.time.OffsetDateTime etc.),
- a pojo to describe an xml model (`JaxbContextData`) with package name, namespace and schema location,
- a provider (`JaxbContextDataProvider`) of these descriptions that may be loaded with java.util.ServiceLoader into
- a JAXB context builder (`JaxbContextBuilder`) which is able to generate a JAXB context on runtime,
- a schema builder (`SchemaBuilder`)
- and an xml document builder (`XmlDocumentBuilder`).

#### Maven Site

- [Release](https://bremersee.github.io/common-xml/index.html)

- [Snapshot](https://nexus.bremersee.org/repository/maven-sites/common-xml/2.3.2-SNAPSHOT/index.html)

## Usage of JaxbContextBuilder

### XML model in packages

For example, there is an extensive xml model that has been generated from many xsd files, like my 
[GPX](https://github.com/bremersee/gpx-model) and [Garmin](https://github.com/bremersee/garmin-model) 
model, and you want to support them in your application.

All the packages which contain the model of the xsd files are described with `JaxbContextData` and 
are summed up in an implementation of `JaxbContextDataProvider`
(see for example [GarminJaxbContextDataProvider](https://github.com/bremersee/garmin-model/blob/master/src/main/java/org/bremersee/garmin/GarminJaxbContextDataProvider.java)).

The implementation of the provider will be announced to the service loader by a service 
[description](https://github.com/bremersee/garmin-model/blob/master/src/main/resources/META-INF/services/org.bremersee.xml.JaxbContextDataProvider).

Then it is very easy to generate the JAXBContext using the `JaxbContextBuilder`:

```java
import java.util.ServiceLoader;
import javax.xml.bind.JAXBContext;

public class Example {

  public static void main(String[] args) {
    JAXBContext jaxbContext = JaxbContextBuilder
        .builder()
        .processAll(ServiceLoader.load(JaxbContextDataProvider.class))
        .buildJaxbContext();
  }
}
```

Another advantage of using `JaxbContextData` is the possibility to add a schema location to an xml
model that was generated with an xjc maven plugin, because the generated `package-info.java` files
don't have information about the schema location and I haven't found a way to add the schema 
location during generation.

If such provider is not present, it is possible to add the packages in different ways and combine 
them:

```java
import javax.xml.bind.JAXBContext;

public class Example {

  public static void main(String[] args) {
    JAXBContext jaxbContext = JaxbContextBuilder
        .builder()
        .add("org.example.model.foo:org.example.model.bar")
        .add(new JaxbContextData("org.example.model.foobar"))
        .buildJaxbContext();
  }
}
```

Important is that the packages, which contain the model, have a `package-info.java` annotated with
`@javax.xml.bind.annotation.XmlSchema` and an `ObjectFactory.java` class. Packages that were 
generated with XJC normally have these. 

### Single model class

The JaxbContextBuilder also supports model classes, which are not organized in a package. They only 
have to be annotated with `@XmlRootElement`:

```java
import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlRootElement;

public class Example {
  
  @XmlRootElement
  public static class Model {
    
    private String value;

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }

  public static void main(String[] args) {
    JAXBContext jaxbContext = JaxbContextBuilder
        .builder()
        .buildJaxbContext(Model.class);
  }
}
```

### Dependency resolving

So far it's not very amazing, the builder just wraps the factory methods of the `JAXBContext`. 
Let's go back to the idea that there is an extensive xml model that has been generated from many 
xsd files. If you marshal an object with that extensive jaxb context, you will get plenty of 
unnecessary name space declarations and schema locations.

```java
import java.io.StringWriter;
import java.util.ServiceLoader;
import javax.xml.bind.JAXBException;
import org.bremersee.garmin.gpx.v3.model.ext.DisplayColorT;
import org.bremersee.garmin.gpx.v3.model.ext.TrackExtension;
import org.bremersee.xml.JaxbContextBuilder;
import org.bremersee.xml.JaxbContextDataProvider;

public class Example {

  public static void main(String[] args) throws JAXBException {
    TrackExtension model = new TrackExtension();
    model.setDisplayColor(DisplayColorT.CYAN);

    StringWriter sw = new StringWriter();
    JaxbContextBuilder.builder()
        .withFormattedOutput(true)
        .processAll(ServiceLoader.load(JaxbContextDataProvider.class))
        .buildMarshaller() // builds a marshaller with the whole jaxb context
        .marshal(model, sw);
    System.out.println(sw.toString());
  }
}
```

The generated xml may look like this (the declaration is over 9000 characters long):

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns30:TrackExtension xsi:schemaLocation="http://www.garmin.com/xmlschemas/AccelerationExtension/v1 http://bremersee.github.io/xmlschemas/garmin/AccelerationExtensionv1.xsd http://www.garmin.com/xmlschemas/ActiveItemExtensions/v1 http://bremersee.github.io/xmlschemas/garmin/ActiveItemExtensionv1.xsd http://www.garmin.com/xmlschemas/ActivityExtension/v1 http://bremersee.github.io/xmlschemas/garmin/ActivityExtensionv1.xsd http://www.garmin.com/xmlschemas/ActivityExtension/v2 http://bremersee.github.io/xmlschemas/garmin/ActivityExtensionv2.xsd http://www.garmin.com/xmlschemas/ActivityGoals/v1 http://bremersee.github.io/xmlschemas/garmin/ActivityGoalsExtensionv1.xsd http://www.garmin.com/xmlschemas/AdventuresExtensions/v1 http://bremersee.github.io/xmlschemas/garmin/AdventuresExtensionv1.xsd http://www.garmin.com/xmlschemas/CalorieBurnData/v1 http://bremersee.github.io/xmlschemas/garmin/CalorieBurnDataV1.xsd http://www.garmin.com/xmlschemas/ClientProfile/v1 http://bremersee.github.io/xmlschemas/garmin/clientprofilev1.xsd http://www.garmin.com/xmlschemas/ConverterPlugin/v1 http://bremersee.github.io/xmlschemas/garmin/ConverterPluginv1.xsd http://www.garmin.com/xmlschemas/CourseExtension/v1 http://bremersee.github.io/xmlschemas/garmin/CourseExtensionv1.xsd http://www.garmin.com/xmlschemas/CreationTimeExtension/v1 http://bremersee.github.io/xmlschemas/garmin/CreationTimeExtensionv1.xsd http://www.garmin.com/xmlschemas/DeviceDownload/v1 http://bremersee.github.io/xmlschemas/garmin/DeviceDownloadV1.xsd http://www.garmin.com/xmlschemas/DirectoryListing/v1 http://bremersee.github.io/xmlschemas/garmin/DirectoryListingV1.xsd http://www.garmin.com/xmlschemas/Eula/v1 http://bremersee.github.io/xmlschemas/garmin/Eulav1.xsd http://www.garmin.com/xmlschemas/FatCalories/v1 http://bremersee.github.io/xmlschemas/garmin/fatcalorieextensionv1.xsd http://www.garmin.com/xmlschemas/FitnessDeviceLimits/v1 http://bremersee.github.io/xmlschemas/garmin/FitnessDeviceLimitsv1.xsd http://www.garmin.com/xmlschemas/ForerunnerLogbook http://bremersee.github.io/xmlschemas/garmin/ForerunnerLogbookv1.xsd http://www.garmin.com/xmlschemas/GarminDevice/v1 http://bremersee.github.io/xmlschemas/garmin/GarminDevicev1.xsd http://www.garmin.com/xmlschemas/GarminDevice/v2 http://bremersee.github.io/xmlschemas/garmin/GarminDevicev2.xsd http://www.garmin.com/xmlschemas/GarminDeviceExtensions/DataTypeLocation/v1 http://bremersee.github.io/xmlschemas/garmin/DataTypeLocationExtension1.xsd http://www.garmin.com/xmlschemas/GarminDeviceExtensions/v1 http://bremersee.github.io/xmlschemas/garmin/GarminDeviceExtensionv1.xsd http://www.garmin.com/xmlschemas/GarminDeviceExtensions/v2 http://bremersee.github.io/xmlschemas/garmin/GarminDeviceExtensionv2.xsd http://www.garmin.com/xmlschemas/GarminMobileAppPathExtension/v1 http://bremersee.github.io/xmlschemas/garmin/GarminMobileAppPathExtensionv1.xsd http://www.garmin.com/xmlschemas/GarminOEMDeviceExtensions/v1 http://bremersee.github.io/xmlschemas/garmin/GarminOEMDeviceExtensionv1.xsd http://www.garmin.com/xmlschemas/GarminTextTranslation/v1 http://bremersee.github.io/xmlschemas/garmin/GarminTextTranslationv1.xsd http://www.garmin.com/xmlschemas/ggz/1/0 http://bremersee.github.io/xmlschemas/garmin/ggz.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v1 http://bremersee.github.io/xmlschemas/garmin/GpxExtensionsv1.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v2 http://bremersee.github.io/xmlschemas/garmin/GpxExtensionsv2.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://bremersee.github.io/xmlschemas/garmin/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/HardwareVersionExtension/v1 http://bremersee.github.io/xmlschemas/garmin/HardwareVersionExtensionv1.xsd http://www.garmin.com/xmlschemas/HistoryDatabase/v1 http://bremersee.github.io/xmlschemas/garmin/HistoryDatabasev1.xsd http://www.garmin.com/xmlschemas/iFixExtensions/v1 http://bremersee.github.io/xmlschemas/garmin/iFixExtensionv1.xsd http://www.garmin.com/xmlschemas/MobileExtensions/v1 http://bremersee.github.io/xmlschemas/garmin/MobileExtensionv1.xsd http://www.garmin.com/xmlschemas/PressureExtension/v1 http://bremersee.github.io/xmlschemas/garmin/PressureExtensionv1.xsd http://www.garmin.com/xmlschemas/ProfileExtension/v1 http://bremersee.github.io/xmlschemas/garmin/UserProfilePowerExtensionv1.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://bremersee.github.io/xmlschemas/garmin/TrackPointExtensionv1.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v2 http://bremersee.github.io/xmlschemas/garmin/TrackPointExtensionv2.xsd http://www.garmin.com/xmlschemas/TrackStatsExtension/v1 http://bremersee.github.io/xmlschemas/garmin/TrackStatsExtension.xsd http://www.garmin.com/xmlschemas/TripExtensions/v1 http://bremersee.github.io/xmlschemas/garmin/TripExtensionsv1.xsd http://www.garmin.com/xmlschemas/TripMetaDataExtensions/v1 http://bremersee.github.io/xmlschemas/garmin/TripMetaDataExtensionsv1.xsd http://www.garmin.com/xmlschemas/UserAccounts/v1 http://bremersee.github.io/xmlschemas/garmin/UserAccountsExtensionv1.xsd http://www.garmin.com/xmlschemas/UserProfile/v1 http://bremersee.github.io/xmlschemas/garmin/UserProfileExtensionv1.xsd http://www.garmin.com/xmlschemas/UserProfile/v2 http://bremersee.github.io/xmlschemas/garmin/UserProfileExtensionv2.xsd http://www.garmin.com/xmlschemas/ViaPointTransportationModeExtensions/v1 http://bremersee.github.io/xmlschemas/garmin/ViaPointTransportationModeExtensionsv1.xsd http://www.garmin.com/xmlschemas/VideoExtension/v1 http://bremersee.github.io/xmlschemas/garmin/VideoExtensionv1.xsd http://www.garmin.com/xmlschemas/WaypointExtension/v1 http://bremersee.github.io/xmlschemas/garmin/WaypointExtensionv1.xsd http://www.garmin.com/xmlschemas/WebLinkExtensions/v1 http://bremersee.github.io/xmlschemas/garmin/WebLinkExtensionsv1.xsd http://www.garmin.com/xmlschemas/WorkoutDatabase/v1 http://bremersee.github.io/xmlschemas/garmin/WorkoutDatabasev1.xsd http://www.garmin.com/xmlschemas/WorkoutExtension/v1 http://bremersee.github.io/xmlschemas/garmin/WorkoutExtensionv1.xsd http://www8.garmin.com/xmlschemas/FlightPlan/v1 http://bremersee.github.io/xmlschemas/garmin/FlightPlanv1.xsd" xmlns="http://www.garmin.com/xmlschemas/AccelerationExtension/v1" xmlns:ns2="http://www.garmin.com/xmlschemas/ActiveItemExtensions/v1" xmlns:ns4="http://www.garmin.com/xmlschemas/ActivityExtension/v2" xmlns:ns3="http://www.garmin.com/xmlschemas/ActivityExtension/v1" xmlns:ns6="http://www.garmin.com/xmlschemas/AdventuresExtensions/v1" xmlns:ns5="http://www.garmin.com/xmlschemas/ActivityGoals/v1" xmlns:ns8="http://www.garmin.com/xmlschemas/ClientProfile/v1" xmlns:ns51="http://www.garmin.com/xmlschemas/PowerExtension/v1" xmlns:ns7="http://www.garmin.com/xmlschemas/CalorieBurnData/v1" xmlns:ns50="http://www.garmin.com/xmlschemas/WorkoutExtension/v1" xmlns:ns13="http://www.garmin.com/xmlschemas/DeviceDownload/v1" xmlns:ns9="http://www.garmin.com/xmlschemas/ConverterPlugin/v1" xmlns:ns12="http://www.garmin.com/xmlschemas/GarminDeviceExtensions/DataTypeLocation/v1" xmlns:ns11="http://www.garmin.com/xmlschemas/CreationTimeExtension/v1" xmlns:ns10="http://www.garmin.com/xmlschemas/CourseExtension/v1" xmlns:ns17="http://www.garmin.com/xmlschemas/FitnessDeviceLimits/v1" xmlns:ns16="http://www.garmin.com/xmlschemas/FatCalories/v1" xmlns:ns15="http://www.garmin.com/xmlschemas/Eula/v1" xmlns:ns14="http://www.garmin.com/xmlschemas/DirectoryListing/v1" xmlns:ns19="http://www.garmin.com/xmlschemas/ForerunnerLogbook" xmlns:ns18="http://www8.garmin.com/xmlschemas/FlightPlan/v1" xmlns:ns42="http://www.garmin.com/xmlschemas/UserProfile/v1" xmlns:ns41="http://www.garmin.com/xmlschemas/UserAccounts/v1" xmlns:ns40="http://www.garmin.com/xmlschemas/TripMetaDataExtensions/v1" xmlns:ns46="http://www.garmin.com/xmlschemas/VideoExtension/v1" xmlns:ns45="http://www.garmin.com/xmlschemas/ViaPointTransportationModeExtensions/v1" xmlns:ns44="http://www.garmin.com/xmlschemas/ProfileExtension/v1" xmlns:ns43="http://www.garmin.com/xmlschemas/UserProfile/v2" xmlns:ns49="http://www.garmin.com/xmlschemas/WorkoutDatabase/v1" xmlns:ns48="http://www.garmin.com/xmlschemas/WebLinkExtensions/v1" xmlns:ns47="http://www.garmin.com/xmlschemas/WaypointExtension/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ns31="http://www.garmin.com/xmlschemas/HardwareVersionExtension/v1" xmlns:ns30="http://www.garmin.com/xmlschemas/GpxExtensions/v3" xmlns:ns35="http://www.garmin.com/xmlschemas/PressureExtension/v1" xmlns:ns34="http://www.garmin.com/xmlschemas/MobileExtensions/v1" xmlns:ns33="http://www.garmin.com/xmlschemas/iFixExtensions/v1" xmlns:ns32="http://www.garmin.com/xmlschemas/HistoryDatabase/v1" xmlns:ns39="http://www.garmin.com/xmlschemas/TripExtensions/v1" xmlns:ns38="http://www.garmin.com/xmlschemas/TrackStatsExtension/v1" xmlns:ns37="http://www.garmin.com/xmlschemas/TrackPointExtension/v2" xmlns:ns36="http://www.garmin.com/xmlschemas/TrackPointExtension/v1" xmlns:ns20="http://www.garmin.com/xmlschemas/GarminDevice/v1" xmlns:ns24="http://www.garmin.com/xmlschemas/GarminMobileAppPathExtension/v1" xmlns:ns23="http://www.garmin.com/xmlschemas/GarminDeviceExtensions/v2" xmlns:ns22="http://www.garmin.com/xmlschemas/GarminDevice/v2" xmlns:ns21="http://www.garmin.com/xmlschemas/GarminDeviceExtensions/v1" xmlns:ns28="http://www.garmin.com/xmlschemas/GpxExtensions/v1" xmlns:ns27="http://www.garmin.com/xmlschemas/ggz/1/0" xmlns:ns26="http://www.garmin.com/xmlschemas/GarminTextTranslation/v1" xmlns:ns25="http://www.garmin.com/xmlschemas/GarminOEMDeviceExtensions/v1" xmlns:ns29="http://www.garmin.com/xmlschemas/GpxExtensions/v2">
    <ns30:DisplayColor>Cyan</ns30:DisplayColor>
</ns30:TrackExtension>
```

But if you call the build marshaller method for the current object, you will get only declarations 
that are necessary:

```java
import java.io.StringWriter;
import java.util.ServiceLoader;
import javax.xml.bind.JAXBException;
import org.bremersee.garmin.gpx.v3.model.ext.DisplayColorT;
import org.bremersee.garmin.gpx.v3.model.ext.TrackExtension;
import org.bremersee.xml.JaxbContextBuilder;
import org.bremersee.xml.JaxbContextDataProvider;

public class Example {

  public static void main(String[] args) throws JAXBException {
    TrackExtension model = new TrackExtension();
    model.setDisplayColor(DisplayColorT.CYAN);

    StringWriter sw = new StringWriter();
    JaxbContextBuilder.builder()
        .withFormattedOutput(true)
        .processAll(ServiceLoader.load(JaxbContextDataProvider.class))
        .buildMarshaller(model) // builds a marshaller with a subset jaxb context
        .marshal(model, sw);
    System.out.println(sw.toString());
  }
}
```

Then the generated xml looks like this:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<TrackExtension xsi:schemaLocation="http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://bremersee.github.io/xmlschemas/garmin/GpxExtensionsv3.xsd" xmlns="http://www.garmin.com/xmlschemas/GpxExtensions/v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <DisplayColor>Cyan</DisplayColor>
</TrackExtension>
```

That's pretty nice, isn't it?

The `JaxbContextBuilder` scans the object and creates internally a new `JAXBContext` with all 
required packages or classes. Since the creation of a jaxb context is quite expensive, the created 
contexts will be cached and reused, if a further object scan returns the same result. 

This dependency resolving can be turned off by setting the resolver to null:

```java
import java.io.StringWriter;
import java.util.ServiceLoader;
import javax.xml.bind.JAXBException;
import org.bremersee.garmin.gpx.v3.model.ext.DisplayColorT;
import org.bremersee.garmin.gpx.v3.model.ext.TrackExtension;
import org.bremersee.xml.JaxbContextBuilder;
import org.bremersee.xml.JaxbContextDataProvider;

public class Example {

  public static void main(String[] args) throws JAXBException {
    TrackExtension model = new TrackExtension();
    model.setDisplayColor(DisplayColorT.CYAN);

    StringWriter sw = new StringWriter();
    JaxbContextBuilder.builder()
        .withFormattedOutput(true)
        .withDependenciesResolver(null) // turns off dependency resolving
        .processAll(ServiceLoader.load(JaxbContextDataProvider.class))
        .buildMarshaller(model)
        .marshal(model, sw);
    System.out.println(sw.toString());
  }
}
```

## Usage of SchemaBuilder

The schema builder wraps the standard `SchemaFactory` of Java into a builder. It also offers the 
ability to load schema using Spring's `ResourceLoader`.

```java
import javax.xml.validation.Schema;

public class Example {

  public static void main(String[] args) {
    Schema schema = SchemaBuilder.builder()
        .buildSchema("classpath:common-xml-test-model-1.xsd",
            "http://bremersee.github.io/xmlschemas/common-xml-test-model-7b.xsd");
  }
}
```

## Usage of XmlDocumentBuilder

The xml document builder wraps the functionality of the standard `DocumentBuilderFactory` and 
`DocumentBuilder` of Java into one builder.

It may be useful to create elements for objects that can contain any element:

```java
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Example {

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Model {

    @XmlValue
    private String value;

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class ModelContainer {

    @XmlElementWrapper(name = "models")
    @XmlAnyElement
    private List<Element> models;

    public List<Element> getModels() {
      if (models == null) {
        models = new ArrayList<>();
      }
      return models;
    }
  }

  public static void main(String[] args) throws JAXBException {
    Model model = new Model();
    model.setValue("Hello world!");

    Document document = XmlDocumentBuilder.builder()
        .buildDocument(model, JaxbContextBuilder.builder().buildMarshaller(model));

    ModelContainer container = new ModelContainer();
    container.getModels().add(document.getDocumentElement());

    StringWriter sw = new StringWriter();
    JaxbContextBuilder.builder().buildMarshaller(container).marshal(container, sw);
    System.out.println(sw.toString());
  }
}
```

The xml will be:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<modelContainer>
    <models>
        <model>Hello world!</model>
    </models>
</modelContainer>
```
