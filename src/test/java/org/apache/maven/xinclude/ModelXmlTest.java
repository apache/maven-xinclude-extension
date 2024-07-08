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

import javax.xml.stream.XMLStreamException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.api.model.Model;
import org.apache.maven.api.model.Plugin;
import org.apache.maven.api.xml.XmlNode;
import org.apache.maven.model.v4.MavenStaxWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModelXmlTest {

    @Test
    void testExternalEntities() throws Exception {
        String xml = "<?xml version='1.0' encoding='UTF-8'?>\n" + "<!DOCTYPE foo [\n"
                + "        <!ENTITY desc SYSTEM \"file:desc.xml\">\n"
                + "        ]>\n"
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n"
                + "\n"
                + "  <modelVersion>4.0.0</modelVersion>\n"
                + "\n"
                + "  <parent>\n"
                + "    <groupId>org.apache.maven</groupId>\n"
                + "    <artifactId>maven-parent</artifactId>\n"
                + "    <version>40</version>\n"
                + "    <relativePath />\n"
                + "  </parent>\n"
                + "\n"
                + "  <groupId>org.apache.maven.daemon</groupId>\n"
                + "  <artifactId>mvnd</artifactId>\n"
                + "  <version>1.0-m7-SNAPSHOT</version>\n"
                + "  &desc;\n"
                + "</project>\n";
        MavenStaxReader staxReader = new MavenStaxReader();
        staxReader.setXmlResolver((publicID, systemID, baseURI, namespace) -> {
            if ("file:desc.xml".equals(systemID)) {
                return "<?xml version='1.0' encoding='UTF-8'?><description>foo</description>";
            }
            return null;
        });
        Model model = staxReader.read(new StringReader(xml));
        assertNotNull(model);
        assertEquals("foo", model.getDescription());
    }

    @Test
    void testDefaultEntities() throws Exception {
        String xml = "<?xml version='1.0'?>\n"
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n"
                + "  <modelVersion>4.0.0</modelVersion>\n"
                + "  <groupId>org.apache.maven.daemon</groupId>\n"
                + "  <artifactId>mvnd&oelig;</artifactId>\n"
                + "  <version>1.0-m7-SNAPSHOT</version>\n"
                + "</project>\n";
        Model model = new MavenStaxReader().read(new StringReader(xml), false, null);
        assertNotNull(model);
        assertEquals("mvndœ", model.getArtifactId());
    }

    @Test
    void testXmlRoundtripWithProperties() throws Exception {
        Map<String, String> props = new LinkedHashMap<>();
        props.put("javax.version", "3.1.0");
        props.put("mockito.version", "1.10.19");
        props.put("hamcret.version", "2.1");
        props.put("lombok.version", "1.18.6");
        props.put("junit.version", "4.12");
        Model model = Model.newBuilder(true).properties(props).build();
        String xml = toXml(model);

        for (int i = 0; i < 10; i++) {
            String newStr = toXml(fromXml(xml));
            assertEquals(newStr, xml);
        }
    }

    @Test
    void testNamespaceInXmlNode() throws XMLStreamException {
        String xml = "<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "         xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
                + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/POM/4.0.0\">\n"
                + "  <build>\n"
                + "    <plugins>\n"
                + "      <plugin>\n"
                + "         <m:configuration xmlns:m=\"http://maven.apache.org/POM/4.0.0\" xmlns=\"http://fabric8.io/fabric8-maven-plugin\">\n"
                + "             <myConfig>foo</myConfig>\n"
                + "         </m:configuration>\n"
                + "      </plugin>\n"
                + "    </plugins>\n"
                + "  </build>\n"
                + "</project>";

        Model model = fromXml(xml);
        Plugin plugin = model.getBuild().getPlugins().get(0);
        XmlNode node = plugin.getConfiguration();
        assertNotNull(node);
        assertEquals("http://maven.apache.org/POM/4.0.0", node.getNamespaceUri());
        assertEquals("m", node.getPrefix());
        assertEquals("configuration", node.getName());
        assertEquals(1, node.getChildren().size());
        XmlNode myConfig = node.getChildren().get(0);
        assertEquals("http://fabric8.io/fabric8-maven-plugin", myConfig.getNamespaceUri());
        assertEquals("", myConfig.getPrefix());
        assertEquals("myConfig", myConfig.getName());
        String config = node.toString();
        assertFalse(config.isEmpty());
    }

    String toXml(Model model) throws IOException, XMLStreamException {
        StringWriter sw = new StringWriter();
        MavenStaxWriter writer = new MavenStaxWriter();
        writer.setAddLocationInformation(false);
        writer.write(sw, model);
        return sw.toString();
    }

    Model fromXml(String xml) throws XMLStreamException {
        return new MavenStaxReader().read(new StringReader(xml));
    }
}
