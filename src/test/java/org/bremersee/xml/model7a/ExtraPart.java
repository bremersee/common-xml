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

package org.bremersee.xml.model7a;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * The extra part.
 *
 * @author Christian Bremer
 */
@XmlType(name = "extraPartType")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ExtraPart {

  private String partNumber;

  /**
   * Gets part number.
   *
   * @return the part number
   */
  public String getPartNumber() {
    return partNumber;
  }

  /**
   * Sets part number.
   *
   * @param partNumber the part number
   */
  public void setPartNumber(String partNumber) {
    this.partNumber = partNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExtraPart extraPart = (ExtraPart) o;
    return Objects.equals(partNumber, extraPart.partNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(partNumber);
  }
}
