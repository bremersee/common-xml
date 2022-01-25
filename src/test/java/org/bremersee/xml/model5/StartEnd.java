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

package org.bremersee.xml.model5;

import java.time.Duration;
import java.time.OffsetDateTime;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bremersee.xml.adapter.DurationXmlAdapter;
import org.bremersee.xml.adapter.OffsetDateTimeXmlAdapter;

/**
 * The start end test model.
 *
 * @author Christian Bremer
 */
@XmlRootElement(name = "startEnd")
@XmlType(name = "startEndType", propOrder = {
    "start",
    "end",
    "duration"
})
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartEnd {

  @XmlJavaTypeAdapter(OffsetDateTimeXmlAdapter.class)
  private OffsetDateTime start;

  @XmlJavaTypeAdapter(OffsetDateTimeXmlAdapter.class)
  private OffsetDateTime end;

  @XmlJavaTypeAdapter(DurationXmlAdapter.class)
  private Duration duration;

}
