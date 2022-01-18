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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The jaxb context builder details implementation.
 *
 * @author Christian Bremer
 */
class JaxbContextBuilderDetailsImpl implements JaxbContextBuilderDetails {

  private static final long serialVersionUID = 1L;

  private Class<?>[] classes;

  private String contextPath;

  private String schemaLocation;

  /**
   * Instantiates a new jaxb context builder details.
   *
   * @param classes the classes
   */
  JaxbContextBuilderDetailsImpl(final Class<?>... classes) {
    if (classes != null) {
      this.classes = Arrays.stream(classes)
          .sorted(Comparator.comparing(Class::getName))
          .toArray(Class<?>[]::new);
    }
  }

  /**
   * Instantiates a new jaxb context builder details.
   *
   * @param packages the packages
   * @param jaxbContextDataMap the jaxb context data map
   */
  JaxbContextBuilderDetailsImpl(
      final Set<String> packages,
      final Map<String, JaxbContextData> jaxbContextDataMap) {

    Assert.notEmpty(jaxbContextDataMap, "Jaxb context data map must be present.");
    final Set<String> packageNames = packages == null || packages.isEmpty()
        ? jaxbContextDataMap.keySet()
        : packages;
    final List<String> contextPathList = new ArrayList<>();
    final List<String> schemaLocationList = new ArrayList<>();
    jaxbContextDataMap.values().stream()
        .filter(data -> packageNames.contains(data.getPackageName()))
        .forEach(data -> {
          contextPathList.add(data.getPackageName());
          if (data.getNameSpace().length() > 0 && StringUtils.hasText(data.getSchemaLocation())) {
            schemaLocationList.add(data.getNameSpace() + " " + data.getSchemaLocation());
          }
        });
    contextPathList.sort(String::compareToIgnoreCase);
    schemaLocationList.sort(String::compareToIgnoreCase);
    this.contextPath = String.join(":", contextPathList);
    this.schemaLocation = String.join(" ", schemaLocationList);
  }

  @Override
  public boolean isBuildWithContextPath() {
    return contextPath != null;
  }

  @Override
  public Class<?>[] getClasses() {
    return classes;
  }

  @Override
  public String getContextPath() {
    return contextPath;
  }

  @Override
  public String getSchemaLocation() {
    if (StringUtils.hasText(schemaLocation)) {
      return schemaLocation;
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JaxbContextBuilderDetailsImpl that = (JaxbContextBuilderDetailsImpl) o;
    return Arrays.equals(classes, that.classes)
        && Objects.equals(contextPath, that.contextPath)
        && Objects.equals(schemaLocation, that.schemaLocation);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(contextPath, schemaLocation);
    result = 31 * result + Arrays.hashCode(classes);
    return result;
  }

  @Override
  public String toString() {
    return "JaxbContextBuilderDetails {"
        + "classes=" + (classes == null
        ? "null"
        : Arrays.stream(classes).map(Class::getName).collect(Collectors.joining(" ")))
        + ", contextPath='" + contextPath + '\''
        + ", schemaLocation='" + schemaLocation + '\''
        + '}';
  }

}
