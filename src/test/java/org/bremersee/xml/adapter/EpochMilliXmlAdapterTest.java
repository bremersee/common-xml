package org.bremersee.xml.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Locale;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;

/**
 * The epoch milli xml adapter test.
 */
class EpochMilliXmlAdapterTest {

  /**
   * Convert.
   *
   * @throws Exception the exception
   */
  @Test
  void convert() throws Exception {
    EpochMilliXmlAdapter adapter = new EpochMilliXmlAdapter();

    assertNull(adapter.marshal(null));
    assertNull(adapter.unmarshal(null));

    String expected = "2000-01-16T12:00:00.000Z";
    Long date = adapter.unmarshal("2000-01-16T12:00:00Z");
    assertNotNull(date);
    String actual = adapter.marshal(date);
    assertEquals(expected, actual);

    adapter = new EpochMilliXmlAdapter(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY);
    actual = adapter.marshal(date);
    assertEquals("2000-01-16T13:00:00.000+01:00", actual);
  }
}