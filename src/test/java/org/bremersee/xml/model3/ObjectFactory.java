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

package org.bremersee.xml.model3;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * The xml object factory.
 *
 * @author Christian Bremer
 */
@XmlRegistry
@SuppressWarnings("unused")
public class ObjectFactory {

  /**
   * Create company.
   *
   * @return the company
   */
  public Company createCompany() {
    return new Company();
  }

}
