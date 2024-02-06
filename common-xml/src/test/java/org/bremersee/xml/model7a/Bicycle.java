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

package org.bremersee.xml.model7a;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.w3c.dom.Element;

/**
 * The bicycle.
 *
 * @author Christian Bremer
 */
@XmlType(name = "bicycleType")
@XmlAccessorType(XmlAccessType.FIELD)
@ToString
@EqualsAndHashCode(exclude = {"extraParts"})
@NoArgsConstructor
public abstract class Bicycle {

  @Getter
  @Setter
  private Producer producer;

  @Getter
  @Setter
  private Integer gear;

  @Getter
  @Setter
  private String color;

  @XmlElementWrapper(name = "extraParts")
  @XmlAnyElement
  private List<Element> extraParts;

  /**
   * Gets extra parts.
   *
   * @return the extra parts
   */
  public List<Element> getExtraParts() {
    if (extraParts == null) {
      extraParts = new ArrayList<>();
    }
    return extraParts;
  }

}
