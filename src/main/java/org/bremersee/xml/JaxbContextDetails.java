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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * The jaxb context builder details.
 *
 * @author Christian Bremer
 */
interface JaxbContextDetails {

  /**
   * Context data collector collector.
   *
   * @return the collector
   */
  static Collector<JaxbContextData, ?, JaxbContextDetailsImpl> contextDataCollector() {
    return Collector.of(JaxbContextDetailsImpl::new, JaxbContextDetailsImpl::add, (left, right) -> {
      if (left.size() < right.size()) {
        right.addAll(left);
        return right;
      } else {
        left.addAll(right);
        return left;
      }
    });
  }

  /**
   * Returns empty jaxb context details.
   *
   * @return the empty jaxb context details
   */
  static JaxbContextDetails empty() {
    return new JaxbContextDetailsImpl();
  }

  /**
   * Is empty boolean.
   *
   * @return the boolean
   */
  boolean isEmpty();

  /**
   * Get classes.
   *
   * @param classLoaders the class loaders
   * @return the classes
   */
  Class<?>[] getClasses(ClassLoader... classLoaders);

  /**
   * Gets schema location.
   *
   * @return the schema location
   */
  String getSchemaLocation();

  /**
   * Gets name spaces with schema locations.
   *
   * @return the name spaces with schema locations
   */
  default Set<String> getNameSpacesWithSchemaLocations() {
    return Optional.of(schemaLocationToArray(getSchemaLocation()))
        .map(parts -> {
          Set<String> locations = new LinkedHashSet<>();
          for (int i = 1; i < parts.length; i = i + 2) {
            locations.add(parts[i - 1] + " " + parts[i]);
          }
          return locations;
        })
        .orElseGet(Collections::emptySet);
  }

  /**
   * Gets the set of name spaces where the schema location is present.
   *
   * @return the name spaces
   */
  default Set<String> getNameSpaces() {
    return Optional.of(schemaLocationToArray(getSchemaLocation()))
        .map(parts -> {
          Set<String> nameSpaces = new LinkedHashSet<>();
          for (int i = 0; i < parts.length; i = i + 2) {
            nameSpaces.add(parts[i]);
          }
          return nameSpaces;
        })
        .orElseGet(Collections::emptySet);
  }

  /**
   * Gets the set of schema locations, normally as URL.
   * <pre>
   * http://example.org/model.xsd, http://example.org/another-model.xsd
   * </pre>
   *
   * @return the schema locations
   */
  default Set<String> getSchemaLocations() {
    return Optional.of(schemaLocationToArray(getSchemaLocation()))
        .map(parts -> {
          Set<String> locations = new LinkedHashSet<>();
          for (int i = 1; i < parts.length; i = i + 2) {
            locations.add(parts[i]);
          }
          return locations;
        })
        .orElse(Collections.emptySet());
  }

  private static String[] schemaLocationToArray(String schemaLocation) {
    return Optional.ofNullable(schemaLocation)
        .map(sl -> sl.replaceAll("^\\s*|\\s*$", ""))
        .map(sl -> sl.replaceAll("[\\s]{2,}", " "))
        .map(sl -> StringUtils.delimitedListToStringArray(sl, " "))
        .orElseGet(() -> new String[0]);
  }

  /**
   * The jaxb context details implementation.
   */
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  class JaxbContextDetailsImpl
      extends LinkedHashSet<JaxbContextData>
      implements JaxbContextDetails {

    @Override
    public String getSchemaLocation() {
      return this.stream()
          .flatMap(JaxbContextData::getNameSpacesWithSchemaLocations)
          .distinct()
          .sorted()
          .map(SchemaLocation::toString)
          .collect(Collectors.joining(" "));
    }

    @Override
    public Class<?>[] getClasses(ClassLoader... classLoaders) {
      return ClassUtils
          .toClassArray(this
              .stream()
              .flatMap(data -> data.getJaxbClasses(classLoaders))
              .collect(Collectors.toSet()));
    }

  }

}
