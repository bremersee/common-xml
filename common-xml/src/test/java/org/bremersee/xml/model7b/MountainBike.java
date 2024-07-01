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

package org.bremersee.xml.model7b;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bremersee.xml.model7a.Bicycle;

/**
 * The mountain bike.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("DefaultAnnotationParam")
@XmlRootElement(name = "MountainBike")
@XmlType(name = "mountainBikeType")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MountainBike extends Bicycle {

  private Integer seatHeight;

  /**
   * Gets seat height.
   *
   * @return the seat height
   */
  @XmlElement
  @SuppressWarnings("unused")
  public Integer getSeatHeight() {
    return seatHeight;
  }

  /**
   * Sets seat height.
   *
   * @param seatHeight the seat height
   */
  public void setSeatHeight(Integer seatHeight) {
    this.seatHeight = seatHeight;
  }
}
