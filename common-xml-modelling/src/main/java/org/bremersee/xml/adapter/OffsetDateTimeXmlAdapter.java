/*
 * Copyright 2020-2022 the original author or authors.
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

import static org.bremersee.xml.ConverterUtils.offsetDateTimeToXmlCalendar;
import static org.bremersee.xml.ConverterUtils.xmlCalendarToOffsetDateTime;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.OffsetDateTime;
import java.util.Optional;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * The offset date time xml adapter.
 *
 * @author Christian Bremer
 */
public class OffsetDateTimeXmlAdapter extends XmlAdapter<String, OffsetDateTime> {

  @Override
  public OffsetDateTime unmarshal(String v) throws Exception {
    return v == null
        ? null
        : xmlCalendarToOffsetDateTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(v));
  }

  @Override
  public String marshal(OffsetDateTime v) {
    return Optional
        .ofNullable(offsetDateTimeToXmlCalendar(v))
        .map(XMLGregorianCalendar::toXMLFormat)
        .orElse(null);
  }
}
