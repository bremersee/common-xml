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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;

/**
 * @author Christian Bremer
 */
@XmlType(name = "bicycleType")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Bicycle {

  private Producer producer;

  private Integer gear;

  private String color;

  @XmlElementWrapper(name = "extraParts")
  @XmlAnyElement
  private List<Element> extraParts;

  public Producer getProducer() {
    return producer;
  }

  public void setProducer(Producer producer) {
    this.producer = producer;
  }

  public Integer getGear() {
    return gear;
  }

  public void setGear(Integer gear) {
    this.gear = gear;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public List<Element> getExtraParts() {
    if (extraParts == null) {
      extraParts = new ArrayList<>();
    }
    return extraParts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Bicycle bicycle = (Bicycle) o;
    return Objects.equals(producer, bicycle.producer) &&
        Objects.equals(gear, bicycle.gear) &&
        Objects.equals(color, bicycle.color) &&
        Objects.equals(extraParts, bicycle.extraParts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(producer, gear, color, extraParts);
  }
}
