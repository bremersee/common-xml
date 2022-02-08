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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bremersee.xml.model7a.Producer;

/**
 * The sport bikes.
 *
 * @author Christian Bremer
 */
@XmlRootElement(name = "SportBikes")
@XmlType(name = "sportBikesType")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SportBikes extends Producer {

  @XmlTransient
  private List<Producer> chain;

  /**
   * Gets chain.
   *
   * @return the chain
   */
  @XmlElement(name = "Reseller")
  public List<Producer> getChain() {
    if (chain == null) {
      chain = new ArrayList<>();
    }
    return chain;
  }

}
