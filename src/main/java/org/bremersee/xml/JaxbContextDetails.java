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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * The jaxb context builder details.
 *
 * @author Christian Bremer
 */
interface JaxbContextDetails extends Serializable {

  /**
   * Returns a jaxb context details builder.
   *
   * @return the jaxb context details builder
   */
  static JaxbContextDetailsBuilder builder() {
    return new JaxbContextDetailsBuilderImpl();
  }

  /**
   * Returns empty jaxb context details.
   *
   * @return the empty jaxb context details
   */
  static JaxbContextDetails empty() {
    return builder().build();
  }

  /**
   * Determines whether these details are empty.
   *
   * @return the boolean
   */
  default boolean isEmpty() {
    return ObjectUtils.isEmpty(getClasses()) && ObjectUtils.isEmpty(getContextPath());
  }

  /**
   * Get classes.
   *
   * @return the classes
   */
  Class<?>[] getClasses();

  /**
   * Gets context path (package names separated by colon).
   *
   * @return the context path
   */
  String getContextPath();

  /**
   * Gets schema location.
   *
   * @return the schema location
   */
  String getSchemaLocation();

  /**
   * Gets package names.
   *
   * @return the package names
   */
  default Set<String> getPackageNames() {
    return Optional.of(contextPathToArray(getContextPath()))
        .map(Arrays::asList)
        .stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

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

  private static String[] contextPathToArray(String contextPath) {
    return Optional.ofNullable(contextPath)
        .map(sl -> sl.replaceAll("[\\s]", ""))
        .map(sl -> sl.replaceAll("^:*|:*$", ""))
        .map(sp -> sp.replaceAll("[:]{2,}", ":"))
        .map(cp -> StringUtils.delimitedListToStringArray(cp, ":"))
        .orElseGet(() -> new String[0]);
  }

  private static String[] schemaLocationToArray(String schemaLocation) {
    return Optional.ofNullable(schemaLocation)
        .map(sl -> sl.replaceAll("^\\s*|\\s*$", ""))
        .map(sl -> sl.replaceAll("[\\s]{2,}", " "))
        .map(sl -> StringUtils.delimitedListToStringArray(sl, " "))
        .orElseGet(() -> new String[0]);
  }

  /**
   * The jaxb context details builder.
   */
  interface JaxbContextDetailsBuilder {

    /**
     * Merge jaxb context details.
     *
     * @param details the details
     * @return the jaxb context details builder
     */
    default JaxbContextDetailsBuilder merge(JaxbContextDetails details) {
      return Optional.ofNullable(details)
          .map(d -> add(d.getClasses())
              .addSchemaLocation(d.getSchemaLocation())
              .addContextPath(d.getContextPath()))
          .orElse(this);
    }

    /**
     * Add jaxb context details.
     *
     * @param data the data
     * @return the jaxb context details builder
     */
    JaxbContextDetailsBuilder add(JaxbContextData data);

    /**
     * Add jaxb context details,
     *
     * @param classes the classes
     * @return the jaxb context details builder
     */
    JaxbContextDetailsBuilder add(Class<?>... classes);

    /**
     * Add context path.
     *
     * @param contextPath the context path
     * @return the jaxb context details builder
     */
    JaxbContextDetailsBuilder addContextPath(String contextPath);

    /**
     * Add schema location.
     *
     * @param schemaLocation the schema location
     * @return the jaxb context details builder
     */
    JaxbContextDetailsBuilder addSchemaLocation(String schemaLocation);

    /**
     * Build jaxb context details.
     *
     * @return the jaxb context details
     */
    JaxbContextDetails build();
  }

  /**
   * The jaxb context details builder implementation.
   */
  @EqualsAndHashCode
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  class JaxbContextDetailsBuilderImpl implements JaxbContextDetailsBuilder {

    private final Set<String> contextPaths = new TreeSet<>();

    private final Set<String> schemaLocations = new TreeSet<>();

    private final Set<Class<?>> classes = new HashSet<>();

    @Override
    public JaxbContextDetailsBuilder add(JaxbContextData data) {
      return Optional.ofNullable(data)
          .map(d -> addContextPath(d.getPackageName())
              .addSchemaLocation(d.getNameSpaceWithSchemaLocation().orElse("")))
          .orElse(this);
    }

    public JaxbContextDetailsBuilder add(Class<?>... classes) {
      return Optional.ofNullable(classes)
          .map(c -> {
            this.classes.addAll(Arrays.asList(c));
            return this;
          })
          .orElse(this);
    }

    public JaxbContextDetailsBuilder addContextPath(String contextPath) {
      return Optional.ofNullable(contextPathToArray(contextPath))
          .map(cp -> {
            contextPaths.addAll(Arrays.asList(cp));
            return this;
          })
          .orElse(this);
    }

    public JaxbContextDetailsBuilder addSchemaLocation(String schemaLocation) {
      return Optional.of(schemaLocationToArray(schemaLocation))
          .map(parts -> {
            for (int i = 1; i < parts.length; i = i + 2) {
              schemaLocations.add(parts[i - 1] + " " + parts[i]);
            }
            return this;
          })
          .orElse(this);
    }

    @Override
    public JaxbContextDetails build() {
      return new JaxbContextDetailsImpl(
          classes.toArray(new Class[0]),
          String.join(":", contextPaths),
          String.join(" ", schemaLocations));
    }

    @Override
    public String toString() {
      return "JaxbContextDetailsBuilder {"
          + "classes=" + classes.stream().map(Class::getName).collect(Collectors.joining(" "))
          + ", contextPath='" + contextPaths + '\''
          + ", schemaLocation='" + schemaLocations + '\''
          + '}';
    }
  }

  /**
   * The jaxb context details implementation.
   */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  @EqualsAndHashCode
  @Getter
  class JaxbContextDetailsImpl implements JaxbContextDetails {

    private static final long serialVersionUID = 1L;

    private final Class<?>[] classes;

    private final String contextPath;

    private final String schemaLocation;

    @Override
    public String toString() {
      return "JaxbContextDetails {"
          + "classes=" + (ObjectUtils.isEmpty(classes)
          ? "null"
          : Arrays.stream(classes).map(Class::getName).collect(Collectors.joining(" ")))
          + ", contextPath='" + contextPath + '\''
          + ", schemaLocation='" + schemaLocation + '\''
          + '}';
    }
  }

}
