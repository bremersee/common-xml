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

package org.bremersee.xml.adapter;

import static org.bremersee.xml.ConverterUtils.durationToXmlDuration;
import static org.bremersee.xml.ConverterUtils.xmlDurationToDuration;

import java.time.Duration;
import java.util.Optional;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeFactory;

/**
 * The duration xml adapter.
 *
 * @author Christian Bremer
 */
public class DurationXmlAdapter extends XmlAdapter<String, Duration> {

  @Override
  public Duration unmarshal(String v) throws Exception {
    return v == null
        ? null
        : xmlDurationToDuration(DatatypeFactory.newInstance().newDuration(v));
  }

  @Override
  public String marshal(Duration v) {
    return Optional.ofNullable(durationToXmlDuration(v))
        .map(javax.xml.datatype.Duration::toString)
        .orElse(null);
  }
}
