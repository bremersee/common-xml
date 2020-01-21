package org.bremersee.xml.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;

/**
 * The duration xml adapter test.
 *
 * @author Christian Bremer
 */
class DurationXmlAdapterTest {

  /**
   * Convert.
   *
   * @throws Exception the exception
   */
  @Test
  void convert() throws Exception {
    DurationXmlAdapter adapter = new DurationXmlAdapter();
    String expected = "P5Y2M10DT15H0M0.000S";
    Duration duration = adapter.unmarshal(expected);
    assertNotNull(duration);
    String actual = adapter.marshal(duration);
    assertEquals(expected, actual);
  }
}