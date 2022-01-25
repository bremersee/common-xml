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

import java.util.Collection;

/**
 * The provider describes a collection of a java xml model and should be loadable by the {@link
 * java.util.ServiceLoader}.
 *
 * @author Christian Bremer
 */
public interface JaxbContextDataProvider {

  /**
   * Get a collection of the xml meta-data.
   *
   * @return a collection of the xml meta-data
   */
  Collection<JaxbContextData> getJaxbContextData();

}
