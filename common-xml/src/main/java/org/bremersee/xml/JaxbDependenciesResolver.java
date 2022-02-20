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

/**
 * The jaxb dependencies resolver is used to determine the classes to build the jaxb context.
 *
 * <p>This can be nice if you have to support plenty of xml name spaces which normally are all
 * added to the xml file. With a dependency resolver only these name spaces are added to the xml
 * file which are necessary.
 *
 * @author Christian Bremer
 */
public interface JaxbDependenciesResolver {

  /**
   * Resolve dependencies to other classes.
   *
   * @param value the value (POJO) that should be processed by the marshaller or unmarshaller or
   *     a single class or an array of classes
   * @return the resolved classes
   */
  Class<?>[] resolveClasses(Object value);

}
