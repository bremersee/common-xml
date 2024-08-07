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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import jakarta.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

/**
 * XML schema output helper.
 *
 * @author Christian Bremer
 */
public class BufferSchemaOutputResolver extends SchemaOutputResolver {

  private final Map<String, StreamResult> buffers = new HashMap<>();

  @Override
  public Result createOutput(String namespaceUri, String suggestedFileName) {
    StringWriter out = new StringWriter();
    StreamResult res = new StreamResult(out);
    res.setSystemId(suggestedFileName);
    buffers.put(namespaceUri, res);
    return res;
  }

  private String getSchema(String namespaceUri) {
    return buffers.get(namespaceUri).getWriter().toString();
  }

  private String getSystemId(String namespaceUri) {
    return buffers.get(namespaceUri).getSystemId();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String s : buffers.keySet()) {
      sb.append("***** Begin schema ").append(s).append(", system-id=").append(getSystemId(s))
          .append(" *****");
      sb.append(System.getProperty("line.separator"));
      sb.append(getSchema(s));
      sb.append("***** End schema ").append(s).append(" *****");
      sb.append(System.getProperty("line.separator"));
      sb.append(System.getProperty("line.separator"));
    }
    return sb.toString();
  }

}
