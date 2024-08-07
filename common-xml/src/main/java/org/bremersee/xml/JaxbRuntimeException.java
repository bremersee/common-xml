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

import org.springframework.util.ObjectUtils;

/**
 * The {@link JaxbContextBuilder} throws this exception instead of {@link
 * jakarta.xml.bind.JAXBException}.
 *
 * @author Christian Bremer
 */
public class JaxbRuntimeException extends RuntimeException {

  /**
   * Instantiates a new jaxb runtime exception.
   *
   * @param cause the cause
   */
  public JaxbRuntimeException(Throwable cause) {
    this("Creating JAXB context failed.", cause);
  }

  /**
   * Instantiates a new jaxb runtime exception.
   *
   * @param message the message
   */
  public JaxbRuntimeException(String message) {
    super(ObjectUtils.isEmpty(message) ? "Creating JAXB context failed." : message);
  }

  /**
   * Instantiates a new jaxb runtime exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public JaxbRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

}
