package org.bremersee.xml.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Month;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;

/**
 * The offset date time xml adapter test.
 */
class OffsetDateTimeXmlAdapterTest {

  /**
   * Convert.
   *
   * @throws Exception the exception
   */
  @Test
  void convert() throws Exception {
    OffsetDateTimeXmlAdapter adapter = new OffsetDateTimeXmlAdapter();

    assertNull(adapter.marshal(null));
    assertNull(adapter.unmarshal(null));

    String expected = "2000-01-16T13:00:00.000+01:00";
    OffsetDateTime date = adapter.unmarshal(expected);
    assertNotNull(date);
    assertEquals(2000, date.getYear());
    assertEquals(Month.JANUARY, date.getMonth());
    assertEquals(16, date.getDayOfMonth());
    assertEquals(13, date.getHour());
    assertEquals(0, date.getMinute());
    assertEquals("+01:00", date.getOffset().getId());

    String actual = adapter.marshal(date);
    assertEquals(expected, actual);
  }
}