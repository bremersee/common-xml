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

package org.bremersee.xml.http.codec;

import static java.util.Objects.isNull;

import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.MarshalException;
import jakarta.xml.bind.Marshaller;
import org.bremersee.xml.JaxbContextBuilder;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractSingleValueEncoder;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.EncodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Encode from single value to a byte stream containing XML elements.
 *
 * <p>{@link jakarta.xml.bind.annotation.XmlElements @XmlElements} and
 * {@link jakarta.xml.bind.annotation.XmlElement @XmlElement} can be used to specify how collections
 * should be marshalled.
 *
 * <p>The encoding parts are taken from {@link org.springframework.http.codec.xml.Jaxb2XmlEncoder}.
 *
 * @author Sebastien Deleuze
 * @author Arjen Poutsma
 * @author Christian Bremer
 */
public class ReactiveJaxbEncoder extends AbstractSingleValueEncoder<Object> {

  private final JaxbContextBuilder jaxbContextBuilder;

  private final Set<Class<?>> ignoreWritingClasses;

  /**
   * Instantiates a new reactive jaxb encoder.
   *
   * @param jaxbContextBuilder the jaxb context builder
   */
  public ReactiveJaxbEncoder(JaxbContextBuilder jaxbContextBuilder) {
    this(jaxbContextBuilder, null);
  }

  /**
   * Instantiates a new reactive jaxb encoder.
   *
   * @param jaxbContextBuilder the jaxb context builder
   * @param ignoreWritingClasses ignore writing classes
   */
  public ReactiveJaxbEncoder(
      JaxbContextBuilder jaxbContextBuilder,
      Set<Class<?>> ignoreWritingClasses) {

    super(MimeTypeUtils.APPLICATION_XML, MimeTypeUtils.TEXT_XML);
    Assert.notNull(jaxbContextBuilder, "JaxbContextBuilder must be present.");
    this.jaxbContextBuilder = jaxbContextBuilder;
    this.ignoreWritingClasses = isNull(ignoreWritingClasses) ? Set.of() : ignoreWritingClasses;
  }

  @Override
  public boolean canEncode(
      @NonNull ResolvableType elementType,
      @Nullable final MimeType mimeType) {

    if (super.canEncode(elementType, mimeType)) {
      final Class<?> outputClass = elementType.toClass();
      return !ignoreWritingClasses.contains(outputClass)
          && jaxbContextBuilder.canMarshal(outputClass);
    } else {
      return false;
    }
  }

  @NonNull
  @Override
  protected Flux<DataBuffer> encode(
      @NonNull Object value,
      @NonNull DataBufferFactory bufferFactory,
      @NonNull ResolvableType valueType,
      @Nullable MimeType mimeType,
      @Nullable Map<String, Object> hints) {

    // we're relying on doOnDiscard in base class
    return Mono.fromCallable(() -> encodeValue(value, bufferFactory, valueType, mimeType, hints))
        .flux();
  }

  @NonNull
  @Override
  public DataBuffer encodeValue(
      @NonNull Object value,
      @NonNull DataBufferFactory bufferFactory,
      @NonNull ResolvableType valueType,
      @Nullable MimeType mimeType,
      @Nullable Map<String, Object> hints) {

    if (!Hints.isLoggingSuppressed(hints)) {
      LogFormatUtils.traceDebug(logger, traceOn -> {
        String formatted = LogFormatUtils.formatValue(value, !traceOn);
        return Hints.getLogPrefix(hints) + "Encoding [" + formatted + "]";
      });
    }

    boolean release = true;
    DataBuffer buffer = bufferFactory.allocateBuffer(1024);
    try {
      OutputStream outputStream = buffer.asOutputStream();
      Marshaller marshaller = jaxbContextBuilder.buildMarshaller(value);
      marshaller.marshal(value, outputStream);
      release = false;
      return buffer;
    } catch (MarshalException ex) {
      throw new EncodingException("Could not marshal " + value.getClass() + " to XML", ex);
    } catch (JAXBException ex) {
      throw new CodecException("Invalid JAXB configuration", ex);
    } finally {
      if (release) {
        DataBufferUtils.release(buffer);
      }
    }
  }

}
