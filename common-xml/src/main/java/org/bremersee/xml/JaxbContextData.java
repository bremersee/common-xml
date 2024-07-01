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
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.util.Assert;

/**
 * Meta-data to describe a xml model.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("SameNameButDifferent")
@EqualsAndHashCode
public final class JaxbContextData implements JaxbContextMember, Comparable<JaxbContextData> {

  @Getter
  private final Class<?> clazz;

  @Getter
  private final String clazzElementSchemaLocation;

  @Getter
  private final String clazzTypeSchemaLocation;

  private final Package pakkage;

  @Getter
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

    this(validate(JaxbContextMember.byClass(clazz)
        .clazzElementSchemaLocation(clazzElementSchemaLocation)
        .clazzTypeSchemaLocation(clazzTypeSchemaLocation)
        .build()));
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
    this(validate(new JaxbContextMemberImpl(
        clazz,
        null,
        null,
        null,
        pakkageSchemaLocation)));
  }

  /**
   * Instantiates a new jaxb context data.
   *
   * @param pakkage the package
   * @param pakkageSchemaLocation the package schema location
   */
  public JaxbContextData(Package pakkage, String pakkageSchemaLocation) {
    this(validate(JaxbContextMember.byPackage(pakkage)
        .schemaLocation(pakkageSchemaLocation)
        .build()));
  }

  JaxbContextData(JaxbContextMember jaxbContextMember) {
    JaxbContextMember validated = validate(jaxbContextMember);
    this.pakkage = validated.getPakkage();
    this.pakkageSchemaLocation = validated.getPakkageSchemaLocation();
    this.clazz = validated.getClazz();
    this.clazzElementSchemaLocation = validated.getClazzElementSchemaLocation();
    this.clazzTypeSchemaLocation = validated.getClazzTypeSchemaLocation();
  }

  private static JaxbContextMember validate(JaxbContextMember source) {
    Assert.notNull(source, "Jaxb context member must be present.");
    if (nonNull(source.getClazz()) && nonNull(source.getPakkage())) {
      Assert.isTrue(source.getPakkage().equals(source.getClazz().getPackage()),
          "If package and class are present, class must be a member of the package.");
      if (JaxbUtils.isJaxbModelPackage(source.getPakkage())) {
        return validate(JaxbContextMember.byPackage(source.getPakkage())
            .schemaLocation(source.getPakkageSchemaLocation())
            .build());
      }
      return validate(JaxbContextMember.byClass(source.getClazz())
          .clazzElementSchemaLocation(source.getClazzElementSchemaLocation())
          .clazzTypeSchemaLocation(source.getClazzTypeSchemaLocation())
          .build());
    }
    if (nonNull(source.getClazz()) && JaxbUtils.isInJaxbModelPackage(source.getClazz())) {
      return validate(JaxbContextMember.byPackage(source.getClazz().getPackage())
          .schemaLocation(source.getPakkageSchemaLocation())
          .build());
    }
    if (nonNull(source.getClazz())
        && (source.getClazz().isAnnotationPresent(XmlRootElement.class)
        || source.getClazz().isAnnotationPresent(XmlType.class))) {
      return source;
    } else if (nonNull(source.getClazz())) {
      throw new IllegalArgumentException(
          String.format(
              "Class '%s' must be annotated with 'XmlRootElement' or 'XmlType'.",
              source.getClazz().getName()));
    }
    if (nonNull(source.getPakkage()) && JaxbUtils.isJaxbModelPackage(source.getPakkage())) {
      return JaxbContextMember.byPackage(source.getPakkage())
          .schemaLocation(Optional.ofNullable(source.getPakkageSchemaLocation())
              .filter(location -> !location.isBlank())
              .or(() -> JaxbUtils.getSchemaLocation(source.getPakkage()))
              .orElse(null))
          .build();
    } else if (nonNull(source.getPakkage())) {
      throw new IllegalArgumentException(
          String.format(
              "Package '%s' does not contain 'ObjectFactory.class' or 'jaxb.index'.",
              source.getPakkage().getName()));
    }
    throw new IllegalArgumentException("Class or package must be present.");
  }

  public Package getPakkage() {
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
    return JaxbUtils.findJaxbClasses(getPakkage().getName(), classLoaders);
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
    return "JaxbContextData{"
        + "clazz=" + (nonNull(clazz) ? clazz.getName() : "null")
        + ", clazzElementSchemaLocation='" + clazzElementSchemaLocation + '\''
        + ", clazzTypeSchemaLocation='" + clazzTypeSchemaLocation + '\''
        + ", pakkage=" + (nonNull(pakkage) ? pakkage.getName() : "null")
        + ", pakkageSchemaLocation='" + pakkageSchemaLocation + '\''
        + '}';
  }

  @Override
  public int compareTo(JaxbContextData o) {
    String a = nonNull(pakkage) ? pakkage.getName() : requireNonNull(clazz).getName();
    String b = nonNull(o.pakkage) ? o.pakkage.getName() : requireNonNull(o.clazz).getName();
    return a.compareToIgnoreCase(b);
  }

}
