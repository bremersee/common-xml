/*
 * Copyright 2020-2022 the original author or authors.
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
import jakarta.xml.bind.annotation.XmlSchema;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;

/**
 * The jaxb utils.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("SameNameButDifferent")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
abstract class JaxbUtils {

  /**
   * Gets name space of element.
   *
   * @param clazz the class
   * @return the name space of element
   */
  static Optional<String> getNameSpaceOfElement(Class<?> clazz) {
    return Optional.ofNullable(clazz)
        .map(c -> c.getAnnotation(XmlRootElement.class))
        .map(XmlRootElement::namespace)
        .filter(ns -> !ns.isBlank() && !"##default".equals(ns))
        .or(() -> isInJaxbModelPackage(clazz)
            ? getNameSpace(requireNonNull(clazz).getPackage())
            : Optional.empty());
  }

  /**
   * Gets name space of type.
   *
   * @param clazz the class
   * @return the name space of type
   */
  static Optional<String> getNameSpaceOfType(Class<?> clazz) {
    return Optional.ofNullable(clazz)
        .map(c -> c.getAnnotation(XmlType.class))
        .map(XmlType::namespace)
        .filter(ns -> !ns.isBlank() && !"##default".equals(ns))
        .or(() -> isInJaxbModelPackage(clazz)
            ? getNameSpace(requireNonNull(clazz).getPackage())
            : Optional.empty());
  }

  /**
   * Gets name space.
   *
   * @param pakkage the package
   * @return the name space
   */
  static Optional<String> getNameSpace(Package pakkage) {
    return Optional.ofNullable(pakkage)
        .map(p -> p.getAnnotation(XmlSchema.class))
        .map(XmlSchema::namespace)
        .filter(ns -> !ns.isBlank());
  }

  /**
   * Gets schema location.
   *
   * @param pakkage the package
   * @return the schema location
   */
  static Optional<String> getSchemaLocation(Package pakkage) {
    return Optional.ofNullable(pakkage)
        .map(p -> p.getAnnotation(XmlSchema.class))
        .map(XmlSchema::location)
        .filter(location -> !location.isBlank() && !XmlSchema.NO_LOCATION.equals(location));
  }

  /**
   * Is in jaxb model package boolean.
   *
   * @param clazz the class
   * @return the boolean
   */
  static boolean isInJaxbModelPackage(Class<?> clazz) {
    return nonNull(clazz) && isJaxbModelPackage(clazz.getPackage());
  }

  /**
   * Is jaxb model package boolean.
   *
   * @param pakkage the package
   * @return the boolean
   */
  static boolean isJaxbModelPackage(Package pakkage) {
    return objectFactoryExists(pakkage) || jaxbIndexExists(pakkage);
  }

  private static boolean objectFactoryExists(Package pakkage) {
    return Optional.ofNullable(pakkage)
        .map(p -> {
          try {
            Class.forName(pakkage.getName() + ".ObjectFactory");
            return true;
          } catch (ClassNotFoundException e) {
            return false;
          }
        })
        .orElse(false);
  }

  private static boolean jaxbIndexExists(Package pakkage) {
    return Optional.ofNullable(pakkage)
        .map(Package::getName)
        .map(p -> p.replaceAll("[.]", "/"))
        .map(path -> path + "/jaxb.index")
        .map(resource -> new ClassPathResource(resource).exists())
        .orElse(false);
  }

  /**
   * Find jaxb classes stream.
   *
   * @param packageName the package name
   * @param classLoaders the class loaders
   * @return the stream
   */
  static Stream<Class<?>> findJaxbClasses(String packageName, ClassLoader... classLoaders) {
    Assert.hasText(packageName, "Package name must be present.");
    return new Reflections(packageName)
        .get(Scanners.SubTypes.of(Scanners.TypesAnnotated
            .with(XmlRootElement.class, XmlType.class)).asClass(classLoaders))
        .stream();
  }

}
