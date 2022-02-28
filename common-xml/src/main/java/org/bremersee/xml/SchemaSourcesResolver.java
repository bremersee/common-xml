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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * The schema sources resolver.
 *
 * @author Christian Bremer
 */
@SuppressWarnings("SameNameButDifferent")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
class SchemaSourcesResolver extends SchemaOutputResolver {

  private final Map<String, StreamResult> buffers = new LinkedHashMap<>();

  @Override
  public Result createOutput(String namespaceUri, String suggestedFileName) {
    StringWriter out = new StringWriter();
    StreamResult res = new StreamResult(out);
    res.setSystemId(suggestedFileName);
    buffers.put(namespaceUri, res);
    return res;
  }

  /**
   * To sources.
   *
   * @param excludedNameSpaces the excluded name spaces
   * @return the list with the schema sources
   */
  List<Source> toSources(Collection<String> excludedNameSpaces) {
    Set<String> excluded = Optional.ofNullable(excludedNameSpaces)
        .map(HashSet::new)
        .orElseGet(HashSet::new);
    List<Source> sources = new ArrayList<>(buffers.size());
    for (Map.Entry<String, StreamResult> result : buffers.entrySet()) {
      if (!excluded.contains(result.getKey())) {
        String systemId = result.getValue().getSystemId();
        String schema = result.getValue().getWriter().toString();
        StreamSource source = new StreamSource(new StringReader(schema), systemId);
        sources.add(source);
      }
    }
    return sources;
  }

}
