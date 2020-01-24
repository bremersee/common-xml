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

package org.bremersee.xml.model7c;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.bremersee.xml.model7a.Producer;

/**
 * The bike schmied.
 *
 * @author Christian Bremer
 */
@XmlRootElement(name = "BikeSchmied")
@XmlType(name = "bikeSchmiedType")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("unused")
public class BikeSchmied extends Producer {

  private String address;

  /**
   * Gets address.
   *
   * @return the address
   */
  public String getAddress() {
    return address;
  }

  /**
   * Sets address.
   *
   * @param address the address
   */
  public void setAddress(String address) {
    this.address = address;
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
    BikeSchmied that = (BikeSchmied) o;
    return Objects.equals(address, that.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), address);
  }
}
