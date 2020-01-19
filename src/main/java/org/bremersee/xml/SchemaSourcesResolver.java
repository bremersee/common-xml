/*
 * Copyright 2020 the original author or authors.
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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * @author Christian Bremer
 */
public class SchemaSourcesResolver extends SchemaOutputResolver {

  private final Map<String, StreamResult> buffers = new LinkedHashMap<>();

  @Override
  public Result createOutput(final String namespaceUri, final String suggestedFileName) {
    final StringWriter out = new StringWriter();
    final StreamResult res = new StreamResult(out);
    res.setSystemId(suggestedFileName);
    buffers.put(namespaceUri, res);
    return res;
  }

  public List<Source> toSources() {
    final List<Source> sources = new ArrayList<>(buffers.size());
    for (final Map.Entry<String, StreamResult> result : buffers.entrySet()) {
      final String systemId = result.getValue().getSystemId();
      final String schema = result.getValue().getWriter().toString();
      final StreamSource source = new StreamSource(new StringReader(schema), systemId);
      sources.add(source);
    }
    return sources;
  }

}
