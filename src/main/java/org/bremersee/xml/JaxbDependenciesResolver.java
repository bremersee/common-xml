/*
 * Copyright 2020 the original author or authors.
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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The jaxb dependencies resolver.
 *
 * @author Christian Bremer
 */
public interface JaxbDependenciesResolver {

  /**
   * Resolve dependencies to other packages to add their name space to the schema location tag of
   * the xml file.
   *
   * @param value the value that should be processed by the marshaller
   * @return the set with package names
   */
  default Set<String> resolvePackages(Object value) {
    return Arrays.stream(resolveClasses(value))
        .map(clazz -> clazz.getPackage().getName())
        .collect(Collectors.toSet());
  }

  /**
   * Resolve dependencies to other classes.
   *
   * @param value the value that should be processed by the marshaller
   * @return the resolved classes
   */
  Class<?>[] resolveClasses(Object value);

}
