/*
 * Copyright 2019 the original author or authors.
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
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
@SuppressWarnings("unused")
public class StartEnd {

  @XmlJavaTypeAdapter(OffsetDateTimeXmlAdapter.class)
  private OffsetDateTime start;

  @XmlJavaTypeAdapter(OffsetDateTimeXmlAdapter.class)
  private OffsetDateTime end;

  @XmlJavaTypeAdapter(DurationXmlAdapter.class)
  private Duration duration;

  /**
   * Gets start.
   *
   * @return the start
   */
  public OffsetDateTime getStart() {
    return start;
  }

  /**
   * Sets start.
   *
   * @param start the start
   */
  public void setStart(OffsetDateTime start) {
    this.start = start;
  }

  /**
   * Gets end.
   *
   * @return the end
   */
  public OffsetDateTime getEnd() {
    return end;
  }

  /**
   * Sets end.
   *
   * @param end the end
   */
  public void setEnd(OffsetDateTime end) {
    this.end = end;
  }

  /**
   * Gets duration.
   *
   * @return the duration
   */
  public Duration getDuration() {
    return duration;
  }

  /**
   * Sets duration.
   *
   * @param duration the duration
   */
  public void setDuration(Duration duration) {
    this.duration = duration;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StartEnd startEnd = (StartEnd) o;
    return Objects.equals(start, startEnd.start)
        && Objects.equals(end, startEnd.end)
        && Objects.equals(duration, startEnd.duration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, end, duration);
  }
}
