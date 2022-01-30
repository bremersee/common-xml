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

package org.bremersee.xml;

import java.io.File;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

/**
 * The xml document builder wraps the functionality of {@link DocumentBuilderFactory}. Checked
 * exceptions will be wrapped into {@link XmlRuntimeException}.
 *
 * @author Christian Bremer
 */
public interface XmlDocumentBuilder {

  /**
   * Configures the {@link DocumentBuilderFactory}. The default xml document builder has the same
   * default values as the underlying factory except that
   * {@link DocumentBuilderFactory#setNamespaceAware(boolean)} is set to {@code true}.
   *
   * @param configurator the configurator
   * @return the xml document builder
   */
  XmlDocumentBuilder configureFactory(
      XmlDocumentBuilderFactoryConfigurator configurator);

  /**
   * Configures the {@link DocumentBuilderFactory}. The default xml document builder has the same
   * default values as the underlying factory except that
   * {@link DocumentBuilderFactory#setNamespaceAware(boolean)} is set to {@code true}.
   *
   * <p>A value with {@code null} will be ignored and the default will be used.
   *
   * <p>The default values are:
   * <pre>
   * coalescing                       = false
   * expandEntityReferences           = true
   * ignoringComments                 = false
   * ignoringElementContentWhitespace = false
   * namespaceAware                   = true
   * validating                       = false
   * xIncludeAware                    = false
   * </pre>
   *
   * @param coalescing the coalescing
   * @param expandEntityReferences the expanded entity references
   * @param ignoringComments the ignoring comments
   * @param ignoringElementContentWhitespace the ignoring element content whitespace
   * @param namespaceAware the namespace aware
   * @param validating the validating
   * @param xIncludeAware the x include aware
   * @return the xml document builder
   */
  XmlDocumentBuilder configureFactory(
      Boolean coalescing,
      Boolean expandEntityReferences,
      Boolean ignoringComments,
      Boolean ignoringElementContentWhitespace,
      Boolean namespaceAware,
      Boolean validating,
      Boolean xIncludeAware);

  /**
   * Sets the given attribute to the underlying {@link DocumentBuilderFactory}.
   *
   * @param name the name
   * @param value the value
   * @return the xml document builder
   * @see DocumentBuilderFactory#setAttribute(String, Object)
   */
  XmlDocumentBuilder configureFactoryAttribute(String name, Object value);

  /**
   * Sets the given feature to the underlying {@link DocumentBuilderFactory}.
   *
   * @param name the name
   * @param value the value
   * @return the xml document builder
   * @see DocumentBuilderFactory#setFeature(String, boolean)
   */
  XmlDocumentBuilder configureFactoryFeature(String name, boolean value);

  /**
   * Sets the given schema to the underlying {@link DocumentBuilderFactory}.
   *
   * @param schema the schema
   * @return the xml document builder
   * @see DocumentBuilderFactory#setSchema(Schema)
   */
  XmlDocumentBuilder configureFactorySchema(Schema schema);

  /**
   * Sets the entity resolver to the created  {@link DocumentBuilder}.
   *
   * @param entityResolver the entity resolver
   * @return the xml document builder
   * @see DocumentBuilder#setEntityResolver(EntityResolver)
   */
  XmlDocumentBuilder configureEntityResolver(EntityResolver entityResolver);

  /**
   * Sets error handler to the created  {@link DocumentBuilder}.
   *
   * @param errorHandler the error handler
   * @return the xml document builder
   * @see DocumentBuilder#setErrorHandler(ErrorHandler)
   */
  XmlDocumentBuilder configureErrorHandler(ErrorHandler errorHandler);

  /**
   * Creates a new document builder.
   *
   * @return the document builder
   */
  DocumentBuilder buildDocumentBuilder();

  /**
   * Builds an empty document.
   *
   * @return the document
   */
  Document buildDocument();

  /**
   * Builds document from file.
   *
   * @param file the file
   * @return the document
   */
  Document buildDocument(File file);

  /**
   * Builds document from uri.
   *
   * @param uri the uri
   * @return the document
   */
  Document buildDocument(String uri);

  /**
   * Builds document from input source.
   *
   * @param is the input source
   * @return the document
   */
  Document buildDocument(InputSource is);

  /**
   * Builds document from input stream.
   *
   * @param is the input stream
   * @return the document
   */
  Document buildDocument(InputStream is);

  /**
   * Builds document from input stream and system ID.
   *
   * @param is the input stream
   * @param systemId the system id
   * @return the document
   */
  Document buildDocument(InputStream is, String systemId);

  /**
   * Builds document from an object (POJO) that can be processed with {@link JAXBContext}.
   *
   * @param jaxbElement the jaxb element
   * @param jaxbContext the jaxb context
   * @return the document
   */
  Document buildDocument(Object jaxbElement, JAXBContext jaxbContext);

  /**
   * Builds document from an object (POJO) that can be processed with {@link Marshaller}.
   *
   * @param jaxbElement the jaxb element
   * @param marshaller the marshaller
   * @return the document
   */
  Document buildDocument(Object jaxbElement, Marshaller marshaller);

  /**
   * Creates default builder instance.
   *
   * @return the default builder instance
   */
  static XmlDocumentBuilder builder() {
    return new XmlDocumentBuilderImpl();
  }

}
