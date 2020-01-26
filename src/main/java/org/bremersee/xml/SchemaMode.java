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

/**
 * The schema mode.
 *
 * @author Christian Bremer
 */
public enum SchemaMode {

  /**
   * Never add a schema to the jaxb marshaller or unmarshaller.
   */
  NEVER,

  /**
   * Always add schema to the jaxb marshaller or unmarshaller.
   */
  ALWAYS,

  /**
   * Add always a schema to the jaxb marshaller, but not to the unmarshaller.
   */
  MARSHAL,

  /**
   * Add always a schema to the jaxb unmarshaller, but not to the marshaller.
   */
  UNMARSHAL,

  /**
   * Add only a schema to the marshaller or unmarshaller if an external schema specification is
   * used.
   */
  EXTERNAL_XSD

}
