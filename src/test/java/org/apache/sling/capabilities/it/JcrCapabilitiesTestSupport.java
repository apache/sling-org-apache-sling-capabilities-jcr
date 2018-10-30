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
package org.apache.sling.capabilities.it;

import org.apache.sling.testing.paxexam.SlingOptions;
import org.apache.sling.testing.paxexam.TestSupport;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.apache.sling.testing.paxexam.SlingOptions.logback;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

public abstract class JcrCapabilitiesTestSupport extends TestSupport {

    @Configuration
    public Option[] configuration() {
        // Keep versions low for our dependencies, to make sure this bundle stays
        // compatible with older Sling versions.
        SlingOptions.versionResolver.setVersion("slf4j", "slf4j-api", "1.7.6");
                
        return new Option[]{
            baseConfiguration(),
            
            // Use older versions to stay compatible with older versions of Sling - no need for bleeding edge stuff
            mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.http.servlet-api").version("1.1.2"),
            mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.scr").version("2.0.2"),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.api").version("2.11.0"),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.jcr.jcr-wrapper").version("2.0.0"),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.jcr.api").version("2.3.0"),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.serviceusermapper").version("1.2.2"),
            
            // Dependencies of the above bundles
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.commons.json").version("2.0.16"),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.commons.osgi").version("2.4.0"),
            mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.eventadmin").version("1.4.4"),

            // This bundle and the core capabilities bundle
            testBundle("bundle.filename"),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.capabilities").versionAsInProject(),
            
            // Test stuff
            junitBundles(),
            logback()
        };
    }
}
