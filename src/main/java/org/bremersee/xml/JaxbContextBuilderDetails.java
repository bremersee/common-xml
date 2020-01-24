/*
 * Copyright 2018-2020 the original author or authors.
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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.util.StringUtils;

/**
 * @author Christian Bremer
 */
public interface JaxbContextBuilderDetails extends Serializable {

  boolean isBuildWithContextPath();

  Class<?>[] getClasses();

  String getContextPath();

  String getSchemaLocation();

  /**
   * Gets the set of name spaces where the schema location is present.
   *
   * @return the name spaces
   */
  default Set<String> getNameSpacesWithLocation() {
    return Optional.ofNullable(getSchemaLocation())
        .map(schemaLocation -> {
          String[] parts = StringUtils.delimitedListToStringArray(schemaLocation, " ");
          Set<String> locations = new LinkedHashSet<>();
          for (int i = 0; i < parts.length; i = i + 2) {
            locations.add(parts[i]);
          }
          return locations;
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
    final String[] parts = StringUtils.delimitedListToStringArray(getSchemaLocation(), " ");
    if (parts.length == 0) {
      return Collections.emptySet();
    }
    final Set<String> locations = new LinkedHashSet<>();
    for (int i = 1; i < parts.length; i = i + 2) {
      locations.add(parts[i]);
    }
    return locations;
  }

}
