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

import static org.bremersee.xml.ConverterUtils.dateToXmlCalendar;
import static org.bremersee.xml.ConverterUtils.xmlCalendarToDate;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * The date xml adapter.
 *
 * @author Christian Bremer
 */
public class DateXmlAdapter extends XmlAdapter<String, Date> {

  private TimeZone timeZone = TimeZone.getTimeZone(ZoneOffset.UTC);

  private Locale locale;

  /**
   * Instantiates a new date xml adapter.
   */
  public DateXmlAdapter() {
    super();
  }

  /**
   * Instantiates a new date xml adapter.
   *
   * @param timeZone the time zone
   * @param locale the locale
   */
  public DateXmlAdapter(TimeZone timeZone, Locale locale) {
    super();
    if (timeZone != null) {
      this.timeZone = timeZone;
    }
    this.locale = locale;
  }

  @Override
  public Date unmarshal(String v) throws Exception {
    return v == null
        ? null
        : xmlCalendarToDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(v));
  }

  @Override
  public String marshal(Date v) {
    return Optional
        .ofNullable(dateToXmlCalendar(v, timeZone, locale))
        .map(XMLGregorianCalendar::toXMLFormat)
        .orElse(null);
  }

}
