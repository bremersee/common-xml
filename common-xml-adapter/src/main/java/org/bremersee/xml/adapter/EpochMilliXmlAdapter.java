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

import static org.bremersee.xml.ConverterUtils.millisToXmlCalendar;
import static org.bremersee.xml.ConverterUtils.xmlCalendarToMillis;

import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * The epoch milli xml adapter.
 *
 * @author Christian Bremer
 */
public class EpochMilliXmlAdapter extends XmlAdapter<String, Long> {

  private TimeZone timeZone = TimeZone.getTimeZone(ZoneOffset.UTC);

  private Locale locale;

  /**
   * Instantiates a new epoch milli xml adapter.
   */
  public EpochMilliXmlAdapter() {
  }

  /**
   * Instantiates a new epoch milli xml adapter.
   *
   * @param timeZone the time zone
   * @param locale the locale
   */
  public EpochMilliXmlAdapter(TimeZone timeZone, Locale locale) {
    if (timeZone != null) {
      this.timeZone = timeZone;
    }
    this.locale = locale;
  }

  @Override
  public Long unmarshal(String v) throws Exception {
    return v == null
        ? null
        : xmlCalendarToMillis(DatatypeFactory.newInstance().newXMLGregorianCalendar(v));
  }

  @Override
  public String marshal(Long v) {
    return Optional
        .ofNullable(millisToXmlCalendar(v, timeZone, locale))
        .map(XMLGregorianCalendar::toXMLFormat)
        .orElse(null);
  }
}
