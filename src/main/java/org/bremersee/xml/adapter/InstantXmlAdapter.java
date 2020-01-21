/*
 * Copyright 2020 the original author or authors.
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

import static org.bremersee.xml.ConverterUtils.instantToXmlCalendar;
import static org.bremersee.xml.ConverterUtils.xmlCalendarToInstant;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author Christian Bremer
 */
public class InstantXmlAdapter extends XmlAdapter<String, Instant> {

  private ZoneId zoneId = ZoneOffset.UTC;

  private Locale locale;

  public InstantXmlAdapter() {
  }

  public InstantXmlAdapter(ZoneId zoneId, Locale locale) {
    if (zoneId != null) {
      this.zoneId = zoneId;
    }
    this.locale = locale;
  }

  @Override
  public Instant unmarshal(String v) throws Exception {
    return v == null
        ? null
        : xmlCalendarToInstant(DatatypeFactory.newInstance().newXMLGregorianCalendar(v));
  }

  @Override
  public String marshal(Instant v) throws Exception {
    return Optional
        .ofNullable(instantToXmlCalendar(v, zoneId, locale))
        .map(XMLGregorianCalendar::toXMLFormat)
        .orElse(null);
  }
}
