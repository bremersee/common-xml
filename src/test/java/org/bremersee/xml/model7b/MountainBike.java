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

package org.bremersee.xml.model7b;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.bremersee.xml.model7a.Bicycle;

/**
 * The mountain bike.
 *
 * @author Christian Bremer
 */
@XmlRootElement(name = "MountainBike")
@XmlType(name = "mountainBikeType")
@XmlAccessorType(XmlAccessType.FIELD)
public class MountainBike extends Bicycle {

  private Integer seatHeight;

  /**
   * Gets seat height.
   *
   * @return the seat height
   */
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
    MountainBike that = (MountainBike) o;
    return Objects.equals(seatHeight, that.seatHeight);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), seatHeight);
  }

}
