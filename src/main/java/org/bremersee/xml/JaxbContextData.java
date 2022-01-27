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

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import lombok.EqualsAndHashCode;
import org.springframework.util.Assert;

/**
 * Meta-data to describe a xml model.
 *
 * @author Christian Bremer
 */
@EqualsAndHashCode
public final class JaxbContextData implements Comparable<JaxbContextData> {

  private final Class<?> clazz;

  private final String clazzElementSchemaLocation;

  private final String clazzTypeSchemaLocation;

  private final Package pakkage;

  private final String pakkageSchemaLocation;

  /**
   * Instantiates a new jaxb context data.
   *
   * @param clazz the class
   */
  public JaxbContextData(Class<?> clazz) {
    this(clazz, null, null);
  }

  /**
   * Instantiates a new jaxb context data.
   *
   * @param clazz the class
   * @param clazzElementSchemaLocation the class element schema location
   * @param clazzTypeSchemaLocation the class type schema location
   */
  public JaxbContextData(
      Class<?> clazz,
      String clazzElementSchemaLocation,
      String clazzTypeSchemaLocation) {

    Assert.notNull(clazz, "Class must be present.");
    if (JaxbUtils.isInJaxbModelPackage(clazz)) {
      this.pakkage = clazz.getPackage();
      this.pakkageSchemaLocation = JaxbUtils
          .getSchemaLocation(this.pakkage)
          .orElse(null);
      this.clazz = null;
      this.clazzElementSchemaLocation = null;
      this.clazzTypeSchemaLocation = null;
    } else if (clazz.isAnnotationPresent(XmlRootElement.class)
        || clazz.isAnnotationPresent(XmlType.class)) {
      this.clazz = clazz;
      this.clazzElementSchemaLocation = Optional.ofNullable(clazzElementSchemaLocation)
          .filter(location -> !location.isBlank())
          .orElse(null);
      this.clazzTypeSchemaLocation = Optional.ofNullable(clazzTypeSchemaLocation)
          .filter(location -> !location.isBlank())
          .orElse(null);
      this.pakkage = null;
      this.pakkageSchemaLocation = null;
    } else {
      throw new IllegalArgumentException(String.format(
          "Class '%s' is not annotated with XmlRootElement or XmlType nor it is a member a jaxb "
              + "context package.", clazz.getName()));
    }
  }

  /**
   * Instantiates a new jaxb context data.
   *
   * @param pakkage the package
   */
  public JaxbContextData(Package pakkage) {
    this(pakkage, null);
  }

  /**
   * Instantiates a new Jaxb context data.
   *
   * @param clazz the class
   * @param pakkageSchemaLocation the package schema location
   */
  public JaxbContextData(Class<?> clazz, String pakkageSchemaLocation) {
    Assert.notNull(clazz, "Class must be present.");
    if (JaxbUtils.isInJaxbModelPackage(clazz)) {
      this.pakkage = clazz.getPackage();
      this.pakkageSchemaLocation = Optional.ofNullable(pakkageSchemaLocation)
          .filter(location -> !location.isBlank())
          .or(() -> JaxbUtils.getSchemaLocation(this.pakkage))
          .orElse(null);
      this.clazz = null;
      this.clazzElementSchemaLocation = null;
      this.clazzTypeSchemaLocation = null;
    } else if (clazz.isAnnotationPresent(XmlRootElement.class)
        || clazz.isAnnotationPresent(XmlType.class)) {
      this.clazz = clazz;
      this.clazzElementSchemaLocation = null;
      this.clazzTypeSchemaLocation = null;
      this.pakkage = null;
      this.pakkageSchemaLocation = null;
    } else {
      throw new IllegalArgumentException(String.format(
          "Class '%s' is not annotated with XmlRootElement or XmlType nor it is a member a jaxb "
              + "context package.", clazz.getName()));
    }
  }

  /**
   * Instantiates a new jaxb context data.
   *
   * @param pakkage the package
   * @param pakkageSchemaLocation the package schema location
   */
  public JaxbContextData(Package pakkage, String pakkageSchemaLocation) {
    Assert.notNull(pakkage, "Package must be present.");
    Assert.isTrue(JaxbUtils.isJaxbModelPackage(pakkage), String.format(
        "Package '%s' does not contain 'ObjectFactory.class' or 'jaxb.index'.",
        pakkage.getName()));
    this.pakkage = pakkage;
    this.pakkageSchemaLocation = Optional.ofNullable(pakkageSchemaLocation)
        .filter(location -> !location.isBlank())
        .or(() -> JaxbUtils.getSchemaLocation(this.pakkage))
        .orElse(null);
    this.clazz = null;
    this.clazzElementSchemaLocation = null;
    this.clazzTypeSchemaLocation = null;
  }

  private Package getPackage() {
    return nonNull(pakkage) ? pakkage : requireNonNull(clazz).getPackage();
  }

  /**
   * Gets key.
   *
   * @return the key
   */
  Object getKey() {
    return nonNull(pakkage) ? pakkage : requireNonNull(clazz);
  }

  /**
   * Gets jaxb classes.
   *
   * @param classLoaders the class loaders
   * @return the jaxb classes
   */
  public Stream<Class<?>> getJaxbClasses(ClassLoader... classLoaders) {
    return JaxbUtils.findJaxbClasses(getPackage().getName(), classLoaders);
  }

  /**
   * Gets name spaces with schema locations.
   *
   * @return the name spaces with schema locations
   */
  public Stream<SchemaLocation> getNameSpacesWithSchemaLocations() {
    return Stream.of(
            Optional.ofNullable(pakkageSchemaLocation)
                .filter(location -> !location.isBlank())
                .flatMap(location -> JaxbUtils.getNameSpace(pakkage)
                    .map(ns -> new SchemaLocation(ns, location))),
            Optional.ofNullable(clazzElementSchemaLocation)
                .filter(location -> !location.isBlank())
                .flatMap(location -> JaxbUtils.getNameSpaceOfElement(clazz)
                    .map(ns -> new SchemaLocation(ns, location))),
            Optional.ofNullable(clazzTypeSchemaLocation)
                .filter(location -> !location.isBlank())
                .flatMap(location -> JaxbUtils.getNameSpaceOfType(clazz)
                    .map(ns -> new SchemaLocation(ns, location))))
        .flatMap(Optional::stream)
        .distinct();
  }

  @Override
  public String toString() {
    return "JaxbContextData{" +
        "clazz=" + (nonNull(clazz) ? clazz.getName() : "null") +
        ", clazzElementSchemaLocation='" + clazzElementSchemaLocation + '\'' +
        ", clazzTypeSchemaLocation='" + clazzTypeSchemaLocation + '\'' +
        ", pakkage=" + (nonNull(pakkage) ? pakkage.getName() : "null") +
        ", pakkageSchemaLocation='" + pakkageSchemaLocation + '\'' +
        '}';
  }

  @Override
  public int compareTo(JaxbContextData o) {
    String a = nonNull(pakkage) ? pakkage.getName() : requireNonNull(clazz).getName();
    String b = nonNull(o.pakkage) ? o.pakkage.getName() : requireNonNull(o.clazz).getName();
    return a.compareToIgnoreCase(b);
  }

}
