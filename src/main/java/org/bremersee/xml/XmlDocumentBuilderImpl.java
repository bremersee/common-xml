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
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The default xml document builder implementation.
 *
 * @author Christian Bremer
 */
class XmlDocumentBuilderImpl implements XmlDocumentBuilder {

  private final DocumentBuilderFactory factory;

  private EntityResolver entityResolver;

  private ErrorHandler errorHandler;

  /**
   * Instantiates a new xml document builder.
   */
  XmlDocumentBuilderImpl() {
    factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
  }

  @Override
  public XmlDocumentBuilder configureFactory(
      final XmlDocumentBuilderFactoryConfigurator configurator) {
    if (configurator != null) {
      configurator.configure(factory);
    }
    return this;
  }

  @Override
  public XmlDocumentBuilder configureFactory(
      final Boolean coalescing,
      final Boolean expandEntityReferences,
      final Boolean ignoringComments,
      final Boolean ignoringElementContentWhitespace,
      final Boolean namespaceAware,
      final Boolean validating,
      final Boolean xIncludeAware) {

    if (coalescing != null) {
      factory.setCoalescing(coalescing);
    }
    if (expandEntityReferences != null) {
      factory.setExpandEntityReferences(expandEntityReferences);
    }
    if (ignoringComments != null) {
      factory.setIgnoringComments(ignoringComments);
    }
    if (ignoringElementContentWhitespace != null) {
      factory.setIgnoringElementContentWhitespace(ignoringElementContentWhitespace);
    }
    if (namespaceAware != null) {
      factory.setNamespaceAware(namespaceAware);
    }
    if (validating != null) {
      factory.setValidating(validating);
    }
    if (xIncludeAware != null) {
      factory.setXIncludeAware(xIncludeAware);
    }
    return this;
  }

  @Override
  public XmlDocumentBuilder configureFactoryAttribute(final String name, final Object value) {
    factory.setAttribute(name, value);
    return this;
  }

  @Override
  public XmlDocumentBuilder configureFactoryFeature(final String name, final boolean value) {
    try {
      factory.setFeature(name, value);
    } catch (ParserConfigurationException e) {
      throw new XmlRuntimeException(e);
    }
    return this;
  }

  @Override
  public XmlDocumentBuilder configureFactorySchema(final Schema schema) {
    factory.setSchema(schema);
    return this;
  }

  @Override
  public XmlDocumentBuilder configureEntityResolver(EntityResolver entityResolver) {
    this.entityResolver = entityResolver;
    return this;
  }

  @Override
  public XmlDocumentBuilder configureErrorHandler(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public DocumentBuilder buildDocumentBuilder() {
    final DocumentBuilder documentBuilder;
    try {
      documentBuilder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new XmlRuntimeException(e);
    }
    if (entityResolver != null) {
      documentBuilder.setEntityResolver(entityResolver);
    }
    if (errorHandler != null) {
      documentBuilder.setErrorHandler(errorHandler);
    }
    return documentBuilder;
  }

  @Override
  public Document buildDocument() {
    return buildDocumentBuilder().newDocument();
  }

  @Override
  public Document buildDocument(File file) {
    try {
      return buildDocumentBuilder().parse(file);

    } catch (SAXException | IOException e) {
      throw new XmlRuntimeException(e);
    }
  }

  @Override
  public Document buildDocument(String uri) {
    try {
      return buildDocumentBuilder().parse(uri);

    } catch (SAXException | IOException e) {
      throw new XmlRuntimeException(e);
    }
  }

  @Override
  public Document buildDocument(InputSource is) {
    try {
      return buildDocumentBuilder().parse(is);

    } catch (SAXException | IOException e) {
      throw new XmlRuntimeException(e);
    }
  }

  @Override
  public Document buildDocument(InputStream is) {
    try {
      return buildDocumentBuilder().parse(is);

    } catch (SAXException | IOException e) {
      throw new XmlRuntimeException(e);
    }
  }

  @Override
  public Document buildDocument(InputStream is, String systemId) {
    try {
      return buildDocumentBuilder().parse(is, systemId);

    } catch (SAXException | IOException e) {
      throw new XmlRuntimeException(e);
    }
  }

  @Override
  public Document buildDocument(Object jaxbElement, JAXBContext jaxbContext) {
    if (jaxbElement == null) {
      return null;
    }
    try {
      return buildDocument(jaxbElement, jaxbContext.createMarshaller());
    } catch (JAXBException e) {
      throw new JaxbRuntimeException(e);
    }
  }

  @Override
  public Document buildDocument(Object jaxbElement, Marshaller marshaller) {
    if (jaxbElement == null) {
      return null;
    }
    final Document document = buildDocument();
    try {
      marshaller.marshal(jaxbElement, document);
    } catch (JAXBException e) {
      throw new JaxbRuntimeException(e);
    }
    return document;
  }

}