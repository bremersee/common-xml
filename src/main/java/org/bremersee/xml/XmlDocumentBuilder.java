/*
 * Copyright 2018-2020 the original author or authors.
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
import javax.xml.validation.Schema;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

/**
 * The xml document builder.
 *
 * @author Christian Bremer
 */
public interface XmlDocumentBuilder {

  /**
   * Configure factory xml document builder.
   *
   * @param configurator the configurator
   * @return the xml document builder
   */
  XmlDocumentBuilder configureFactory(
      XmlDocumentBuilderFactoryConfigurator configurator);

  /**
   * Configure factory xml document builder.
   *
   * @param coalescing the coalescing
   * @param expandEntityReferences the expand entity references
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
   * Configure factory attribute xml document builder.
   *
   * @param name the name
   * @param value the value
   * @return the xml document builder
   */
  XmlDocumentBuilder configureFactoryAttribute(String name, Object value);

  /**
   * Configure factory feature xml document builder.
   *
   * @param name the name
   * @param value the value
   * @return the xml document builder
   */
  XmlDocumentBuilder configureFactoryFeature(String name, boolean value);

  /**
   * Configure factory schema xml document builder.
   *
   * @param schema the schema
   * @return the xml document builder
   */
  XmlDocumentBuilder configureFactorySchema(Schema schema);

  /**
   * Configure entity resolver xml document builder.
   *
   * @param entityResolver the entity resolver
   * @return the xml document builder
   */
  XmlDocumentBuilder configureEntityResolver(EntityResolver entityResolver);

  /**
   * Configure error handler xml document builder.
   *
   * @param errorHandler the error handler
   * @return the xml document builder
   */
  XmlDocumentBuilder configureErrorHandler(ErrorHandler errorHandler);

  /**
   * Build document builder document builder.
   *
   * @return the document builder
   */
  DocumentBuilder buildDocumentBuilder();

  /**
   * Build document document.
   *
   * @return the document
   */
  Document buildDocument();

  /**
   * Build document from file.
   *
   * @param file the file
   * @return the document
   */
  Document buildDocument(File file);

  /**
   * Build document from uri.
   *
   * @param uri the uri
   * @return the document
   */
  Document buildDocument(String uri);

  /**
   * Build document from input source.
   *
   * @param is the input source
   * @return the document
   */
  Document buildDocument(InputSource is);

  /**
   * Build document from input stream.
   *
   * @param is the input stream
   * @return the document
   */
  Document buildDocument(InputStream is);

  /**
   * Build document from input stream and system ID.
   *
   * @param is the input stream
   * @param systemId the system id
   * @return the document
   */
  Document buildDocument(InputStream is, String systemId);

  /**
   * Build document from an object that can be processed with {@link JAXBContext}.
   *
   * @param jaxbElement the jaxb element
   * @param jaxbContext the jaxb context
   * @return the document
   */
  Document buildDocument(Object jaxbElement, JAXBContext jaxbContext);

  /**
   * Build document from an object that can be processed with {@link Marshaller}.
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
