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

package org.bremersee.xml.model7b;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.bremersee.xml.model7a.Bicycle;

/**
 * @author Christian Bremer
 */
@XmlRootElement(name = "RacingBike")
@XmlType(name = "racingBikeType")
@XmlAccessorType(XmlAccessType.FIELD)
public class RacingBike extends Bicycle {

  private Integer frameSize;

  public Integer getFrameSize() {
    return frameSize;
  }

  public void setFrameSize(Integer frameSize) {
    this.frameSize = frameSize;
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
    RacingBike that = (RacingBike) o;
    return Objects.equals(frameSize, that.frameSize);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), frameSize);
  }
}
