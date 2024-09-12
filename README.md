<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
[Apache Maven XInclude Extension](https://maven.apache.org/extensions/maven-xinclude-extension/)
==================================

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)][license]
[![Maven Central](https://img.shields.io/maven-central/v/org.apache.maven.extensions/maven-xinclude-extension.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.apache.maven.extensions/maven-xinclude-extension)

This project provides an XInclude extension for Maven 4. It allows POMs to use xinclude support 
and XML entities to refer to xml snippets located inside the project. It does not support loading
external documents.

License
-------
This code is under the [Apache License, Version 2.0, January 2004][license].

See the [`NOTICE`](./NOTICE) file for required notices and attributions.

[home]: https://maven.apache.org/extensions/maven-xinclude-extension/
[license]: https://www.apache.org/licenses/LICENSE-2.0
[build-status]: https://img.shields.io/jenkins/s/https/ci-maven.apache.org/job/Maven/job/maven-box/job/maven-build-cache-extension/job/master.svg
[build-tests]: https://img.shields.io/jenkins/t/https/ci-maven.apache.org/job/Maven/job/maven-box/job/maven-build-cache-extension/job/master.svg

Usage
-----
To use this extension, the following declaration needs to be done in your `${rootDirectory}/.mvn/extensions.xml`:
```
<extensions xmlns="http://maven.apache.org/EXTENSIONS/1.2.0">
    <extension>
        <groupId>org.apache.maven.extensions</groupId>
        <artifactId>maven-xinclude-extension</artifactId>
        <version>@project.version@</version>
    </extension>
</extensions>
```
This allows defining a POM with XInclude pointers and XML entities:
```
<!DOCTYPE foo SYSTEM "../foo.dtd">
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns:xi="http://www.w3.org/2001/XInclude"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd
                             http://www.w3.org/2001/XInclude https://www.w3.org/2001/XInclude/XInclude.xsd">


    <modelVersion>4.0.0</modelVersion>

    <xi:include href="../snippets.xml" xpointer="id-parent" />

    <groupId>&groupId;</groupId>
    <artifactId>child</artifactId>
    &version;
    <packaging>jar</packaging>

</project>
```
