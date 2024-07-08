/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.xinclude;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.api.di.Named;
import org.apache.maven.api.di.Priority;
import org.apache.maven.api.di.Singleton;
import org.apache.maven.api.model.InputSource;
import org.apache.maven.api.model.Model;
import org.apache.maven.api.services.xml.ModelXmlFactory;
import org.apache.maven.api.services.xml.XmlReaderException;
import org.apache.maven.api.services.xml.XmlReaderRequest;
import org.apache.maven.api.services.xml.XmlWriterException;
import org.apache.maven.api.services.xml.XmlWriterRequest;
import org.apache.maven.model.v4.MavenStaxWriter;
import org.apache.maven.xinclude.stax.XInclude;
import org.codehaus.stax2.io.Stax2FileSource;

import static org.apache.maven.internal.impl.StaxLocation.getLocation;
import static org.apache.maven.internal.impl.StaxLocation.getMessage;
import static org.apache.maven.xinclude.Utils.nonNull;

@Named
@Singleton
@Priority(10)
public class XIncludeModelXmlFactory implements ModelXmlFactory {

    @Override
    public Model read(XmlReaderRequest request) throws XmlReaderException {
        nonNull(request, "request");
        Path path = request.getPath();
        URL url = request.getURL();
        Reader reader = request.getReader();
        InputStream inputStream = request.getInputStream();
        if (path == null && url == null && reader == null && inputStream == null) {
            throw new IllegalArgumentException("path, url, reader or inputStream must be non null");
        }
        try {
            InputSource source = null;
            if (request.getModelId() != null || request.getLocation() != null) {
                source = new InputSource(
                        request.getModelId(), path != null ? path.toUri().toString() : null);
            }
            MavenStaxReader xml = new MavenStaxReader();
            xml.setAddDefaultEntities(request.isAddDefaultEntities());
            if (path != null && request.isStrict()) {
                Source src = inputStream != null
                        ? new StreamSource(inputStream, path.toUri().toString())
                        : new Stax2FileSource(path.toFile());
                XMLStreamReader parser = XInclude.xinclude(src, new LocalXmlResolver(request.getRootDirectory()));
                try {
                    return xml.read(parser, request.isStrict(), source);
                } finally {
                    parser.close();
                }
            } else if (inputStream != null) {
                return xml.read(inputStream, request.isStrict(), source);
            } else if (reader != null) {
                return xml.read(reader, request.isStrict(), source);
            } else if (path != null) {
                try (InputStream is = Files.newInputStream(path)) {
                    return xml.read(is, request.isStrict(), source);
                }
            } else {
                try (InputStream is = url.openStream()) {
                    return xml.read(is, request.isStrict(), source);
                }
            }
        } catch (Exception e) {
            throw new XmlReaderException("Unable to read model: " + getMessage(e), getLocation(e), e);
        }
    }

    @Override
    public void write(XmlWriterRequest<Model> request) throws XmlWriterException {
        nonNull(request, "request");
        Model content = nonNull(request.getContent(), "content");
        Path path = request.getPath();
        OutputStream outputStream = request.getOutputStream();
        Writer writer = request.getWriter();
        if (writer == null && outputStream == null && path == null) {
            throw new IllegalArgumentException("writer, outputStream or path must be non null");
        }
        try {
            if (writer != null) {
                new MavenStaxWriter().write(writer, content);
            } else if (outputStream != null) {
                new MavenStaxWriter().write(outputStream, content);
            } else {
                try (OutputStream os = Files.newOutputStream(path)) {
                    new MavenStaxWriter().write(outputStream, content);
                }
            }
        } catch (Exception e) {
            throw new XmlWriterException("Unable to write model: " + getMessage(e), getLocation(e), e);
        }
    }
}
