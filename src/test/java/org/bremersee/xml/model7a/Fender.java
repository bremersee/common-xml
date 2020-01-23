/*
 * Copyright 2019 the original author or authors.
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

package org.bremersee.xml.model7a;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Christian Bremer
 */
@XmlRootElement(name = "Fender")
@XmlType(name = "fenderType")
@XmlAccessorType(XmlAccessType.FIELD)
public class Fender extends ExtraPart {

  private Boolean front;

  public Boolean getFront() {
    return front;
  }

  public void setFront(Boolean front) {
    this.front = front;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Fender fender = (Fender) o;
    return Objects.equals(front, fender.front);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), front);
  }
}
